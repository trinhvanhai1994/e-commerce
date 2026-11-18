package com.dragun.ecommerce.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductReviewPageResponse {
    private List<ProductReviewResponse> items;
    private long totalElements;
    private int page;
    private int size;
    private int totalPages;
    /** Count per status (PENDING/APPROVED/HIDDEN) for the same product filter, for the admin tabs. */
    private Map<String, Long> statusCounts;
}
