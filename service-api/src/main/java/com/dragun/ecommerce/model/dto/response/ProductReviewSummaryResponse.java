package com.dragun.ecommerce.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Public reviews of one product: approved rows plus the rating summary computed from them. */
@Data
@Builder
public class ProductReviewSummaryResponse {
    private Long productId;
    /** Average of approved ratings rounded to 1 decimal; 0 when there is none. */
    private double averageRating;
    private long reviewCount;
    private List<ProductReviewResponse> reviews;
}
