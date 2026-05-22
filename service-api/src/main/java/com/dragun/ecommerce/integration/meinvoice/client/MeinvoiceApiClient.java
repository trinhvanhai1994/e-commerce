package com.dragun.ecommerce.integration.meinvoice.client;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceInvoiceData;
import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceTemplateListRequest;
import com.dragun.ecommerce.integration.meinvoice.service.MeinvoiceAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * HTTP client for MISA MeInvoice Integration API.
 */
@Component
@Slf4j
public class MeinvoiceApiClient {

    private final MeinvoiceIntegrationConfig config;
    private final MeinvoiceAuthService authService;
    private final WebClient meinvoiceWebClient;
    private final ObjectMapper objectMapper;

    public MeinvoiceApiClient(MeinvoiceIntegrationConfig config,
                              MeinvoiceAuthService authService,
                              @Qualifier("meinvoiceWebClient") WebClient meinvoiceWebClient,
                              ObjectMapper objectMapper) {
        this.config = config;
        this.authService = authService;
        this.meinvoiceWebClient = meinvoiceWebClient;
        this.objectMapper = objectMapper;
    }

    public JsonNode fetchTemplates(boolean invoiceWithCode) {
        MeinvoiceTemplateListRequest body = MeinvoiceTemplateListRequest.builder()
                .taxCode(config.getCredentials().getTaxcode())
                .userName(config.getCredentials().getUsername())
                .password(config.getCredentials().getPassword())
                .build();
        return authorizedPost(MeinvoiceIntegrationConstants.API_PATH_TEMPLATES, invoiceWithCode, body).block();
    }

    public JsonNode previewInvoice(MeinvoiceInvoiceData invoiceData) {
        return authorizedPost(MeinvoiceIntegrationConstants.API_PATH_PREVIEW, null, invoiceData).block();
    }

    public JsonNode insertInvoices(List<MeinvoiceInvoiceData> invoices) {
        return authorizedPost(MeinvoiceIntegrationConstants.API_PATH_INSERT, null, invoices).block();
    }

    public JsonNode getInvoicesByRefIds(boolean invoiceWithCode, List<String> refIds) {
        return authorizedPost(MeinvoiceIntegrationConstants.API_PATH_GET_LIST, invoiceWithCode, refIds).block();
    }

    public byte[] fetchInvoicePdfBytesByViewRefId(boolean invoiceWithCode, String refId) {
        byte[] body = authorizedGetBytes(
                MeinvoiceIntegrationConstants.API_PATH_VIEW_REF_ID,
                invoiceWithCode,
                refId);
        return MeinvoiceViewRefIdParser.parseHttpBody(body, objectMapper);
    }

    public JsonNode deleteDraft(boolean invoiceWithCode, String refId) {
        String token = authService.getAccessToken();
        return meinvoiceWebClient.delete()
                .uri(uriBuilder -> uriBuilder.path(MeinvoiceIntegrationConstants.API_PATH_DELETE)
                        .queryParam(MeinvoiceIntegrationConstants.QUERY_PARAM_INVOICE_WITH_CODE, invoiceWithCode)
                        .queryParam(MeinvoiceIntegrationConstants.QUERY_PARAM_REF_ID, refId)
                        .build())
                .headers(headers -> applyAuthHeaders(headers, token))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errBody -> Mono.error(new IllegalStateException(String.format(
                                        Locale.ROOT,
                                        MeinvoiceIntegrationConstants.ERROR_HTTP_DELETE_FORMAT,
                                        MeinvoiceIntegrationConstants.API_PATH_DELETE,
                                        response.statusCode(),
                                        errBody)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(requestTimeoutMs()))
                .doOnError(e -> logUnexpectedError(MeinvoiceIntegrationConstants.API_PATH_DELETE, e))
                .block();
    }

    private byte[] authorizedGetBytes(String path, boolean invoiceWithCode, String refId) {
        String token = authService.getAccessToken();
        return meinvoiceWebClient.get()
                .uri(uriBuilder -> uriBuilder.path(path)
                        .queryParam(MeinvoiceIntegrationConstants.QUERY_PARAM_INVOICE_WITH_CODE, invoiceWithCode)
                        .queryParam(MeinvoiceIntegrationConstants.QUERY_PARAM_REF_ID, refId)
                        .build())
                .headers(headers -> applyAuthHeaders(headers, token)
                        .set(HttpHeaders.ACCEPT, MeinvoiceIntegrationConstants.HEADER_ACCEPT_PDF_AND_JSON))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errBody -> Mono.error(new IllegalStateException(String.format(
                                        Locale.ROOT,
                                        MeinvoiceIntegrationConstants.ERROR_HTTP_GET_FORMAT,
                                        path,
                                        response.statusCode(),
                                        errBody)))))
                .bodyToMono(byte[].class)
                .timeout(Duration.ofMillis(requestTimeoutMs()))
                .block();
    }

    private <T> Mono<JsonNode> authorizedPost(String path, Boolean invoiceWithCode, T body) {
        String token = authService.getAccessToken();
        return meinvoiceWebClient.post()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    if (invoiceWithCode != null) {
                        builder.queryParam(
                                MeinvoiceIntegrationConstants.QUERY_PARAM_INVOICE_WITH_CODE,
                                invoiceWithCode);
                    }
                    return builder.build();
                })
                .headers(headers -> applyAuthHeaders(headers, token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errBody -> Mono.error(new IllegalStateException(String.format(
                                        Locale.ROOT,
                                        MeinvoiceIntegrationConstants.ERROR_HTTP_POST_FORMAT,
                                        path,
                                        response.statusCode(),
                                        errBody)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(requestTimeoutMs()))
                .doOnError(e -> logUnexpectedError(path, e));
    }

    private HttpHeaders applyAuthHeaders(HttpHeaders headers, String token) {
        headers.set(HttpHeaders.AUTHORIZATION, MeinvoiceIntegrationConstants.bearerToken(token));
        headers.set(MeinvoiceIntegrationConstants.HEADER_TAXCODE, config.getCredentials().getTaxcode());
        return headers;
    }

    private long requestTimeoutMs() {
        return Math.max(
                MeinvoiceIntegrationConstants.MIN_REQUEST_TIMEOUT_MS,
                config.getApi().getTimeoutMs());
    }

    private void logUnexpectedError(String path, Throwable e) {
        if (!(e instanceof IllegalStateException) && !(e instanceof WebClientResponseException)) {
            log.error("MeInvoice {} failed: {}", path, e.getMessage());
        }
    }
}
