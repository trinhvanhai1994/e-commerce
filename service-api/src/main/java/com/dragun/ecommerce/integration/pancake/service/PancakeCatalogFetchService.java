package com.dragun.ecommerce.integration.pancake.service;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.PancakeCatalogConstants;
import com.dragun.ecommerce.integration.pancake.client.PancakeApiClient;
import com.dragun.ecommerce.integration.pancake.dto.PancakeProductDto;
import com.dragun.ecommerce.integration.pancake.model.PancakeCatalogEntry;
import com.dragun.ecommerce.integration.pancake.model.PancakeProductMapping;
import com.dragun.ecommerce.integration.pancake.repository.PancakeCatalogEntryRepository;
import com.dragun.ecommerce.integration.pancake.repository.PancakeProductMappingRepository;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Fetches Pancake POS catalog into {@code pancake_catalog_entry} and links rows to local {@link Product} for order/invoice sync.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PancakeCatalogFetchService {

    private final PancakeApiClient pancakeApiClient;
    private final PancakeCatalogEntryRepository catalogEntryRepository;
    private final ProductRepository productRepository;
    private final PancakeProductMappingRepository mappingRepository;
    private final PancakeIntegrationConfig config;
    private final ObjectMapper objectMapper;

    @Transactional
    public CatalogFetchResult fetchAndLinkCatalog() {
        String shopId = config.getApi().getShopId();
        if (!StringUtils.hasText(shopId)) {
            throw new IllegalStateException("pancake.api.shop-id is required to fetch catalog");
        }
        shopId = shopId.trim();

        List<PancakeProductDto> pancakeProducts = pancakeApiClient.getProducts().block();
        if (pancakeProducts == null || pancakeProducts.isEmpty()) {
            log.warn("Pancake catalog fetch returned no products for shop {}", shopId);
            return new CatalogFetchResult(0, 0, catalogEntryRepository.countByShopId(shopId));
        }

        int upserted = 0;
        for (PancakeProductDto pancakeProduct : pancakeProducts) {
            if (pancakeProduct.getId() == null || PancakeCatalogConstants.isSystemPancakeProductId(pancakeProduct.getId())) {
                continue;
            }
            if (pancakeProduct.getVariations() == null || pancakeProduct.getVariations().isEmpty()) {
                upserted += upsertEntry(shopId, pancakeProduct, null);
            } else {
                for (PancakeProductDto.PancakeVariation variation : pancakeProduct.getVariations()) {
                    upserted += upsertEntry(shopId, pancakeProduct, variation);
                }
            }
        }

        int linked = linkUnmappedEntries(shopId);
        log.info("Pancake catalog fetch shop {}: upserted={}, linked={}, totalEntries={}",
                shopId, upserted, linked, catalogEntryRepository.countByShopId(shopId));
        return new CatalogFetchResult(upserted, linked, catalogEntryRepository.countByShopId(shopId));
    }

    private int upsertEntry(String shopId, PancakeProductDto product, PancakeProductDto.PancakeVariation variation) {
        String variationId = variation != null && StringUtils.hasText(variation.getId()) ? variation.getId().trim() : "";
        String lookupKey = variationId.isEmpty() ? product.getId() : variationId;

        PancakeCatalogEntry entry = catalogEntryRepository
                .findByShopIdAndPancakeProductIdAndPancakeVariationId(shopId, product.getId(), variationId)
                .orElseGet(() -> PancakeCatalogEntry.builder()
                        .shopId(shopId)
                        .pancakeProductId(product.getId())
                        .pancakeVariationId(variationId)
                        .build());

        entry.setName(variation != null && StringUtils.hasText(variation.getName())
                ? variation.getName()
                : product.getName());
        entry.setSku(variation != null ? variation.getSku() : null);
        entry.setPrice(resolvePrice(product, variation));
        entry.setCategory(product.getCategory());
        entry.setActive(variation != null && variation.getActive() != null
                ? variation.getActive()
                : product.getActive());
        entry.setFetchedAt(LocalDateTime.now());
        entry.setRawPayload(serializeRaw(product, variation));

        catalogEntryRepository.save(entry);

        Optional<Product> local = productRepository.findByPancakeProductId(lookupKey);
        if (local.isEmpty() && StringUtils.hasText(product.getId())) {
            local = productRepository.findByPancakeProductId(product.getId());
        }
        if (local.isPresent() && PancakeCatalogConstants.isSyncableToPancake(local.get())) {
            attachLocalProduct(entry, local.get(), product.getId(), variationId);
        }
        return 1;
    }

    private int linkUnmappedEntries(String shopId) {
        int linked = 0;
        for (PancakeCatalogEntry entry : catalogEntryRepository.findByShopIdAndLocalProductIdIsNull(shopId)) {
            String lookupId = StringUtils.hasText(entry.getPancakeVariationId())
                    ? entry.getPancakeVariationId()
                    : entry.getPancakeProductId();
            Optional<Product> local = productRepository.findByPancakeProductId(lookupId);
            if (local.isEmpty()) {
                local = productRepository.findByPancakeProductId(entry.getPancakeProductId());
            }
            if (local.isPresent() && PancakeCatalogConstants.isSyncableToPancake(local.get())) {
                attachLocalProduct(entry, local.get(), entry.getPancakeProductId(), entry.getPancakeVariationId());
                linked++;
            }
        }
        return linked;
    }

    private void attachLocalProduct(
            PancakeCatalogEntry entry,
            Product local,
            String pancakeProductId,
            String pancakeVariationId) {
        entry.setLocalProduct(local);
        catalogEntryRepository.save(entry);

        if (!StringUtils.hasText(local.getPancakeProductId())) {
            String idToStore = StringUtils.hasText(pancakeVariationId) ? pancakeVariationId : pancakeProductId;
            local.setPancakeProductId(idToStore);
            local.setPancakeSyncedAt(LocalDateTime.now());
            productRepository.save(local);
        }

        if (!mappingRepository.existsByPancakeProductId(pancakeProductId)) {
            mappingRepository.save(PancakeProductMapping.builder()
                    .localProduct(local)
                    .pancakeProductId(pancakeProductId)
                    .pancakeVariationId(StringUtils.hasText(pancakeVariationId) ? pancakeVariationId : null)
                    .lastSyncedAt(LocalDateTime.now())
                    .build());
        }
    }

    private static BigDecimal resolvePrice(PancakeProductDto product, PancakeProductDto.PancakeVariation variation) {
        if (variation != null && variation.getPrice() != null) {
            return variation.getPrice();
        }
        if (product.getVariations() != null && !product.getVariations().isEmpty()
                && product.getVariations().get(0).getPrice() != null) {
            return product.getVariations().get(0).getPrice();
        }
        return BigDecimal.ZERO;
    }

    private String serializeRaw(PancakeProductDto product, PancakeProductDto.PancakeVariation variation) {
        try {
            if (variation != null) {
                return objectMapper.writeValueAsString(java.util.Map.of("product", product, "variation", variation));
            }
            return objectMapper.writeValueAsString(product);
        } catch (Exception e) {
            return null;
        }
    }

    public record CatalogFetchResult(int entriesUpserted, int entriesLinked, long totalEntries) {
    }
}
