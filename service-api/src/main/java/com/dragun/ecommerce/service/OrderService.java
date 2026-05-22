package com.dragun.ecommerce.service;

import com.dragun.ecommerce.exception.BadRequestException;
import com.dragun.ecommerce.exception.ResourceNotFoundException;
import com.dragun.ecommerce.integration.meinvoice.model.MeinvoiceSubmission;
import com.dragun.ecommerce.integration.meinvoice.repository.MeinvoiceSubmissionRepository;
import com.dragun.ecommerce.model.dto.request.CreateOrderRequest;
import com.dragun.ecommerce.model.dto.request.UpdateOrderStatusRequest;
import com.dragun.ecommerce.model.dto.response.OrderResponse;
import com.dragun.ecommerce.model.OrderIntegrationConstants;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.model.enums.OrderStatus;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MeinvoiceSubmissionRepository meinvoiceSubmissionRepository;
    
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
        order.setOrderType(OrderIntegrationConstants.ORDER_TYPE_THI_YEN);
        
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
        return mapToResponse(savedOrder, null);
    }
    
    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return mapToResponse(order, resolveMisaInvoiceRef(order, null));
    }
    
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        Map<String, String> misaRefByOrderId = loadLatestMisaInvoiceRefs(orders);
        return orders.stream()
            .map(order -> mapToResponse(order, resolveSubmissionRefForOrder(order, misaRefByOrderId)))
            .collect(Collectors.toList());
    }
    
    public List<OrderResponse> getOrdersByCustomerPhone(String phone) {
        List<Order> orders = orderRepository.findByCustomerPhone(phone);
        Map<String, String> misaRefByOrderId = loadLatestMisaInvoiceRefs(orders);
        return orders.stream()
            .map(order -> mapToResponse(order, resolveSubmissionRefForOrder(order, misaRefByOrderId)))
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
        return mapToResponse(updatedOrder, resolveMisaInvoiceRef(updatedOrder, null));
    }

    private Map<String, String> loadLatestMisaInvoiceRefs(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Map.of();
        }
        List<String> orderIds = orders.stream().map(Order::getOrderId).distinct().toList();
        List<MeinvoiceSubmission> submissions =
                meinvoiceSubmissionRepository.findByOrderBusinessIdInAndSuccessTrueOrderByCreatedAtDesc(orderIds);
        Map<String, String> refs = new HashMap<>();
        for (MeinvoiceSubmission submission : submissions) {
            refs.putIfAbsent(submission.getOrderBusinessId(), submission.getRefId());
        }
        return refs;
    }

    private String resolveSubmissionRefForOrder(Order order, Map<String, String> misaRefByOrderId) {
        return resolveMisaInvoiceRef(order, misaRefByOrderId != null ? misaRefByOrderId.get(order.getOrderId()) : null);
    }

    private String resolveMisaInvoiceRef(Order order, String submissionRefId) {
        if (order != null && StringUtils.hasText(order.getMeinvoiceRefId())) {
            return order.getMeinvoiceRefId();
        }
        if (StringUtils.hasText(submissionRefId)) {
            return submissionRefId;
        }
        if (order == null) {
            return null;
        }
        return meinvoiceSubmissionRepository
                .findFirstByOrderBusinessIdAndSuccessTrueOrderByCreatedAtDesc(order.getOrderId())
                .map(MeinvoiceSubmission::getRefId)
                .orElse(null);
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
    
    private OrderResponse mapToResponse(Order order, String misaInvoiceRef) {
        String resolvedRef = resolveMisaInvoiceRef(order, misaInvoiceRef);
        boolean draftDeleted = Boolean.TRUE.equals(order.getMeinvoiceDraftDeleted());
        boolean meinvoicePublished = Boolean.TRUE.equals(order.getMeinvoicePublished());
        boolean meinvoiceInvoiced = !draftDeleted
                && !meinvoicePublished
                && (Boolean.TRUE.equals(order.getMeinvoiceInvoiced()) || StringUtils.hasText(resolvedRef));
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
            .pancakeOrderId(order.getPancakeOrderId())
            .meinvoiceInvoiced(meinvoiceInvoiced)
            .meinvoiceDraftDeleted(draftDeleted)
            .meinvoicePublished(meinvoicePublished)
            .meinvoicePublishedAt(order.getMeinvoicePublishedAt())
            .meinvoiceTransactionId(order.getMeinvoiceTransactionId())
            .meinvoiceInvNo(order.getMeinvoiceInvNo())
            .meinvoiceSendTaxStatus(order.getMeinvoiceSendTaxStatus())
            .misaInvoiceRef(resolvedRef)
            .createdAt(order.getCreatedAt())
            .build();
    }
}

