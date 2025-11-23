package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    
    /**
     * Find visitor by session ID.
     * Returns the most recent one if multiple exist (should not happen with unique constraint).
     * This query uses LIMIT 1 to ensure only one result is returned.
     */
    @Query(value = "SELECT * FROM visitors WHERE session_id = :sessionId ORDER BY last_visit_at DESC, id DESC LIMIT 1", nativeQuery = true)
    Optional<Visitor> findBySessionId(String sessionId);
    
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM Visitor v")
    long countUniqueVisitors();
    
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM Visitor v WHERE v.firstVisitAt >= :startDate")
    long countUniqueVisitorsSince(LocalDateTime startDate);
    
    @Query("SELECT COUNT(v) FROM Visitor v WHERE v.lastVisitAt >= :startDate")
    long countVisitsSince(LocalDateTime startDate);
}

