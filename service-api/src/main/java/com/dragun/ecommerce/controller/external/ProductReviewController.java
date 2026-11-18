package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewSummaryResponse;
import com.dragun.ecommerce.security.CustomerPrincipal;
import com.dragun.ecommerce.service.ProductReviewService;
import com.dragun.ecommerce.util.ClientIp;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Product reviews. Reading is public (/api/dragun/products/** in Constants.PUBLIC_ENDPOINTS); writing needs a
 * logged-in customer (SecurityConfig: POST /api/dragun/products/{id}/reviews requires ROLE_CUSTOMER, task-003 R3).
 */
@RestController
@RequestMapping("/api/dragun/products/{productId}/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProductReviewSummaryResponse>> list(@PathVariable Long productId) {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
            .body(ApiResponse.success(reviewService.getPublicReviews(productId)));
    }

    /**
     * Logged-in customer's review (multipart: rating, content, images[] up to 4). The author name comes from the
     * account profile. Published immediately (APPROVED); one review per product per account.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductReviewResponse>> submit(
        @PathVariable Long productId,
        @AuthenticationPrincipal CustomerPrincipal me,
        @RequestParam("rating") Integer rating,
        @RequestParam("content") String content,
        // Honeypot: hidden in the form, real users leave it empty
        @RequestParam(value = "website", required = false) String website,
        @RequestParam(value = "images", required = false) List<MultipartFile> images,
        HttpServletRequest request
    ) {
        ProductReviewResponse saved = reviewService.submit(
            productId, me.id(), rating, content, website, images, ClientIp.of(request));
        return ResponseEntity.ok(ApiResponse.success(saved, "Cảm ơn bạn! Đánh giá đã được đăng."));
    }
}
