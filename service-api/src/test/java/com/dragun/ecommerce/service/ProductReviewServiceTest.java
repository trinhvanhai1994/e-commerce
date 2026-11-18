package com.dragun.ecommerce.service;

import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.model.dto.request.ProductReviewRequest;
import com.dragun.ecommerce.model.dto.response.ProductReviewResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewSummaryResponse;
import com.dragun.ecommerce.model.entity.ProductReview;
import com.dragun.ecommerce.repository.ProductRepository;
import com.dragun.ecommerce.repository.ProductReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Review rules on a real PostgreSQL (jsonb images, Flyway V21 seed). Each test rolls back; the rate limiter is
 * in-memory and shared, so every test submits under its own client key.
 */
@SpringBootTest
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("ProductReviewService")
class ProductReviewServiceTest {

    @Autowired
    private ProductReviewService reviewService;

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;
    private String clientKey;

    @BeforeEach
    void setUp() {
        productId = productRepository.findAll().get(0).getId();
        clientKey = "test-" + UUID.randomUUID();
    }

    private ProductReviewResponse submit(int rating, String content) {
        return reviewService.submit(productId, "Khách Test", rating, content, null, List.of(), clientKey);
    }

    @Test
    @DisplayName("customer review is published immediately and counted in the public summary")
    void submit_publishesImmediately() {
        ProductReviewSummaryResponse before = reviewService.getPublicReviews(productId);

        ProductReviewResponse saved = submit(1, "Đánh giá hiện ngay");

        ProductReview stored = reviewRepository.findById(saved.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(ProductReview.STATUS_APPROVED);
        assertThat(stored.getSource()).isEqualTo(ProductReview.SOURCE_CUSTOMER);
        assertThat(stored.getApprovedBy()).isEqualTo(ProductReviewService.AUTO_APPROVER);
        assertThat(stored.getApprovedAt()).isNotNull();

        ProductReviewSummaryResponse after = reviewService.getPublicReviews(productId);
        assertThat(after.getReviewCount()).isEqualTo(before.getReviewCount() + 1);
        assertThat(after.getReviews()).extracting(ProductReviewResponse::getId).contains(saved.getId());
        assertThat(after.getAverageRating()).isLessThan(before.getAverageRating());
    }

    @Test
    @DisplayName("public response does not expose admin-only fields")
    void submit_publicResponseHasNoAdminFields() {
        ProductReviewResponse saved = submit(5, "Không lộ trường admin");

        assertThat(saved.getStatus()).isNull();
        assertThat(saved.getSource()).isNull();
        assertThat(saved.getUpdatedBy()).isNull();
    }

    @Test
    @DisplayName("admin hides a review: it leaves the public list; showing it again brings it back")
    void hideAndShowAgain() {
        ProductReviewResponse saved = submit(5, "Sẽ bị ẩn rồi hiện lại");

        reviewService.updateStatus(saved.getId(), "HIDDEN", "admin");
        assertThat(reviewService.getPublicReviews(productId).getReviews())
            .extracting(ProductReviewResponse::getId).doesNotContain(saved.getId());

        reviewService.updateStatus(saved.getId(), "approved", "admin");
        assertThat(reviewService.getPublicReviews(productId).getReviews())
            .extracting(ProductReviewResponse::getId).contains(saved.getId());
        assertThat(reviewRepository.findById(saved.getId()).orElseThrow().getApprovedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("admin edits content with the current updatedAt; a stale updatedAt is rejected with 409")
    void adminEdit_optimisticLock() {
        ProductReviewResponse saved = submit(5, "Nội dung gốc");
        ProductReviewResponse loaded = reviewService.getAdmin(saved.getId());

        ProductReviewRequest edit = new ProductReviewRequest();
        edit.setAuthorName("Khách Test");
        edit.setRating(4);
        edit.setContent("Nội dung admin sửa");
        edit.setUpdatedAt(loaded.getUpdatedAt());
        ProductReviewResponse updated = reviewService.update(saved.getId(), edit, "admin");

        assertThat(updated.getContent()).isEqualTo("Nội dung admin sửa");
        assertThat(updated.getStatus()).isEqualTo(ProductReview.STATUS_APPROVED);

        edit.setUpdatedAt(loaded.getUpdatedAt().minusSeconds(1));
        assertThatThrownBy(() -> reviewService.update(saved.getId(), edit, "admin"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("honeypot submission answers like a success but stores nothing")
    void honeypot_storesNothing() {
        long before = reviewRepository.count();

        ProductReviewResponse response =
            reviewService.submit(productId, "Bot", 5, "spam spam spam", "http://spam", List.of(), clientKey);

        assertThat(response.getId()).isNull();
        assertThat(reviewRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("invalid input is rejected with 400, unknown product with 404")
    void validation() {
        assertStatus(() -> submit(0, "Số sao sai"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> submit(6, "Số sao sai"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> submit(5, "ngắn"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> submit(5, "x".repeat(2001)), HttpStatus.BAD_REQUEST);
        assertStatus(() -> reviewService.submit(productId, "   ", 5, "Tên trống", null, List.of(), clientKey),
            HttpStatus.BAD_REQUEST);
        assertStatus(() -> reviewService.submit(-1L, "Khách", 5, "Sản phẩm không có", null, List.of(), clientKey),
            HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("the 6th submission from one client within the window gets 429")
    void rateLimit() {
        for (int i = 0; i < 5; i++) {
            submit(5, "Lần gửi số " + (i + 1));
        }
        assertStatus(() -> submit(5, "Lần gửi số 6"), HttpStatus.TOO_MANY_REQUESTS);
    }

    private static void assertStatus(Runnable call, HttpStatus expected) {
        assertThatThrownBy(call::run)
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(expected);
    }
}
