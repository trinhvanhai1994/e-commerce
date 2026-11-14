package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewSummaryResponse;
import com.dragun.ecommerce.service.ProductReviewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

/** Public product reviews. Under /api/dragun/products/** so it is already public (Constants.PUBLIC_ENDPOINTS). */
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

    /** Customer submission (multipart: authorName, rating, content, images[] up to 4). Stored as PENDING. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductReviewResponse>> submit(
        @PathVariable Long productId,
        @RequestParam("authorName") String authorName,
        @RequestParam("rating") Integer rating,
        @RequestParam("content") String content,
        // Honeypot: hidden in the form, real users leave it empty
        @RequestParam(value = "website", required = false) String website,
        @RequestParam(value = "images", required = false) List<MultipartFile> images,
        HttpServletRequest request
    ) {
        ProductReviewResponse saved = reviewService.submit(
            productId, authorName, rating, content, website, images, clientKey(request));
        return ResponseEntity.ok(ApiResponse.success(saved, "Cảm ơn bạn! Đánh giá sẽ hiển thị sau khi được duyệt."));
    }

    /**
     * Client IP for rate limiting. X-Real-IP or the LAST X-Forwarded-For entry is what the reverse proxy saw;
     * the first X-Forwarded-For entry is client-supplied and would let anyone bypass the limit.
     */
    private static String clientKey(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            return parts[parts.length - 1].trim();
        }
        return request.getRemoteAddr();
    }
}
