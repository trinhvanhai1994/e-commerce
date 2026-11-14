package com.dragun.ecommerce.service;

import com.dragun.ecommerce.cms.ImageSlot;
import com.dragun.ecommerce.model.dto.request.ProductReviewRequest;
import com.dragun.ecommerce.model.dto.response.ProductReviewPageResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewSummaryResponse;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.model.entity.ProductReview;
import com.dragun.ecommerce.repository.ProductRepository;
import com.dragun.ecommerce.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReviewService {

    static final int MAX_IMAGES = 4;
    static final int MAX_AUTHOR = 100;
    static final int MAX_CONTENT = 2000;
    static final int MIN_CONTENT = 5;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> STATUSES = Set.of(
        ProductReview.STATUS_PENDING, ProductReview.STATUS_APPROVED, ProductReview.STATUS_HIDDEN);

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final CmsImageService cmsImageService;
    private final ReviewSubmissionLimiter submissionLimiter;

    // ---------- public ----------

    @Transactional(readOnly = true)
    public ProductReviewSummaryResponse getPublicReviews(Long productId) {
        requireProduct(productId);
        List<ProductReview> approved =
            reviewRepository.findByProductIdAndStatusOrderByCreatedAtDesc(productId, ProductReview.STATUS_APPROVED);
        double average = approved.stream().mapToInt(r -> r.getRating()).average().orElse(0);
        return ProductReviewSummaryResponse.builder()
            .productId(productId)
            .averageRating(Math.round(average * 10) / 10.0)
            .reviewCount(approved.size())
            .reviews(approved.stream().map(r -> toResponse(r, null, false)).toList())
            .build();
    }

    /**
     * Customer submission: stored as PENDING and shown only after an admin approves it.
     * All text and every image are validated before any file is written.
     */
    @Transactional
    public ProductReviewResponse submit(Long productId, String authorName, Integer rating, String content,
                                        String honeypot, List<MultipartFile> files, String clientKey) {
        if (honeypot != null && !honeypot.isBlank()) {
            // Bots fill every field; answer like a success so they do not adapt, but store nothing.
            log.info("Review honeypot hit product={} client={}", productId, clientKey);
            return ProductReviewResponse.builder().productId(productId).status(ProductReview.STATUS_PENDING).build();
        }
        requireProduct(productId);
        String author = cleanText(authorName);
        String text = cleanText(content);
        if (author == null || author.length() > MAX_AUTHOR) {
            throw badRequest("Tên tối đa " + MAX_AUTHOR + " ký tự và không được để trống");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw badRequest("Số sao từ 1 đến 5");
        }
        if (text == null || text.length() < MIN_CONTENT || text.length() > MAX_CONTENT) {
            throw badRequest("Nội dung đánh giá từ " + MIN_CONTENT + " đến " + MAX_CONTENT + " ký tự");
        }
        List<MultipartFile> images = files == null ? List.of()
            : files.stream().filter(f -> f != null && !f.isEmpty()).toList();
        if (images.size() > MAX_IMAGES) {
            throw badRequest("Tối đa " + MAX_IMAGES + " ảnh");
        }
        if (!submissionLimiter.tryAcquire(clientKey)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Bạn đã gửi quá nhiều đánh giá. Vui lòng thử lại sau ít phút.");
        }

        try {
            for (MultipartFile file : images) {
                cmsImageService.validate(file, ImageSlot.REVIEW_IMAGE);
            }
            List<String> paths = new ArrayList<>();
            for (MultipartFile file : images) {
                paths.add(cmsImageService.store(file, ImageSlot.REVIEW_IMAGE, "review", String.valueOf(productId)));
            }
            ProductReview review = ProductReview.builder()
                .productId(productId)
                .authorName(author)
                .rating(rating.shortValue())
                .content(text)
                .images(paths)
                .status(ProductReview.STATUS_PENDING)
                .source(ProductReview.SOURCE_CUSTOMER)
                .updatedBy("customer")
                .build();
            return toResponse(reviewRepository.save(review), null, false);
        } catch (IllegalArgumentException e) {
            throw badRequest(e.getMessage());
        } catch (IOException e) {
            log.error("Review image store failed product={}: {}", productId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không lưu được ảnh. Vui lòng thử lại.");
        }
    }

    // ---------- admin ----------

    @Transactional(readOnly = true)
    public ProductReviewPageResponse listAdmin(Long productId, String status, int page, int size) {
        String statusFilter = status == null || status.isBlank() ? null : normalizeStatus(status);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Page<ProductReview> result =
            reviewRepository.search(productId, statusFilter, PageRequest.of(Math.max(page, 0), safeSize));

        Map<Long, String> names = productNames(result.getContent().stream().map(ProductReview::getProductId).toList());
        Map<String, Long> counts = new LinkedHashMap<>();
        STATUSES.stream().sorted().forEach(s -> counts.put(s, 0L));
        for (Object[] row : reviewRepository.countByStatus(productId)) {
            counts.put((String) row[0], ((Number) row[1]).longValue());
        }
        return ProductReviewPageResponse.builder()
            .items(result.getContent().stream().map(r -> toResponse(r, names.get(r.getProductId()), true)).toList())
            .totalElements(result.getTotalElements())
            .page(result.getNumber())
            .size(result.getSize())
            .totalPages(result.getTotalPages())
            .statusCounts(counts)
            .build();
    }

    @Transactional(readOnly = true)
    public ProductReviewResponse getAdmin(Long id) {
        ProductReview review = requireReview(id);
        return toResponse(review, productNames(List.of(review.getProductId())).get(review.getProductId()), true);
    }

    @Transactional
    public ProductReviewResponse create(ProductReviewRequest request, String username) {
        if (request.getProductId() == null) {
            throw badRequest("Chưa chọn sản phẩm");
        }
        requireProduct(request.getProductId());
        String status = request.getStatus() == null || request.getStatus().isBlank()
            ? ProductReview.STATUS_APPROVED : normalizeStatus(request.getStatus());
        ProductReview review = ProductReview.builder()
            .productId(request.getProductId())
            .authorName(cleanText(request.getAuthorName()))
            .rating(request.getRating().shortValue())
            .content(cleanText(request.getContent()))
            .images(sanitizeImages(request.getImages()))
            .status(status)
            .source(ProductReview.SOURCE_ADMIN)
            .updatedBy(username)
            .build();
        markApproval(review, status, username);
        return toAdminResponse(reviewRepository.saveAndFlush(review));
    }

    @Transactional
    public ProductReviewResponse update(Long id, ProductReviewRequest request, String username) {
        ProductReview review = requireReview(id);
        requireFresh(review, request.getUpdatedAt());
        review.setAuthorName(cleanText(request.getAuthorName()));
        review.setRating(request.getRating().shortValue());
        review.setContent(cleanText(request.getContent()));
        review.setImages(sanitizeImages(request.getImages()));
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            String next = normalizeStatus(request.getStatus());
            markApproval(review, next, username);
            review.setStatus(next);
        }
        review.setUpdatedBy(username);
        // saveAndFlush so @PreUpdate sets updatedAt before the response (next save's lock token) is built
        return toAdminResponse(reviewRepository.saveAndFlush(review));
    }

    @Transactional
    public ProductReviewResponse updateStatus(Long id, String status, String username) {
        ProductReview review = requireReview(id);
        String next = normalizeStatus(status);
        markApproval(review, next, username);
        review.setStatus(next);
        review.setUpdatedBy(username);
        return toAdminResponse(reviewRepository.saveAndFlush(review));
    }

    /** Hard delete. Uploaded image files are left on disk (same as other CMS images). */
    @Transactional
    public void delete(Long id, String username) {
        ProductReview review = requireReview(id);
        reviewRepository.delete(review);
        log.info("Review {} of product {} deleted by {}", id, review.getProductId(), username);
    }

    // ---------- helpers ----------

    private void markApproval(ProductReview review, String nextStatus, String username) {
        if (ProductReview.STATUS_APPROVED.equals(nextStatus) && !ProductReview.STATUS_APPROVED.equals(review.getStatus())) {
            review.setApprovedAt(OffsetDateTime.now());
            review.setApprovedBy(username);
        }
    }

    private void requireFresh(ProductReview review, OffsetDateTime clientUpdatedAt) {
        if (clientUpdatedAt == null) {
            throw badRequest("Thiếu updatedAt. Hãy tải lại đánh giá rồi lưu lại.");
        }
        if (review.getUpdatedAt() == null || !review.getUpdatedAt().toInstant().equals(clientUpdatedAt.toInstant())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Đánh giá đã được cập nhật bởi người khác. Hãy tải lại.");
        }
    }

    private void requireProduct(Long productId) {
        if (productId == null || !productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại");
        }
    }

    private ProductReview requireReview(Long id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Đánh giá không tồn tại"));
    }

    private Map<Long, String> productNames(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return productRepository.findAllById(ids.stream().distinct().toList()).stream()
            .collect(Collectors.toMap(Product::getId, p -> p.getName() == null ? "" : p.getName(), (a, b) -> a));
    }

    private String normalizeStatus(String status) {
        String s = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(s)) {
            throw badRequest("status phải là PENDING, APPROVED hoặc HIDDEN");
        }
        return s;
    }

    /** Only paths produced by upload-cms (or the seeded /images/review/...) are accepted. */
    private List<String> sanitizeImages(List<String> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>();
        for (String raw : images) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String path = raw.trim();
            if (!path.startsWith("/images/") || path.contains("..") || path.contains("\\") || path.length() > 1000) {
                throw badRequest("Ảnh phải là đường dẫn /images/... hợp lệ");
            }
            out.add(path);
        }
        if (out.size() > MAX_IMAGES) {
            throw badRequest("Tối đa " + MAX_IMAGES + " ảnh");
        }
        return out;
    }

    /** Trims and collapses control characters; rendering escapes HTML, so no markup stripping is needed. */
    private static String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("[\\p{Cntrl}&&[^\\n]]", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ProductReviewResponse toAdminResponse(ProductReview review) {
        return toResponse(review, productNames(List.of(review.getProductId())).get(review.getProductId()), true);
    }

    private static ProductReviewResponse toResponse(ProductReview r, String productName, boolean admin) {
        ProductReviewResponse.ProductReviewResponseBuilder b = ProductReviewResponse.builder()
            .id(r.getId())
            .productId(r.getProductId())
            .authorName(r.getAuthorName())
            .rating(r.getRating() == null ? null : r.getRating().intValue())
            .content(r.getContent())
            .images(r.getImages() == null ? List.of() : r.getImages())
            .createdAt(r.getCreatedAt());
        if (admin) {
            b.productName(productName)
                .status(r.getStatus())
                .source(r.getSource())
                .approvedAt(r.getApprovedAt())
                .updatedAt(r.getUpdatedAt())
                .updatedBy(r.getUpdatedBy());
        } else if (ProductReview.STATUS_PENDING.equals(r.getStatus())) {
            // A customer's own submission: tell the form it is waiting for approval
            b.status(r.getStatus());
        }
        return b.build();
    }
}
