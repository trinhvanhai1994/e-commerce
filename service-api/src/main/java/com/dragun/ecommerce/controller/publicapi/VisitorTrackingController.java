package com.dragun.ecommerce.controller.publicapi;

import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.service.VisitorTrackingService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/visitors")
@RequiredArgsConstructor
@Slf4j
public class VisitorTrackingController {
    
    private final VisitorTrackingService visitorTrackingService;
    
    private static final String VISITOR_SESSION_COOKIE = "visitor_session_id";
    private static final int COOKIE_MAX_AGE = 365 * 24 * 60 * 60; // 1 year in seconds
    
    @PostMapping("/track")
    public ResponseEntity<ApiResponse<String>> trackVisit(
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // Try to get sessionId from cookie first
        String actualSessionId = getSessionIdFromCookie(request);
        
        // If not in cookie, use provided sessionId or generate new one
        if (actualSessionId == null || actualSessionId.isEmpty()) {
            if (sessionId != null && !sessionId.isEmpty()) {
                actualSessionId = sessionId;
            } else {
                actualSessionId = java.util.UUID.randomUUID().toString();
            }
        }
        
        // Track the visit
        visitorTrackingService.trackVisit(actualSessionId, request);
        
        // Set cookie for future visits
        Cookie cookie = new Cookie(VISITOR_SESSION_COOKIE, actualSessionId);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setPath("/");
        cookie.setHttpOnly(false); // Allow JavaScript to read it
        cookie.setSecure(false); // Set to true in production with HTTPS
        response.addCookie(cookie);
        
        return ResponseEntity.ok(ApiResponse.success(actualSessionId, "Visit tracked successfully"));
    }
    
    private String getSessionIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (VISITOR_SESSION_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

