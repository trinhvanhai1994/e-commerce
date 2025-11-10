package com.dragun.ecommerce.integration.pancake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PancakeOrderDto {
    private String id;
    private String orderNumber;
    private PancakeCustomer customer;
    private List<PancakeOrderItem> items;
    private String status;
    private String paymentMethod;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String createdAt;
    private String updatedAt;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PancakeCustomer {
        private String name;
        private String phone;
        private String address;
        private String province;
        private String district;
        private String ward;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PancakeOrderItem {
        private String productId;
        private String variationId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}

