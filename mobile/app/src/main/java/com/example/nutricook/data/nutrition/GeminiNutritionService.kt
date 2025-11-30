package com.example.nutricook.data.nutrition

import com.example.nutricook.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service để tính calories và dinh dưỡng từ tên món ăn sử dụng Gemini API
 * 
 * Cách sử dụng:
 * 1. Lấy API key từ Google AI Studio: https://makersuite.google.com/app/apikey
 * 2. Thêm vào local.properties: GEMINI_API_KEY=your_api_key_here
 * 3. Rebuild project để BuildConfig được cập nhật
 */
@Singleton
class GeminiNutritionService @Inject constructor() {
    
    // Lấy API key từ BuildConfig (được tạo từ local.properties)
    private val apiKey: String? = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() }
    
    private val client = OkHttpClient()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
    
    /**
     * Tính calories và dinh dưỡng từ tên món ăn
     * @param foodName Tên món ăn (ví dụ: "1 quả táo", "100g bơ")
     * @return NutritionInfo hoặc null nếu lỗi
     */
    suspend fun calculateNutrition(foodName: String): NutritionInfo? = withContext(Dispatchers.IO) {
        if (apiKey == null) {
            return@withContext null
        }
        
        try {
            val prompt = """
                Bạn là chuyên gia dinh dưỡng. Hãy tính calories và các chất dinh dưỡng cho món ăn sau.
                Trả về JSON với format:
                {
                    "calories": số_calories,
                    "protein": số_gam_protein,
                    "fat": số_gam_fat,
                    "carb": số_gam_carb
                }
                
                Món ăn: "$foodName"
                
                Chỉ trả về JSON, không có text khác.
            """.trimIndent()
            
            val requestBody = JSONObject().apply {
                put("contents", JSONObject().apply {
                    put("parts", JSONObject().apply {
                        put("text", prompt)
                    })
                })
            }.toString().toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null
            
            if (!response.isSuccessful) {
                return@withContext null
            }
            
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.getJSONArray("candidates")
            if (candidates.length() == 0) {
                return@withContext null
            }
            
            val content = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
            
            // Parse JSON từ response
            val nutritionJson = JSONObject(content.trim())
            
            NutritionInfo(
                calories = nutritionJson.optDouble("calories", 0.0).toFloat(),
                protein = nutritionJson.optDouble("protein", 0.0).toFloat(),
                fat = nutritionJson.optDouble("fat", 0.0).toFloat(),
                carb = nutritionJson.optDouble("carb", 0.0).toFloat()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Kiểm tra xem API key đã được cấu hình chưa
     */
    fun isApiKeyConfigured(): Boolean = apiKey != null && apiKey.isNotBlank()
}

data class NutritionInfo(
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carb: Float
)

