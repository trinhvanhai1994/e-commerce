package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.HomepageSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomepageSectionRepository extends JpaRepository<HomepageSection, Long> {
    Optional<HomepageSection> findByPageKeyAndSectionKey(String pageKey, String sectionKey);

    List<HomepageSection> findByPageKeyOrderBySortOrderAsc(String pageKey);

    boolean existsByPageKeyAndSectionKey(String pageKey, String sectionKey);
}
