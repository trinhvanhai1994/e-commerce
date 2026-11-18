package com.dragun.ecommerce.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class HomepageContentResponse {
    private Map<String, HomepageSectionResponse> sections;
    private List<BlogPostResponse> featuredPosts;
}
