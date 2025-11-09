package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, String> {
    List<District> findByParentCodeOrderByNameAsc(String parentCode);
    long count();
}

