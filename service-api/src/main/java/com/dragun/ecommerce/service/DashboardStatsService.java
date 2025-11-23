package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.dto.response.DashboardStatsResponse;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.repository.OrderItemRepository;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.repository.VisitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardStatsService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VisitorRepository visitorRepository;
    
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        // Số đơn hàng
        long totalOrders = orderRepository.count();
        
        // Tổng giá trị đã order
        BigDecimal totalRevenue = orderRepository.findAll().stream()
            .map(order -> order.getTotal() != null ? order.getTotal() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Số người truy cập website (unique visitors từ bảng visitors)
        long totalVisitors = visitorRepository.countUniqueVisitors();
        
        // Số người mua hàng (unique customers - unique phone numbers từ orders)
        long totalBuyers = orderRepository.findAll().stream()
            .map(order -> order.getCustomerPhone() != null ? order.getCustomerPhone().trim() : "")
            .filter(phone -> !phone.isEmpty())
            .distinct()
            .count();
        
        // Số lượng đã bán của từng sản phẩm
        // Group by productId (using productName as key since product might be lazy loaded)
        List<DashboardStatsResponse.ProductSalesStats> productSales = orderItemRepository.findAll().stream()
            .filter(item -> item.getProductName() != null && !item.getProductName().isEmpty())
            .collect(Collectors.groupingBy(
                item -> {
                    // Use productId if available, otherwise use productName as key
                    if (item.getProduct() != null && item.getProduct().getId() != null) {
                        return item.getProduct().getId();
                    }
                    // Fallback: use productName hash as key (not ideal but works)
                    return item.getProductName().hashCode();
                },
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    items -> {
                        if (items.isEmpty()) {
                            return null;
                        }
                        OrderItem firstItem = items.get(0);
                        
                        long totalQuantity = items.stream()
                            .mapToLong(item -> item.getQuantity() != null ? item.getQuantity() : 0L)
                            .sum();
                        BigDecimal revenue = items.stream()
                            .map(item -> {
                                BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                                Integer qty = item.getQuantity() != null ? item.getQuantity() : 0;
                                return price.multiply(BigDecimal.valueOf(qty));
                            })
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        Long productId = null;
                        if (firstItem.getProduct() != null && firstItem.getProduct().getId() != null) {
                            productId = firstItem.getProduct().getId();
                        }
                        
                        String productName = firstItem.getProductName();
                        if (productName == null && firstItem.getProduct() != null) {
                            productName = firstItem.getProduct().getName();
                        }
                        
                        return DashboardStatsResponse.ProductSalesStats.builder()
                            .productId(productId)
                            .productName(productName != null ? productName : "Sản phẩm không xác định")
                            .totalQuantitySold(totalQuantity)
                            .totalRevenue(revenue)
                            .build();
                    }
                )
            ))
            .values()
            .stream()
            .filter(stats -> stats != null)
            .sorted((a, b) -> Long.compare(b.getTotalQuantitySold(), a.getTotalQuantitySold())) // Sort by quantity sold descending
            .collect(Collectors.toList());
        
        return DashboardStatsResponse.builder()
            .totalVisitors(totalVisitors)
            .totalBuyers(totalBuyers)
            .totalOrders(totalOrders)
            .totalRevenue(totalRevenue)
            .productSales(productSales)
            .build();
    }
}

