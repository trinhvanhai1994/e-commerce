package com.dragun.ecommerce.integration.mail;

import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Province;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderMailService")
class OrderMailServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private LocationService locationService;

    private final List<MailMessage> sent = new ArrayList<>();
    private MailProperties properties;
    private OrderMailService service;

    @BeforeEach
    void setUp() {
        properties = new MailProperties();
        properties.setAdminRecipients(List.of("admin@shop.test", " "));
        properties.setStoreUrl("https://shop.test/");
        MailSender capture = message -> sent.add(message);
        service = new OrderMailService(orderRepository, locationService, new MailTemplateRenderer(), capture,
            properties);
        lenient().when(locationService.getProvinceByCode(any())).thenReturn(null);
    }

    private Order order(String email) {
        Order order = Order.builder()
            .id(7L)
            .orderId("2026111800001")
            .customerName("Trần <b>Bình</b>")
            .customerPhone("0912345678")
            .customerEmail(email)
            .customerAddress("1 Lê Lợi")
            .provinceCode("79")
            .paymentMethod("COD")
            .subTotal(new BigDecimal("240000"))
            .shippingFee(new BigDecimal("20000"))
            .total(new BigDecimal("260000"))
            .createdAt(LocalDateTime.of(2026, 11, 18, 9, 5))
            .build();
        OrderItem item = new OrderItem();
        item.setProductName("Chè mè đen");
        item.setPrice(new BigDecimal("120000"));
        item.setQuantity(2);
        order.setItems(List.of(item));
        return order;
    }

    @Test
    @DisplayName("order with email: one mail to admins, one to the customer, with the order details")
    void withEmail_sendsAdminAndCustomer() {
        Province province = new Province();
        province.setNameWithType("Thành phố Hồ Chí Minh");
        when(locationService.getProvinceByCode("79")).thenReturn(province);
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order("khach@example.com")));

        service.sendOrderPlacedMails(7L);

        assertThat(sent).hasSize(2);
        MailMessage admin = sent.get(0);
        assertThat(admin.to()).containsExactly("admin@shop.test");
        assertThat(admin.subject()).isEqualTo("[Đơn mới] #2026111800001 - Trần <b>Bình</b>");
        assertThat(admin.html()).contains("2026111800001", "0912345678", "khach@example.com",
            "1 Lê Lợi, Thành phố Hồ Chí Minh", "Chè mè đen", "240.000đ", "260.000đ",
            "https://shop.test/admin/orders");

        MailMessage customer = sent.get(1);
        assertThat(customer.to()).containsExactly("khach@example.com");
        assertThat(customer.subject()).isEqualTo("Thi Yên xác nhận đơn hàng #2026111800001");
        assertThat(customer.html()).contains("18/11/2026 09:05", "120.000đ", "Thanh toán khi nhận hàng (COD)")
            .doesNotContain("/admin/orders");
        assertThat(customer.text()).contains("Mã đơn: #2026111800001", "Tổng cộng: 260.000đ")
            .doesNotContain("<table", "<td");
    }

    @Test
    @DisplayName("customer-typed text is HTML-escaped in the templates")
    void escapesHtml() {
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order("khach@example.com")));

        service.sendOrderPlacedMails(7L);

        assertThat(sent).allSatisfy(m -> assertThat(m.html())
            .contains("Trần &lt;b&gt;Bình&lt;/b&gt;")
            .doesNotContain("<b>Bình</b>"));
    }

    @Test
    @DisplayName("order without email: only the admin mail")
    void withoutEmail_adminOnly() {
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order(null)));

        service.sendOrderPlacedMails(7L);

        assertThat(sent).singleElement().satisfies(m -> assertThat(m.to()).containsExactly("admin@shop.test"));
    }

    @Test
    @DisplayName("no admin recipients configured: only the customer mail")
    void noAdmins_customerOnly() {
        properties.setAdminRecipients(List.of());
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order("khach@example.com")));

        service.sendOrderPlacedMails(7L);

        assertThat(sent).singleElement().satisfies(m -> assertThat(m.to()).containsExactly("khach@example.com"));
    }

    @Test
    @DisplayName("order not found: nothing is sent")
    void orderMissing_nothingSent() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        service.sendOrderPlacedMails(99L);

        assertThat(sent).isEmpty();
    }

    @Test
    @DisplayName("formats money in VND with dot grouping")
    void vnd() {
        assertThat(OrderMailService.vnd(new BigDecimal("1234567.00"))).isEqualTo("1.234.567đ");
        assertThat(OrderMailService.vnd(null)).isEqualTo("0đ");
    }
}
