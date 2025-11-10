package com.dragun.ecommerce.integration.pancake.mapper;

import com.dragun.ecommerce.integration.pancake.dto.PancakeProductDto;
import com.dragun.ecommerce.model.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductMapper Tests")
class ProductMapperTest {

    @InjectMocks
    private ProductMapper productMapper;

    private PancakeProductDto pancakeProduct;
    private Product thiYenProduct;

    @BeforeEach
    void setUp() {
        // Setup Pancake Product
        pancakeProduct = new PancakeProductDto();
        pancakeProduct.setId("pancake-product-1");
        pancakeProduct.setName("Test Product");
        pancakeProduct.setDescription("Test Description");
        pancakeProduct.setCategory("Electronics");
        pancakeProduct.setActive(true);
        pancakeProduct.setCreatedAt("2024-01-01T10:00:00");
        pancakeProduct.setUpdatedAt("2024-01-02T10:00:00");

        PancakeProductDto.PancakeVariation variation = new PancakeProductDto.PancakeVariation();
        variation.setId("variation-1");
        variation.setName("Default");
        variation.setPrice(BigDecimal.valueOf(100000));
        variation.setStock(50);
        variation.setActive(true);
        variation.setImage("https://example.com/variation-image.jpg");

        pancakeProduct.setVariations(Arrays.asList(variation));
        pancakeProduct.setImages(Arrays.asList(
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg",
                "https://example.com/image3.jpg"
        ));

        // Setup Thi Yen Product
        thiYenProduct = Product.builder()
                .id(1L)
                .name("Thi Yen Product")
                .description("Thi Yen Description")
                .category("Food")
                .price(BigDecimal.valueOf(50000))
                .stock(100)
                .mainImage("https://example.com/main.jpg")
                .gallery(Arrays.asList("https://example.com/gallery1.jpg"))
                .pancakeProductId("pancake-product-1")
                .build();
    }

    @Test
    @DisplayName("Should map Pancake Product to Thi Yen Product successfully")
    void testToThiYenProduct_Success() {
        // When
        Product result = productMapper.toThiYenProduct(pancakeProduct);

        // Then
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("Electronics", result.getCategory());
        assertEquals("pancake-product-1", result.getPancakeProductId());
        assertEquals(BigDecimal.valueOf(100000), result.getPrice());
        assertEquals(50, result.getStock());
        assertEquals("https://example.com/variation-image.jpg", result.getMainImage());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertNotNull(result.getPancakeSyncedAt());
    }

    @Test
    @DisplayName("Should return null when Pancake Product is null")
    void testToThiYenProduct_NullInput() {
        // When
        Product result = productMapper.toThiYenProduct(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should use product images when variation has no image")
    void testToThiYenProduct_UseProductImages() {
        // Given
        pancakeProduct.getVariations().get(0).setImage(null);
        pancakeProduct.setImages(Arrays.asList("https://example.com/main.jpg", "https://example.com/gallery.jpg"));

        // When
        Product result = productMapper.toThiYenProduct(pancakeProduct);

        // Then
        assertNotNull(result);
        assertEquals("https://example.com/main.jpg", result.getMainImage());
        assertEquals(1, result.getGallery().size());
        assertEquals("https://example.com/gallery.jpg", result.getGallery().get(0));
    }

    @Test
    @DisplayName("Should handle product with no variations")
    void testToThiYenProduct_NoVariations() {
        // Given
        pancakeProduct.setVariations(null);

        // When
        Product result = productMapper.toThiYenProduct(pancakeProduct);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getPrice());
        assertEquals(0, result.getStock());
    }

    @Test
    @DisplayName("Should select first active variation")
    void testToThiYenProduct_SelectActiveVariation() {
        // Given
        PancakeProductDto.PancakeVariation inactiveVariation = new PancakeProductDto.PancakeVariation();
        inactiveVariation.setId("variation-2");
        inactiveVariation.setName("Inactive");
        inactiveVariation.setPrice(BigDecimal.valueOf(200000));
        inactiveVariation.setStock(30);
        inactiveVariation.setActive(false);

        PancakeProductDto.PancakeVariation activeVariation = new PancakeProductDto.PancakeVariation();
        activeVariation.setId("variation-3");
        activeVariation.setName("Active");
        activeVariation.setPrice(BigDecimal.valueOf(150000));
        activeVariation.setStock(40);
        activeVariation.setActive(true);

        pancakeProduct.setVariations(Arrays.asList(inactiveVariation, activeVariation));

        // When
        Product result = productMapper.toThiYenProduct(pancakeProduct);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(150000), result.getPrice());
        assertEquals(40, result.getStock());
    }

