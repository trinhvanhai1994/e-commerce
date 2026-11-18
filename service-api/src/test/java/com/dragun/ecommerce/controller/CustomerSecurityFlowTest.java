package com.dragun.ecommerce.controller;

import com.dragun.ecommerce.TestCustomers;
import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import com.dragun.ecommerce.repository.UserRepository;
import com.dragun.ecommerce.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Customer accounts through the real security filter chain (task-003): which token may call what, and that the
 * admin token keeps working after JwtAuthenticationFilter learned customer tokens.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("Customer security flow (HTTP)")
class CustomerSecurityFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Product product;
    private String phone;

    @BeforeEach
    void setUp() {
        product = productRepository.findAll().stream().filter(p -> p.getStock() != null && p.getStock() > 0)
            .findFirst().orElseThrow();
        phone = TestCustomers.randomPhone();
    }

    private JsonNode register() throws Exception {
        String body = json.writeValueAsString(Map.of(
            "phone", phone, "fullName", "Trần Thị Bình", "password", "matkhau123", "consent", true));
        String res = mvc.perform(post("/api/dragun/auth/register").contentType(MediaType.APPLICATION_JSON).content(body)
                .with(r -> { r.setRemoteAddr("10.0.0." + (int) (Math.random() * 250)); return r; }))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("data");
    }

    private String orderBody() throws Exception {
        return json.writeValueAsString(Map.of(
            "customerInfo", Map.of("name", "Trần Thị Bình", "phone", phone, "address", "1 Lê Lợi"),
            "items", new Object[]{Map.of("productId", product.getId(), "quantity", 1)}));
    }

    @Test
    @DisplayName("writing a review without login gets 401 JSON; reading reviews stays public")
    void review_requiresLogin() throws Exception {
        mvc.perform(multipart("/api/dragun/products/" + product.getId() + "/reviews")
                .param("rating", "5").param("content", "Chưa đăng nhập"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));

        mvc.perform(get("/api/dragun/products/" + product.getId() + "/reviews"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a logged-in customer writes a review under the profile name")
    void review_withCustomerToken() throws Exception {
        String token = register().path("accessToken").asText();

        mvc.perform(multipart("/api/dragun/products/" + product.getId() + "/reviews")
                .param("rating", "5").param("content", "Đã đăng nhập, đánh giá được")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.authorName").value("Trần Thị Bình"));
    }

    @Test
    @DisplayName("account endpoints need a customer token; a customer token cannot reach admin APIs")
    void account_andAdminBoundaries() throws Exception {
        String token = register().path("accessToken").asText();

        mvc.perform(get("/api/dragun/account/profile"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dragun/account/profile").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.phone").value(phone));
        mvc.perform(get("/api/dragun/admin/reviews").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("the admin token still works on admin APIs and cannot use customer account APIs")
    void adminToken_unchanged() throws Exception {
        String adminToken = jwtTokenProvider.generateToken(userRepository.findByUsername("admin").orElseThrow());

        mvc.perform(get("/api/dragun/admin/reviews").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
        mvc.perform(get("/api/dragun/account/profile").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("an order placed while logged in belongs to the account; guest checkout still works")
    void orders_attachToAccount() throws Exception {
        JsonNode session = register();
        String token = session.path("accessToken").asText();
        long customerId = session.path("customer").path("id").asLong();

        String withToken = mvc.perform(post("/api/extend/orders").contentType(MediaType.APPLICATION_JSON)
                .content(orderBody()).header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String guest = mvc.perform(post("/api/extend/orders").contentType(MediaType.APPLICATION_JSON)
                .content(orderBody()))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        Order mine = orderRepository.findByOrderId(json.readTree(withToken).path("data").path("orderId").asText()).orElseThrow();
        Order anonymous = orderRepository.findByOrderId(json.readTree(guest).path("data").path("orderId").asText()).orElseThrow();
        assertThat(mine.getCustomerId()).isEqualTo(customerId);
        assertThat(anonymous.getCustomerId()).isNull();

        mvc.perform(get("/api/dragun/account/orders").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].orderId").value(mine.getOrderId()));
    }

    @Test
    @DisplayName("after a password change the old access token is rejected immediately")
    void changePassword_invalidatesOldAccessToken() throws Exception {
        String oldToken = register().path("accessToken").asText();
        String body = json.writeValueAsString(Map.of("currentPassword", "matkhau123", "newPassword", "matkhaumoi456"));

        String res = mvc.perform(put("/api/dragun/account/password").contentType(MediaType.APPLICATION_JSON)
                .content(body).header("Authorization", "Bearer " + oldToken))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String newToken = json.readTree(res).path("data").path("accessToken").asText();

        mvc.perform(get("/api/dragun/account/profile").header("Authorization", "Bearer " + oldToken))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/dragun/account/profile").header("Authorization", "Bearer " + newToken))
            .andExpect(status().isOk());
    }
}
