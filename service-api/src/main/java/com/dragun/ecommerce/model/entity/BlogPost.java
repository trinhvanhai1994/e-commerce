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
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "blog_posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique among non-deleted rows only (partial index uq_blog_posts_slug_active, V20). No unique=true here:
    // ddl-auto=update (dev) would recreate a table-wide constraint.
    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String excerpt;

    @Column(name = "cover_image", length = 1000)
    private String coverImage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content_json", nullable = false, columnDefinition = "jsonb")
    private List<Map<String, Object>> contentJson;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "show_on_home", nullable = false)
    private Boolean showOnHome;

    @Column(name = "home_order")
    private Integer homeOrder;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    void onCreate() {
        // Postgres timestamptz keeps microseconds; truncate so the value returned to clients equals the stored one
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = "DRAFT";
        }
        if (showOnHome == null) {
            showOnHome = false;
        }
        if (contentJson == null) {
            contentJson = List.of();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
