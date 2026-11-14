package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductReviewStatusRequest {
    @NotBlank
    private String status;
}
