package com.dragun.ecommerce.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
public class HomepageSectionResponse {
    private Long id;
    private String pageKey;
    private String sectionKey;
    private String title;
    private String body;
    private String ctaText;
    private String ctaLink;
    private String imageDesktop;
    private String imageMobile;
    private Boolean useDefaultDesktop;
    private Boolean useDefaultMobile;
    private Map<String, Object> extraJson;
    private Boolean enabled;
    private Integer sortOrder;
    private OffsetDateTime updatedAt;
    private String updatedBy;
}
