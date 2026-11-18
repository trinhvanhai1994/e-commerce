package com.dragun.ecommerce.integration.mail;

import com.dragun.ecommerce.TestCustomers;
import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.model.dto.request.CreateOrderRequest;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import com.dragun.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Checkout email end to end (task-005): the request field is stored, and mails leave only after the order commits.
 * Not @Transactional: the after-commit listener needs a real commit, so created orders are removed in tearDown.
 */
@SpringBootTest(properties = "app.mail.admin-recipients=admin@shop.test")
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@DisplayName("Order placed mails (HTTP + after commit)")
class OrderPlacedMailFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockBean
    private MailSender mailSender;

    private Product product;
    private int stockBefore;
    private final List<String> createdOrderIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        product = productRepository.findAll().stream().filter(p -> p.getStock() != null && p.getStock() > 0)
            .findFirst().orElseThrow();
        stockBefore = product.getStock();
    }

    @AfterEach
    void tearDown() {
        createdOrderIds.forEach(id -> orderRepository.findByOrderId(id).ifPresent(orderRepository::delete));
        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        reloaded.setStock(stockBefore);
        productRepository.save(reloaded);
    }

    private String body(String email) throws Exception {
        Map<String, Object> customer = new HashMap<>(Map.of(
            "name", "Trần Thị Bình", "phone", TestCustomers.randomPhone(), "address", "1 Lê Lợi"));
        if (email != null) {
            customer.put("email", email);
        }
        return json.writeValueAsString(Map.of(
            "customerInfo", customer,
            "items", new Object[]{Map.of("productId", product.getId(), "quantity", 1)}));
    }

    private String placeOrder(String email) throws Exception {
        String response = mvc.perform(post("/api/extend/orders").contentType(MediaType.APPLICATION_JSON)
                .content(body(email)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        String orderId = json.readTree(response).path("data").path("orderId").asText();
        createdOrderIds.add(orderId);
        return orderId;
    }

    @Test
    @DisplayName("email at checkout is stored trimmed, and both admin and customer mails are sent after commit")
    void withEmail_storedAndMailed() throws Exception {
        String orderId = placeOrder("  khach@example.com ");

        Order saved = orderRepository.findByOrderId(orderId).orElseThrow();
        assertThat(saved.getCustomerEmail()).isEqualTo("khach@example.com");

        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSender, timeout(5000).times(2)).send(captor.capture());
        assertThat(captor.getAllValues()).extracting(m -> m.to().get(0))
            .containsExactlyInAnyOrder("admin@shop.test", "khach@example.com");
        assertThat(captor.getAllValues()).allSatisfy(m -> assertThat(m.html()).contains(orderId));
    }

    @Test
    @DisplayName("no email: the order is created as before and only the admin is mailed")
    void withoutEmail_adminOnly() throws Exception {
        String orderId = placeOrder(null);

        assertThat(orderRepository.findByOrderId(orderId).orElseThrow().getCustomerEmail()).isNull();
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailSender, timeout(5000)).send(captor.capture());
        verify(mailSender, after(500).times(1)).send(any());
        assertThat(captor.getValue().to()).containsExactly("admin@shop.test");
    }

    @Test
    @DisplayName("a blank email is treated as none")
    void blankEmail_isNull() throws Exception {
        String orderId = placeOrder("   ");

        assertThat(orderRepository.findByOrderId(orderId).orElseThrow().getCustomerEmail()).isNull();
        // Wait for the async admin mail so it cannot land in the next test's mock
        verify(mailSender, timeout(5000)).send(any());
    }

    @Test
    @DisplayName("an invalid email is rejected with 400 and nothing is mailed")
    void invalidEmail_400() throws Exception {
        mvc.perform(post("/api/extend/orders").contentType(MediaType.APPLICATION_JSON)
                .content(body("khong-phai-email")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        verify(mailSender, after(500).never()).send(any());
    }

    @Test
    @DisplayName("an order whose transaction rolls back sends no mail")
    void rolledBack_noMail() throws Exception {
        CreateOrderRequest request = json.readValue(body("khach@example.com"), CreateOrderRequest.class);

        transactionTemplate.executeWithoutResult(tx -> {
            orderService.createOrder(request, null);
            tx.setRollbackOnly();
        });

        verify(mailSender, after(1000).never()).send(any());
    }
}
