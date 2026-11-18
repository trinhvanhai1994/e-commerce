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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_HIDDEN = "HIDDEN";

    public static final String SOURCE_CUSTOMER = "CUSTOMER";
    public static final String SOURCE_ADMIN = "ADMIN";
    public static final String SOURCE_SEED = "SEED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "author_name", nullable = false, length = 100)
    private String authorName;

    @Column(nullable = false)
    private Short rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> images;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 20)
    private String source;

    /** Author account (V22); null for guest, seed and admin reviews. One review per product per account. */
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    void onCreate() {
        // Postgres timestamptz keeps microseconds; truncate so the value returned to clients equals the stored one
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = STATUS_PENDING;
        }
        if (source == null) {
            source = SOURCE_CUSTOMER;
        }
        if (images == null) {
            images = new ArrayList<>();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
