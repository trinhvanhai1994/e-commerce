package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.model.dto.request.BlogPostRequest;
import com.dragun.ecommerce.model.dto.request.BlogPostStatusRequest;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.model.dto.response.BlogPostResponse;
import com.dragun.ecommerce.service.BlogPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dragun/admin/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBlogPostController {

    private final BlogPostService blogPostService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BlogPostResponse>>> list(
        @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        return ResponseEntity.ok(ApiResponse.success(blogPostService.listAdmin(includeDeleted)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(blogPostService.getAdminById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BlogPostResponse>> create(
        @Valid @RequestBody BlogPostRequest request,
        Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success(blogPostService.create(request, user), "Tạo bài viết thành công"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BlogPostResponse>> update(
        @PathVariable Long id,
        @Valid @RequestBody BlogPostRequest request,
        Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success(blogPostService.update(id, request, user), "Cập nhật bài viết thành công"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BlogPostResponse>> status(
        @PathVariable Long id,
        @Valid @RequestBody BlogPostStatusRequest request,
        Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        return ResponseEntity.ok(ApiResponse.success(
            blogPostService.updateStatus(id, request.getStatus(), user),
            "Cập nhật trạng thái thành công"
        ));
    }

    @PostMapping("/{id}/soft-delete")
    public ResponseEntity<ApiResponse<Object>> softDelete(
        @PathVariable Long id,
        Authentication authentication
    ) {
        String user = authentication != null ? authentication.getName() : "admin";
        blogPostService.softDelete(id, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa bài viết (soft-delete)"));
    }
}
