package com.dragun.ecommerce.integration.meinvoice.publish.dto;

import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceOriginalInvoiceDetail;
import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceTaxRateInfo;
import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * MISA MeInvoice V2 {@code InvoiceData} (doc §14.3) — used for {@code POST /invoice} publish only.
 * Not interchangeable with V1 {@code /webapp/insert} payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeinvoiceV2InvoiceData {

    @JsonProperty("RefID")
    private String refId;

    @JsonProperty("InvSeries")
    private String invSeries;

    @JsonProperty("InvDate")
    private String invDate;

    @JsonProperty("CurrencyCode")
    private String currencyCode;

    @JsonProperty("ExchangeRate")
    private BigDecimal exchangeRate;

    @JsonProperty("PaymentMethodName")
    private String paymentMethodName;

    @JsonProperty("IsInvoiceSummary")
    private Boolean isInvoiceSummary;

    @JsonProperty("IsSendEmail")
    private Boolean isSendEmail;

    @JsonProperty("BuyerFullName")
    private String buyerFullName;

    @JsonProperty("BuyerAddress")
    private String buyerAddress;

    @JsonProperty("BuyerPhoneNumber")
    private String buyerPhoneNumber;

    @JsonProperty("BuyerOrderCode")
    private String buyerOrderCode;

    @JsonProperty("TotalSaleAmountOC")
    private BigDecimal totalSaleAmountOC;

    @JsonProperty("TotalSaleAmount")
    private BigDecimal totalSaleAmount;

    @JsonProperty("TotalDiscountAmountOC")
    private BigDecimal totalDiscountAmountOC;

    @JsonProperty("TotalDiscountAmount")
    private BigDecimal totalDiscountAmount;

    @JsonProperty("TotalAmountWithoutVATOC")
    private BigDecimal totalAmountWithoutVatOC;

    @JsonProperty("TotalAmountWithoutVAT")
    private BigDecimal totalAmountWithoutVat;

    @JsonProperty("TotalVATAmountOC")
    private BigDecimal totalVatAmountOC;

    @JsonProperty("TotalVATAmount")
    private BigDecimal totalVatAmount;

    @JsonProperty("TotalAmountOC")
    private BigDecimal totalAmountOC;

    @JsonProperty("TotalAmount")
    private BigDecimal totalAmount;

    @JsonProperty(MeinvoicePublishConstants.JSON_FIELD_ORIGINAL_INVOICE_DETAIL)
    private List<MeinvoiceOriginalInvoiceDetail> originalInvoiceDetail;

    @JsonProperty(MeinvoicePublishConstants.JSON_FIELD_TAX_RATE_INFO)
    private List<MeinvoiceTaxRateInfo> taxRateInfo;
}
