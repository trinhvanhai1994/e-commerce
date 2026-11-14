package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdAndStatusOrderByCreatedAtDesc(Long productId, String status);

    /** Admin list; null filters mean "any". */
    @Query("""
        SELECT r FROM ProductReview r
        WHERE (:productId IS NULL OR r.productId = :productId)
          AND (:status IS NULL OR r.status = :status)
        ORDER BY r.createdAt DESC
        """)
    Page<ProductReview> search(@Param("productId") Long productId, @Param("status") String status, Pageable pageable);

    /** Rows of [status, count] for the admin tabs, optionally for one product. */
    @Query("""
        SELECT r.status, COUNT(r) FROM ProductReview r
        WHERE (:productId IS NULL OR r.productId = :productId)
        GROUP BY r.status
        """)
    List<Object[]> countByStatus(@Param("productId") Long productId);
}
