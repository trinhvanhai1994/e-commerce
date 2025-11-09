package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.entity.District;
import com.dragun.ecommerce.model.entity.Province;
import com.dragun.ecommerce.model.entity.Ward;
import com.dragun.ecommerce.service.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("LocationController API Tests")
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocationService locationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Province testProvince;
    private District testDistrict;
    private Ward testWard;

    @BeforeEach
    void setUp() {
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
    @DisplayName("GET /provinces - Should return all provinces")
    void testGetAllProvinces() throws Exception {
        // Given
        List<Province> provinces = Arrays.asList(
                testProvince,
                Province.builder()
                        .code("02")
                        .name("Hà Giang")
                        .slug("ha-giang")
                        .type("tinh")
                        .nameWithType("Tỉnh Hà Giang")
                        .build()
        );
        when(locationService.getAllProvinces()).thenReturn(provinces);

        // When & Then
        mockMvc.perform(get("/provinces")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("01"))
                .andExpect(jsonPath("$.data[0].name").value("Hà Nội"))
                .andExpect(jsonPath("$.data[1].code").value("02"))
                .andExpect(jsonPath("$.data[1].name").value("Hà Giang"));
    }

    @Test
    @DisplayName("GET /provinces - Should return empty list when no provinces")
    void testGetAllProvinces_Empty() throws Exception {
        // Given
        when(locationService.getAllProvinces()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/provinces")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /districts/{provinceCode} - Should return districts by province code")
    void testGetDistricts() throws Exception {
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
        when(locationService.getDistrictsByProvinceCode("01")).thenReturn(districts);

        // When & Then
        mockMvc.perform(get("/districts/01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("001"))
                .andExpect(jsonPath("$.data[0].name").value("Ba Đình"))
                .andExpect(jsonPath("$.data[0].parent_code").value("01"))
                .andExpect(jsonPath("$.data[1].code").value("002"))
                .andExpect(jsonPath("$.data[1].name").value("Hoàn Kiếm"));
    }

    @Test
    @DisplayName("GET /districts/{provinceCode} - Should return empty list when no districts found")
    void testGetDistricts_NotFound() throws Exception {
        // Given
        when(locationService.getDistrictsByProvinceCode("99")).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/districts/99")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("GET /wards/{districtCode} - Should return wards by district code")
    void testGetWards() throws Exception {
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
        when(locationService.getWardsByDistrictCode("001")).thenReturn(wards);

        // When & Then
        mockMvc.perform(get("/wards/001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("00001"))
                .andExpect(jsonPath("$.data[0].name").value("Phúc Xá"))
                .andExpect(jsonPath("$.data[0].parent_code").value("001"))
                .andExpect(jsonPath("$.data[1].code").value("00004"))
                .andExpect(jsonPath("$.data[1].name").value("Trúc Bạch"));
    }

    @Test
    @DisplayName("GET /wards/{districtCode} - Should return empty list when no wards found")
    void testGetWards_NotFound() throws Exception {
        // Given
        when(locationService.getWardsByDistrictCode("999")).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/wards/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}

