package com.dragun.ecommerce.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private String shortDesc;
    private String description;
    private String mainImage;
    private List<String> gallery;
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
}


