package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.entity.District;
import com.dragun.ecommerce.model.entity.Province;
import com.dragun.ecommerce.model.entity.Ward;
import com.dragun.ecommerce.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LocationController {
    
    private final LocationService locationService;
    
    @GetMapping("/provinces")
    public ResponseEntity<ApiResponse<List<Province>>> getAllProvinces() {
        List<Province> provinces = locationService.getAllProvinces();
        return ResponseEntity.ok(ApiResponse.success(provinces));
    }
    
    @GetMapping("/districts/{provinceCode}")
    public ResponseEntity<ApiResponse<List<District>>> getDistricts(@PathVariable String provinceCode) {
        List<District> districts = locationService.getDistrictsByProvinceCode(provinceCode);
        return ResponseEntity.ok(ApiResponse.success(districts));
    }
    
    @GetMapping("/wards/{districtCode}")
    public ResponseEntity<ApiResponse<List<Ward>>> getWards(@PathVariable String districtCode) {
        List<Ward> wards = locationService.getWardsByDistrictCode(districtCode);
        return ResponseEntity.ok(ApiResponse.success(wards));
    }
}
