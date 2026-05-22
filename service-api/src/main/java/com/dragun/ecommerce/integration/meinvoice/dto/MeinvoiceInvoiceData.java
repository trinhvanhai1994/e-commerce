package com.dragun.ecommerce.integration.meinvoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeinvoiceInvoiceData {

    @JsonProperty("RefID")
    private String refId;

    @JsonProperty("InvoiceTemplateID")
    private String invoiceTemplateId;

    @JsonProperty("InvSeries")
    private String invSeries;

    @JsonProperty("InvDate")
    private String invDate;

    @JsonProperty("AccountObjectName")
    private String accountObjectName;

    @JsonProperty("AccountObjectAddress")
    private String accountObjectAddress;

    @JsonProperty("ContactName")
    private String contactName;

    @JsonProperty("ReceiverMobile")
    private String receiverMobile;

    @JsonProperty("PaymentMethod")
    private String paymentMethod;

    @JsonProperty("CurrencyCode")
    private String currencyCode;

    @JsonProperty("CurrencyID")
    private String currencyId;

    @JsonProperty("InvNo")
    private String invNo;

    /**
     * Master VAT rate (Postman sample uses e.g. {@code 8.0}).
     */
    @JsonProperty("VATRate")
    private BigDecimal vatRate;

    @JsonProperty("DiscountRate")
    private BigDecimal discountRate;

    @JsonProperty("ExchangeRate")
    private BigDecimal exchangeRate;

    @JsonProperty("TotalSaleAmountOC")
    private BigDecimal totalSaleAmountOC;

    @JsonProperty("TotalSaleAmount")
    private BigDecimal totalSaleAmount;

    @JsonProperty("TotalDiscountAmountOC")
    private BigDecimal totalDiscountAmountOC;

    @JsonProperty("TotalDiscountAmount")
    private BigDecimal totalDiscountAmount;

    @JsonProperty("TotalVATAmountOC")
    private BigDecimal totalVatAmountOC;

    @JsonProperty("TotalVATAmount")
    private BigDecimal totalVatAmount;

    @JsonProperty("TotalAmountOC")
    private BigDecimal totalAmountOC;

    @JsonProperty("TotalAmount")
    private BigDecimal totalAmount;

    @JsonProperty("CreatedDate")
    private String createdDate;

    @JsonProperty("ModifiedDate")
    private String modifiedDate;

    @JsonProperty("InvoiceDetails")
    private List<MeinvoiceInvoiceDetail> invoiceDetails;
}
