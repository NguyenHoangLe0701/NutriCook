package com.nutricook.dashboard;

import org.springframework.boot.SpringApplication; // <-- 1. Thêm import này
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class DashboardApplication {

    public static void main(String[] args) {
        // 2. Tải tệp .env trước khi Spring chạy
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        if (dotenv != null) {
            String dbHost = dotenv.get("DB_HOST");
            String dbName = dotenv.get("DB_NAME");
            String dbUser = dotenv.get("DB_USER");
            String dbPass = dotenv.get("DB_PASS");
            if (dbHost != null) System.setProperty("DB_HOST", dbHost);
            if (dbName != null) System.setProperty("DB_NAME", dbName);
            if (dbUser != null) System.setProperty("DB_USER", dbUser);
            if (dbPass != null) System.setProperty("DB_PASS", dbPass);
            System.out.println("[main] Loaded .env: DB_HOST=" + (dbHost!=null?dbHost:"(null)"));
            
            // Load Cloudinary credentials from .env
            String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
            String apiKey = dotenv.get("CLOUDINARY_API_KEY");
            String apiSecret = dotenv.get("CLOUDINARY_API_SECRET");
            if (cloudName != null) System.setProperty("CLOUDINARY_CLOUD_NAME", cloudName);
            if (apiKey != null) System.setProperty("CLOUDINARY_API_KEY", apiKey);
            if (apiSecret != null) System.setProperty("CLOUDINARY_API_SECRET", apiSecret);
            if (cloudName != null && apiKey != null && apiSecret != null) {
                System.out.println("[main] Loaded .env: Cloudinary configured");
            } else {
                System.out.println("[main] Cloudinary credentials not found in .env file");
            }
        }

        SpringApplication.run(DashboardApplication.class, args);
    }

}