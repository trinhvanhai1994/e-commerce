package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class BlogPostRequest {
    // Limits mirror the blog_posts columns (V16)
    @NotBlank
    @Size(max = 120, message = "Slug tối đa 120 ký tự")
    private String slug;
    @NotBlank
    private String title;
    @NotBlank
    private String excerpt;
    @Size(max = 1000, message = "Đường dẫn ảnh bìa tối đa 1000 ký tự")
    private String coverImage;
    private List<Map<String, Object>> contentJson;
    @Size(max = 20)
    private String status;
    private Boolean showOnHome;
    private Integer homeOrder;
    /** Required on update (optimistic lock), ignored on create. */
    private OffsetDateTime updatedAt;
}
