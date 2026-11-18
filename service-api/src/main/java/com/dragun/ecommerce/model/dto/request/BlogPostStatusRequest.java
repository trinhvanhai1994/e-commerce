package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogPostStatusRequest {
    @NotBlank
    private String status;
}
