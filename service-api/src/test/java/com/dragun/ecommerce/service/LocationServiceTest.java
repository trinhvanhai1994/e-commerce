package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.entity.District;
import com.dragun.ecommerce.model.entity.Province;
import com.dragun.ecommerce.model.entity.Ward;
import com.dragun.ecommerce.repository.DistrictRepository;
import com.dragun.ecommerce.repository.ProvinceRepository;
import com.dragun.ecommerce.repository.WardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocationService Tests")
class LocationServiceTest {

    @Mock
    private ProvinceRepository provinceRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private WardRepository wardRepository;

    @InjectMocks
    private LocationService locationService;

    private Province testProvince;
    private District testDistrict;
    private Ward testWard;

    @BeforeEach
    void setUp() {
        // Setup test data
        testProvince = Province.builder()
                .code("01")
                .name("Hà Nội")
                .slug("ha-noi")
                .type("thanh-pho")
                .nameWithType("Thành phố Hà Nội")
                .build();

        testDistrict = District.builder()
                .code("001")
                .name("Ba Đình")
                .type("quan")
                .slug("ba-dinh")
                .nameWithType("Quận Ba Đình")
                .path("Ba Đình, Hà Nội")
                .pathWithType("Quận Ba Đình, Thành phố Hà Nội")
                .parentCode("01")
                .build();

        testWard = Ward.builder()
                .code("00001")
                .name("Phúc Xá")
                .type("phuong")
                .slug("phuc-xa")
                .nameWithType("Phường Phúc Xá")
                .path("Phúc Xá, Ba Đình, Hà Nội")
                .pathWithType("Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội")
                .parentCode("001")
                .build();
    }

    @Test
    @DisplayName("Should return all provinces sorted by name")
    void testGetAllProvinces() {
        // Given
        Province haGiang = Province.builder()
                .code("02")
                .name("Hà Giang")
                .slug("ha-giang")
                .type("tinh")
                .nameWithType("Tỉnh Hà Giang")
                .build();
        // Repository returns sorted list (Hà Giang comes before Hà Nội alphabetically)
        List<Province> provinces = Arrays.asList(haGiang, testProvince);
        when(provinceRepository.findAllByOrderByNameAsc()).thenReturn(provinces);

        // When
        List<Province> result = locationService.getAllProvinces();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hà Giang", result.get(0).getName());
        assertEquals("Hà Nội", result.get(1).getName());
        verify(provinceRepository, times(1)).findAllByOrderByNameAsc();
    }

    @Test
    @DisplayName("Should return districts by province code")
    void testGetDistrictsByProvinceCode() {
        // Given
        List<District> districts = Arrays.asList(
                testDistrict,
                District.builder()
                        .code("002")
                        .name("Hoàn Kiếm")
                        .type("quan")
                        .slug("hoan-kiem")
                        .nameWithType("Quận Hoàn Kiếm")
                        .parentCode("01")
                        .build()
        );
        when(districtRepository.findByParentCodeOrderByNameAsc("01")).thenReturn(districts);

        // When
        List<District> result = locationService.getDistrictsByProvinceCode("01");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> "01".equals(d.getParentCode())));
        verify(districtRepository, times(1)).findByParentCodeOrderByNameAsc("01");
    }

    @Test
    @DisplayName("Should return empty list when no districts found for province code")
    void testGetDistrictsByProvinceCode_NotFound() {
        // Given
        when(districtRepository.findByParentCodeOrderByNameAsc("99")).thenReturn(List.of());

        // When
        List<District> result = locationService.getDistrictsByProvinceCode("99");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(districtRepository, times(1)).findByParentCodeOrderByNameAsc("99");
    }

    @Test
    @DisplayName("Should return wards by district code")
    void testGetWardsByDistrictCode() {
        // Given
        List<Ward> wards = Arrays.asList(
                testWard,
                Ward.builder()
                        .code("00004")
                        .name("Trúc Bạch")
                        .type("phuong")
                        .slug("truc-bach")
                        .nameWithType("Phường Trúc Bạch")
                        .parentCode("001")
                        .build()
        );
        when(wardRepository.findByParentCodeOrderByNameAsc("001")).thenReturn(wards);

        // When
        List<Ward> result = locationService.getWardsByDistrictCode("001");

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(w -> "001".equals(w.getParentCode())));
        verify(wardRepository, times(1)).findByParentCodeOrderByNameAsc("001");
    }

    @Test
    @DisplayName("Should return province by code")
    void testGetProvinceByCode() {
        // Given
        when(provinceRepository.findById("01")).thenReturn(Optional.of(testProvince));

        // When
        Province result = locationService.getProvinceByCode("01");

        // Then
        assertNotNull(result);
        assertEquals("01", result.getCode());
        assertEquals("Hà Nội", result.getName());
        verify(provinceRepository, times(1)).findById("01");
    }

    @Test
    @DisplayName("Should return null when province not found")
    void testGetProvinceByCode_NotFound() {
        // Given
        when(provinceRepository.findById("99")).thenReturn(Optional.empty());

        // When
        Province result = locationService.getProvinceByCode("99");

        // Then
        assertNull(result);
        verify(provinceRepository, times(1)).findById("99");
    }

    @Test
    @DisplayName("Should return district by code")
    void testGetDistrictByCode() {
        // Given
        when(districtRepository.findById("001")).thenReturn(Optional.of(testDistrict));

        // When
        District result = locationService.getDistrictByCode("001");

        // Then
        assertNotNull(result);
        assertEquals("001", result.getCode());
        assertEquals("Ba Đình", result.getName());
        verify(districtRepository, times(1)).findById("001");
    }

    @Test
    @DisplayName("Should return ward by code")
    void testGetWardByCode() {
        // Given
        when(wardRepository.findById("00001")).thenReturn(Optional.of(testWard));

        // When
        Ward result = locationService.getWardByCode("00001");

        // Then
        assertNotNull(result);
        assertEquals("00001", result.getCode());
        assertEquals("Phúc Xá", result.getName());
        verify(wardRepository, times(1)).findById("00001");
    }
}

