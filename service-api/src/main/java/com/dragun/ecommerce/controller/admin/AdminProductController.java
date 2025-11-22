package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.model.dto.request.ProductRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.ProductResponse;
import com.dragun.ecommerce.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dragun/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    
    private final AdminService adminService;
    private final com.dragun.ecommerce.service.ProductService productService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = adminService.saveProduct(request);
        String message = "Tạo sản phẩm thành công";
        if (Boolean.TRUE.equals(request.getSyncToPancake())) {
            message += " và đã đồng bộ lên Pancake POS";
        }
        return ResponseEntity.ok(ApiResponse.success(product, message));
    }
    
    @PutMapping
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse product = adminService.saveProduct(request);
        String message = "Cập nhật sản phẩm thành công";
        if (Boolean.TRUE.equals(request.getSyncToPancake())) {
            message += " và đã đồng bộ lên Pancake POS";
        }
        return ResponseEntity.ok(ApiResponse.success(product, message));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa sản phẩm thành công"));
    }
    
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<ProductResponse>> toggleProductStatus(@PathVariable Long id) {
        ProductResponse product = adminService.toggleProductStatus(id);
        String status = "ACTIVE".equals(product.getStatus()) ? "kích hoạt" : "ẩn";
        return ResponseEntity.ok(ApiResponse.success(product, "Đã " + status + " sản phẩm thành công"));
    }
}


