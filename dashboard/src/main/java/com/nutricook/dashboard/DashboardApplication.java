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
        }

        SpringApplication.run(DashboardApplication.class, args);
    }

}