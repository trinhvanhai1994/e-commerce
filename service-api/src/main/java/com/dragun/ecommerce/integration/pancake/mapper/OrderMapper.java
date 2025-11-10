package com.dragun.ecommerce.integration.pancake.mapper;

import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderDto;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.model.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
    
    /**
     * Map Pancake Order status to Thi Yen OrderStatus enum
     */
    private OrderStatus mapPancakeStatusToThiYen(String pancakeStatus) {
        if (pancakeStatus == null) {
            return OrderStatus.ORDER_STATUS_PENDING;
        }
        
        String upperStatus = pancakeStatus.toUpperCase();
        return switch (upperStatus) {
            case "PENDING" -> OrderStatus.ORDER_STATUS_PENDING;
            case "CONFIRMED" -> OrderStatus.ORDER_STATUS_CONFIRMED;
            case "SHIPPING" -> OrderStatus.ORDER_STATUS_SHIPPING;
            case "DELIVERED" -> OrderStatus.ORDER_STATUS_DELIVERED;
            case "CANCELLED" -> OrderStatus.ORDER_STATUS_CANCELLED;
            default -> OrderStatus.ORDER_STATUS_PENDING;
        };
    }
    
    /**
     * Map Thi Yen OrderStatus to Pancake status string
     */
    private String mapThiYenStatusToPancake(OrderStatus thiYenStatus) {
        if (thiYenStatus == null) {
            return "pending";
        }
        
        return switch (thiYenStatus) {
            case ORDER_STATUS_PENDING -> "pending";
            case ORDER_STATUS_CONFIRMED -> "confirmed";
            case ORDER_STATUS_SHIPPING -> "shipping";
            case ORDER_STATUS_DELIVERED -> "delivered";
            case ORDER_STATUS_CANCELLED -> "cancelled";
        };
    }
    
    /**
     * Map Pancake Order to Thi Yen Order
     * Note: This creates a basic order structure. Product mapping should be handled separately.
     */
    public Order toThiYenOrder(PancakeOrderDto pancakeOrder) {
        if (pancakeOrder == null) {
            return null;
        }
        
        Order order = new Order();
        order.setOrderId(pancakeOrder.getOrderNumber() != null ? pancakeOrder.getOrderNumber() : pancakeOrder.getId());
        order.setPancakeOrderId(pancakeOrder.getId());
        order.setStatus(mapPancakeStatusToThiYen(pancakeOrder.getStatus()));
        order.setPaymentMethod(pancakeOrder.getPaymentMethod());
        order.setSubTotal(pancakeOrder.getSubtotal() != null ? pancakeOrder.getSubtotal() : BigDecimal.ZERO);
        order.setShippingFee(pancakeOrder.getShippingFee() != null ? pancakeOrder.getShippingFee() : BigDecimal.ZERO);
        order.setTotal(pancakeOrder.getTotal() != null ? pancakeOrder.getTotal() : BigDecimal.ZERO);
        order.setOrderType("PANCAKE");
        
        // Map customer info
        if (pancakeOrder.getCustomer() != null) {
            PancakeOrderDto.PancakeCustomer customer = pancakeOrder.getCustomer();
            order.setCustomerName(customer.getName() != null ? customer.getName() : "");
            order.setCustomerPhone(customer.getPhone() != null ? customer.getPhone() : "");
            
            // Build address
            StringBuilder address = new StringBuilder();
            if (customer.getAddress() != null) {
                address.append(customer.getAddress());
            }
            if (customer.getWard() != null) {
                if (address.length() > 0) address.append(", ");
                address.append(customer.getWard());
            }
            if (customer.getDistrict() != null) {
                if (address.length() > 0) address.append(", ");
                address.append(customer.getDistrict());
            }
            if (customer.getProvince() != null) {
                if (address.length() > 0) address.append(", ");
                address.append(customer.getProvince());
            }
            order.setCustomerAddress(address.toString());
            order.setProvinceCode(customer.getProvince());
            order.setDistrictCode(customer.getDistrict());
            order.setWardCode(customer.getWard());
        }
        
        // Parse dates
        if (pancakeOrder.getCreatedAt() != null) {
            try {
                order.setCreatedAt(LocalDateTime.parse(pancakeOrder.getCreatedAt(), DATE_TIME_FORMATTER));
            } catch (Exception e) {
                order.setCreatedAt(LocalDateTime.now());
            }
        } else {
            order.setCreatedAt(LocalDateTime.now());
        }
        
        order.setUpdatedAt(LocalDateTime.now());
        order.setPancakeSyncedAt(LocalDateTime.now());
        
        return order;
    }
    
    /**
     * Map Thi Yen Order to Pancake Order
     */
    public PancakeOrderDto toPancakeOrder(Order thiYenOrder) {
        if (thiYenOrder == null) {
            return null;
        }
        
        PancakeOrderDto pancakeOrder = new PancakeOrderDto();
        pancakeOrder.setId(thiYenOrder.getPancakeOrderId());
        pancakeOrder.setOrderNumber(thiYenOrder.getOrderId());
        pancakeOrder.setStatus(mapThiYenStatusToPancake(thiYenOrder.getStatus()));
        pancakeOrder.setPaymentMethod(thiYenOrder.getPaymentMethod());
        pancakeOrder.setSubtotal(thiYenOrder.getSubTotal());
        pancakeOrder.setShippingFee(thiYenOrder.getShippingFee() != null ? thiYenOrder.getShippingFee() : BigDecimal.ZERO);
        pancakeOrder.setTotal(thiYenOrder.getTotal());
        
        // Map customer
        PancakeOrderDto.PancakeCustomer customer = new PancakeOrderDto.PancakeCustomer();
        customer.setName(thiYenOrder.getCustomerName());
        customer.setPhone(thiYenOrder.getCustomerPhone());
        customer.setAddress(thiYenOrder.getCustomerAddress());
        customer.setProvince(thiYenOrder.getProvinceCode());
        customer.setDistrict(thiYenOrder.getDistrictCode());
        customer.setWard(thiYenOrder.getWardCode());
        pancakeOrder.setCustomer(customer);
        
        // Map order items
        if (thiYenOrder.getItems() != null) {
            List<PancakeOrderDto.PancakeOrderItem> pancakeItems = thiYenOrder.getItems().stream()
                    .map(item -> {
                        PancakeOrderDto.PancakeOrderItem pancakeItem = new PancakeOrderDto.PancakeOrderItem();
                        pancakeItem.setProductId(item.getProduct() != null && item.getProduct().getPancakeProductId() != null 
                                ? item.getProduct().getPancakeProductId() : "");
                        pancakeItem.setProductName(item.getProductName());
                        pancakeItem.setQuantity(item.getQuantity());
                        pancakeItem.setPrice(item.getPrice());
                        return pancakeItem;
                    })
                    .collect(Collectors.toList());
            pancakeOrder.setItems(pancakeItems);
        }
        
        return pancakeOrder;
    }
    
    /**
     * Create OrderItems from Pancake Order items
     * Note: Requires product lookup by pancake_product_id
     */
    public List<OrderItem> createOrderItemsFromPancake(
            Order order, 
            List<PancakeOrderDto.PancakeOrderItem> pancakeItems,
            java.util.function.Function<String, Product> productLookup) {
        
        if (pancakeItems == null || pancakeItems.isEmpty()) {
            return List.of();
        }
        
        return pancakeItems.stream()
                .map(pancakeItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    
                    // Try to find product by pancake_product_id
                    Product product = null;
                    if (pancakeItem.getProductId() != null && productLookup != null) {
                        product = productLookup.apply(pancakeItem.getProductId());
                    }
                    
                    orderItem.setProduct(product);
                    orderItem.setProductName(pancakeItem.getProductName() != null ? pancakeItem.getProductName() : "");
                    orderItem.setQuantity(pancakeItem.getQuantity() != null ? pancakeItem.getQuantity() : 0);
                    orderItem.setPrice(pancakeItem.getPrice() != null ? pancakeItem.getPrice() : BigDecimal.ZERO);
                    
                    return orderItem;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Update Thi Yen Order with Pancake Order data
     */
    public void updateThiYenOrder(Order existingOrder, PancakeOrderDto pancakeOrder) {
        if (pancakeOrder == null || existingOrder == null) {
            return;
        }
        
        existingOrder.setStatus(mapPancakeStatusToThiYen(pancakeOrder.getStatus()));
        existingOrder.setPaymentMethod(pancakeOrder.getPaymentMethod());
        existingOrder.setSubTotal(pancakeOrder.getSubtotal() != null ? pancakeOrder.getSubtotal() : BigDecimal.ZERO);
        existingOrder.setShippingFee(pancakeOrder.getShippingFee() != null ? pancakeOrder.getShippingFee() : BigDecimal.ZERO);
        existingOrder.setTotal(pancakeOrder.getTotal() != null ? pancakeOrder.getTotal() : BigDecimal.ZERO);
        
        // Update customer info
        if (pancakeOrder.getCustomer() != null) {
            PancakeOrderDto.PancakeCustomer customer = pancakeOrder.getCustomer();
            existingOrder.setCustomerName(customer.getName() != null ? customer.getName() : existingOrder.getCustomerName());
            existingOrder.setCustomerPhone(customer.getPhone() != null ? customer.getPhone() : existingOrder.getCustomerPhone());
            
            // Build address
            StringBuilder address = new StringBuilder();
            if (customer.getAddress() != null) {
                address.append(customer.getAddress());
            }
            if (customer.getWard() != null) {
                if (address.length() > 0) address.append(", ");
                address.append(customer.getWard());
            }
            if (customer.getDistrict() != null) {
                if (address.length() > 0) address.append(", ");
                address.append(customer.getDistrict());
            }
            if (customer.getProvince() != null) {
                if (address.length() > 0) address.append(", ");
                address.append(customer.getProvince());
            }
            if (address.length() > 0) {
                existingOrder.setCustomerAddress(address.toString());
            }
            existingOrder.setProvinceCode(customer.getProvince());
            existingOrder.setDistrictCode(customer.getDistrict());
            existingOrder.setWardCode(customer.getWard());
        }
        
        existingOrder.setUpdatedAt(LocalDateTime.now());
        existingOrder.setPancakeSyncedAt(LocalDateTime.now());
    }
}

