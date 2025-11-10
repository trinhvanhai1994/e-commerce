package com.dragun.ecommerce.integration.pancake.mapper;

import com.dragun.ecommerce.integration.pancake.dto.PancakeProductDto;
import com.dragun.ecommerce.model.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ProductMapper {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    /**
     * Map Pancake Product to Thi Yen Product
     */
    public Product toThiYenProduct(PancakeProductDto pancakeProduct) {
        if (pancakeProduct == null) {
            return null;
        }
        
        Product product = new Product();
        product.setName(pancakeProduct.getName());
        product.setDescription(pancakeProduct.getDescription());
        product.setCategory(pancakeProduct.getCategory());
        product.setPancakeProductId(pancakeProduct.getId());
        
        // Handle variations - use first active variation or first variation
        if (pancakeProduct.getVariations() != null && !pancakeProduct.getVariations().isEmpty()) {
            PancakeProductDto.PancakeVariation variation = pancakeProduct.getVariations().stream()
                    .filter(v -> v.getActive() != null && v.getActive())
                    .findFirst()
                    .orElse(pancakeProduct.getVariations().get(0));
            
            product.setPrice(variation.getPrice() != null ? variation.getPrice() : BigDecimal.ZERO);
            product.setStock(variation.getStock() != null ? variation.getStock() : 0);
            
            // Use variation image if available, otherwise use product images
            if (variation.getImage() != null && !variation.getImage().isEmpty()) {
                product.setMainImage(variation.getImage());
            } else if (pancakeProduct.getImages() != null && !pancakeProduct.getImages().isEmpty()) {
                product.setMainImage(pancakeProduct.getImages().get(0));
                if (pancakeProduct.getImages().size() > 1) {
                    product.setGallery(pancakeProduct.getImages().subList(1, pancakeProduct.getImages().size()));
                }
            }
        } else {
            product.setPrice(BigDecimal.ZERO);
            product.setStock(0);
        }
        
        // Set images
        if (product.getMainImage() == null && pancakeProduct.getImages() != null && !pancakeProduct.getImages().isEmpty()) {
            product.setMainImage(pancakeProduct.getImages().get(0));
            if (pancakeProduct.getImages().size() > 1) {
                product.setGallery(pancakeProduct.getImages().subList(1, pancakeProduct.getImages().size()));
            }
        }
        
        // Parse dates
        if (pancakeProduct.getCreatedAt() != null) {
            try {
                product.setCreatedAt(LocalDateTime.parse(pancakeProduct.getCreatedAt(), DATE_TIME_FORMATTER));
            } catch (Exception e) {
                product.setCreatedAt(LocalDateTime.now());
            }
        } else {
            product.setCreatedAt(LocalDateTime.now());
        }
        
        product.setUpdatedAt(LocalDateTime.now());
        product.setPancakeSyncedAt(LocalDateTime.now());
        
        return product;
    }
    
    /**
     * Map Thi Yen Product to Pancake Product
     */
    public PancakeProductDto toPancakeProduct(Product thiYenProduct) {
        if (thiYenProduct == null) {
            return null;
        }
        
        PancakeProductDto pancakeProduct = new PancakeProductDto();
        pancakeProduct.setId(thiYenProduct.getPancakeProductId());
        pancakeProduct.setName(thiYenProduct.getName());
        pancakeProduct.setDescription(thiYenProduct.getDescription());
        pancakeProduct.setCategory(thiYenProduct.getCategory());
        pancakeProduct.setActive(true);
        
        // Build images list
        List<String> images = new ArrayList<>();
        if (thiYenProduct.getMainImage() != null) {
            images.add(thiYenProduct.getMainImage());
        }
        // Safely handle lazy-loaded gallery collection
        try {
            if (thiYenProduct.getGallery() != null && !thiYenProduct.getGallery().isEmpty()) {
                images.addAll(thiYenProduct.getGallery());
            }
        } catch (org.hibernate.LazyInitializationException e) {
            // Gallery is not initialized, skip it
            log.debug("Gallery not initialized for product {}, skipping gallery images", thiYenProduct.getId());
        }
        pancakeProduct.setImages(images);
        
        // Create variation from product
        PancakeProductDto.PancakeVariation variation = new PancakeProductDto.PancakeVariation();
        variation.setName("Default");
        variation.setPrice(thiYenProduct.getPrice());
        variation.setStock(thiYenProduct.getStock());
        variation.setActive(true);
        if (thiYenProduct.getMainImage() != null) {
            variation.setImage(thiYenProduct.getMainImage());
        }
        
        pancakeProduct.setVariations(List.of(variation));
        
        return pancakeProduct;
    }
    
    /**
     * Update Thi Yen Product with Pancake Product data
     */
    public void updateThiYenProduct(Product existingProduct, PancakeProductDto pancakeProduct) {
        if (pancakeProduct == null || existingProduct == null) {
            return;
        }
        
        existingProduct.setName(pancakeProduct.getName());
        existingProduct.setDescription(pancakeProduct.getDescription());
        existingProduct.setCategory(pancakeProduct.getCategory());
        existingProduct.setPancakeProductId(pancakeProduct.getId());
        
        // Handle variations
        if (pancakeProduct.getVariations() != null && !pancakeProduct.getVariations().isEmpty()) {
            PancakeProductDto.PancakeVariation variation = pancakeProduct.getVariations().stream()
                    .filter(v -> v.getActive() != null && v.getActive())
                    .findFirst()
                    .orElse(pancakeProduct.getVariations().get(0));
            
            existingProduct.setPrice(variation.getPrice() != null ? variation.getPrice() : BigDecimal.ZERO);
            existingProduct.setStock(variation.getStock() != null ? variation.getStock() : 0);
            
            if (variation.getImage() != null && !variation.getImage().isEmpty()) {
                existingProduct.setMainImage(variation.getImage());
            } else if (pancakeProduct.getImages() != null && !pancakeProduct.getImages().isEmpty()) {
                existingProduct.setMainImage(pancakeProduct.getImages().get(0));
                if (pancakeProduct.getImages().size() > 1) {
                    existingProduct.setGallery(pancakeProduct.getImages().subList(1, pancakeProduct.getImages().size()));
                }
            }
        }
        
        // Update images
        if (pancakeProduct.getImages() != null && !pancakeProduct.getImages().isEmpty()) {
            if (existingProduct.getMainImage() == null) {
                existingProduct.setMainImage(pancakeProduct.getImages().get(0));
                if (pancakeProduct.getImages().size() > 1) {
                    existingProduct.setGallery(pancakeProduct.getImages().subList(1, pancakeProduct.getImages().size()));
                }
            }
        }
        
        existingProduct.setUpdatedAt(LocalDateTime.now());
        existingProduct.setPancakeSyncedAt(LocalDateTime.now());
    }
}

