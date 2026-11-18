package com.dragun.ecommerce.controller.external;

import com.dragun.ecommerce.model.dto.request.ChangePasswordRequest;
import com.dragun.ecommerce.model.dto.request.CustomerProfileRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.CustomerAuthResponse;
import com.dragun.ecommerce.model.dto.response.CustomerProfileResponse;
import com.dragun.ecommerce.model.dto.response.OrderResponse;
import com.dragun.ecommerce.security.CustomerPrincipal;
import com.dragun.ecommerce.service.CustomerAccountService;
import com.dragun.ecommerce.service.CustomerAuthService;
import com.dragun.ecommerce.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Logged-in customer's own data. SecurityConfig requires ROLE_CUSTOMER for /api/dragun/account/**. */
@RestController
@RequestMapping("/api/dragun/account")
@RequiredArgsConstructor
public class CustomerAccountController {

    private final CustomerAccountService accountService;
    private final CustomerAuthService authService;
    private final OrderService orderService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> profile(@AuthenticationPrincipal CustomerPrincipal me) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getProfile(me.id())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> updateProfile(
        @AuthenticationPrincipal CustomerPrincipal me, @Valid @RequestBody CustomerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(accountService.updateProfile(me.id(), request), "Đã lưu thông tin"));
    }

    /** Returns a new session for this device; every other session of the account ends. */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<CustomerAuthResponse>> changePassword(
        @AuthenticationPrincipal CustomerPrincipal me, @Valid @RequestBody ChangePasswordRequest request,
        HttpServletRequest http) {
        return ResponseEntity.ok(ApiResponse.success(
            authService.changePassword(me.id(), request, http.getHeader("User-Agent")), "Đã đổi mật khẩu"));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> orders(@AuthenticationPrincipal CustomerPrincipal me) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByCustomerId(me.id())));
    }
}
