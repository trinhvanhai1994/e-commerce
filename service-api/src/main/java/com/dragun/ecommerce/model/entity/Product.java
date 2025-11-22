package com.dragun.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000)
    private String name;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
    
    @Column(name = "old_price", precision = 19, scale = 2)
    private BigDecimal oldPrice;
    
    @Column(name = "short_desc", columnDefinition = "TEXT")
    private String shortDesc;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "main_image", columnDefinition = "TEXT")
    private String mainImage;
    
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> gallery;
    
    @Column(nullable = false)
    private Integer stock;
    
    @Column(length = 255)
    private String category;
    
    @Column(length = 255)
    private String quantity;
    
    @Column(columnDefinition = "TEXT")
    private String benefits;
    
    @Column(columnDefinition = "TEXT")
    private String ingredients;
    
    @Column(columnDefinition = "TEXT")
    private String specifications;
    
    @Column(columnDefinition = "TEXT")
    private String technology;
    
    @Column(columnDefinition = "TEXT")
    private String storage;
    
    private Integer discount;
    
    private Double rating;
    
    @Column(name = "review_count")
    private Integer reviewCount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "pancake_product_id")
    private String pancakeProductId;
    
    @Column(name = "pancake_synced_at")
    private LocalDateTime pancakeSyncedAt;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
    
    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "ACTIVE";
    
    @Column(nullable = false)
    @Builder.Default
    private Integer priority = 999;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (deleted == null) {
            deleted = false;
        }
        if (status == null || status.isEmpty()) {
            status = "ACTIVE";
        }
        if (priority == null) {
            priority = 999;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


