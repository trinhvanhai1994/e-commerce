package com.dragun.ecommerce.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalVisitors; // Số người truy cập website (unique customers)
    private Long totalBuyers; // Số người mua hàng (customers with orders)
    private Long totalOrders; // Số đơn hàng
    private BigDecimal totalRevenue; // Tổng giá trị đã order
    private List<ProductSalesStats> productSales; // Số lượng đã bán của từng sản phẩm
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSalesStats {
        private Long productId;
        private String productName;
        private Long totalQuantitySold; // Tổng số lượng đã bán
        private BigDecimal totalRevenue; // Tổng doanh thu từ sản phẩm này
    }
}

