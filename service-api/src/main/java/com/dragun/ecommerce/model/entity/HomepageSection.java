package com.dragun.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Entity
@Table(
    name = "homepage_sections",
    uniqueConstraints = @UniqueConstraint(name = "uq_homepage_sections_page_section", columnNames = {"page_key", "section_key"})
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomepageSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Public page the section belongs to: home, story, philosophy (see ContentService.PAGE_SECTIONS). */
    @Column(name = "page_key", nullable = false, length = 32)
    @Builder.Default
    private String pageKey = "home";

    @Column(name = "section_key", nullable = false, length = 64)
    private String sectionKey;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "cta_text", length = 255)
    private String ctaText;

    @Column(name = "cta_link", length = 500)
    private String ctaLink;

    @Column(name = "image_desktop", length = 1000)
    private String imageDesktop;

    @Column(name = "image_mobile", length = 1000)
    private String imageMobile;

    @Column(name = "use_default_desktop", nullable = false)
    @Builder.Default
    private Boolean useDefaultDesktop = true;

    @Column(name = "use_default_mobile", nullable = false)
    @Builder.Default
    private Boolean useDefaultMobile = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> extraJson;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    @PreUpdate
    void touch() {
        // Postgres timestamptz keeps microseconds; truncate so the optimistic-lock value sent to admin equals the stored one
        updatedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        if (enabled == null) {
            enabled = true;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (extraJson == null) {
            extraJson = Map.of();
        }
        if (useDefaultDesktop == null) {
            useDefaultDesktop = true;
        }
        if (useDefaultMobile == null) {
            useDefaultMobile = true;
        }
    }
}
