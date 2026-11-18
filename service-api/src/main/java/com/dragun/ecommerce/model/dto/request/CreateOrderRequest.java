package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

        /** Optional; blank means none. Receives the order confirmation mail (task-005). */
        @Email(regexp = ".+@.+\\..+", message = "Email không hợp lệ")
        @Size(max = 255, message = "Email tối đa 255 ký tự")
        private String email;

        /** Trimmed before validation, so " a@b.vn " is accepted and "  " means no email. */
        public void setEmail(String email) {
            this.email = email == null || email.isBlank() ? null : email.trim();
        }

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


