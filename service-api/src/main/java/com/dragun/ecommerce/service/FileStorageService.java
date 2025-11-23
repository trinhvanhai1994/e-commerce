package com.dragun.ecommerce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
public class FileStorageService {
    
    @Value("${file.upload-dir:/var/www/html/images/thiyen}")
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
        
        // Log the base upload directory for debugging
        log.debug("Base upload directory: {}", path.toAbsolutePath());
        
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
        Path basePath = getUploadDirPath();
        Path targetPath = basePath.resolve(relativePath);
        
        log.info("Upload file: {} -> {}", file.getOriginalFilename(), targetPath.toAbsolutePath());
        
        // Tạo thư mục nếu chưa tồn tại
        File targetFile = targetPath.toFile();
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                log.info("Đã tạo thư mục: {}", parentDir.getAbsolutePath());
            } else {
                log.warn("Không thể tạo thư mục: {}", parentDir.getAbsolutePath());
                throw new IOException("Không thể tạo thư mục: " + parentDir.getAbsolutePath());
            }
        }
        
        // Kiểm tra file cũ có tồn tại không
        boolean fileExists = Files.exists(targetPath);
        if (fileExists) {
            log.info("File đã tồn tại, sẽ được ghi đè: {}", targetPath.toAbsolutePath());
            // Xóa file cũ trước để đảm bảo ghi đè hoàn toàn
            try {
                Files.deleteIfExists(targetPath);
                // Đợi một chút để đảm bảo file được xóa hoàn toàn
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Thread bị interrupt khi đợi xóa file cũ");
            }
        }
        
        // Đảm bảo parent directory tồn tại và có quyền ghi
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                log.error("❌ Không thể tạo thư mục: {}", parentDir.getAbsolutePath());
                throw new IOException("Không thể tạo thư mục: " + parentDir.getAbsolutePath());
            }
            log.info("✅ Đã tạo thư mục: {}", parentDir.getAbsolutePath());
        }
        
        // Kiểm tra quyền ghi vào parent directory
        if (parentDir != null && !parentDir.canWrite()) {
            log.error("❌ Không có quyền ghi vào thư mục: {}", parentDir.getAbsolutePath());
            throw new IOException("Không có quyền ghi vào thư mục: " + parentDir.getAbsolutePath());
        }
        
        // Lưu file mới (ghi đè nếu đã tồn tại)
        // Sử dụng try-with-resources để đảm bảo stream được đóng đúng cách
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Force sync để đảm bảo file được ghi vào disk
            try (FileChannel channel = FileChannel.open(targetPath, StandardOpenOption.WRITE)) {
                channel.force(true);
            } catch (Exception e) {
                log.warn("Không thể force sync file (có thể không hỗ trợ): {}", e.getMessage());
            }
        }
        
        // Verify file đã được lưu - đợi một chút để đảm bảo file được ghi xong
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (Files.exists(targetPath) && Files.isRegularFile(targetPath)) {
            long fileSize = Files.size(targetPath);
            if (fileSize > 0) {
                log.info("✅ Đã lưu file thành công: {} -> {} (Size: {} bytes)", 
                    file.getOriginalFilename(), targetPath.toAbsolutePath(), fileSize);
            } else {
                log.error("❌ Lỗi: File được tạo nhưng size = 0: {}", targetPath.toAbsolutePath());
                throw new IOException("File được tạo nhưng size = 0: " + targetPath.toAbsolutePath());
            }
        } else {
            log.error("❌ Lỗi: File không được lưu tại: {} (exists: {}, isRegularFile: {})", 
                targetPath.toAbsolutePath(), 
                Files.exists(targetPath),
                Files.exists(targetPath) ? Files.isRegularFile(targetPath) : false);
            throw new IOException("File không được lưu thành công tại: " + targetPath.toAbsolutePath());
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

