package com.dragun.ecommerce.integration.mail;

import com.dragun.ecommerce.model.entity.District;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Province;
import com.dragun.ecommerce.model.entity.Ward;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.service.LocationService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Mails for a newly placed order (task-005): one to the admins, one to the customer when the order has an email.
 * Runs after commit on a worker thread (OrderMailListener); failures are logged and never reach the order.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMailService {

    static final String ADMIN_TEMPLATE = "order-admin";
    static final String CUSTOMER_TEMPLATE = "order-customer";

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrderRepository orderRepository;
    private final LocationService locationService;
    private final MailTemplateRenderer renderer;
    private final MailSender mailSender;
    private final MailProperties properties;

    @Transactional(readOnly = true)
    public void sendOrderPlacedMails(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            log.warn("Order mail skipped: order id={} not found", orderId);
            return;
        }
        OrderMail mail = toMail(order);

        List<String> admins = properties.adminRecipientList();
        if (admins.isEmpty()) {
            log.debug("Admin order mail skipped: app.mail.admin-recipients is empty");
        } else {
            send(admins, "[Đơn mới] #" + mail.getOrderId() + " - " + mail.getCustomerName(),
                ADMIN_TEMPLATE, mail, adminText(mail));
        }

        if (StringUtils.hasText(mail.getCustomerEmail())) {
            send(List.of(mail.getCustomerEmail()), mail.getShopName() + " xác nhận đơn hàng #" + mail.getOrderId(),
                CUSTOMER_TEMPLATE, mail, customerText(mail));
        }
    }

    private void send(List<String> to, String subject, String template, OrderMail mail, String text) {
        try {
            String html = renderer.render(template, Map.of("mail", mail));
            mailSender.send(new MailMessage(to, subject, html, text));
        } catch (RuntimeException e) {
            log.error("Order mail '{}' for order {} failed: {}", template, mail.getOrderId(), e.getMessage(), e);
        }
    }

    OrderMail toMail(Order order) {
        List<Line> lines = new ArrayList<>();
        for (OrderItem item : order.getItems() == null ? List.<OrderItem>of() : order.getItems()) {
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            lines.add(Line.builder()
                .name(item.getProductName())
                .quantity(quantity)
                .unitPrice(vnd(price))
                .lineTotal(vnd(price.multiply(BigDecimal.valueOf(quantity))))
                .build());
        }
        String storeUrl = trimTrailingSlash(properties.getStoreUrl());
        return OrderMail.builder()
            .shopName(properties.getFromName())
            .orderId(order.getOrderId())
            .orderDate(order.getCreatedAt() != null ? order.getCreatedAt().format(DATE_TIME) : "")
            .customerName(order.getCustomerName())
            .customerPhone(order.getCustomerPhone())
            .customerEmail(order.getCustomerEmail())
            .shippingAddress(fullAddress(order))
            .paymentMethod(paymentLabel(order.getPaymentMethod()))
            .lines(lines)
            .subTotal(vnd(order.getSubTotal()))
            .shippingFee(vnd(order.getShippingFee()))
            .total(vnd(order.getTotal()))
            .storeUrl(storeUrl)
            .adminOrdersUrl(storeUrl != null ? storeUrl + "/admin/orders" : null)
            .supportPhone(StringUtils.hasText(properties.getSupportPhone()) ? properties.getSupportPhone().trim() : null)
            .build();
    }

    /** Address line plus ward, district, province names; the order only stores their codes. */
    private String fullAddress(Order order) {
        Ward ward = order.getWardCode() != null ? locationService.getWardByCode(order.getWardCode()) : null;
        District district = order.getDistrictCode() != null
            ? locationService.getDistrictByCode(order.getDistrictCode()) : null;
        Province province = order.getProvinceCode() != null
            ? locationService.getProvinceByCode(order.getProvinceCode()) : null;
        return String.join(", ", Stream.of(
                order.getCustomerAddress(),
                ward != null ? ward.getNameWithType() : null,
                district != null ? district.getNameWithType() : null,
                province != null ? province.getNameWithType() : null)
            .filter(StringUtils::hasText)
            .toList());
    }

    private static String paymentLabel(String method) {
        if (method == null || "COD".equalsIgnoreCase(method)) {
            return "Thanh toán khi nhận hàng (COD)";
        }
        return method;
    }

    static String vnd(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        return format.format(amount != null ? amount : BigDecimal.ZERO) + "đ";
    }

    private static String trimTrailingSlash(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String adminText(OrderMail m) {
        StringBuilder sb = new StringBuilder();
        sb.append("Có đơn hàng mới #").append(m.getOrderId()).append(" lúc ").append(m.getOrderDate()).append("\n\n");
        appendCustomer(sb, m);
        appendLines(sb, m);
        if (m.getAdminOrdersUrl() != null) {
            sb.append("\nXem đơn: ").append(m.getAdminOrdersUrl()).append("\n");
        }
        return sb.toString();
    }

    private static String customerText(OrderMail m) {
        StringBuilder sb = new StringBuilder();
        sb.append("Xin chào ").append(m.getCustomerName()).append(",\n\n");
        sb.append("Cảm ơn bạn đã đặt hàng tại ").append(m.getShopName()).append(".\n");
        sb.append("Mã đơn: #").append(m.getOrderId()).append("\n");
        sb.append("Ngày đặt: ").append(m.getOrderDate()).append("\n\n");
        appendCustomer(sb, m);
        appendLines(sb, m);
        if (m.getSupportPhone() != null) {
            sb.append("\nCần hỗ trợ, vui lòng gọi ").append(m.getSupportPhone()).append(".\n");
        }
        if (m.getStoreUrl() != null) {
            sb.append(m.getStoreUrl()).append("\n");
        }
        return sb.toString();
    }

    private static void appendCustomer(StringBuilder sb, OrderMail m) {
        sb.append("Người nhận: ").append(m.getCustomerName()).append("\n");
        sb.append("Số điện thoại: ").append(m.getCustomerPhone()).append("\n");
        if (m.getCustomerEmail() != null) {
            sb.append("Email: ").append(m.getCustomerEmail()).append("\n");
        }
        sb.append("Địa chỉ: ").append(m.getShippingAddress()).append("\n");
        sb.append("Thanh toán: ").append(m.getPaymentMethod()).append("\n\n");
    }

    private static void appendLines(StringBuilder sb, OrderMail m) {
        sb.append("Sản phẩm:\n");
        for (Line line : m.getLines()) {
            sb.append("- ").append(line.getName()).append(" x").append(line.getQuantity())
                .append(": ").append(line.getLineTotal()).append("\n");
        }
        sb.append("\nTạm tính: ").append(m.getSubTotal()).append("\n");
        sb.append("Phí vận chuyển: ").append(m.getShippingFee()).append("\n");
        sb.append("Tổng cộng: ").append(m.getTotal()).append("\n");
    }

    /** Template model: getters only, so Thymeleaf's expression language can read it. */
    @Value
    @Builder
    public static class OrderMail {
        String shopName;
        String orderId;
        String orderDate;
        String customerName;
        String customerPhone;
        String customerEmail;
        String shippingAddress;
        String paymentMethod;
        List<Line> lines;
        String subTotal;
        String shippingFee;
        String total;
        String storeUrl;
        String adminOrdersUrl;
        String supportPhone;
    }

    @Value
    @Builder
    public static class Line {
        String name;
        int quantity;
        String unitPrice;
        String lineTotal;
    }
}
