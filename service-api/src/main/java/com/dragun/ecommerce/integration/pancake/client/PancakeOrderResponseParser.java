package com.dragun.ecommerce.integration.pancake.client;

import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderDto;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Resilient parser for Pancake order JSON (geo/payment/status fields are often nested objects, not strings).
 */
final class PancakeOrderResponseParser {

    private PancakeOrderResponseParser() {
    }

    static PancakeOrderDto parseOrderDetail(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        if (root.has("data")) {
            JsonNode data = root.get("data");
            if (data.isObject()) {
                return parseOrder(data);
            }
            if (data.isArray() && !data.isEmpty()) {
                return parseOrder(data.get(0));
            }
        }
        if (root.isObject() && (root.has("id") || root.has("order_id"))) {
            return parseOrder(root);
        }
        List<PancakeOrderDto> orders = parseOrderList(root);
        return orders.isEmpty() ? null : orders.get(0);
    }

    static List<PancakeOrderDto> parseOrderList(JsonNode root) {
        if (root == null || root.isNull()) {
            return List.of();
        }
        JsonNode array = locateOrderArray(root);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<PancakeOrderDto> orders = new ArrayList<>();
        for (JsonNode node : array) {
            PancakeOrderDto order = parseOrder(node);
            if (order != null && order.getId() != null && !order.getId().isBlank()) {
                orders.add(order);
            }
        }
        return orders;
    }

    static PancakeOrderDto parseOrder(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        PancakeOrderDto dto = new PancakeOrderDto();
        dto.setId(textValue(firstPresent(node, "id", "order_id")));
        dto.setOrderNumber(textValue(firstPresent(node, "order_number", "code", "order_code")));
        dto.setCustomerName(textValue(firstPresent(node, "customer_name", "bill_full_name", "buyer_name", "full_name")));
        dto.setCustomerPhone(textValue(firstPresent(node, "customer_phone", "phone", "buyer_phone", "bill_phone_number")));
        dto.setAddress(textValue(firstPresent(node, "address", "shipping_address", "customer_address", "full_address")));
        dto.setProvince(textValue(firstPresent(node, "province", "province_name")));
        dto.setDistrict(textValue(firstPresent(node, "district", "district_name")));
        dto.setWard(textValue(firstPresent(node, "ward", "commune", "ward_name")));
        dto.setStatus(textValue(firstPresent(node, "status", "order_status", "state")));
        dto.setPaymentMethod(textValue(firstPresent(node, "payment_method", "payment_method_name", "payment_type")));
        dto.setSubtotal(decimalValue(firstPresent(node, "subtotal", "sub_total", "subtotal_amount")));
        dto.setShippingFee(decimalValue(firstPresent(node, "shipping_fee", "ship_fee", "delivery_fee")));
        dto.setTotal(decimalValue(firstPresent(node, "total", "total_amount", "order_total")));
        dto.setCreatedAt(textValue(firstPresent(node, "created_at", "inserted_at", "created_time")));
        dto.setUpdatedAt(textValue(firstPresent(node, "updated_at", "updated_time")));
        dto.setItems(parseItems(firstPresent(node, "items", "order_items", "line_items", "products")));

        JsonNode customerNode = node.get("customer");
        if (customerNode != null && customerNode.isObject()) {
            PancakeOrderDto.PancakeCustomer customer = new PancakeOrderDto.PancakeCustomer();
            customer.setName(textValue(firstPresent(customerNode, "name", "customer_name", "full_name")));
            customer.setPhone(textValue(firstPresent(customerNode, "phone", "customer_phone", "phone_number")));
            customer.setAddress(textValue(firstPresent(customerNode, "address", "full_address", "shipping_address")));
            customer.setProvince(textValue(firstPresent(customerNode, "province", "province_name")));
            customer.setDistrict(textValue(firstPresent(customerNode, "district", "district_name")));
            customer.setWard(textValue(firstPresent(customerNode, "ward", "commune", "ward_name")));
            dto.setCustomer(customer);
        }

        if (!hasText(dto.getCustomerName()) && dto.getCustomer() != null) {
            dto.setCustomerName(dto.getCustomer().getName());
        }
        if (!hasText(dto.getCustomerPhone()) && dto.getCustomer() != null) {
            dto.setCustomerPhone(dto.getCustomer().getPhone());
        }
        if (!hasText(dto.getAddress()) && dto.getCustomer() != null) {
            dto.setAddress(dto.getCustomer().getAddress());
        }

        return dto;
    }

    private static List<PancakeOrderDto.PancakeOrderItem> parseItems(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return List.of();
        }
        List<PancakeOrderDto.PancakeOrderItem> items = new ArrayList<>();
        for (JsonNode itemNode : itemsNode) {
            PancakeOrderDto.PancakeOrderItem item = new PancakeOrderDto.PancakeOrderItem();
            item.setProductId(textValue(firstPresent(itemNode, "product_id", "id")));
            item.setVariationId(textValue(firstPresent(itemNode, "variation_id", "variant_id")));
            item.setProductName(textValue(firstPresent(itemNode, "product_name", "name", "title")));
            item.setQuantity(intValue(firstPresent(itemNode, "quantity", "qty", "amount")));
            item.setPrice(decimalValue(firstPresent(itemNode, "price", "unit_price", "retail_price")));
            items.add(item);
        }
        return items;
    }

    private static JsonNode locateOrderArray(JsonNode root) {
        if (root.isArray()) {
            return root;
        }
        if (root.has("data")) {
            JsonNode data = root.get("data");
            if (data.isArray()) {
                return data;
            }
            if (data.has("orders") && data.get("orders").isArray()) {
                return data.get("orders");
            }
            if (data.has("items") && data.get("items").isArray()) {
                return data.get("items");
            }
        }
        if (root.has("orders") && root.get("orders").isArray()) {
            return root.get("orders");
        }
        Iterator<String> names = root.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            JsonNode child = root.get(name);
            if (child != null && child.isArray() && !child.isEmpty()) {
                JsonNode first = child.get(0);
                if (first.has("id") || first.has("order_id") || first.has("customer_name")) {
                    return child;
                }
            }
        }
        return null;
    }

    private static JsonNode firstPresent(JsonNode node, String... fieldNames) {
        if (node == null) {
            return null;
        }
        for (String field : fieldNames) {
            if (node.has(field) && !node.get(field).isNull()) {
                return node.get(field);
            }
        }
        return null;
    }

    private static String textValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return node.asText();
        }
        if (node.isObject()) {
            return textValue(firstPresent(node,
                    "name", "title", "label", "code", "id",
                    "full_name", "customer_name", "phone", "address",
                    "status_name", "value", "text"));
        }
        return null;
    }

    private static BigDecimal decimalValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            try {
                return new BigDecimal(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (node.isObject()) {
            return decimalValue(firstPresent(node, "amount", "value", "price", "total"));
        }
        return null;
    }

    private static Integer intValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isInt() || node.isLong()) {
            return node.asInt();
        }
        if (node.isTextual()) {
            try {
                return Integer.parseInt(node.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (node.isObject()) {
            return intValue(firstPresent(node, "quantity", "qty", "amount", "value"));
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
