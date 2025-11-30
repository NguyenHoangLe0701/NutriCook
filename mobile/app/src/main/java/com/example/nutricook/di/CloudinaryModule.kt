package com.example.nutricook.di

import android.content.Context
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

    @Provides
    @Singleton
    fun provideCloudinary(@ApplicationContext context: Context): MediaManager {
        // Kiểm tra xem MediaManager đã được init chưa
        try {
            val existing = MediaManager.get()
            if (existing != null) {
                android.util.Log.d("CloudinaryModule", "MediaManager already initialized")
                return existing
            }
        } catch (e: Exception) {
            // MediaManager chưa được init, tiếp tục init
            android.util.Log.d("CloudinaryModule", "MediaManager not initialized yet")
        }
        
        // Lấy config từ BuildConfig (được tạo từ secrets.properties)
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val apiKey = BuildConfig.CLOUDINARY_API_KEY
        val apiSecret = BuildConfig.CLOUDINARY_API_SECRET
        
        // Kiểm tra xem đã cấu hình chưa
        if (cloudName == "your_cloud_name" || apiKey == "your_api_key" || apiSecret == "your_api_secret") {
            // Thay vì throw exception, log warning và init với giá trị mặc định
            android.util.Log.w("CloudinaryModule", 
                "Cloudinary chưa được cấu hình! Upload ảnh sẽ không hoạt động.\n" +
                "Vui lòng cập nhật các giá trị trong mobile/app/secrets.properties:\n" +
                "- CLOUDINARY_CLOUD_NAME\n" +
                "- CLOUDINARY_API_KEY\n" +
                "- CLOUDINARY_API_SECRET\n" +
                "Lấy thông tin từ: https://console.cloudinary.com/settings/api-keys"
            )
            
            // Init với giá trị mặc định để app không crash
            // MediaManager sẽ hoạt động nhưng upload sẽ fail
            val defaultConfig = hashMapOf(
                "cloud_name" to "default",
                "api_key" to "default",
                "api_secret" to "default"
            )
            try {
                MediaManager.init(context, defaultConfig)
                return MediaManager.get()
            } catch (e: Exception) {
                android.util.Log.e("CloudinaryModule", "Failed to init MediaManager: ${e.message}")
                // Nếu init fail, vẫn trả về MediaManager.get() để không crash
                return try {
                    MediaManager.get()
                } catch (e2: Exception) {
                    // Nếu vẫn fail, tạo một instance mới (có thể không hoạt động nhưng không crash)
                    throw IllegalStateException("Cannot initialize MediaManager. Please configure Cloudinary in secrets.properties")
                }
            }
        }
        
        val config = hashMapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret
        )
        
        try {
            MediaManager.init(context, config)
            return MediaManager.get()
        } catch (e: Exception) {
            android.util.Log.e("CloudinaryModule", "Failed to init MediaManager with config: ${e.message}")
            // Fallback: trả về MediaManager.get() nếu đã được init trước đó
            return try {
                MediaManager.get()
            } catch (e2: Exception) {
                throw IllegalStateException("Cannot initialize MediaManager: ${e.message}")
            }
        }
    }
}

