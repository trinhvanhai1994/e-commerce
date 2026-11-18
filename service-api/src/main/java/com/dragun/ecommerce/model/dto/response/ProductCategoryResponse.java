package com.dragun.ecommerce.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductCategoryResponse {
    private Long id;
    private String code;
    private String name;
    private Integer sortOrder;
    private Boolean visible;
    /** Admin list only: number of non-deleted products using this code. */
    private Long productCount;
    private OffsetDateTime updatedAt;
}
