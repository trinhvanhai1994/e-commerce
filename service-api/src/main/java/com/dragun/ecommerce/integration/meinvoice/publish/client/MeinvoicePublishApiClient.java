package com.dragun.ecommerce.integration.meinvoice.publish.client;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.dragun.ecommerce.integration.meinvoice.publish.dto.MeinvoicePublishInvoiceRequest;
import com.dragun.ecommerce.integration.meinvoice.publish.service.MeinvoicePublishAuthService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class MeinvoicePublishApiClient {

    private final MeinvoiceIntegrationConfig config;
    private final MeinvoicePublishAuthService publishAuthService;
    private final WebClient meinvoiceWebClient;

    public MeinvoicePublishApiClient(MeinvoiceIntegrationConfig config,
                                     MeinvoicePublishAuthService publishAuthService,
                                     @Qualifier("meinvoiceWebClient") WebClient meinvoiceWebClient) {
        this.config = config;
        this.publishAuthService = publishAuthService;
        this.meinvoiceWebClient = meinvoiceWebClient;
    }

    public JsonNode getCertificates() {
        return authorizedGet(MeinvoicePublishConstants.API_PATH_GET_CERTIFICATES, null).block();
    }

    public JsonNode publishInvoices(MeinvoicePublishInvoiceRequest request) {
        return authorizedPost(MeinvoicePublishConstants.API_PATH_INVOICE, request).block();
    }

    public JsonNode getInvoiceStatus(
            boolean invoiceWithCode,
            boolean invoiceCalcu,
            boolean lookupByTransactionId,
            List<String> ids) {
        String inputType = lookupByTransactionId
                ? MeinvoicePublishConstants.INPUT_TYPE_TRANSACTION_ID
                : MeinvoicePublishConstants.INPUT_TYPE_REF_ID;
        return meinvoiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(MeinvoicePublishConstants.API_PATH_INVOICE_STATUS)
                        .queryParam(MeinvoicePublishConstants.QUERY_PARAM_INVOICE_WITH_CODE, invoiceWithCode)
                        .queryParam(MeinvoicePublishConstants.QUERY_PARAM_INPUT_TYPE, inputType)
                        .queryParam(MeinvoicePublishConstants.QUERY_PARAM_INVOICE_CALCU, invoiceCalcu)
                        .build())
                .headers(headers -> applyAuthHeaders(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(ids)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errBody -> Mono.error(new IllegalStateException(String.format(
                                        Locale.ROOT,
                                        MeinvoicePublishConstants.ERROR_HTTP_POST_FORMAT,
                                        MeinvoicePublishConstants.API_PATH_INVOICE_STATUS,
                                        response.statusCode(),
                                        errBody)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(requestTimeoutMs()))
                .block();
    }

    public JsonNode downloadPublishedPdf(boolean invoiceWithCode, boolean invoiceCalcu, String transactionId) {
        return meinvoiceWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(MeinvoicePublishConstants.API_PATH_INVOICE_DOWNLOAD)
                        .queryParam(MeinvoicePublishConstants.QUERY_PARAM_INVOICE_WITH_CODE, invoiceWithCode)
                        .queryParam(MeinvoicePublishConstants.QUERY_PARAM_INVOICE_CALCU, invoiceCalcu)
                        .queryParam(
                                MeinvoicePublishConstants.QUERY_PARAM_DOWNLOAD_DATA_TYPE,
                                MeinvoicePublishConstants.DOWNLOAD_DATA_TYPE_PDF)
                        .build())
                .headers(headers -> applyAuthHeaders(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(transactionId))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errBody -> Mono.error(new IllegalStateException(String.format(
                                        Locale.ROOT,
                                        MeinvoicePublishConstants.ERROR_HTTP_POST_FORMAT,
                                        MeinvoicePublishConstants.API_PATH_INVOICE_DOWNLOAD,
                                        response.statusCode(),
                                        errBody)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(requestTimeoutMs()))
                .block();
    }

    private Mono<JsonNode> authorizedPost(String path, Object body) {
        return meinvoiceWebClient.post()
                .uri(path)
                .headers(headers -> applyAuthHeaders(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errBody -> Mono.error(new IllegalStateException(String.format(
                                        Locale.ROOT,
                                        MeinvoicePublishConstants.ERROR_HTTP_POST_FORMAT,
                                        path,
                                        response.statusCode(),
                                        errBody)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(requestTimeoutMs()));
    }

    private Mono<JsonNode> authorizedGet(String path, Boolean invoiceWithCode) {
        return meinvoiceWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path(path);
                    if (invoiceWithCode != null) {
                        builder.queryParam(
                                MeinvoicePublishConstants.QUERY_PARAM_INVOICE_WITH_CODE,
                                invoiceWithCode);
                    }
                    return builder.build();
                })
                .headers(headers -> applyAuthHeaders(headers))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(errBody -> Mono.error(new IllegalStateException(String.format(
                                        Locale.ROOT,
                                        MeinvoicePublishConstants.ERROR_HTTP_GET_FORMAT,
                                        path,
                                        response.statusCode(),
                                        errBody)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofMillis(requestTimeoutMs()));
    }

    private void applyAuthHeaders(HttpHeaders headers) {
        String token = publishAuthService.getAccessToken();
        headers.set(HttpHeaders.AUTHORIZATION, MeinvoiceIntegrationConstants.bearerToken(token));
        headers.set(MeinvoiceIntegrationConstants.HEADER_TAXCODE, config.getCredentials().getTaxcode());
        headers.set(
                MeinvoicePublishConstants.HEADER_COMPANY_TAX_CODE,
                config.getCredentials().getTaxcode());
    }

    private long requestTimeoutMs() {
        return Math.max(
                MeinvoiceIntegrationConstants.MIN_REQUEST_TIMEOUT_MS,
                config.getApi().getTimeoutMs());
    }
}
