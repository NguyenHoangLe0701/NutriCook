package com.nutricook.dashboard.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = false)
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        System.out.println("🔥 FirebaseConfig.initialize() called");
        try {
            // If emulator is configured via env, skip Firebase Admin init (emulator uses env vars)
            String emulator = System.getenv("FIRESTORE_EMULATOR_HOST");
            if (emulator != null && !emulator.isBlank()) {
                System.out.println("🔥 Using Firestore emulator at: " + emulator);
                return;
            }
            
            GoogleCredentials credentials = null;
            
            // Ưu tiên 1: Đọc từ GOOGLE_APPLICATION_CREDENTIALS (chuẩn của Google Cloud)
            String googleCredsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (googleCredsPath != null && !googleCredsPath.isBlank()) {
                try {
                    java.io.File credsFile = new java.io.File(googleCredsPath);
                    if (credsFile.exists()) {
                        System.out.println("✅ Loading Firebase credentials from: " + googleCredsPath);
                        try (InputStream serviceAccount = new java.io.FileInputStream(credsFile)) {
                            credentials = GoogleCredentials.fromStream(serviceAccount);
                        }
                        System.out.println("✅ Credentials loaded from GOOGLE_APPLICATION_CREDENTIALS");
                    }
                } catch (Exception e) {
                    System.out.println("⚠️  Failed to load from GOOGLE_APPLICATION_CREDENTIALS: " + e.getMessage());
                }
            }
            
            // Ưu tiên 2: Đọc từ classpath (serviceAccountKey.json trong resources)
            if (credentials == null) {
                ClassPathResource credResource = new ClassPathResource("serviceAccountKey.json");
                if (credResource.exists()) {
                    System.out.println("✅ Loading Firebase credentials from classpath");
                    try (InputStream serviceAccount = credResource.getInputStream()) {
                        credentials = GoogleCredentials.fromStream(serviceAccount);
                    }
                    System.out.println("✅ Credentials loaded from classpath");
                } else {
                    System.out.println("❌ serviceAccountKey.json not found on classpath and GOOGLE_APPLICATION_CREDENTIALS not set");
                    return;
                }
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

            // Khởi tạo Firebase App (chỉ một lần)
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized successfully");
            } else {
                System.out.println("⚠️  Firebase already initialized");
            }

        } catch (Exception e) {
            System.out.println("❌ Firebase initialization error:");
            e.printStackTrace();
        }
    }

    // Provide a Firestore bean for injection
    @Bean
    public Firestore firestore() {
        System.out.println("🔥 Creating Firestore bean");
        // If emulator env present, return a Firestore client pointed at emulator
        String emulator = System.getenv("FIRESTORE_EMULATOR_HOST");
        if (emulator != null && !emulator.isBlank()) {
            String projectId = System.getenv().getOrDefault("FIREBASE_PROJECT_ID", "demo-project");
            System.out.println("🔥 Using emulator Firestore for project: " + projectId);
            return FirestoreOptions.getDefaultInstance().toBuilder().setProjectId(projectId).build().getService();
        }

        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("❌ FirebaseApp not initialized");
            return null;
        }
        
        Firestore fs = FirestoreClient.getFirestore();
        System.out.println("✅ Firestore bean created successfully");
        return fs;
    }
}