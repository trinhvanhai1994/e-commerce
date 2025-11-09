package com.dragun.ecommerce.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private CustomerInfo customerInfo;
    private List<OrderItemResponse> items;
    private BigDecimal subTotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String status;
    private String paymentMethod;
    private String orderType;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        private String name;
        private String phone;
        private String address;
        private String province;
        private String district;
        private String ward;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer quantity;
    }
}


