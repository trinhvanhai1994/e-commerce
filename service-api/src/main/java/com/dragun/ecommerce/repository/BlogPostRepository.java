package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    Optional<BlogPost> findBySlugAndDeletedAtIsNull(String slug);

    Optional<BlogPost> findByIdAndDeletedAtIsNull(Long id);

    List<BlogPost> findByStatusAndDeletedAtIsNullOrderByPublishedAtDesc(String status);

    List<BlogPost> findByDeletedAtIsNullOrderByUpdatedAtDesc();

    @Query("""
        SELECT b FROM BlogPost b
        WHERE b.deletedAt IS NULL
          AND b.status = 'PUBLISHED'
          AND b.showOnHome = TRUE
        ORDER BY b.homeOrder ASC
        """)
    List<BlogPost> findFeaturedForHome();

    long countByShowOnHomeTrueAndDeletedAtIsNullAndIdNot(Long id);

    long countByShowOnHomeTrueAndDeletedAtIsNull();

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    /** Includes soft-deleted rows. */
    boolean existsBySlug(String slug);

    boolean existsByHomeOrderAndShowOnHomeTrueAndDeletedAtIsNullAndIdNot(Integer homeOrder, Long id);
}
