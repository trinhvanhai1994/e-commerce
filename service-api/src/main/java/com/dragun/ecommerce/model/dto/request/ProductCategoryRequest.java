package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProductCategoryRequest {
    // Limits mirror the product_categories columns (V23). The code is generated from the name on create.
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
    private String name;
    @Min(value = 0, message = "Thứ tự phải từ 0 trở lên")
    @Max(value = 9999, message = "Thứ tự tối đa 9999")
    private Integer sortOrder;
    private Boolean visible;
    /** Required on update (optimistic lock), ignored on create. */
    private OffsetDateTime updatedAt;
}
