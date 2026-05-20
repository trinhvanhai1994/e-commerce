package com.dragun.ecommerce.integration.pancake.mapper;

import com.dragun.ecommerce.integration.pancake.PancakeIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderDto;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.model.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private static final DateTimeFormatter ISO_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * POS line title when present; otherwise catalog {@link Product#getName()} (MeInvoice requires a non-empty name).
     */
    static String resolveOrderLineProductName(String pancakeLineName, Product product) {
        if (pancakeLineName != null && !pancakeLineName.isBlank()) {
            return pancakeLineName.trim();
        }
        if (product != null && product.getName() != null && !product.getName().isBlank()) {
            return product.getName().trim();
        }
        return "";
    }

    /**
     * Map Pancake Order status to Thi Yen OrderStatus enum
     */
    private OrderStatus mapPancakeStatusToThiYen(String pancakeStatus) {
        if (pancakeStatus == null || pancakeStatus.isBlank()) {
            return OrderStatus.ORDER_STATUS_PENDING;
        }

        String normalized = pancakeStatus.trim().toLowerCase();
        return switch (normalized) {
            case "new", "pending", "order_status_pending" -> OrderStatus.ORDER_STATUS_PENDING;
            case "confirmed", "order_status_confirmed" -> OrderStatus.ORDER_STATUS_CONFIRMED;
            case "shipping", "delivering", "order_status_shipping" -> OrderStatus.ORDER_STATUS_SHIPPING;
            case "delivered", "done", "completed", "order_status_delivered" -> OrderStatus.ORDER_STATUS_DELIVERED;
            case "cancelled", "canceled", "order_status_cancelled" -> OrderStatus.ORDER_STATUS_CANCELLED;
            default -> OrderStatus.ORDER_STATUS_PENDING;
        };
    }

    public boolean hasCustomerPayload(PancakeOrderDto pancakeOrder) {
        if (pancakeOrder == null) {
            return false;
        }
        return isNotBlank(pancakeOrder.getCustomerName())
                || isNotBlank(pancakeOrder.getCustomerPhone())
                || isNotBlank(pancakeOrder.getAddress())
                || (pancakeOrder.getCustomer() != null
                && (isNotBlank(pancakeOrder.getCustomer().getName())
                || isNotBlank(pancakeOrder.getCustomer().getPhone())
                || isNotBlank(pancakeOrder.getCustomer().getAddress())));
    }

    /**
     * Prefer detail payload; keep list summary fields when detail omits them.
     */
    public PancakeOrderDto mergeOrderDetail(PancakeOrderDto summary, PancakeOrderDto detail) {
        if (summary == null) {
            return detail;
        }
        if (detail == null) {
            return summary;
        }
        if (!isNotBlank(detail.getOrderNumber()) && isNotBlank(summary.getOrderNumber())) {
            detail.setOrderNumber(summary.getOrderNumber());
        }
        if (detail.getItems() == null || detail.getItems().isEmpty()) {
            detail.setItems(summary.getItems());
        }
        if (!hasCustomerPayload(detail) && hasCustomerPayload(summary)) {
            detail.setCustomerName(summary.getCustomerName());
            detail.setCustomerPhone(summary.getCustomerPhone());
            detail.setAddress(summary.getAddress());
            detail.setProvince(summary.getProvince());
            detail.setDistrict(summary.getDistrict());
            detail.setWard(summary.getWard());
            detail.setCustomer(summary.getCustomer());
        }
        return detail;
    }

    /**
     * Map Thi Yen OrderStatus to Pancake status string
     */
    private String mapThiYenStatusToPancake(OrderStatus thiYenStatus) {
        if (thiYenStatus == null) {
            return "pending";
        }

        return switch (thiYenStatus) {
            case ORDER_STATUS_PENDING -> "pending";
            case ORDER_STATUS_CONFIRMED -> "confirmed";
            case ORDER_STATUS_SHIPPING -> "shipping";
            case ORDER_STATUS_DELIVERED -> "delivered";
            case ORDER_STATUS_CANCELLED -> "cancelled";
        };
    }

    /**
     * Pancake timestamps may be ISO-8601 with offset, zoned, or local.
     */
    public LocalDateTime parsePancakeDateTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalDateTime.now();
        }
        String s = raw.trim();
        try {
            return OffsetDateTime.parse(s).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return ZonedDateTime.parse(s).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(s, ISO_LOCAL);
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDate.parse(s).atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        return LocalDateTime.now();
    }

    /**
     * Map Pancake Order to Thi Yen Order
     */
    public Order toThiYenOrder(PancakeOrderDto pancakeOrder) {
        if (pancakeOrder == null) {
            return null;
        }

        Order order = new Order();
        order.setOrderId(pancakeOrder.getOrderNumber() != null && !pancakeOrder.getOrderNumber().isBlank()
                ? pancakeOrder.getOrderNumber()
                : pancakeOrder.getId());
        order.setPancakeOrderId(pancakeOrder.getId());
        order.setStatus(mapPancakeStatusToThiYen(pancakeOrder.getStatus()));
        order.setPaymentMethod(pancakeOrder.getPaymentMethod());
        order.setSubTotal(pancakeOrder.getSubtotal() != null ? pancakeOrder.getSubtotal() : BigDecimal.ZERO);
        order.setShippingFee(pancakeOrder.getShippingFee() != null ? pancakeOrder.getShippingFee() : BigDecimal.ZERO);
        order.setTotal(pancakeOrder.getTotal() != null ? pancakeOrder.getTotal() : BigDecimal.ZERO);
        order.setOrderType(PancakeIntegrationConstants.ORDER_TYPE_PANCAKE);
        applyCustomerFields(order, pancakeOrder);

        if (pancakeOrder.getCreatedAt() != null) {
            order.setCreatedAt(parsePancakeDateTime(pancakeOrder.getCreatedAt()));
        } else {
            order.setCreatedAt(LocalDateTime.now());
        }

        order.setUpdatedAt(LocalDateTime.now());
        order.setPancakeSyncedAt(LocalDateTime.now());

        return order;
    }

    /**
     * Map Thi Yen Order to Pancake Order
     */
    public PancakeOrderDto toPancakeOrder(Order thiYenOrder) {
        if (thiYenOrder == null) {
            return null;
        }

        PancakeOrderDto pancakeOrder = new PancakeOrderDto();
        pancakeOrder.setId(thiYenOrder.getPancakeOrderId());
        pancakeOrder.setOrderNumber(thiYenOrder.getOrderId());
        pancakeOrder.setStatus(mapThiYenStatusToPancake(thiYenOrder.getStatus()));
        pancakeOrder.setPaymentMethod(thiYenOrder.getPaymentMethod());
        pancakeOrder.setSubtotal(thiYenOrder.getSubTotal());
        pancakeOrder.setShippingFee(thiYenOrder.getShippingFee() != null ? thiYenOrder.getShippingFee() : BigDecimal.ZERO);
        pancakeOrder.setTotal(thiYenOrder.getTotal());

        PancakeOrderDto.PancakeCustomer customer = new PancakeOrderDto.PancakeCustomer();
        customer.setName(thiYenOrder.getCustomerName());
        customer.setPhone(thiYenOrder.getCustomerPhone());
        customer.setAddress(thiYenOrder.getCustomerAddress());
        customer.setProvince(thiYenOrder.getProvinceCode());
        customer.setDistrict(thiYenOrder.getDistrictCode());
        customer.setWard(thiYenOrder.getWardCode());
        pancakeOrder.setCustomer(customer);

        if (thiYenOrder.getItems() != null) {
            List<PancakeOrderDto.PancakeOrderItem> pancakeItems = thiYenOrder.getItems().stream()
                    .map(item -> {
                        PancakeOrderDto.PancakeOrderItem pancakeItem = new PancakeOrderDto.PancakeOrderItem();
                        pancakeItem.setProductId(item.getProduct() != null && item.getProduct().getPancakeProductId() != null
                                ? item.getProduct().getPancakeProductId()
                                : "");
                        pancakeItem.setProductName(item.getProductName());
                        pancakeItem.setQuantity(item.getQuantity());
                        pancakeItem.setPrice(item.getPrice());
                        return pancakeItem;
                    })
                    .collect(Collectors.toList());
            pancakeOrder.setItems(pancakeItems);
        }

        return pancakeOrder;
    }

    /**
     * Replace all order lines from a Pancake payload. Caller should only invoke when {@code pancakeItems} is non-empty
     * to avoid wiping lines on partial API responses.
     */
    public void replaceOrderItemsFromPancake(
            Order order,
            List<PancakeOrderDto.PancakeOrderItem> pancakeItems,
            Function<PancakeOrderDto.PancakeOrderItem, Product> lineProductResolver,
            Product unmappedLineProduct) {
        if (order == null || unmappedLineProduct == null) {
            throw new IllegalArgumentException("order and unmappedLineProduct are required");
        }
        if (pancakeItems == null || pancakeItems.isEmpty()) {
            return;
        }
        if (order.getItems() == null) {
            order.setItems(new ArrayList<>());
        } else {
            order.getItems().clear();
        }
        order.getItems().addAll(createOrderItemsFromPancake(order, pancakeItems, lineProductResolver, unmappedLineProduct));
    }

    /**
     * Build {@link OrderItem} rows from Pancake lines. Every line must resolve to a non-null {@link Product}
     * (FK {@code order_items.product_id} is NOT NULL) — use {@code unmappedLineProduct} when POS SKU is unknown locally.
     */
    public List<OrderItem> createOrderItemsFromPancake(
            Order order,
            List<PancakeOrderDto.PancakeOrderItem> pancakeItems,
            Function<PancakeOrderDto.PancakeOrderItem, Product> lineProductResolver,
            Product unmappedLineProduct) {

        if (pancakeItems == null || pancakeItems.isEmpty()) {
            return List.of();
        }
        if (unmappedLineProduct == null) {
            throw new IllegalArgumentException("unmappedLineProduct is required (Flyway V17 placeholder)");
        }

        return pancakeItems.stream()
                .map(pancakeItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);

                    Product product = lineProductResolver != null ? lineProductResolver.apply(pancakeItem) : null;
                    if (product == null) {
                        product = unmappedLineProduct;
                    }

                    orderItem.setProduct(product);
                    orderItem.setProductName(resolveOrderLineProductName(pancakeItem.getProductName(), product));
                    orderItem.setQuantity(pancakeItem.getQuantity() != null ? pancakeItem.getQuantity() : 0);
                    orderItem.setPrice(pancakeItem.getPrice() != null ? pancakeItem.getPrice() : BigDecimal.ZERO);

                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    /**
     * Update Thi Yen Order with Pancake Order data (header fields only; lines handled by {@link #replaceOrderItemsFromPancake}).
     */
    public void updateThiYenOrder(Order existingOrder, PancakeOrderDto pancakeOrder) {
        if (pancakeOrder == null || existingOrder == null) {
            return;
        }

        if (pancakeOrder.getId() != null && existingOrder.getPancakeOrderId() == null) {
            existingOrder.setPancakeOrderId(pancakeOrder.getId());
        }

        existingOrder.setStatus(mapPancakeStatusToThiYen(pancakeOrder.getStatus()));
        existingOrder.setPaymentMethod(pancakeOrder.getPaymentMethod());
        existingOrder.setSubTotal(pancakeOrder.getSubtotal() != null ? pancakeOrder.getSubtotal() : BigDecimal.ZERO);
        existingOrder.setShippingFee(pancakeOrder.getShippingFee() != null ? pancakeOrder.getShippingFee() : BigDecimal.ZERO);
        existingOrder.setTotal(pancakeOrder.getTotal() != null ? pancakeOrder.getTotal() : BigDecimal.ZERO);

        applyCustomerFields(existingOrder, pancakeOrder);

        if (pancakeOrder.getCreatedAt() != null) {
            existingOrder.setCreatedAt(parsePancakeDateTime(pancakeOrder.getCreatedAt()));
        }

        existingOrder.setUpdatedAt(LocalDateTime.now());
        existingOrder.setPancakeSyncedAt(LocalDateTime.now());
    }

    private void applyCustomerFields(Order order, PancakeOrderDto pancakeOrder) {
        String name = firstNonBlank(
                pancakeOrder.getCustomerName(),
                pancakeOrder.getCustomer() != null ? pancakeOrder.getCustomer().getName() : null);
        String phone = firstNonBlank(
                pancakeOrder.getCustomerPhone(),
                pancakeOrder.getCustomer() != null ? pancakeOrder.getCustomer().getPhone() : null);
        String address = firstNonBlank(
                buildAddressFromFlat(pancakeOrder),
                pancakeOrder.getCustomer() != null ? buildAddressFromCustomer(pancakeOrder.getCustomer()) : null);

        String province = firstNonBlank(
                pancakeOrder.getProvince(),
                pancakeOrder.getCustomer() != null ? pancakeOrder.getCustomer().getProvince() : null);
        String district = firstNonBlank(
                pancakeOrder.getDistrict(),
                pancakeOrder.getCustomer() != null ? pancakeOrder.getCustomer().getDistrict() : null);
        String ward = firstNonBlank(
                pancakeOrder.getWard(),
                pancakeOrder.getCustomer() != null ? pancakeOrder.getCustomer().getWard() : null);

        String orderRef = firstNonBlank(pancakeOrder.getOrderNumber(), pancakeOrder.getId(), "?");
        order.setCustomerName(isNotBlank(name) ? name.trim()
                : String.format(Locale.ROOT, MeinvoiceIntegrationConstants.DEFAULT_POS_CUSTOMER_NAME_FORMAT, orderRef));
        order.setCustomerPhone(isNotBlank(phone) ? phone.trim() : MeinvoiceIntegrationConstants.DEFAULT_MISSING_PHONE_OR_ADDRESS);
        order.setCustomerAddress(isNotBlank(address) ? address.trim() : MeinvoiceIntegrationConstants.DEFAULT_MISSING_PHONE_OR_ADDRESS);
        order.setProvinceCode(province);
        order.setDistrictCode(district);
        order.setWardCode(ward);
    }

    private static String buildAddressFromFlat(PancakeOrderDto pancakeOrder) {
        if (isNotBlank(pancakeOrder.getAddress())) {
            return appendLocationParts(pancakeOrder.getAddress(), pancakeOrder.getWard(),
                    pancakeOrder.getDistrict(), pancakeOrder.getProvince());
        }
        return appendLocationParts(null, pancakeOrder.getWard(), pancakeOrder.getDistrict(), pancakeOrder.getProvince());
    }

    private static String buildAddressFromCustomer(PancakeOrderDto.PancakeCustomer customer) {
        return appendLocationParts(customer.getAddress(), customer.getWard(), customer.getDistrict(), customer.getProvince());
    }

    private static String appendLocationParts(String base, String ward, String district, String province) {
        StringBuilder address = new StringBuilder();
        if (isNotBlank(base)) {
            address.append(base.trim());
        }
        appendPart(address, ward);
        appendPart(address, district);
        appendPart(address, province);
        return address.toString();
    }

    private static void appendPart(StringBuilder address, String part) {
        if (!isNotBlank(part)) {
            return;
        }
        if (address.length() > 0) {
            address.append(", ");
        }
        address.append(part.trim());
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
