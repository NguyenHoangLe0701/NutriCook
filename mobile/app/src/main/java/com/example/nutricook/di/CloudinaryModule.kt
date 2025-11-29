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
        // Lấy config từ BuildConfig (được tạo từ secrets.properties)
        val cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME
        val apiKey = BuildConfig.CLOUDINARY_API_KEY
        val apiSecret = BuildConfig.CLOUDINARY_API_SECRET
        
        // Kiểm tra xem đã cấu hình chưa
        if (cloudName == "your_cloud_name" || apiKey == "your_api_key" || apiSecret == "your_api_secret") {
            throw IllegalStateException(
                "Cloudinary chưa được cấu hình! Vui lòng cập nhật các giá trị trong mobile/app/secrets.properties:\n" +
                "- CLOUDINARY_CLOUD_NAME\n" +
                "- CLOUDINARY_API_KEY\n" +
                "- CLOUDINARY_API_SECRET\n" +
                "Lấy thông tin từ: https://console.cloudinary.com/settings/api-keys"
            )
        }
        
        val config = hashMapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret
        )
        
        MediaManager.init(context, config)
        return MediaManager.get()
    }
}

