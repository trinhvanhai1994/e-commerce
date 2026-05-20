package com.dragun.ecommerce.integration.meinvoice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body for {@code POST /webapp/token} and {@code POST /webapp/templates} (per MeInvoice doc).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeinvoiceLoginRequest {

    @JsonProperty("taxcode")
    private String taxcode;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
