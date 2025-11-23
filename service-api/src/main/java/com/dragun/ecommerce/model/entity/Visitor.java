package com.dragun.ecommerce.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "visitors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visitor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(columnDefinition = "TEXT")
    private String referrer;
    
    @Column(name = "page_path", length = 500)
    private String pagePath;
    
    @Column(name = "first_visit_at", nullable = false)
    private LocalDateTime firstVisitAt;
    
    @Column(name = "last_visit_at", nullable = false)
    private LocalDateTime lastVisitAt;
    
    @Column(name = "visit_count", nullable = false)
    @Builder.Default
    private Integer visitCount = 1;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (firstVisitAt == null) {
            firstVisitAt = now;
        }
        if (lastVisitAt == null) {
            lastVisitAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

