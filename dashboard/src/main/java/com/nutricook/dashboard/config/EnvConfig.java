package com.nutricook.dashboard.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Load environment variables from .env file
 * This runs at startup before any beans are created
 */
@Configuration
public class EnvConfig {

    @PostConstruct
    public void loadEnv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

            // Load environment variables from .env file
            String dbHost = dotenv.get("DB_HOST");
            String dbName = dotenv.get("DB_NAME");
            String dbUser = dotenv.get("DB_USER");
            String dbPass = dotenv.get("DB_PASS");

            if (dbHost != null) System.setProperty("DB_HOST", dbHost);
            if (dbName != null) System.setProperty("DB_NAME", dbName);
            if (dbUser != null) System.setProperty("DB_USER", dbUser);
            if (dbPass != null) System.setProperty("DB_PASS", dbPass);

            System.out.println("✅ Environment variables loaded from .env file");
            System.out.println("   DB_HOST: " + (dbHost != null ? "***" : "NOT SET"));
            System.out.println("   DB_NAME: " + (dbName != null ? dbName : "NOT SET"));
            System.out.println("   DB_USER: " + (dbUser != null ? dbUser : "NOT SET"));

        } catch (Exception e) {
            System.out.println("⚠️  .env file not found or error loading: " + e.getMessage());
            System.out.println("   Make sure .env file exists in project root directory");
        }
    }
}
