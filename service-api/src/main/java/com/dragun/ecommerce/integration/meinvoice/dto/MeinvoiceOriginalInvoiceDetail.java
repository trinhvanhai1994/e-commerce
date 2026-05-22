package com.dragun.ecommerce.integration.meinvoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * V2 publish line item ({@code OriginalInvoiceDetail} in MISA doc §14.4).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeinvoiceOriginalInvoiceDetail {

    @JsonProperty("ItemType")
    private Integer itemType;

    @JsonProperty("SortOrder")
    private Integer sortOrder;

    @JsonProperty("LineNumber")
    private Integer lineNumber;

    @JsonProperty("ItemCode")
    private String itemCode;

    @JsonProperty("ItemName")
    private String itemName;

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

    @JsonProperty("AmountWithoutVATOC")
    private BigDecimal amountWithoutVatOC;

    @JsonProperty("AmountWithoutVAT")
    private BigDecimal amountWithoutVat;

    @JsonProperty("VATRateName")
    private String vatRateName;

    @JsonProperty("VATAmountOC")
    private BigDecimal vatAmountOC;

    @JsonProperty("VATAmount")
    private BigDecimal vatAmount;
}
