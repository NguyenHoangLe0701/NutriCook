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
            // Nếu không có file serviceAccountKey.json trên classpath thì bỏ qua (ví dụ: môi trường test)
            ClassPathResource credResource = new ClassPathResource("serviceAccountKey.json");
            if (!credResource.exists()) {
                // Không có file chứng thực => không khởi tạo Firebase
                System.out.println("❌ serviceAccountKey.json not found on classpath");
                return;
            }

            System.out.println("✅ serviceAccountKey.json found, initializing Firebase...");
            // Lấy file serviceAccountKey.json từ thư mục resources
            InputStream serviceAccount = credResource.getInputStream();

            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            System.out.println("✅ Credentials loaded: " + credentials);

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