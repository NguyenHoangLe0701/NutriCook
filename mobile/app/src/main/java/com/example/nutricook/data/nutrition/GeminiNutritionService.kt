package com.example.nutricook.data.nutrition

import com.example.nutricook.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log
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
    // Dùng v1 API với gemini-1.5-flash (model nhanh, được hỗ trợ tốt)
    private val baseUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent"
    
    /**
     * Tính calories và dinh dưỡng từ tên món ăn
     * @param foodName Tên món ăn (ví dụ: "1 quả táo", "100g bơ")
     * @return NutritionInfo hoặc null nếu lỗi
     */
    suspend fun calculateNutrition(foodName: String): NutritionInfo? = withContext(Dispatchers.IO) {
        // Debug: Kiểm tra API key
        val apiKeyValue = BuildConfig.GEMINI_API_KEY
        Log.d("GeminiService", "BuildConfig.GEMINI_API_KEY length: ${apiKeyValue.length}, isBlank: ${apiKeyValue.isBlank()}")
        
        if (apiKey == null || apiKey.isBlank()) {
            Log.e("GeminiService", "API key is null or blank!")
            return@withContext null
        }
        
        try {
            val prompt = """Bạn là chuyên gia dinh dưỡng. Tính calories và dinh dưỡng cho món ăn: "$foodName". 
Trả về CHỈ JSON với format này, không có text khác:
{"calories": số_calories, "protein": số_gam_protein, "fat": số_gam_fat, "carb": số_gam_carb}"""
            
            // Request body đúng format cho Gemini API
            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }
            
            val request = Request.Builder()
                .url("$baseUrl?key=$apiKey")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null
            
            Log.d("GeminiService", "Response code: ${response.code}")
            
            if (!response.isSuccessful) {
                Log.e("GeminiService", "API failed: ${response.code} - $responseBody")
                return@withContext null
            }
            
            val jsonResponse = JSONObject(responseBody)
            
            // Kiểm tra error
            if (jsonResponse.has("error")) {
                Log.e("GeminiService", "API error: ${jsonResponse.getJSONObject("error")}")
                return@withContext null
            }
            
            val candidates = jsonResponse.getJSONArray("candidates")
            if (candidates.length() == 0) {
                Log.e("GeminiService", "No candidates in response")
                return@withContext null
            }
            
            val content = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
            
            Log.d("GeminiService", "Raw content: $content")
            
            // Xử lý markdown code blocks và tìm JSON
            var jsonText = content.trim()
            jsonText = jsonText.replace("```json", "").replace("```", "").trim()
            
            // Tìm JSON object trong text
            val jsonStart = jsonText.indexOf('{')
            val jsonEnd = jsonText.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonText = jsonText.substring(jsonStart, jsonEnd + 1)
            } else {
                Log.e("GeminiService", "No JSON found in content: $content")
                return@withContext null
            }
            
            Log.d("GeminiService", "Extracted JSON: $jsonText")
            
            val nutritionJson = JSONObject(jsonText)
            val caloriesValue = nutritionJson.optDouble("calories", 0.0).toFloat()
            val proteinValue = nutritionJson.optDouble("protein", 0.0).toFloat()
            val fatValue = nutritionJson.optDouble("fat", 0.0).toFloat()
            val carbValue = nutritionJson.optDouble("carb", 0.0).toFloat()
            
            Log.d("GeminiService", "Parsed: calories=$caloriesValue, protein=$proteinValue, fat=$fatValue, carb=$carbValue")
            
            if (caloriesValue <= 0) {
                Log.w("GeminiService", "Calories is 0 or negative")
                return@withContext null
            }
            
            NutritionInfo(
                calories = caloriesValue,
                protein = proteinValue,
                fat = fatValue,
                carb = carbValue
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Error: ${e.javaClass.simpleName} - ${e.message}", e)
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

