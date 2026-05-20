package com.dragun.ecommerce.integration.meinvoice.service;

import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceLoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Caches MeInvoice access token (doc: TTL ~14 days; refresh conservatively before expiry).
 */
@Service
@Slf4j
public class MeinvoiceAuthService {

    private static final int TOKEN_REFRESH_SAFETY_HOURS = 6;

    private final MeinvoiceIntegrationConfig config;
    private final WebClient meinvoiceWebClient;
    private final ObjectMapper objectMapper;

    public MeinvoiceAuthService(MeinvoiceIntegrationConfig config,
                                @Qualifier("meinvoiceWebClient") WebClient meinvoiceWebClient,
                                ObjectMapper objectMapper) {
        this.config = config;
        this.meinvoiceWebClient = meinvoiceWebClient;
        this.objectMapper = objectMapper;
    }

    private final Object tokenLock = new Object();
    private String cachedAccessToken;
    private Instant cachedValidUntil = Instant.EPOCH;

    public String getAccessToken() {
        synchronized (tokenLock) {
            if (cachedAccessToken != null && Instant.now().isBefore(cachedValidUntil)) {
                return cachedAccessToken;
            }
            refreshTokenLocked();
            return cachedAccessToken;
        }
    }

    public void invalidateToken() {
        synchronized (tokenLock) {
            cachedAccessToken = null;
            cachedValidUntil = Instant.EPOCH;
        }
    }

    private void refreshTokenLocked() {
        MeinvoiceLoginRequest body = MeinvoiceLoginRequest.builder()
                .taxcode(config.getCredentials().getTaxcode())
                .username(config.getCredentials().getUsername())
                .password(config.getCredentials().getPassword())
                .build();

        long timeoutMs = Math.max(1_000L, config.getApi().getTimeoutMs());

        JsonNode root = meinvoiceWebClient.post()
                .uri("/webapp/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

        if (root == null || !root.path("success").asBoolean(false)) {
            String err = root == null ? "empty response" : root.toString();
            throw new IllegalStateException("MeInvoice token request failed: " + err);
        }

        String token;
        try {
            token = extractAccessTokenFromData(root.get("data"));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("MeInvoice token: data is not valid JSON string/object", e);
        }
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("MeInvoice token response missing data.access_token (see Postman: JSON.parse(res.data))");
        }

        cachedAccessToken = token;
        cachedValidUntil = Instant.now().plus(13, ChronoUnit.DAYS).minus(TOKEN_REFRESH_SAFETY_HOURS, ChronoUnit.HOURS);
        log.info("MeInvoice access token refreshed (cached until {})", cachedValidUntil);
    }

    /**
     * Postman collection parses token via {@code JSON.parse(res.data)} — {@code data} is often a
     * <strong>string</strong> containing JSON, not a nested object.
     */
    private String extractAccessTokenFromData(JsonNode dataNode) throws JsonProcessingException {
        if (dataNode == null || dataNode.isNull() || dataNode.isMissingNode()) {
            return null;
        }
        if (dataNode.isTextual()) {
            JsonNode parsed = objectMapper.readTree(dataNode.asText());
            return parsed.path("access_token").asText(null);
        }
        if (dataNode.isObject()) {
            return dataNode.path("access_token").asText(null);
        }
        return null;
    }
}
