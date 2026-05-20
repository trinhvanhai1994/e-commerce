package com.dragun.ecommerce.integration.meinvoice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meinvoice_submissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeinvoiceSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_id", nullable = false, unique = true, length = 64)
    private String refId;

    @Column(name = "order_business_id", nullable = false, length = 128)
    private String orderBusinessId;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "last_error_code", length = 256)
    private String lastErrorCode;

    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
