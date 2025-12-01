package com.nutricook.dashboard.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Load variables from .env into the Spring Environment before the context is created.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            Map<String, Object> map = new HashMap<>();

            String dbHost = dotenv.get("DB_HOST");
            String dbName = dotenv.get("DB_NAME");
            String dbUser = dotenv.get("DB_USER");
            String dbPass = dotenv.get("DB_PASS");
            
            // Cloudinary credentials
            String cloudName = dotenv.get("CLOUDINARY_CLOUD_NAME");
            String apiKey = dotenv.get("CLOUDINARY_API_KEY");
            String apiSecret = dotenv.get("CLOUDINARY_API_SECRET");

            if (dbHost != null) {
                map.put("DB_HOST", dbHost);
                System.setProperty("DB_HOST", dbHost);
            }
            if (dbName != null) {
                map.put("DB_NAME", dbName);
                System.setProperty("DB_NAME", dbName);
            }
            if (dbUser != null) {
                map.put("DB_USER", dbUser);
                System.setProperty("DB_USER", dbUser);
            }
            if (dbPass != null) {
                map.put("DB_PASS", dbPass);
                System.setProperty("DB_PASS", dbPass);
            }
            
            // Set Cloudinary environment variables
            if (cloudName != null) {
                map.put("CLOUDINARY_CLOUD_NAME", cloudName);
                System.setProperty("CLOUDINARY_CLOUD_NAME", cloudName);
            }
            if (apiKey != null) {
                map.put("CLOUDINARY_API_KEY", apiKey);
                System.setProperty("CLOUDINARY_API_KEY", apiKey);
            }
            if (apiSecret != null) {
                map.put("CLOUDINARY_API_SECRET", apiSecret);
                System.setProperty("CLOUDINARY_API_SECRET", apiSecret);
            }

            // Additionally set spring.datasource.* properties directly so DataSource resolves
            String dbPort = dotenv.get("DB_PORT");
            String dbSslMode = dotenv.get("DB_SSL_MODE");
            if (dbPort == null || dbPort.isEmpty()) {
                dbPort = "3306";
            }
            if (dbHost != null && dbName != null) {
                String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s", dbHost, dbPort, dbName);
                // Build query params based on SSL mode
                StringBuilder params = new StringBuilder();
                if (dbSslMode != null && !dbSslMode.isEmpty()) {
                    params.append("sslMode=").append(dbSslMode);
                } else {
                    // default params for local/dev if SSL not specified
                    params.append("useSSL=false&allowPublicKeyRetrieval=true");
                }
                if (params.length() > 0) {
                    params.append("&serverTimezone=UTC");
                } else {
                    params.append("serverTimezone=UTC");
                }
                jdbcUrl = jdbcUrl + "?" + params.toString();
                map.put("spring.datasource.url", jdbcUrl);
                System.setProperty("spring.datasource.url", jdbcUrl);
            }
            if (dbUser != null) {
                map.put("spring.datasource.username", dbUser);
                System.setProperty("spring.datasource.username", dbUser);
            }
            if (dbPass != null) {
                map.put("spring.datasource.password", dbPass);
                System.setProperty("spring.datasource.password", dbPass);
            }

            if (!map.isEmpty()) {
                MapPropertySource ps = new MapPropertySource("dotenvProperties", map);
                environment.getPropertySources().addFirst(ps);
                System.out.println("✅ DotenvEnvironmentPostProcessor added properties: " + map.keySet());
                // Print resolved datasource-related properties for debugging
                try {
                    String resolvedUrl = environment.getProperty("spring.datasource.url");
                    String resolvedDriver = environment.getProperty("spring.datasource.driver-class-name");
                    System.out.println("[Dotenv] spring.datasource.url=" + (resolvedUrl!=null?resolvedUrl:"(null)"));
                    System.out.println("[Dotenv] spring.datasource.driver-class-name=" + (resolvedDriver!=null?resolvedDriver:"(null)"));
                    System.out.println("[Dotenv] DB_HOST=" + map.get("DB_HOST") + ", DB_NAME=" + map.get("DB_NAME") + ", DB_USER=" + map.get("DB_USER"));
                } catch (Exception e) {
                    System.out.println("[Dotenv] failed to read resolved datasource props: " + e.getMessage());
                }
            } else {
                System.out.println("⚠️ DotenvEnvironmentPostProcessor found no .env values");
            }
        } catch (Exception e) {
            System.out.println("⚠️ DotenvEnvironmentPostProcessor error: " + e.getMessage());
        }
    }
}
