package com.dragun.ecommerce.util;

import jakarta.servlet.http.HttpServletRequest;

/** Client IP for rate limiting. */
public final class ClientIp {

    private ClientIp() {
    }

    /**
     * X-Real-IP or the LAST X-Forwarded-For entry is what the reverse proxy saw; the first X-Forwarded-For entry
     * is client-supplied and would let anyone bypass a limit. Assumes exactly one reverse proxy in front.
     */
    public static String of(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            return parts[parts.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}
