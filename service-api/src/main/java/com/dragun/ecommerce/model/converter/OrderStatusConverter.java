package com.dragun.ecommerce.model.converter;

import com.dragun.ecommerce.model.enums.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for OrderStatus enum
 * Converts between String value (e.g., "PENDING") and OrderStatus enum (e.g., ORDER_STATUS_PENDING)
 */
@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    
    @Override
    public String convertToDatabaseColumn(OrderStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }
    
    @Override
    public OrderStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return OrderStatus.fromValue(dbData);
    }
}

