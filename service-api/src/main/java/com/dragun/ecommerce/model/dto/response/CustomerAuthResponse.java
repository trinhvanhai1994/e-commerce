package com.dragun.ecommerce.model.dto.response;

import lombok.Builder;
import lombok.Data;

/** Result of register, login, refresh and password change: a fresh session plus the profile to show at once. */
@Data
@Builder
public class CustomerAuthResponse {
    private String accessToken;
    /** Seconds until accessToken expires. */
    private long expiresIn;
    private String refreshToken;
    private CustomerProfileResponse customer;
}
