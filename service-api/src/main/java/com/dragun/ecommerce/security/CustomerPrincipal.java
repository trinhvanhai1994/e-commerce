package com.dragun.ecommerce.security;

/**
 * Authenticated storefront customer, set by JwtAuthenticationFilter from a customer access token.
 * Controllers read it with {@code @AuthenticationPrincipal CustomerPrincipal}; it only ever has ROLE_CUSTOMER.
 */
public record CustomerPrincipal(Long id, String phone) {

    public static final String ROLE = "ROLE_CUSTOMER";
}
