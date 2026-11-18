package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.model.dto.request.ProductReviewRequest;
import com.dragun.ecommerce.model.dto.request.ProductReviewStatusRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewPageResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewResponse;
import com.dragun.ecommerce.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dragun/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProductReviewPageResponse>> list(
        @RequestParam(required = false) Long productId,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.listAdmin(productId, status, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductReviewResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getAdmin(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductReviewResponse>> create(
        @Valid @RequestBody ProductReviewRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
            reviewService.create(request, user(authentication)), "Đã thêm đánh giá"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductReviewResponse>> update(
        @PathVariable Long id, @Valid @RequestBody ProductReviewRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
            reviewService.update(id, request, user(authentication)), "Đã lưu đánh giá"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductReviewResponse>> status(
        @PathVariable Long id, @Valid @RequestBody ProductReviewStatusRequest request, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
            reviewService.updateStatus(id, request.getStatus(), user(authentication)), "Đã cập nhật trạng thái"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id, Authentication authentication) {
        reviewService.delete(id, user(authentication));
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xoá đánh giá"));
    }

    private static String user(Authentication authentication) {
        return authentication != null ? authentication.getName() : "admin";
    }
}
