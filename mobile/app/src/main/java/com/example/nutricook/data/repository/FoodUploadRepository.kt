package com.example.nutricook.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// !! QUAN TRỌNG: Thay IP này bằng IP Wi-Fi THẬT của máy tính bạn
private const val BASE_URL = "http://192.168.88.164:8080/"

@Singleton
class FoodUploadRepository @Inject constructor() {
    
    private val client = OkHttpClient()
    
    /**
     * Upload món ăn lên server
     */
    suspend fun uploadFood(
        name: String,
        calories: String,
        categoryId: Long,
        description: String?,
        rating: Double?,
        imageFile: File?,
        userId: Long? = null
    ): Result<String> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val actualUserId = userId ?: currentUser?.let { 
                // Có thể lấy userId từ Firebase user hoặc user document
                // Tạm thời để null, server sẽ tự xử lý
                null
            }
            
            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", name)
                .addFormDataPart("calories", calories)
                .addFormDataPart("categoryId", categoryId.toString())
            
            if (!description.isNullOrBlank()) {
                requestBodyBuilder.addFormDataPart("description", description)
            }
            
            if (rating != null) {
                requestBodyBuilder.addFormDataPart("rating", rating.toString())
            }
            
            if (actualUserId != null) {
                requestBodyBuilder.addFormDataPart("userId", actualUserId.toString())
            }
            
            if (imageFile != null && imageFile.exists()) {
                val imageRequestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                requestBodyBuilder.addFormDataPart(
                    "imageFile",
                    imageFile.name,
                    imageRequestBody
                )
            }
            
            val requestBody = requestBodyBuilder.build()
            
            val request = Request.Builder()
                .url("${BASE_URL}admin/api/foods/upload")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                Result.success(response.body?.string() ?: "Upload thành công")
            } else {
                Result.failure(Exception("Upload thất bại: ${response.code} - ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

