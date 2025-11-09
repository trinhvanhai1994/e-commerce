package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @Valid
    @NotNull(message = "Customer info is required")
    private CustomerInfo customerInfo;
    
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> items;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        @NotBlank(message = "Customer name is required")
        private String name;
        
        @NotBlank(message = "Customer phone is required")
        private String phone;
        
        @NotBlank(message = "Address is required")
        private String address;
        
        private String province;
        private String district;
        private String ward;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}


