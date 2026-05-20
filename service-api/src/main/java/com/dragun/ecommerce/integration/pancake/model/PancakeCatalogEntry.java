package com.dragun.ecommerce.integration.pancake.model;

import com.dragun.ecommerce.model.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pancake_catalog_entry")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PancakeCatalogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false, length = 50)
    private String shopId;

    @Column(name = "pancake_product_id", nullable = false, length = 100)
    private String pancakeProductId;

    @Column(name = "pancake_variation_id", nullable = false, length = 100)
    @Builder.Default
    private String pancakeVariationId = "";

    @Column(length = 500)
    private String name;

    @Column(length = 200)
    private String sku;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @Column(length = 200)
    private String category;

    @Column
    private Boolean active;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_product_id")
    private Product localProduct;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (fetchedAt == null) {
            fetchedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
