package com.dragun.ecommerce.integration.meinvoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeinvoiceInvoiceDetail {

    @JsonProperty("InventoryItemType")
    private Integer inventoryItemType;

    @JsonProperty("InventoryItemCode")
    private String inventoryItemCode;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("SortOrderView")
    private Integer sortOrderView;

    @JsonProperty("SortOrder")
    private Integer sortOrder;

    @JsonProperty("IsPromotion")
    private Boolean isPromotion;

    @JsonProperty("UnitName")
    private String unitName;

    @JsonProperty("Quantity")
    private BigDecimal quantity;

    @JsonProperty("UnitPrice")
    private BigDecimal unitPrice;

    @JsonProperty("AmountOC")
    private BigDecimal amountOC;

    @JsonProperty("Amount")
    private BigDecimal amount;

    @JsonProperty("DiscountRate")
    private BigDecimal discountRate;

    @JsonProperty("DiscountAmountOC")
    private BigDecimal discountAmountOC;

    @JsonProperty("DiscountAmount")
    private BigDecimal discountAmount;

    @JsonProperty("VATRate")
    private BigDecimal vatRate;

    @JsonProperty("VATAmountOC")
    private BigDecimal vatAmountOC;

    @JsonProperty("VATAmount")
    private BigDecimal vatAmount;
}
