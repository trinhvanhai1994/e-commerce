package com.dragun.ecommerce.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Order Status Enum
 * Represents the possible statuses of an order
 */
public enum OrderStatus {
    ORDER_STATUS_PENDING("PENDING"),
    ORDER_STATUS_CONFIRMED("CONFIRMED"),
    ORDER_STATUS_SHIPPING("SHIPPING"),
    ORDER_STATUS_DELIVERED("DELIVERED"),
    ORDER_STATUS_CANCELLED("CANCELLED");
    
    private final String value;
    
    OrderStatus(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    /**
     * Convert string value to OrderStatus enum
     * @param value String value (e.g., "PENDING", "CONFIRMED" or "pending", "confirmed" for backward compatibility)
     * @return OrderStatus enum or null if not found
     */
    public static OrderStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        // Try exact match first (uppercase)
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        // Fallback: case-insensitive match for backward compatibility
        String upperValue = value.toUpperCase();
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equals(upperValue)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * Check if a string value is a valid order status
     * @param value String value to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
        return fromValue(value) != null;
    }
}

