package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.model.dto.request.LoginRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.LoginResponse;
import com.dragun.ecommerce.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/dragun/admin", "/api/thiyen/admin"})
@RequiredArgsConstructor
public class AdminAuthController {
    
    private final AdminService adminService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = adminService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}


