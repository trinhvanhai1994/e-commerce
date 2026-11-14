package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/** Admin create/update of a product review. Limits mirror product_reviews (V21). */
@Data
public class ProductReviewRequest {
    /** Required on create; ignored on update (a review never moves to another product). */
    private Long productId;

    @NotBlank(message = "Tên người đánh giá không được để trống")
    @Size(max = 100, message = "Tên người đánh giá tối đa 100 ký tự")
    private String authorName;

    @NotNull(message = "Số sao không được để trống")
    @Min(value = 1, message = "Số sao từ 1 đến 5")
    @Max(value = 5, message = "Số sao từ 1 đến 5")
    private Integer rating;

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(max = 2000, message = "Nội dung tối đa 2000 ký tự")
    private String content;

    @Size(max = 4, message = "Tối đa 4 ảnh")
    private List<String> images;

    /** PENDING / APPROVED / HIDDEN; defaults to APPROVED on admin create, unchanged on update when null. */
    private String status;

    /** Required on update (optimistic lock), ignored on create. */
    private OffsetDateTime updatedAt;
}
