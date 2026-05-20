package com.dragun.ecommerce.integration.pancake.client;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.PancakeIntegrationConstants;
import com.dragun.ecommerce.integration.pancake.dto.PancakeApiResponse;
import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderDto;
import com.dragun.ecommerce.integration.pancake.dto.PancakeProductDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class PancakeApiClient {

    private final PancakeIntegrationConfig config;
    private final WebClient webClient;

    public PancakeApiClient(PancakeIntegrationConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(normalizeBaseUrl(config.getApi().getBaseUrl()))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    static String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return PancakeIntegrationConstants.DEFAULT_API_BASE_URL;
        }
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String requireShopId() {
        if (!StringUtils.hasText(config.getApi().getShopId())) {
            throw new IllegalStateException("pancake.api.shop-id is required (PANCAKE_SHOP_ID)");
        }
        return config.getApi().getShopId().trim();
    }

    private String requireApiKey() {
        if (!StringUtils.hasText(config.getApi().getApiKey())) {
            throw new IllegalStateException("pancake.api.api-key is required (PANCAKE_API_KEY)");
        }
        return config.getApi().getApiKey().trim();
    }

    private <T> Mono<T> executeWithRetry(Mono<T> request) {
        return request
                .retryWhen(Retry.backoff(
                        config.getApi().getRetry().getMaxAttempts(),
                        Duration.ofMillis(config.getApi().getRetry().getBackoffDelay())
                ).filter(throwable -> throwable instanceof WebClientResponseException))
                .timeout(Duration.ofMillis(config.getApi().getTimeout()))
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException ex) {
                        log.error("Pancake API error - Status: {}, Response: {}",
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("Pancake API error: {}", error.getMessage());
                    }
                });
    }

    private static <T> T unwrapData(PancakeApiResponse<T> response, T defaultValue) {
        if (response == null) {
            return defaultValue;
        }
        T data = response.getData();
        return data != null ? data : defaultValue;
    }

    /**
     * Raw catalog payload for resilient parsing (Pancake may wrap list under {@code data} / {@code products}).
     */
    public Mono<JsonNode> getProductsCatalogRaw() {
        String shopId = requireShopId();
        return executeWithRetry(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOP_PRODUCTS)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_PAGE_SIZE,
                                        PancakeIntegrationConstants.API_DEFAULT_PAGE_SIZE_PRODUCTS)
                                .build(shopId))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
        );
    }

    public Mono<List<PancakeProductDto>> getProducts() {
        return getProductsCatalogRaw()
                .map(PancakeApiResponseParser::parseProductList);
    }

    public Mono<PancakeProductDto> getProductById(String productId) {
        String shopId = requireShopId();
        return executeWithRetry(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOP_PRODUCT_BY_ID)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .build(shopId, productId))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeProductDto>>() {})
                        .map(response -> unwrapData(response, null))
        );
    }

    public Mono<PancakeProductDto> createProduct(PancakeProductDto product) {
        String shopId = requireShopId();
        log.info("Creating product in Pancake shop {}: {}", shopId, product.getName());

        return executeWithRetry(
                webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOP_PRODUCTS)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .build(shopId))
                        .bodyValue(product)
                        .retrieve()
                        .onStatus(status -> status.isError(), response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(String.format(
                                        Locale.ROOT,
                                        "Failed to create product: %s - %s",
                                        response.statusCode(),
                                        body)))))
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeProductDto>>() {})
                        .map(response -> {
                            PancakeProductDto data = unwrapData(response, null);
                            if (data == null) {
                                throw new RuntimeException("Pancake API returned null product data: "
                                        + (response != null ? response.getMessage() : "no response"));
                            }
                            return data;
                        })
        );
    }

    public Mono<PancakeProductDto> updateProduct(String productId, PancakeProductDto product) {
        String shopId = requireShopId();
        return executeWithRetry(
                webClient.put()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOP_PRODUCT_BY_ID)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .build(shopId, productId))
                        .bodyValue(product)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeProductDto>>() {})
                        .map(response -> unwrapData(response, null))
        );
    }

    public Mono<List<PancakeOrderDto>> getOrders() {
        String shopId = requireShopId();
        return executeWithRetry(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOP_ORDERS)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_PAGE_SIZE,
                                        PancakeIntegrationConstants.API_DEFAULT_PAGE_SIZE_ORDERS)
                                .build(shopId))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(PancakeOrderResponseParser::parseOrderList)
        );
    }

    public Mono<PancakeOrderDto> getOrderById(String orderId) {
        String shopId = requireShopId();
        return executeWithRetry(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOP_ORDER_BY_ID)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .build(shopId, orderId))
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .map(PancakeOrderResponseParser::parseOrderDetail)
        );
    }

    public Mono<PancakeOrderDto> createOrder(PancakeOrderDto order) {
        String shopId = requireShopId();
        return executeWithRetry(
                webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOP_ORDERS)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .build(shopId))
                        .bodyValue(order)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeOrderDto>>() {})
                        .map(response -> unwrapData(response, null))
        );
    }

    public Mono<PancakeOrderDto> updateOrder(String orderId, PancakeOrderDto order) {
        String shopId = requireShopId();
        return executeWithRetry(
                webClient.put()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOP_ORDER_BY_ID)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .build(shopId, orderId))
                        .bodyValue(order)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeOrderDto>>() {})
                        .map(response -> unwrapData(response, null))
        );
    }

    public Mono<Boolean> testConnection() {
        return executeWithRetry(
                webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(PancakeIntegrationConstants.API_PATH_SHOPS)
                                .queryParam(PancakeIntegrationConstants.API_QUERY_PARAM_API_KEY, requireApiKey())
                                .build())
                        .retrieve()
                        .toBodilessEntity()
                        .map(response -> response.getStatusCode().is2xxSuccessful())
        ).onErrorReturn(false);
    }
}
