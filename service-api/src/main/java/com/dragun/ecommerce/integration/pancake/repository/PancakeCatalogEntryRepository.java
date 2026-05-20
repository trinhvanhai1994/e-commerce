package com.dragun.ecommerce.integration.pancake.repository;

import com.dragun.ecommerce.integration.pancake.model.PancakeCatalogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PancakeCatalogEntryRepository extends JpaRepository<PancakeCatalogEntry, Long> {

    Optional<PancakeCatalogEntry> findByShopIdAndPancakeProductIdAndPancakeVariationId(
            String shopId, String pancakeProductId, String pancakeVariationId);

    List<PancakeCatalogEntry> findByShopIdAndLocalProductIdIsNull(String shopId);

    long countByShopId(String shopId);

    Optional<PancakeCatalogEntry> findFirstByShopIdAndPancakeVariationId(String shopId, String pancakeVariationId);

    Optional<PancakeCatalogEntry> findFirstByShopIdAndPancakeProductIdAndPancakeVariationId(
            String shopId, String pancakeProductId, String pancakeVariationId);
}
