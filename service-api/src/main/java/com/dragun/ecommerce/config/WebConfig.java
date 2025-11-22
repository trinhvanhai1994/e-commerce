package com.dragun.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:src/main/resources/public/images}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from upload directory (absolute path)
        Path uploadPath = Paths.get(uploadDir);
        // If relative path, resolve against project root
        if (!uploadPath.isAbsolute()) {
            uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
        }
        
        String uploadPathStr = uploadPath.toAbsolutePath().toString();
        
        // Ensure path ends with separator
        if (!uploadPathStr.endsWith("/") && !uploadPathStr.endsWith("\\")) {
            uploadPathStr += "/";
        }
        
        // Convert Windows path separator to forward slash for file: URL
        uploadPathStr = uploadPathStr.replace("\\", "/");
        
        // Serve from file system (for uploaded files)
        registry.addResourceHandler("/images/**")
            .addResourceLocations("file:" + uploadPathStr)
            .setCachePeriod(3600); // Cache 1 hour
        
        // Also serve from classpath for development (fallback - for static resources)
        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/public/images/")
            .setCachePeriod(3600);
    }
}

