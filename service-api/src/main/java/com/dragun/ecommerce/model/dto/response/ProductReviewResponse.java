package com.dragun.ecommerce.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
// Public responses leave the admin-only fields null; omit them instead of sending empty keys
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductReviewResponse {
    private Long id;
    private Long productId;
    /** Admin responses only. */
    private String productName;
    private String authorName;
    private Integer rating;
    private String content;
    private List<String> images;
    /** Admin responses only (public list returns APPROVED rows). */
    private String status;
    private String source;
    private OffsetDateTime createdAt;
    private OffsetDateTime approvedAt;
    private OffsetDateTime updatedAt;
    private String updatedBy;
}
