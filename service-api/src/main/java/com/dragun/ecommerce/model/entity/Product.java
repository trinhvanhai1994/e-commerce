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
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
    
    @Column(name = "old_price", precision = 19, scale = 2)
    private BigDecimal oldPrice;
    
    @Column(name = "short_desc", columnDefinition = "TEXT")
    private String shortDesc;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "main_image")
    private String mainImage;
    
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> gallery;
    
    @Column(nullable = false)
    private Integer stock;
    
    private String category;
    
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
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


