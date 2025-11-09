package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.dto.request.CreateOrderRequest;
import com.dragun.ecommerce.model.dto.request.UpdateOrderStatusRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.OrderResponse;
import com.dragun.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/extend/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("orderId", order.getId(), "message", "Đơn hàng đã được tạo thành công")
        ));
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable String orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @GetMapping("/customer/{phone}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByCustomer(@PathVariable String phone) {
        List<OrderResponse> orders = orderService.getOrdersByCustomerPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse order = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(order, 
            "Cập nhật trạng thái đơn hàng " + orderId + " thành " + request.getStatus() + " thành công"));
    }
}
