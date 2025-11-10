package com.dragun.ecommerce.integration.pancake.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pancake_sync_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PancakeSyncLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType; // PRODUCT, ORDER, CUSTOMER
    
    @Column(name = "entity_id", nullable = false)
    private String entityId;
    
    @Column(name = "sync_direction", nullable = false)
    private String syncDirection; // TO_PANCAKE, FROM_PANCAKE
    
    @Column(name = "status", nullable = false)
    private String status; // SUCCESS, FAILED, PARTIAL
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (syncedAt == null) {
            syncedAt = LocalDateTime.now();
        }
    }
}

