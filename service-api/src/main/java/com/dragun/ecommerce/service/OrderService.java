package com.dragun.ecommerce.service;

import com.dragun.ecommerce.exception.BadRequestException;
import com.dragun.ecommerce.exception.ResourceNotFoundException;
import com.dragun.ecommerce.model.dto.request.CreateOrderRequest;
import com.dragun.ecommerce.model.dto.request.UpdateOrderStatusRequest;
import com.dragun.ecommerce.model.dto.response.OrderResponse;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.model.enums.OrderStatus;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        
        // Generate orderId with format: yyyymmdd + 5 digits incrementing
        String orderId = generateOrderId();
        order.setOrderId(orderId);
        
        order.setCustomerName(request.getCustomerInfo().getName());
        order.setCustomerPhone(request.getCustomerInfo().getPhone());
        order.setCustomerAddress(request.getCustomerInfo().getAddress());
        order.setProvinceCode(request.getCustomerInfo().getProvince());
        order.setDistrictCode(request.getCustomerInfo().getDistrict());
        order.setWardCode(request.getCustomerInfo().getWard());
        order.setStatus(OrderStatus.ORDER_STATUS_PENDING);
        order.setPaymentMethod("COD");
        order.setOrderType("THI_YEN");
        
        BigDecimal[] subTotal = {BigDecimal.ZERO};
        List<OrderItem> items = request.getItems().stream()
            .map(itemRequest -> {
                Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
                
                if (product.getStock() < itemRequest.getQuantity()) {
                    throw new BadRequestException("Insufficient stock for product: " + product.getName());
                }
                
                BigDecimal itemPrice = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
                subTotal[0] = subTotal[0].add(itemPrice);
                
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(product.getPrice());
                orderItem.setProductName(product.getName());
                
                // Update stock
                product.setStock(product.getStock() - itemRequest.getQuantity());
                productRepository.save(product);
                
                return orderItem;
            })
            .collect(Collectors.toList());
        
        // Calculate totals
        BigDecimal shippingFee = BigDecimal.valueOf(20000); // Default shipping fee
        BigDecimal total = subTotal[0].add(shippingFee);
        
        order.setSubTotal(subTotal[0]);
        order.setShippingFee(shippingFee);
        order.setTotal(total);
        order.setItems(items);
        
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }
    
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return mapToResponse(order);
    }
    
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    public List<OrderResponse> getOrdersByCustomerPhone(String phone) {
        return orderRepository.findByCustomerPhone(phone).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        
        // Convert string to enum and validate
        OrderStatus newStatus = OrderStatus.fromValue(request.getStatus());
        if (newStatus == null) {
            throw new BadRequestException("Invalid order status: " + request.getStatus());
        }
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }
    
    /**
     * Generate orderId with format: yyyymmdd + 5 digits incrementing
     * Example: 20241109 + 00001 = 2024110900001
     */
    private String generateOrderId() {
        LocalDate today = LocalDate.now();
        String datePrefix = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // Count orders created today
        long orderCountToday = orderRepository.countByCreatedAtDate(today);
        
        // Generate 5-digit sequence number (00001, 00002, ...)
        String sequence = String.format("%05d", orderCountToday + 1);
        
        return datePrefix + sequence;
    }
    
    private OrderResponse mapToResponse(Order order) {
        OrderResponse.CustomerInfo customerInfo = OrderResponse.CustomerInfo.builder()
            .name(order.getCustomerName())
            .phone(order.getCustomerPhone())
            .address(order.getCustomerAddress())
            .province(order.getProvinceCode())
            .district(order.getDistrictCode())
            .ward(order.getWardCode())
            .build();
        
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
            .map(item -> OrderResponse.OrderItemResponse.builder()
                .id(item.getId())
                .name(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build())
            .collect(Collectors.toList());
        
        return OrderResponse.builder()
            .id(order.getOrderId()) // Keep for backward compatibility
            .orderId(order.getOrderId()) // New explicit field
            .customerInfo(customerInfo)
            .items(items)
            .subTotal(order.getSubTotal())
            .shippingFee(order.getShippingFee())
            .total(order.getTotal())
            .status(order.getStatus() != null ? order.getStatus().getValue() : null)
            .paymentMethod(order.getPaymentMethod())
            .orderType(order.getOrderType())
            .createdAt(order.getCreatedAt())
            .build();
    }
}

