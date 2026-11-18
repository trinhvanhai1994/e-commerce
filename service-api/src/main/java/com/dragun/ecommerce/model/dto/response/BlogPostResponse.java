package com.dragun.ecommerce.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class BlogPostResponse {
    private Long id;
    private String slug;
    private String title;
    private String excerpt;
    private String coverImage;
    private List<Map<String, Object>> contentJson;
    private String status;
    private Boolean showOnHome;
    private Integer homeOrder;
    private OffsetDateTime publishedAt;
    private OffsetDateTime updatedAt;
    private String updatedBy;
}
