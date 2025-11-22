package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.dragun.ecommerce.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/dragun/admin/files")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminFileUploadController {
    
    private final FileStorageService fileStorageService;
    
    /**
     * Upload ảnh sản phẩm
     * @param file File ảnh cần upload
     * @param path Đường dẫn tương đối (ví dụ: products/details/black/1.png)
     * @return Đường dẫn đầy đủ để truy cập file
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("path") String path) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
            }
            
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chỉ chấp nhận file ảnh"));
            }
            
            // Validate path
            if (path == null || path.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Đường dẫn không được để trống"));
            }
            
            // Normalize path (loại bỏ /images/ nếu có ở đầu)
            String normalizedPath = path.startsWith("/images/") 
                ? path.substring(8) // Bỏ "/images/"
                : path.startsWith("images/")
                    ? path.substring(7) // Bỏ "images/"
                    : path;
            
            // Upload file
            String filePath = fileStorageService.uploadFile(file, normalizedPath);
            
            log.info("Upload thành công: {} -> {}", file.getOriginalFilename(), filePath);
            
            return ResponseEntity.ok(ApiResponse.success(filePath, "Upload file thành công"));
            
        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (IOException e) {
            log.error("Lỗi khi upload file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Lỗi khi lưu file: " + e.getMessage()));
        }
    }
    
    /**
     * Xóa file
     * @param path Đường dẫn tương đối
     * @return Kết quả xóa file
     */
    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Object>> deleteFile(@RequestParam("path") String path) {
        try {
            // Normalize path
            String normalizedPath = path.startsWith("/images/") 
                ? path.substring(8)
                : path.startsWith("images/")
                    ? path.substring(7)
                    : path;
            
            boolean deleted = fileStorageService.deleteFile(normalizedPath);
            
            if (deleted) {
                return ResponseEntity.ok(ApiResponse.success(null, "Xóa file thành công"));
            } else {
                return ResponseEntity.ok(ApiResponse.success(null, "File không tồn tại"));
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Lỗi khi xóa file: " + e.getMessage()));
        }
    }
}

