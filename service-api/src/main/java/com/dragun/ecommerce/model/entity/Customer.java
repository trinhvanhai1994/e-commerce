package com.dragun.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

/** Storefront customer account (V22). Admins live in {@link User}; the two never share a table or a token. */
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";
    public static final String STATUS_DELETED = "DELETED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Normalized phone (0 + 9 digits), also the login name. */
    @Column(nullable = false, unique = true, length = 10)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 10)
    private String gender;

    @Column(name = "address_recipient", length = 100)
    private String addressRecipient;

    @Column(name = "address_phone", length = 10)
    private String addressPhone;

    @Column(name = "address_line", columnDefinition = "TEXT")
    private String addressLine;

    @Column(name = "province_code", length = 20)
    private String provinceCode;

    @Column(name = "district_code", length = 20)
    private String districtCode;

    @Column(name = "ward_code", length = 20)
    private String wardCode;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "phone_verified_at")
    private OffsetDateTime phoneVerifiedAt;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;

    @Column(name = "token_version", nullable = false)
    private int tokenVersion;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "consent_at", nullable = false)
    private OffsetDateTime consentAt;

    @Column(name = "consent_version", nullable = false, length = 20)
    private String consentVersion;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        // Postgres timestamptz keeps microseconds; truncate so the value returned to clients equals the stored one
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = STATUS_ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
