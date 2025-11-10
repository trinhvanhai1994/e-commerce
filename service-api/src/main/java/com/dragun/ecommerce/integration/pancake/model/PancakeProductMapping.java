package com.dragun.ecommerce.integration.pancake.model;

import com.dragun.ecommerce.model.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pancake_product_mapping")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PancakeProductMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_product_id", nullable = false)
    private Product localProduct;
    
    @Column(name = "pancake_product_id", nullable = false)
    private String pancakeProductId;
    
    @Column(name = "pancake_variation_id")
    private String pancakeVariationId;
    
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastSyncedAt == null) {
            lastSyncedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

