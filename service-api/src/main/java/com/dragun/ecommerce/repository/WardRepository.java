package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, String> {
    List<Ward> findByParentCodeOrderByNameAsc(String parentCode);
    long count();
}

