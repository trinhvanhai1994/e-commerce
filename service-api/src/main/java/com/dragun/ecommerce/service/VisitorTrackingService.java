package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.entity.Visitor;
import com.dragun.ecommerce.repository.VisitorRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
            // Create new visitor
            Visitor visitor = Visitor.builder()
                .sessionId(sessionId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .referrer(referrer)
                .pagePath(pagePath)
                .firstVisitAt(LocalDateTime.now())
                .lastVisitAt(LocalDateTime.now())
                .visitCount(1)
                .build();
            
            return visitorRepository.save(visitor);
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

