package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    
    Optional<Visitor> findBySessionId(String sessionId);
    
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM Visitor v")
    long countUniqueVisitors();
    
    @Query("SELECT COUNT(DISTINCT v.sessionId) FROM Visitor v WHERE v.firstVisitAt >= :startDate")
    long countUniqueVisitorsSince(LocalDateTime startDate);
    
    @Query("SELECT COUNT(v) FROM Visitor v WHERE v.lastVisitAt >= :startDate")
    long countVisitsSince(LocalDateTime startDate);
}

