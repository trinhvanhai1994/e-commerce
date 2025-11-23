package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.entity.Visitor;
import com.dragun.ecommerce.repository.VisitorRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorTrackingService {
    
    private final VisitorRepository visitorRepository;
    
    /**
     * Track a visitor visit
     * Handles race conditions by catching duplicate key exceptions and retrying with update
     * @param sessionId Session ID (from cookie or generated)
     * @param request HTTP request to extract IP, user agent, etc.
     * @return Visitor entity
     */
    @Transactional
    public Visitor trackVisit(String sessionId, HttpServletRequest request) {
        // If no sessionId provided, generate one
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        // Extract information from request
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");
        String pagePath = request.getRequestURI();
        
        // Check if visitor with this sessionId already exists
        // Note: With unique constraint, there should be at most one record
        Optional<Visitor> existingVisitor = visitorRepository.findBySessionId(sessionId);
        
        if (existingVisitor.isPresent()) {
            // Update existing visitor
            Visitor visitor = existingVisitor.get();
            visitor.setLastVisitAt(LocalDateTime.now());
            visitor.setVisitCount(visitor.getVisitCount() + 1);
            visitor.setIpAddress(ipAddress);
            visitor.setUserAgent(userAgent);
            visitor.setReferrer(referrer);
            visitor.setPagePath(pagePath);
            visitor.setUpdatedAt(LocalDateTime.now());
            
            return visitorRepository.save(visitor);
        } else {
            // Try to create new visitor
            // If duplicate key exception occurs (race condition), retry with update
            try {
                Visitor newVisitor = Visitor.builder()
                    .sessionId(sessionId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .pagePath(pagePath)
                    .firstVisitAt(LocalDateTime.now())
                    .lastVisitAt(LocalDateTime.now())
                    .visitCount(1)
                    .build();
                
                if (newVisitor != null) {
                    return visitorRepository.save(newVisitor);
                } else {
                    throw new IllegalStateException("Failed to create visitor entity");
                }
            } catch (DataIntegrityViolationException e) {
                // Handle race condition: another request inserted the same session_id
                // Retry in a new transaction to avoid Hibernate session issues
                log.warn("Duplicate session_id detected (race condition): {}. Retrying with update in new transaction.", sessionId);
                
                // Retry in a new transaction to clear the dirty session
                return retryUpdateVisitor(sessionId, ipAddress, userAgent, referrer, pagePath);
            }
        }
    }
    
    /**
     * Retry updating visitor in a new transaction
     * This is called when a duplicate key exception occurs (race condition)
     * Using REQUIRES_NEW to create a fresh transaction and clear the dirty session
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private Visitor retryUpdateVisitor(String sessionId, String ipAddress, String userAgent, 
                                       String referrer, String pagePath) {
        // Find the visitor that was just created by another thread
        Optional<Visitor> retryVisitor = visitorRepository.findBySessionId(sessionId);
        if (retryVisitor.isPresent()) {
            Visitor visitor = retryVisitor.get();
            visitor.setLastVisitAt(LocalDateTime.now());
            visitor.setVisitCount(visitor.getVisitCount() + 1);
            visitor.setIpAddress(ipAddress);
            visitor.setUserAgent(userAgent);
            visitor.setReferrer(referrer);
            visitor.setPagePath(pagePath);
            visitor.setUpdatedAt(LocalDateTime.now());
            
            return visitorRepository.save(visitor);
        } else {
            // This should not happen, but if it does, log and throw
            log.error("Failed to find visitor after duplicate key exception for session_id: {}", sessionId);
            throw new IllegalStateException("Visitor with session_id " + sessionId + " not found after duplicate key exception");
        }
    }
    
    /**
     * Get total unique visitors count
     */
    @Transactional(readOnly = true)
    public long getTotalUniqueVisitors() {
        return visitorRepository.countUniqueVisitors();
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Handle multiple IPs (X-Forwarded-For can contain multiple IPs)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}

