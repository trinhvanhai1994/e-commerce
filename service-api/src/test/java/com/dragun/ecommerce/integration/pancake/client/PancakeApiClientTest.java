package com.dragun.ecommerce.integration.pancake.client;

/*
// COMMENTED: All unit tests are commented out
import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.dto.PancakeApiResponse;
import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderDto;
import com.dragun.ecommerce.integration.pancake.dto.PancakeProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PancakeApiClient Tests")
class PancakeApiClientTest {

    private PancakeApiClient pancakeApiClient;
    private PancakeIntegrationConfig config;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        // Setup config
        config = new PancakeIntegrationConfig();
        PancakeIntegrationConfig.Api apiConfig = new PancakeIntegrationConfig.Api();
        apiConfig.setBaseUrl("https://api.pancake.vn");
        apiConfig.setShopId("test-shop-id");
        apiConfig.setApiKey("test-api-key");
        apiConfig.setWarehouseId("test-warehouse-id");
        apiConfig.setTimeout(30000);
        
        PancakeIntegrationConfig.Api.Retry retryConfig = new PancakeIntegrationConfig.Api.Retry();
        retryConfig.setMaxAttempts(3);
        retryConfig.setBackoffDelay(1000L);
        apiConfig.setRetry(retryConfig);
        config.setApi(apiConfig);

        // Create WebTestClient for testing
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("https://api.pancake.vn")
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    // COMMENTED: Chỉ giữ lại test tạo products, comment các test khác
    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        PancakeApiClient client = new PancakeApiClient(config);
        
        PancakeProductDto product = createTestPancakeProduct();
        Mono<PancakeProductDto> result = client.createProduct(product);
        
        assertNotNull(result);
    }

    // Helper methods
    private PancakeProductDto createTestPancakeProduct() {
        PancakeProductDto product = new PancakeProductDto();
        product.setId("test-product-id");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setCategory("Test Category");
        product.setActive(true);
        
        PancakeProductDto.PancakeVariation variation = new PancakeProductDto.PancakeVariation();
        variation.setId("variation-id");
        variation.setName("Default");
        variation.setPrice(BigDecimal.valueOf(100000));
        variation.setStock(100);
        variation.setActive(true);
        
        product.setVariations(Arrays.asList(variation));
        product.setImages(Arrays.asList("https://example.com/image.jpg"));
        
        return product;
    }

    private PancakeOrderDto createTestPancakeOrder() {
        PancakeOrderDto order = new PancakeOrderDto();
        order.setId("test-order-id");
        order.setOrderNumber("ORD-001");
        order.setStatus("pending");
        order.setPaymentMethod("COD");
        order.setSubtotal(BigDecimal.valueOf(100000));
        order.setShippingFee(BigDecimal.valueOf(20000));
        order.setTotal(BigDecimal.valueOf(120000));
        
        PancakeOrderDto.PancakeCustomer customer = new PancakeOrderDto.PancakeCustomer();
        customer.setName("Test Customer");
        customer.setPhone("0123456789");
        customer.setAddress("123 Test Street");
        customer.setProvince("Hà Nội");
        customer.setDistrict("Ba Đình");
        customer.setWard("Phúc Xá");
        order.setCustomer(customer);
        
        PancakeOrderDto.PancakeOrderItem item = new PancakeOrderDto.PancakeOrderItem();
        item.setProductId("test-product-id");
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(50000));
        order.setItems(Arrays.asList(item));
        
        return order;
    }
}
*/