    @Test
    @DisplayName("Should map Thi Yen Product to Pancake Product successfully")
    void testToPancakeProduct_Success() {
        // When
        PancakeProductDto result = productMapper.toPancakeProduct(thiYenProduct);

        // Then
        assertNotNull(result);
        assertEquals("pancake-product-1", result.getId());
        assertEquals("Thi Yen Product", result.getName());
        assertEquals("Thi Yen Description", result.getDescription());
        assertEquals("Food", result.getCategory());
        assertTrue(result.getActive());
        assertNotNull(result.getImages());
        assertEquals(2, result.getImages().size());
        assertNotNull(result.getVariations());
        assertEquals(1, result.getVariations().size());
        assertEquals("Default", result.getVariations().get(0).getName());
        assertEquals(BigDecimal.valueOf(50000), result.getVariations().get(0).getPrice());
        assertEquals(100, result.getVariations().get(0).getStock());
    }

    @Test
    @DisplayName("Should return null when Thi Yen Product is null")
    void testToPancakeProduct_NullInput() {
        // When
        PancakeProductDto result = productMapper.toPancakeProduct(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle product with no images")
    void testToPancakeProduct_NoImages() {
        // Given
        thiYenProduct.setMainImage(null);
        thiYenProduct.setGallery(null);

        // When
        PancakeProductDto result = productMapper.toPancakeProduct(thiYenProduct);

        // Then
        assertNotNull(result);
        assertNotNull(result.getImages());
        assertTrue(result.getImages().isEmpty());
    }

    @Test
    @DisplayName("Should update Thi Yen Product with Pancake Product data")
    void testUpdateThiYenProduct_Success() {
        // Given
        Product existingProduct = Product.builder()
                .id(1L)
                .name("Old Name")
                .price(BigDecimal.valueOf(50000))
                .stock(100)
                .build();

        // When
        productMapper.updateThiYenProduct(existingProduct, pancakeProduct);

        // Then
        assertEquals("Test Product", existingProduct.getName());
        assertEquals("Test Description", existingProduct.getDescription());
        assertEquals("Electronics", existingProduct.getCategory());
        assertEquals("pancake-product-1", existingProduct.getPancakeProductId());
        assertEquals(BigDecimal.valueOf(100000), existingProduct.getPrice());
        assertEquals(50, existingProduct.getStock());
        assertNotNull(existingProduct.getUpdatedAt());
        assertNotNull(existingProduct.getPancakeSyncedAt());
    }

    @Test
    @DisplayName("Should not update when inputs are null")
    void testUpdateThiYenProduct_NullInputs() {
        // Given
        Product existingProduct = Product.builder()
                .id(1L)
                .name("Original Name")
                .build();

        // When
        productMapper.updateThiYenProduct(null, pancakeProduct);
        productMapper.updateThiYenProduct(existingProduct, null);
        productMapper.updateThiYenProduct(null, null);

        // Then
        assertEquals("Original Name", existingProduct.getName());
    }

    @Test
    @DisplayName("Should handle date parsing errors gracefully")
    void testToThiYenProduct_InvalidDate() {
        // Given
        pancakeProduct.setCreatedAt("invalid-date-format");

        // When
        Product result = productMapper.toThiYenProduct(pancakeProduct);

        // Then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt()); // Should default to now()
    }

    @Test
    @DisplayName("Should handle empty variations list")
    void testToThiYenProduct_EmptyVariations() {
        // Given
        pancakeProduct.setVariations(Collections.emptyList());

        // When
        Product result = productMapper.toThiYenProduct(pancakeProduct);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getPrice());
        assertEquals(0, result.getStock());
    }

    @Test
    @DisplayName("Should handle variation with null price and stock")
    void testToThiYenProduct_NullPriceAndStock() {
        // Given
        pancakeProduct.getVariations().get(0).setPrice(null);
        pancakeProduct.getVariations().get(0).setStock(null);

        // When
        Product result = productMapper.toThiYenProduct(pancakeProduct);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getPrice());
        assertEquals(0, result.getStock());
    }
}

