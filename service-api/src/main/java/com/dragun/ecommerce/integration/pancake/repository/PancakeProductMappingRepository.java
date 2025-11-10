package com.dragun.ecommerce.integration.pancake.repository;

import com.dragun.ecommerce.integration.pancake.model.PancakeProductMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PancakeProductMappingRepository extends JpaRepository<PancakeProductMapping, Long> {
    
    Optional<PancakeProductMapping> findByLocalProductId(Long localProductId);
    
    Optional<PancakeProductMapping> findByPancakeProductId(String pancakeProductId);
    
    boolean existsByLocalProductId(Long localProductId);
    
    boolean existsByPancakeProductId(String pancakeProductId);
}

