package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String name);
    Optional<Product> findByPancakeProductId(String pancakeProductId);
    List<Product> findByPancakeProductIdIsNull();
    
    @Query("SELECT p FROM Product p WHERE p.pancakeSyncedAt IS NULL OR p.updatedAt > :updatedAt")
    List<Product> findProductsNeedingSync(@Param("updatedAt") LocalDateTime updatedAt);
}


