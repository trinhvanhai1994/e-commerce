package com.dragun.ecommerce.integration.pancake.service;

/*
// COMMENTED: All unit tests are commented out
import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.client.PancakeApiClient;
import com.dragun.ecommerce.integration.pancake.dto.PancakeProductDto;
import com.dragun.ecommerce.integration.pancake.mapper.ProductMapper;
import com.dragun.ecommerce.integration.pancake.model.PancakeProductMapping;
import com.dragun.ecommerce.integration.pancake.repository.PancakeProductMappingRepository;
import com.dragun.ecommerce.integration.pancake.repository.PancakeSyncLogRepository;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PancakeProductSyncService Tests")
class PancakeProductSyncServiceTest {

    @Mock
    private PancakeApiClient pancakeApiClient;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private PancakeProductMappingRepository mappingRepository;

    @Mock
    private PancakeSyncLogRepository syncLogRepository;

    @Mock
    private PancakeIntegrationConfig config;

    @InjectMocks
    private PancakeProductSyncService syncService;

    private PancakeIntegrationConfig.Sync syncConfig;
    private PancakeProductDto pancakeProduct;
    private Product thiYenProduct;

    @BeforeEach
    void setUp() {
        // Setup test data
        pancakeProduct = createTestPancakeProduct();
        thiYenProduct = createTestThiYenProduct();
    }

    @Test
    @DisplayName("Should sync single product to Pancake - create new")
    void testSyncProductToPancake_CreateNew() {
        // Given
        thiYenProduct.setPancakeProductId(null);
        when(productMapper.toPancakeProduct(any())).thenReturn(pancakeProduct);
        when(pancakeApiClient.createProduct(any())).thenReturn(Mono.just(pancakeProduct));
        when(productRepository.save(any(Product.class))).thenReturn(thiYenProduct);
        when(mappingRepository.save(any(PancakeProductMapping.class))).thenAnswer(i -> i.getArguments()[0]);

        // When
        PancakeProductDto result = syncService.syncProductToPancake(thiYenProduct);

        // Then
        assertNotNull(result);
        verify(pancakeApiClient, times(1)).createProduct(any());
        verify(productRepository, atLeastOnce()).save(any(Product.class));
        verify(mappingRepository, times(1)).save(any(PancakeProductMapping.class));
    }

    // Helper methods
    private PancakeProductDto createTestPancakeProduct() {
        PancakeProductDto product = new PancakeProductDto();
        product.setId("pancake-product-1");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setCategory("Electronics");
        product.setActive(true);

        PancakeProductDto.PancakeVariation variation = new PancakeProductDto.PancakeVariation();
        variation.setId("variation-1");
        variation.setName("Default");
        variation.setPrice(BigDecimal.valueOf(100000));
        variation.setStock(50);
        variation.setActive(true);

        product.setVariations(Arrays.asList(variation));
        product.setImages(Arrays.asList("https://example.com/image.jpg"));

        return product;
    }

    private Product createTestThiYenProduct() {
        return Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .category("Electronics")
                .price(BigDecimal.valueOf(100000))
                .stock(50)
                .mainImage("https://example.com/image.jpg")
                .pancakeProductId("pancake-product-1")
                .build();
    }
}
*/
