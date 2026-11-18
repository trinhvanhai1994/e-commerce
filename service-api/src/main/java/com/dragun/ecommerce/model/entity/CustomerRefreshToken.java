package com.dragun.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/** Long-lived session of a customer. Only the SHA-256 hash of the token is stored. */
@Entity
@Table(name = "customer_refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        }
    }

    public boolean isActive(OffsetDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
