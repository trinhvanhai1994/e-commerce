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
    private String id; // Keep for backward compatibility, maps to orderId
    private String orderId; // New field for explicit orderId
    private CustomerInfo customerInfo;
    private List<OrderItemResponse> items;
    private BigDecimal subTotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String status; // Serialized as enum value (e.g., "pending", "confirmed")
    private String paymentMethod;
    private String orderType;
    private String pancakeOrderId;
    private Boolean meinvoiceInvoiced;
    /** Latest successful {@code meinvoice_submissions.ref_id} for this order. */
    private String misaInvoiceRef;
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


