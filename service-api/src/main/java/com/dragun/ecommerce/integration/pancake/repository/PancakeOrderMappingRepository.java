package com.dragun.ecommerce.integration.pancake.repository;

import com.dragun.ecommerce.integration.pancake.model.PancakeOrderMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PancakeOrderMappingRepository extends JpaRepository<PancakeOrderMapping, Long> {
    
    Optional<PancakeOrderMapping> findByLocalOrderId(Long localOrderId);
    
    Optional<PancakeOrderMapping> findByPancakeOrderId(String pancakeOrderId);
    
    boolean existsByLocalOrderId(Long localOrderId);
    
    boolean existsByPancakeOrderId(String pancakeOrderId);
}

