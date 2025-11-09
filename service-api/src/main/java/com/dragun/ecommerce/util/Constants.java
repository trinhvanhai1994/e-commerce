package com.dragun.ecommerce.util;

public class Constants {
    
    // API Paths
    public static final String API_PUBLIC_PREFIX = "/api/thiyen";
    public static final String API_ADMIN_PREFIX = "/api/thiyen/admin";
    public static final String API_EXTEND_PREFIX = "/api/extend";
    
    // JWT Constants
    public static final String JWT_SECRET_PROPERTY = "${jwt.secret:ThiYenEcommerceSecretKey2024ForJWTTokenGeneration}";
    public static final long JWT_EXPIRATION = 86400000; // 24 hours in milliseconds
    
    // Security Constants
    public static final String[] PUBLIC_ENDPOINTS = {
        "/api/thiyen/products/**",
        "/api/extend/orders",
        "/api/extend/orders/customer/**",
        "/provinces",
        "/districts/**",
        "/wards/**"
    };
    
    public static final String[] ADMIN_ENDPOINTS = {
        "/api/thiyen/admin/**"
    };
    
    // Order Status
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_CONFIRMED = "confirmed";
    public static final String ORDER_STATUS_SHIPPING = "shipping";
    public static final String ORDER_STATUS_DELIVERED = "delivered";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";
    
    // User Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    
    private Constants() {
        // Utility class
    }
}


