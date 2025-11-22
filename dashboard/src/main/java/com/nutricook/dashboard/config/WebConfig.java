package com.nutricook.dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn tuyệt đối đến thư mục 'uploads'
        // (Khớp với UPLOAD_DIR = "uploads/" trong AdminController)
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        // Ánh xạ URL /uploads/** tới thư mục uploads/ trên hệ thống file
        // Sử dụng "file:///" cho Windows và "file:/" cho Linux/Mac
        String fileUrlPrefix = uploadPath.startsWith("/") ? "file:" : "file:///";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileUrlPrefix + uploadPath.replace("\\", "/") + "/");
    }
}