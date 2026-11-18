package com.dragun.ecommerce.controller;

import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.repository.ProductRepository;
import com.dragun.ecommerce.repository.UserRepository;
import com.dragun.ecommerce.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Category endpoints through the real security filter chain (task-004). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("Product category API (HTTP)")
class ProductCategorySecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("public list needs no token, is cacheable and does not shadow /products/{id}")
    void publicList() throws Exception {
        mvc.perform(get("/api/dragun/products/categories"))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=30")))
            .andExpect(jsonPath("$.data[?(@.code == 'ME_DEN')].name").value("Mè đen"))
            .andExpect(jsonPath("$.data[0].updatedAt").doesNotExist());

        Long anyId = productRepository.findByDeletedFalseAndStatus("ACTIVE").get(0).getId();
        mvc.perform(get("/api/dragun/products/" + anyId)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("admin endpoints need the admin token")
    void adminOnly() throws Exception {
        String body = json.writeValueAsString(Map.of("name", "Trà gạo lứt"));

        mvc.perform(get("/api/dragun/admin/categories")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/dragun/admin/categories").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized());

        String adminToken = jwtTokenProvider.generateToken(userRepository.findByUsername("admin").orElseThrow());
        mvc.perform(post("/api/dragun/admin/categories").header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.code").value("TRA_GAO_LUT"));
        mvc.perform(get("/api/dragun/admin/categories").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[?(@.code == 'TRA_GAO_LUT')].productCount").value(0));
    }
}
