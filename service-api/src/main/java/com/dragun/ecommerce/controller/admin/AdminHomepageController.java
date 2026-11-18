package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.model.dto.request.HomepageSectionRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.HomepageSectionResponse;
import com.dragun.ecommerce.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dragun/admin/homepage")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHomepageController {

    private final ContentService contentService;

    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<HomepageSectionResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(contentService.getAllSectionsAdmin()));
    }

    @PutMapping("/sections/{sectionKey}")
    public ResponseEntity<ApiResponse<HomepageSectionResponse>> update(
        @PathVariable String sectionKey,
        @Valid @RequestBody HomepageSectionRequest request,
        Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success(
            contentService.updateSection(sectionKey, request, user),
            "Cập nhật section thành công"
        ));
    }
}
