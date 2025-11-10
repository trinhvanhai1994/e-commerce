package com.dragun.ecommerce.integration.pancake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PancakeProductDto {
    private String id;
    private String name;
    private String description;
    private List<String> images;
    private String category;
    private List<PancakeVariation> variations;
    private Boolean active;
    private String createdAt;
    private String updatedAt;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PancakeVariation {
        private String id;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private String sku;
        private String image;
        private Boolean active;
    }
}

