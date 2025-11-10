package com.dragun.ecommerce.integration.pancake.repository;

import com.dragun.ecommerce.integration.pancake.model.PancakeSyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PancakeSyncLogRepository extends JpaRepository<PancakeSyncLog, Long> {
    
    List<PancakeSyncLog> findByEntityTypeAndEntityId(String entityType, String entityId);
    
    Page<PancakeSyncLog> findByEntityTypeOrderBySyncedAtDesc(String entityType, Pageable pageable);
    
    List<PancakeSyncLog> findBySyncedAtAfter(LocalDateTime after);
    
    long countByStatusAndSyncedAtAfter(String status, LocalDateTime after);
}

