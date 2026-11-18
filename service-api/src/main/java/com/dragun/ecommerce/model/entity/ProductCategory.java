package com.dragun.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/** Admin-managed product category (V23). Products reference it by {@code code}, which never changes. */
@Entity
@Table(name = "product_categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    // Unique case-insensitively (uq_product_categories_name_ci, V23); checked in ProductCategoryService.
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean visible;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    void onCreate() {
        // Postgres timestamptz keeps microseconds; truncate so the value returned to clients equals the stored one
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        createdAt = now;
        updatedAt = now;
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (visible == null) {
            visible = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
