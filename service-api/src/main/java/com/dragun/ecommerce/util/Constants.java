package com.dragun.ecommerce.util;

public class Constants {
    
    // Security Constants
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/dragun/products/**",
        "/api/extend/orders",
        "/api/extend/orders/**",
        "/provinces",
        "/districts/**",
        "/wards/**",
        "/images/**"  // Allow public access to images
    };
    
    public static final String[] ADMIN_ENDPOINTS = {
        "/api/dragun/admin/**"
    };
    
    // Order Status - Deprecated: Use OrderStatus enum instead
    // Kept for backward compatibility
    @Deprecated
    public static final String ORDER_STATUS_PENDING = "pending";
    @Deprecated
    public static final String ORDER_STATUS_CONFIRMED = "confirmed";
    @Deprecated
    public static final String ORDER_STATUS_SHIPPING = "shipping";
    @Deprecated
    public static final String ORDER_STATUS_DELIVERED = "delivered";
    @Deprecated
    public static final String ORDER_STATUS_CANCELLED = "cancelled";
    
    // User Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    
    private Constants() {
        // Utility class
    }
}


