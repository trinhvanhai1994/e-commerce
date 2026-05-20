package com.dragun.ecommerce.integration.pancake.service;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.PancakeCatalogConstants;
import com.dragun.ecommerce.integration.pancake.PancakeIntegrationConstants;
import com.dragun.ecommerce.integration.pancake.client.PancakeApiClient;
import com.dragun.ecommerce.integration.pancake.dto.PancakeProductDto;
import com.dragun.ecommerce.integration.pancake.mapper.ProductMapper;
import com.dragun.ecommerce.integration.pancake.model.PancakeProductMapping;
import com.dragun.ecommerce.integration.pancake.model.PancakeSyncLog;
import com.dragun.ecommerce.integration.pancake.repository.PancakeProductMappingRepository;
import com.dragun.ecommerce.integration.pancake.repository.PancakeSyncLogRepository;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PancakeProductSyncService {
    
    private final PancakeApiClient pancakeApiClient;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final PancakeProductMappingRepository mappingRepository;
    private final PancakeSyncLogRepository syncLogRepository;
    private final PancakeIntegrationConfig config;
    private final PancakeCatalogFetchService catalogFetchService;
    
    /**
     * Sync products from Pancake to Thi Yen
     */
    @Transactional
    public int syncFromPancake() {
        if (!config.getSync().getEnabled()
                || !PancakeIntegrationConstants.isSyncFromPancakeEnabled(config.getSync().getDirection())) {
            log.info("Sync from Pancake is disabled");
            return 0;
        }
        
        try {
            try {
                catalogFetchService.fetchAndLinkCatalog();
            } catch (Exception e) {
                log.warn("Catalog prefetch before product import failed (continuing): {}", e.getMessage());
            }

            List<PancakeProductDto> pancakeProducts = pancakeApiClient.getProducts().block();
            if (pancakeProducts == null || pancakeProducts.isEmpty()) {
                log.info("No products found in Pancake");
                return 0;
            }
            
            int syncedCount = 0;
            for (PancakeProductDto pancakeProduct : pancakeProducts) {
                try {
                    syncProductFromPancake(pancakeProduct);
                    syncedCount++;
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_PRODUCT, pancakeProduct.getId(),
                            PancakeIntegrationConstants.SYNC_DIRECTION_FROM_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_SUCCESS, null);
                } catch (Exception e) {
                    log.error("Error syncing product {} from Pancake: {}", pancakeProduct.getId(), e.getMessage());
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_PRODUCT, pancakeProduct.getId(),
                            PancakeIntegrationConstants.SYNC_DIRECTION_FROM_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_FAILED, e.getMessage());
                }
            }
            
            log.info("Synced {} products from Pancake", syncedCount);
            return syncedCount;
        } catch (Exception e) {
            log.error("Error syncing products from Pancake: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Sync products from Thi Yen to Pancake
     */
    @Transactional
    public int syncToPancake() {
        if (!config.getSync().getEnabled()
                || !PancakeIntegrationConstants.isSyncToPancakeEnabled(config.getSync().getDirection())) {
            log.info("Sync to Pancake is disabled");
            return 0;
        }
        
        try {
            // Get products that need syncing (not synced or updated after last sync)
            List<Product> productsToSync = productRepository.findProductsNeedingSync(
                    LocalDateTime.now().minusDays(1));
            
            if (productsToSync.isEmpty()) {
                log.info("No products to sync to Pancake");
                return 0;
            }
            
            int syncedCount = 0;
            for (Product product : productsToSync) {
                if (!PancakeCatalogConstants.isSyncableToPancake(product)) {
                    log.debug("Skipping non-syncable product id={} pancakeId={}",
                            product.getId(), product.getPancakeProductId());
                    continue;
                }
                try {
                    syncProductToPancake(product);
                    syncedCount++;
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_PRODUCT, String.valueOf(product.getId()),
                            PancakeIntegrationConstants.SYNC_DIRECTION_TO_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_SUCCESS, null);
                } catch (Exception e) {
                    log.error("Error syncing product {} to Pancake: {}", product.getId(), e.getMessage());
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_PRODUCT, String.valueOf(product.getId()),
                            PancakeIntegrationConstants.SYNC_DIRECTION_TO_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_FAILED, e.getMessage());
                }
            }
            
            log.info("Synced {} products to Pancake", syncedCount);
            return syncedCount;
        } catch (Exception e) {
            log.error("Error syncing products to Pancake: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Sync a single product from Pancake
     */
    @Transactional
    public Product syncProductFromPancake(PancakeProductDto pancakeProduct) {
        // Check if product already exists by pancake_product_id
        Optional<Product> existingProductOpt = productRepository.findByPancakeProductId(pancakeProduct.getId());
        
        Product product;
        if (existingProductOpt.isPresent()) {
            // Update existing product
            product = existingProductOpt.get();
            productMapper.updateThiYenProduct(product, pancakeProduct);
        } else {
            // Check mapping table
            Optional<PancakeProductMapping> mappingOpt = mappingRepository.findByPancakeProductId(pancakeProduct.getId());
            if (mappingOpt.isPresent()) {
                product = mappingOpt.get().getLocalProduct();
                productMapper.updateThiYenProduct(product, pancakeProduct);
            } else {
                // Create new product
                product = productMapper.toThiYenProduct(pancakeProduct);
            }
        }
        
        product = productRepository.save(product);
        
        // Update or create mapping
        Optional<PancakeProductMapping> mappingOpt = mappingRepository.findByLocalProductId(product.getId());
        PancakeProductMapping mapping;
        if (mappingOpt.isPresent()) {
            mapping = mappingOpt.get();
            mapping.setPancakeProductId(pancakeProduct.getId());
            if (pancakeProduct.getVariations() != null && !pancakeProduct.getVariations().isEmpty()) {
                mapping.setPancakeVariationId(pancakeProduct.getVariations().get(0).getId());
            }
        } else {
            mapping = PancakeProductMapping.builder()
                    .localProduct(product)
                    .pancakeProductId(pancakeProduct.getId())
                    .pancakeVariationId(pancakeProduct.getVariations() != null && !pancakeProduct.getVariations().isEmpty() 
                            ? pancakeProduct.getVariations().get(0).getId() : null)
                    .build();
        }
        mapping.setLastSyncedAt(LocalDateTime.now());
        mappingRepository.save(mapping);
        
        return product;
    }
    
    /**
     * Sync a single product to Pancake
     */
    @Transactional
    public PancakeProductDto syncProductToPancake(Product product) {
        if (!PancakeCatalogConstants.isSyncableToPancake(product)) {
            throw new IllegalArgumentException(
                    "Product is not eligible for Pancake push (system/placeholder): id=" + product.getId());
        }
        try {
            Long productId = product.getId();
            log.info("Syncing product to Pancake: {} (ID: {})", product.getName(), productId);

            // Reload product with gallery to avoid LazyInitializationException
            Product productWithGallery = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
            
            // Initialize gallery collection if needed
            if (productWithGallery.getGallery() != null) {
                productWithGallery.getGallery().size(); // Force initialization
            }
            
            PancakeProductDto pancakeProduct = productMapper.toPancakeProduct(productWithGallery);
            
            // Validate required fields
            if (pancakeProduct.getName() == null || pancakeProduct.getName().isEmpty()) {
                throw new IllegalArgumentException("Product name is required");
            }
            if (pancakeProduct.getVariations() == null || pancakeProduct.getVariations().isEmpty()) {
                throw new IllegalArgumentException("Product must have at least one variation");
            }
            if (pancakeProduct.getVariations().get(0).getPrice() == null) {
                throw new IllegalArgumentException("Product variation price is required");
            }
            
            log.debug("Mapped product data: name={}, variations={}", 
                    pancakeProduct.getName(), pancakeProduct.getVariations().size());
            
            if (product.getPancakeProductId() != null
                    && !product.getPancakeProductId().isEmpty()
                    && !PancakeCatalogConstants.isSystemPancakeProductId(product.getPancakeProductId())) {
                // Update existing product in Pancake
                log.info("Updating existing product in Pancake: {}", product.getPancakeProductId());
                pancakeProduct = pancakeApiClient.updateProduct(product.getPancakeProductId(), pancakeProduct)
                        .doOnError(error -> log.error("Error updating product in Pancake: {}", error.getMessage(), error))
                        .block();
            } else {
                // Create new product in Pancake
                log.info("Creating new product in Pancake");
                pancakeProduct = pancakeApiClient.createProduct(pancakeProduct)
                        .doOnError(error -> {
                            log.error("Error creating product in Pancake: {}", error.getMessage(), error);
                            if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                                org.springframework.web.reactive.function.client.WebClientResponseException ex = 
                                        (org.springframework.web.reactive.function.client.WebClientResponseException) error;
                                log.error("Response status: {}, Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                            }
                        })
                        .block();
                
                if (pancakeProduct != null && pancakeProduct.getId() != null) {
                    log.info("Product created successfully in Pancake. ID: {}", pancakeProduct.getId());
                    product.setPancakeProductId(pancakeProduct.getId());
                    product.setPancakeSyncedAt(LocalDateTime.now());
                    productRepository.save(product);
                    
                    // Create mapping
                    PancakeProductMapping mapping = PancakeProductMapping.builder()
                            .localProduct(product)
                            .pancakeProductId(pancakeProduct.getId())
                            .pancakeVariationId(pancakeProduct.getVariations() != null && !pancakeProduct.getVariations().isEmpty() 
                                    ? pancakeProduct.getVariations().get(0).getId() : null)
                            .build();
                    mappingRepository.save(mapping);
                    log.info("Created product mapping: local={}, pancake={}", product.getId(), pancakeProduct.getId());
                } else {
                    log.error("Failed to create product: response is null or missing ID");
                    throw new RuntimeException("Failed to create product in Pancake: response is null or missing ID");
                }
            }
            
            if (pancakeProduct != null) {
                product.setPancakeSyncedAt(LocalDateTime.now());
                productRepository.save(product);
            }
            
            return pancakeProduct;
        } catch (Exception e) {
            log.error("Error syncing product {} to Pancake: {}", product.getName(), e.getMessage(), e);
            throw e;
        }
    }
    
    private void logSync(String entityType, String entityId, String direction, String status, String errorMessage) {
        PancakeSyncLog log = PancakeSyncLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .syncDirection(direction)
                .status(status)
                .errorMessage(errorMessage)
                .build();
        syncLogRepository.save(log);
    }
}

