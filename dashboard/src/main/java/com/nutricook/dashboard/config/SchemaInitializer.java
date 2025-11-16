package com.nutricook.dashboard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void ensureSchema(ApplicationReadyEvent event) {
        try {
            System.out.println("[SchemaInitializer] Ensuring DB schema exists...");

            // Users
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255), " +
                    "full_name VARCHAR(255), " +
                    "avatar VARCHAR(255), " +
                    "role VARCHAR(50), " +
                    "created_at DATETIME, " +
                    "updated_at DATETIME" +
                    ") ENGINE=InnoDB");

            // Categories
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL UNIQUE, " +
                    "description TEXT, " +
                    "icon VARCHAR(255), " +
                    "color VARCHAR(50), " +
                    "created_at DATETIME, " +
                    "updated_at DATETIME" +
                    ") ENGINE=InnoDB");

            // Food items
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS food_items (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "calories VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "image_url VARCHAR(1024), " +
                    "category_id BIGINT NOT NULL, " +
                    "user_id BIGINT, " +
                    "available BOOLEAN, " +
                    "created_at DATETIME, " +
                    "updated_at DATETIME, " +
                    "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB");

            // Food updates
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS food_updates (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "food_item_id BIGINT NOT NULL, " +
                    "action VARCHAR(50) NOT NULL, " +
                    "old_data TEXT, " +
                    "new_data TEXT, " +
                    "created_at DATETIME, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (food_item_id) REFERENCES food_items(id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB");

            System.out.println("[SchemaInitializer] Schema ensured.");
        } catch (Exception e) {
            System.err.println("[SchemaInitializer] Failed to ensure schema: " + e.getMessage());
        }
    }
}
