package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.entity.District;
import com.dragun.ecommerce.model.entity.Province;
import com.dragun.ecommerce.model.entity.Ward;
import com.dragun.ecommerce.repository.DistrictRepository;
import com.dragun.ecommerce.repository.ProvinceRepository;
import com.dragun.ecommerce.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    
    public List<Province> getAllProvinces() {
        return provinceRepository.findAllByOrderByNameAsc();
    }
    
    public List<District> getDistrictsByProvinceCode(String provinceCode) {
        return districtRepository.findByParentCodeOrderByNameAsc(provinceCode);
    }
    
    public List<Ward> getWardsByDistrictCode(String districtCode) {
        return wardRepository.findByParentCodeOrderByNameAsc(districtCode);
    }
    
    public Province getProvinceByCode(String code) {
        return provinceRepository.findById(code).orElse(null);
    }
    
    public District getDistrictByCode(String code) {
        return districtRepository.findById(code).orElse(null);
    }
    
    public Ward getWardByCode(String code) {
        return wardRepository.findById(code).orElse(null);
    }
}
