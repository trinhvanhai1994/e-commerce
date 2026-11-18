package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.dto.request.CustomerLoginRequest;
import com.dragun.ecommerce.model.dto.request.CustomerRegisterRequest;
import com.dragun.ecommerce.model.dto.request.RefreshTokenRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.CustomerAuthResponse;
import com.dragun.ecommerce.service.CustomerAuthService;
import com.dragun.ecommerce.util.ClientIp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Storefront customer sessions. Public (Constants.PUBLIC_ENDPOINTS); the admin login is a separate route. */
@RestController
@RequestMapping("/api/dragun/auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerAuthResponse>> register(
        @Valid @RequestBody CustomerRegisterRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.success(
            authService.register(request, ClientIp.of(http), http.getHeader("User-Agent")), "Đăng ký thành công"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<CustomerAuthResponse>> login(
        @Valid @RequestBody CustomerLoginRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.success(
            authService.login(request, ClientIp.of(http), http.getHeader("User-Agent")), "Đăng nhập thành công"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<CustomerAuthResponse>> refresh(
        @Valid @RequestBody RefreshTokenRequest request, HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.success(
            authService.refresh(request.getRefreshToken(), http.getHeader("User-Agent"))));
    }

    /** Ends this device's session. The short-lived access token simply expires. */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(request == null ? null : request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Đã đăng xuất"));
    }
}
