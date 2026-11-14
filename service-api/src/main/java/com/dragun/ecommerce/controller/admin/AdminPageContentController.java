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

/** CMS sections of the content pages other than the homepage (story, philosophy). */
@RestController
@RequestMapping("/api/dragun/admin/pages")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPageContentController {

    private final ContentService contentService;

    @GetMapping("/{pageKey}/sections")
    public ResponseEntity<ApiResponse<List<HomepageSectionResponse>>> list(@PathVariable String pageKey) {
        return ResponseEntity.ok(ApiResponse.success(contentService.getAllSectionsAdmin(pageKey)));
    }

    @PutMapping("/{pageKey}/sections/{sectionKey}")
    public ResponseEntity<ApiResponse<HomepageSectionResponse>> update(
        @PathVariable String pageKey,
        @PathVariable String sectionKey,
        @Valid @RequestBody HomepageSectionRequest request,
        Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success(
            contentService.updateSection(pageKey, sectionKey, request, user),
            "Cập nhật khối thành công"
        ));
    }
}
