package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.model.dto.request.ProductCategoryRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.ProductCategoryResponse;
import com.dragun.ecommerce.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dragun/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductCategoryController {

    private final ProductCategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductCategoryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.listAdmin()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> create(
        @Valid @RequestBody ProductCategoryRequest request,
        Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success(categoryService.create(request, user), "Đã thêm danh mục"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> update(
        @PathVariable Long id,
        @Valid @RequestBody ProductCategoryRequest request,
        Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success(categoryService.update(id, request, user), "Đã cập nhật danh mục"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xoá danh mục"));
    }
}
