package com.dragun.ecommerce.integration.pancake.client;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class PancakeApiClient {
    
    private final PancakeIntegrationConfig config;
    private final WebClient webClient;
    
    public PancakeApiClient(PancakeIntegrationConfig config) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(config.getApi().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-API-Key", config.getApi().getApiKey())
                .defaultHeader("X-Shop-Id", config.getApi().getShopId())
                .build();
    }
    
    private <T> Mono<T> executeWithRetry(Mono<T> request) {
        return request
                .retryWhen(Retry.backoff(
                        config.getApi().getRetry().getMaxAttempts(),
                        Duration.ofMillis(config.getApi().getRetry().getBackoffDelay())
                ).filter(throwable -> throwable instanceof WebClientResponseException))
                .timeout(Duration.ofMillis(config.getApi().getTimeout()))
                .doOnError(error -> {
                    if (error instanceof WebClientResponseException) {
                        WebClientResponseException ex = (WebClientResponseException) error;
                        log.error("Pancake API error - Status: {}, Response: {}", 
                                ex.getStatusCode(), ex.getResponseBodyAsString());
                    } else {
                        log.error("Pancake API error: {}", error.getMessage(), error);
                    }
                });
    }
    
    // Product endpoints
    public Mono<List<PancakeProductDto>> getProducts() {
        return executeWithRetry(
                webClient.get()
                        .uri("/api/v1/products")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<List<PancakeProductDto>>>() {})
                        .map(response -> response.getData())
        );
    }
    
    public Mono<PancakeProductDto> getProductById(String productId) {
        return executeWithRetry(
                webClient.get()
                        .uri("/api/v1/products/{id}", productId)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeProductDto>>() {})
                        .map(response -> response.getData())
        );
    }
    
    public Mono<PancakeProductDto> createProduct(PancakeProductDto product) {
        log.info("Creating product in Pancake: {}", product.getName());
        log.debug("Product data: {}", product);
        
        return executeWithRetry(
                webClient.post()
                        .uri("/api/v1/products")
                        .bodyValue(product)
                        .retrieve()
                        .onStatus(status -> status.isError(), response -> {
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Pancake API create product error - Status: {}, Body: {}", 
                                                response.statusCode(), body);
                                        return Mono.error(new RuntimeException(
                                                "Failed to create product: " + response.statusCode() + " - " + body));
                                    });
                        })
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeProductDto>>() {})
                        .doOnNext(response -> {
                            if (response.getSuccess() != null && !response.getSuccess()) {
                                log.warn("Pancake API returned success=false: {}", response.getMessage());
                            }
                        })
                        .map(response -> {
                            if (response.getData() == null) {
                                log.error("Pancake API response data is null. Message: {}", response.getMessage());
                                throw new RuntimeException("Pancake API returned null data: " + response.getMessage());
                            }
                            return response.getData();
                        })
        );
    }
    
    public Mono<PancakeProductDto> updateProduct(String productId, PancakeProductDto product) {
        return executeWithRetry(
                webClient.put()
                        .uri("/api/v1/products/{id}", productId)
                        .bodyValue(product)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeProductDto>>() {})
                        .map(response -> response.getData())
        );
    }
    
    // Order endpoints
    public Mono<List<PancakeOrderDto>> getOrders() {
        return executeWithRetry(
                webClient.get()
                        .uri("/api/v1/orders")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<List<PancakeOrderDto>>>() {})
                        .map(response -> response.getData())
        );
    }
    
    public Mono<PancakeOrderDto> getOrderById(String orderId) {
        return executeWithRetry(
                webClient.get()
                        .uri("/api/v1/orders/{id}", orderId)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeOrderDto>>() {})
                        .map(response -> response.getData())
        );
    }
    
    public Mono<PancakeOrderDto> createOrder(PancakeOrderDto order) {
        return executeWithRetry(
                webClient.post()
                        .uri("/api/v1/orders")
                        .bodyValue(order)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeOrderDto>>() {})
                        .map(response -> response.getData())
        );
    }
    
    public Mono<PancakeOrderDto> updateOrder(String orderId, PancakeOrderDto order) {
        return executeWithRetry(
                webClient.put()
                        .uri("/api/v1/orders/{id}", orderId)
                        .bodyValue(order)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<PancakeApiResponse<PancakeOrderDto>>() {})
                        .map(response -> response.getData())
        );
    }
    
    // Test connection
    public Mono<Boolean> testConnection() {
        return executeWithRetry(
                webClient.get()
                        .uri("/api/v1/health")
                        .retrieve()
                        .toBodilessEntity()
                        .map(response -> response.getStatusCode().is2xxSuccessful())
        ).onErrorReturn(false);
    }
}

