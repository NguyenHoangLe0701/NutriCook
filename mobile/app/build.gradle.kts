import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")   // ⬅️ BẮT BUỘC khi dùng Compose mới
    id("com.google.gms.google-services")        // ⬅️ Firebase
    id("com.google.dagger.hilt.android")        // ⬅️ Hilt
    id("org.jetbrains.kotlin.kapt")             // ⬅️ KAPT cho Hilt
}

// Đọc secrets.properties
val secretsPropertiesFile = rootProject.file("app/secrets.properties")
val secretsProperties = Properties()
if (secretsPropertiesFile.exists()) {
    FileInputStream(secretsPropertiesFile).use {
        secretsProperties.load(it)
    }
}

android {
    namespace = "com.example.nutricook"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nutricook"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // BuildConfig fields từ secrets.properties
        buildConfigField(
            "String",
            "CLOUDINARY_CLOUD_NAME",
            "\"${secretsProperties["CLOUDINARY_CLOUD_NAME"] ?: "your_cloud_name"}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_API_KEY",
            "\"${secretsProperties["CLOUDINARY_API_KEY"] ?: "your_api_key"}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_API_SECRET",
            "\"${secretsProperties["CLOUDINARY_API_SECRET"] ?: "your_api_secret"}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true  // Bật BuildConfig để sử dụng các field từ secrets.properties
    }

    // Tránh xung đột META-INF khi kéo nhiều SDK
    packaging {
        resources {
            excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
        }
    }

    // ❌ Không cần composeOptions khi đã dùng plugin compose mới
    // composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }

    lint { abortOnError = false }
}

dependencies {
    // --- Compose ---
    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.0")
    implementation(libs.androidx.foundation)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.0")
    implementation("androidx.compose.foundation:foundation:1.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // --- Hilt + Compose integration ---
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // --- Lifecycle / ViewModel ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")

    // --- Core / Coroutines ---
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // --- Firebase (via BoM) ---
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx") // FCM for push notifications

    // --- Google Sign-In ---
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // --- Facebook Login ---
    implementation("com.facebook.android:facebook-login:18.1.3")

    // --- Material (View) ---
    implementation("com.google.android.material:material:1.12.0")

    // --- Retrofit + Gson + OkHttp ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // --- Coil Compose ---
    implementation("io.coil-kt:coil-compose:2.7.0")

    // --- Cloudinary ---
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    // --- Test ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.0")
}

kapt {
    correctErrorTypes = true
}
