package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
public class HomepageSectionRequest {
    private String title;
    private String body;
    // Limits mirror the homepage_sections columns (V16) so oversized input is a 400, not a DB error
    @Size(max = 255, message = "Chữ nút CTA tối đa 255 ký tự")
    private String ctaText;
    @Size(max = 500, message = "Link CTA tối đa 500 ký tự")
    private String ctaLink;
    @Size(max = 1000, message = "Đường dẫn ảnh desktop tối đa 1000 ký tự")
    private String imageDesktop;
    @Size(max = 1000, message = "Đường dẫn ảnh mobile tối đa 1000 ký tự")
    private String imageMobile;
    private Boolean useDefaultDesktop;
    private Boolean useDefaultMobile;
    private Map<String, Object> extraJson;
    private Boolean enabled;
    private Integer sortOrder;

    @NotNull
    private OffsetDateTime updatedAt;
}
