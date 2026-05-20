package com.dragun.ecommerce.integration.pancake.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PancakeOrderDto {
    private String id;

    @JsonAlias({"order_number", "code", "order_code"})
    private String orderNumber;

  /** Nested customer (legacy / alternate API shape). */
    private PancakeCustomer customer;

    /** Flat fields used by Pancake POS Open API list/detail payloads. */
    @JsonProperty("customer_name")
    @JsonAlias({"bill_full_name", "buyer_name", "full_name"})
    private String customerName;

    @JsonProperty("customer_phone")
    @JsonAlias({"phone", "buyer_phone", "bill_phone_number"})
    private String customerPhone;

    @JsonAlias({"shipping_address", "customer_address", "full_address"})
    private String address;

    private String province;
    private String district;
    private String ward;

    @JsonAlias({"order_items", "line_items", "products"})
    private List<PancakeOrderItem> items;

    @JsonAlias({"order_status", "state"})
    private String status;

    @JsonAlias({"payment_method_name", "payment_type"})
    private String paymentMethod;

    @JsonAlias({"sub_total", "subtotal_amount"})
    private BigDecimal subtotal;

    @JsonAlias({"shipping_fee", "ship_fee", "delivery_fee"})
    private BigDecimal shippingFee;

    @JsonAlias({"total_amount", "order_total"})
    private BigDecimal total;

    @JsonAlias({"inserted_at", "created_time"})
    private String createdAt;

    @JsonAlias({"updated_time"})
    private String updatedAt;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PancakeCustomer {
        @JsonAlias({"full_name", "customer_name"})
        private String name;

        @JsonAlias({"phone_number", "customer_phone"})
        private String phone;

        @JsonAlias({"full_address", "shipping_address"})
        private String address;

        private String province;
        private String district;
        private String ward;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PancakeOrderItem {
        @JsonAlias({"product_id", "id"})
        private String productId;

        @JsonAlias({"variation_id", "variant_id"})
        private String variationId;

        @JsonAlias({"name", "product_name", "title"})
        private String productName;

        @JsonAlias({"qty", "amount"})
        private Integer quantity;

        @JsonAlias({"unit_price", "retail_price"})
        private BigDecimal price;
    }
}
