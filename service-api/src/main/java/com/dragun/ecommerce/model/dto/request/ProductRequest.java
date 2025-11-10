package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private Long id;
    
    @NotBlank(message = "Product name is required")
    private String name;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    private BigDecimal oldPrice;
    private String shortDesc;
    private String description;
    private String mainImage;
    private List<String> gallery;
    
    @NotNull(message = "Stock is required")
    @Positive(message = "Stock must be positive")
    private Integer stock;
    
    private String category;
    private String quantity;
    private String benefits;
    private String ingredients;
    private String specifications;
    private String technology;
    private String storage;
    private Integer discount;
    private Double rating;
    private Integer reviewCount;
    
    // Flag to sync product to Pancake POS
    private Boolean syncToPancake = false;
}


