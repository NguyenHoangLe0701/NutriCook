package com.nutricook.dashboard.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Value("${cloudinary.secure:true}")
    private boolean secure;

    @Bean
    public Cloudinary cloudinary() {
        // Kiểm tra xem Cloudinary đã được cấu hình chưa
        boolean isConfigured = !(cloudName == null || cloudName.equals("your_cloud_name") || 
                                 apiKey == null || apiKey.equals("your_api_key") ||
                                 apiSecret == null || apiSecret.equals("your_api_secret"));
        
        if (!isConfigured) {
            System.err.println("⚠️ WARNING: Cloudinary chưa được cấu hình!");
            System.err.println("   Vui lòng thiết lập các biến môi trường:");
            System.err.println("   - CLOUDINARY_CLOUD_NAME");
            System.err.println("   - CLOUDINARY_API_KEY");
            System.err.println("   - CLOUDINARY_API_SECRET");
            System.err.println("   Hoặc cập nhật trong application.properties");
            System.err.println("   CloudinaryService sẽ không khả dụng cho đến khi được cấu hình đúng.");
            System.err.println("   Hệ thống sẽ sử dụng local storage thay thế.");
            return null; // Return null nếu chưa cấu hình
        }
        
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            config.put("secure", secure);
            
            Cloudinary cloudinaryInstance = new Cloudinary(config);
            
            System.out.println("✅ Cloudinary đã được cấu hình thành công!");
            System.out.println("   Cloud name: " + cloudName);
            System.out.println("   API Key: " + apiKey.substring(0, Math.min(5, apiKey.length())) + "***");
            System.out.println("   Secure: " + secure);
            
            return cloudinaryInstance;
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi khởi tạo Cloudinary: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

