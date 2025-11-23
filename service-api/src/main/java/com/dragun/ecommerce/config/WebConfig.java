package com.dragun.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-dir:/var/www/html/images/thiyen}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
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
        
        System.out.println("📁 Serving images from: file:" + uploadPathStr);
        
        // Serve from file system (for uploaded files)
        registry.addResourceHandler("/images/**")
            .addResourceLocations("file:" + uploadPathStr)
            .setCachePeriod(3600) // Cache 1 hour
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Resource getResource(@NonNull String resourcePath, 
                        @NonNull Resource location) throws IOException {
                    Resource resource = location.createRelative(resourcePath);
                    if (resource.exists() && resource.isReadable()) {
                        return resource;
                    }
                    // Return null để Spring trả về 404 thay vì 500
                    return null;
                }
            });
    }
}

