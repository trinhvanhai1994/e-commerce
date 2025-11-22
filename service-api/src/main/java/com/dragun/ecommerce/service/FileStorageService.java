package com.dragun.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileStorageService {
    
    @Value("${file.upload-dir:src/main/resources/public/images}")
    private String uploadDir;
    
    /**
     * Get the upload directory path
     * @return Absolute path to upload directory
     */
    private Path getUploadDirPath() {
        Path path = Paths.get(uploadDir);
        // If relative path, resolve against project root
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir"), uploadDir);
        }
        return path;
    }
    
    /**
     * Upload file và lưu vào thư mục tương ứng
     * @param file File cần upload
     * @param relativePath Đường dẫn tương đối (ví dụ: products/details/black/1.png)
     * @return Đường dẫn đầy đủ để truy cập file (ví dụ: /images/products/details/black/1.png)
     * @throws IOException Nếu có lỗi khi lưu file
     */
    public String uploadFile(MultipartFile file, String relativePath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }
        
        // Tạo đường dẫn đầy đủ
        Path targetPath = getUploadDirPath().resolve(relativePath);
        
        // Tạo thư mục nếu chưa tồn tại
        File targetFile = targetPath.toFile();
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
            log.info("Đã tạo thư mục: {}", targetFile.getParentFile().getAbsolutePath());
        }
        
        // Kiểm tra file cũ có tồn tại không
        boolean fileExists = Files.exists(targetPath);
        if (fileExists) {
            log.info("File đã tồn tại, sẽ được ghi đè: {}", targetPath.toAbsolutePath());
            // Xóa file cũ trước để đảm bảo ghi đè hoàn toàn
            Files.deleteIfExists(targetPath);
        }
        
        // Lưu file mới (ghi đè nếu đã tồn tại)
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // Verify file đã được lưu
        if (Files.exists(targetPath)) {
            long fileSize = Files.size(targetPath);
            log.info("✅ Đã lưu file thành công: {} -> {} (Size: {} bytes)", 
                file.getOriginalFilename(), targetPath.toAbsolutePath(), fileSize);
        } else {
            log.error("❌ Lỗi: File không được lưu tại: {}", targetPath.toAbsolutePath());
            throw new IOException("File không được lưu thành công");
        }
        
        // Trả về đường dẫn để truy cập file (bắt đầu từ /images)
        return "/images/" + relativePath;
    }
    
    /**
     * Xóa file
     * @param relativePath Đường dẫn tương đối
     * @return true nếu xóa thành công
     */
    public boolean deleteFile(String relativePath) {
        try {
            Path filePath = getUploadDirPath().resolve(relativePath);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Đã xóa file: {}", filePath.toAbsolutePath());
            }
            return deleted;
        } catch (IOException e) {
            log.error("Lỗi khi xóa file: {}", relativePath, e);
            return false;
        }
    }
    
    /**
     * Kiểm tra file có tồn tại không
     * @param relativePath Đường dẫn tương đối
     * @return true nếu file tồn tại
     */
    public boolean fileExists(String relativePath) {
        Path filePath = getUploadDirPath().resolve(relativePath);
        return Files.exists(filePath);
    }
}

