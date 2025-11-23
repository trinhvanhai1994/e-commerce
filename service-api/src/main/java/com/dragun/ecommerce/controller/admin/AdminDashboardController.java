package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.DashboardStatsResponse;
import com.dragun.ecommerce.service.DashboardStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dragun/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    
    private final DashboardStatsService dashboardStatsService;
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = dashboardStatsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

