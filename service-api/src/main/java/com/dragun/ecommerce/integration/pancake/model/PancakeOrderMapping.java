package com.dragun.ecommerce.integration.pancake.model;

import com.dragun.ecommerce.model.entity.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pancake_order_mapping")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PancakeOrderMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_order_id", nullable = false)
    private Order localOrder;
    
    @Column(name = "pancake_order_id", nullable = false)
    private String pancakeOrderId;
    
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

