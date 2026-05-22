package com.dragun.ecommerce.integration.meinvoice.publish.service;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.client.MeinvoiceApiErrorParser;
import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.dragun.ecommerce.integration.meinvoice.publish.dto.MeinvoicePublishLoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class MeinvoicePublishAuthService {

    private static final int TOKEN_REFRESH_SAFETY_HOURS = 6;

    private final MeinvoiceIntegrationConfig config;
    private final WebClient meinvoiceWebClient;
    private final ObjectMapper objectMapper;

    private final Object tokenLock = new Object();
    private String cachedAccessToken;
    private Instant cachedValidUntil = Instant.EPOCH;

    public MeinvoicePublishAuthService(MeinvoiceIntegrationConfig config,
                                       @Qualifier("meinvoiceWebClient") WebClient meinvoiceWebClient,
                                       ObjectMapper objectMapper) {
        this.config = config;
        this.meinvoiceWebClient = meinvoiceWebClient;
        this.objectMapper = objectMapper;
    }

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
        if (!StringUtils.hasText(config.getCredentials().getAppId())) {
            throw new IllegalStateException(MeinvoicePublishConstants.ERROR_APP_ID_REQUIRED);
        }
        MeinvoicePublishLoginRequest body = MeinvoicePublishLoginRequest.builder()
                .appId(config.getCredentials().getAppId().trim())
                .taxcode(config.getCredentials().getTaxcode())
                .username(config.getCredentials().getUsername())
                .password(config.getCredentials().getPassword())
                .build();

        long timeoutMs = Math.max(
                MeinvoiceIntegrationConstants.MIN_REQUEST_TIMEOUT_MS,
                config.getApi().getTimeoutMs());

        JsonNode root = meinvoiceWebClient.post()
                .uri(MeinvoicePublishConstants.API_PATH_AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errBody -> Mono.error(new IllegalStateException(
                                        MeinvoiceApiErrorParser.formatHttpError(
                                                MeinvoicePublishConstants.OPERATION_AUTH_TOKEN,
                                                MeinvoicePublishConstants.API_PATH_AUTH_TOKEN,
                                                response.statusCode(),
                                                errBody)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .block();

        if (!isSuccess(root)) {
            throw new IllegalStateException(MeinvoiceApiErrorParser.formatApiFailure(
                    root,
                    MeinvoicePublishConstants.OPERATION_AUTH_TOKEN));
        }

        String token;
        try {
            token = extractAccessTokenFromData(resolveDataNode(root));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("MeInvoice publish token: invalid data JSON", e);
        }
        if (!StringUtils.hasText(token)) {
            throw new IllegalStateException(MeinvoicePublishConstants.ERROR_PUBLISH_TOKEN_MISSING);
        }

        cachedAccessToken = token;
        cachedValidUntil = Instant.now().plus(13, ChronoUnit.DAYS).minus(TOKEN_REFRESH_SAFETY_HOURS, ChronoUnit.HOURS);
        log.info("MeInvoice publish access token refreshed (cached until {})", cachedValidUntil);
    }

    private static boolean isSuccess(JsonNode root) {
        if (root == null) {
            return false;
        }
        if (root.path("success").asBoolean(false)) {
            return true;
        }
        return root.path("Success").asBoolean(false);
    }

    private static JsonNode resolveDataNode(JsonNode root) {
        if (root == null) {
            return null;
        }
        JsonNode data = root.get("data");
        if (data == null || data.isMissingNode() || data.isNull()) {
            data = root.get("Data");
        }
        return data;
    }

    /**
     * V2 {@code /auth/token}: {@code data} is often the Bearer JWT string directly.
     * V1 {@code /webapp/token}: {@code data} may be a JSON string containing {@code access_token}.
     */
    private String extractAccessTokenFromData(JsonNode dataNode) throws JsonProcessingException {
        if (dataNode == null || dataNode.isNull() || dataNode.isMissingNode()) {
            return null;
        }
        if (dataNode.isTextual()) {
            String text = dataNode.asText().trim();
            if (!StringUtils.hasText(text)) {
                return null;
            }
            if (text.startsWith("{")) {
                JsonNode parsed = objectMapper.readTree(text);
                String nested = parsed.path("access_token").asText(null);
                if (StringUtils.hasText(nested)) {
                    return nested.trim();
                }
            }
            return text;
        }
        if (dataNode.isObject()) {
            return dataNode.path("access_token").asText(null);
        }
        return null;
    }
}
