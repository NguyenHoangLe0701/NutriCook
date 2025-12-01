package com.example.nutricook.di

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.example.nutricook.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    private const val TAG = "CloudinaryModule"
    private const val DEFAULT_CLOUD_NAME_PLACEHOLDER = "your_cloud_name"

    @Provides
    @Singleton
    fun provideCloudinary(@ApplicationContext context: Context): MediaManager {
        // 1. Kiểm tra xem đã init chưa để tránh crash
        try {
            val existing = MediaManager.get()
            if (existing != null) return existing
        } catch (e: IllegalStateException) {
            // Chưa init, tiếp tục xử lý bên dưới
        }

        // 2. Lấy config
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val apiKey = BuildConfig.CLOUDINARY_API_KEY
        val apiSecret = BuildConfig.CLOUDINARY_API_SECRET

        // 3. Kiểm tra config hợp lệ
        val isConfigured = !(cloudName == DEFAULT_CLOUD_NAME_PLACEHOLDER || cloudName.isBlank())

        val configMap = if (isConfigured) {
            hashMapOf(
                "cloud_name" to cloudName,
                "api_key" to apiKey,
                "api_secret" to apiSecret
            )
        } else {
            Log.e(TAG, "⚠️ Missing Cloudinary config. Uploads will fail.")
            hashMapOf("cloud_name" to "dummy", "api_key" to "dummy", "api_secret" to "dummy")
        }

        // 4. Init an toàn
        try {
            MediaManager.init(context, configMap)
            Log.d(TAG, "MediaManager init success.")
            return MediaManager.get()
        } catch (e: Exception) {
            Log.e(TAG, "Init failed: ${e.message}")
            // Trả về dummy hoặc throw tùy chiến lược, ở đây throw để biết lỗi nếu config sai
            throw IllegalStateException("Cloudinary init error: ${e.message}")
        }
    }
}