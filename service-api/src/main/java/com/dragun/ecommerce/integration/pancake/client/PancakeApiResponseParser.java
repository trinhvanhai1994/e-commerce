package com.dragun.ecommerce.integration.pancake.client;

import com.dragun.ecommerce.integration.pancake.dto.PancakeProductDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class PancakeApiResponseParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PancakeApiResponseParser() {
    }

    static List<PancakeProductDto> parseProductList(JsonNode root) {
        if (root == null || root.isNull()) {
            return List.of();
        }
        JsonNode array = locateProductArray(root);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<PancakeProductDto> products = new ArrayList<>();
        for (JsonNode node : array) {
            PancakeProductDto dto = MAPPER.convertValue(node, PancakeProductDto.class);
            if (dto != null && dto.getId() != null) {
                products.add(dto);
            }
        }
        return products;
    }

    private static JsonNode locateProductArray(JsonNode root) {
        if (root.isArray()) {
            return root;
        }
        if (root.has("data")) {
            JsonNode data = root.get("data");
            if (data.isArray()) {
                return data;
            }
            if (data.has("products") && data.get("products").isArray()) {
                return data.get("products");
            }
            if (data.has("items") && data.get("items").isArray()) {
                return data.get("items");
            }
        }
        if (root.has("products") && root.get("products").isArray()) {
            return root.get("products");
        }
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            JsonNode child = root.get(name);
            if (child != null && child.isArray() && !child.isEmpty() && child.get(0).has("id")) {
                return child;
            }
        }
        return null;
    }
}
