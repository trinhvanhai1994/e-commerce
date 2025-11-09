package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.entity.District;
import com.dragun.ecommerce.model.entity.Province;
import com.dragun.ecommerce.model.entity.Ward;
import com.dragun.ecommerce.repository.DistrictRepository;
import com.dragun.ecommerce.repository.ProvinceRepository;
import com.dragun.ecommerce.repository.WardRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationDataInitService {
    
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    @Transactional
    public void initLocationData() {
        log.info("Checking location master data initialization...");
        try {
            // Check và init provinces
            if (provinceRepository.count() == 0) {
                log.info("Initializing provinces data from JSON...");
                loadProvinces();
                log.info("Provinces initialized: {} records", provinceRepository.count());
            } else {
                log.info("Provinces already exist: {} records", provinceRepository.count());
            }
            
            // Check và init districts
            if (districtRepository.count() == 0) {
                log.info("Initializing districts data from JSON...");
                loadDistricts();
                log.info("Districts initialized: {} records", districtRepository.count());
            } else {
                log.info("Districts already exist: {} records", districtRepository.count());
            }
            
            // Check và init wards
            if (wardRepository.count() == 0) {
                log.info("Initializing wards data from JSON...");
                loadWards();
                log.info("Wards initialized: {} records", wardRepository.count());
            } else {
                log.info("Wards already exist: {} records", wardRepository.count());
            }
            
        } catch (Exception e) {
            log.error("Error initializing location data", e);
        }
    }
    
    private void loadProvinces() throws Exception {
        ClassPathResource resource = new ClassPathResource("db/master/province.json");
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Province> provinceMap = objectMapper.readValue(
                inputStream, 
                new TypeReference<Map<String, Province>>() {}
            );
            
            List<Province> provinces = provinceMap.values().stream()
                .filter(p -> p.getCode() != null && p.getName() != null)
                .toList();
            
            if (!provinces.isEmpty()) {
                provinceRepository.saveAll(provinces);
                log.info("Saved {} provinces", provinces.size());
            }
        }
    }
    
    private void loadDistricts() throws Exception {
        ClassPathResource resource = new ClassPathResource("db/master/districts.json");
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, District> districtMap = objectMapper.readValue(
                inputStream, 
                new TypeReference<Map<String, District>>() {}
            );
            
            List<District> districts = districtMap.values().stream()
                .peek(d -> {
                    // Debug: log first few districts
                    if (districtMap.values().stream().limit(3).anyMatch(d2 -> d2 == d)) {
                        log.debug("Parsed district: code={}, name={}, parentCode={}", 
                            d.getCode(), d.getName(), d.getParentCode());
                    }
                })
                .filter(d -> {
                    if (d.getCode() == null || d.getName() == null) {
                        log.warn("District missing code or name: {}", d);
                        return false;
                    }
                    if (d.getParentCode() == null || d.getParentCode().isEmpty()) {
                        log.warn("District {} has null/empty parentCode", d.getCode());
                        return false;
                    }
                    return true;
                })
                .toList();
            
            if (!districts.isEmpty()) {
                // Save in batches to avoid memory issues
                int batchSize = 1000;
                for (int i = 0; i < districts.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, districts.size());
                    List<District> batch = districts.subList(i, end);
                    
                    // Validate batch before saving
                    for (District d : batch) {
                        if (d.getParentCode() == null || d.getParentCode().isEmpty()) {
                            log.error("District {} has null parentCode before save!", d.getCode());
                            throw new IllegalStateException("District " + d.getCode() + " has null parentCode");
                        }
                    }
                    
                    districtRepository.saveAll(batch);
                    log.debug("Saved districts batch {}-{}", i, end);
                }
                log.info("Saved {} districts", districts.size());
            }
        }
    }
    
    private void loadWards() throws Exception {
        ClassPathResource resource = new ClassPathResource("db/master/ward.json");
        try (InputStream inputStream = resource.getInputStream()) {
            Map<String, Ward> wardMap = objectMapper.readValue(
                inputStream, 
                new TypeReference<Map<String, Ward>>() {}
            );
            
            List<Ward> wards = wardMap.values().stream()
                .filter(w -> w.getCode() != null && w.getName() != null && w.getParentCode() != null)
                .filter(w -> w.getParentCode() != null && !w.getParentCode().isEmpty())
                .toList();
            
            if (!wards.isEmpty()) {
                // Save in batches to avoid memory issues
                int batchSize = 1000;
                for (int i = 0; i < wards.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, wards.size());
                    List<Ward> batch = wards.subList(i, end);
                    wardRepository.saveAll(batch);
                    log.debug("Saved wards batch {}-{}", i, end);
                }
                log.info("Saved {} wards", wards.size());
            }
        }
    }
}

