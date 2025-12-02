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
    // Thử các endpoint theo thứ tự ưu tiên
    // Format đúng: https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={api_key}
    // Lưu ý: Model names có thể thay đổi theo thời gian, kiểm tra tại: https://ai.google.dev/api/rest/generativelanguage/models
    // Nếu tất cả fail, gọi listModels() để lấy danh sách model có sẵn
    private val baseUrls = listOf(
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-preview:generateContent"
    )
    
    /**
     * Tính calories và dinh dưỡng từ tên món ăn
     * @param foodName Tên món ăn (ví dụ: "1 quả táo", "100g bơ")
     * @return NutritionInfo hoặc null nếu lỗi
     */
    suspend fun calculateNutrition(foodName: String): NutritionInfo? = withContext(Dispatchers.IO) {
        if (apiKey == null || apiKey.isBlank()) {
            Log.e("GeminiService", "API key not configured. Check local.properties or .env file")
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
            
            // Thử các endpoint theo thứ tự ưu tiên
            var lastError: Exception? = null
            var responseBody: String? = null
            var responseCode: Int = 0
            
            for (baseUrl in baseUrls) {
                try {
                    val request = Request.Builder()
                        .url("$baseUrl?key=$apiKey")
                        .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                        .addHeader("Content-Type", "application/json")
                        .build()
                    
                    val response = client.newCall(request).execute()
                    responseBody = response.body?.string()
                    responseCode = response.code
                    
                    if (responseBody != null) {
                        try {
                            val testJson = JSONObject(responseBody)
                            if (testJson.has("error")) {
                                val error = testJson.getJSONObject("error")
                                val errorCode = error.optInt("code", 0)
                                val errorStatus = error.optString("status", "")
                                
                                if (errorCode == 404 || errorStatus == "NOT_FOUND") {
                                    continue // Thử endpoint tiếp theo
                                } else if (errorCode == 403 || errorStatus == "PERMISSION_DENIED") {
                                    Log.e("GeminiService", "Permission denied. Check API key access.")
                                    return@withContext null
                                } else {
                                    Log.e("GeminiService", "API error: ${error.optString("message", "")}")
                                    return@withContext null
                                }
                            } else if (response.isSuccessful) {
                                break // Thành công
                            }
                        } catch (e: Exception) {
                            if (response.isSuccessful) {
                                break
                            }
                        }
                    }
                    
                    if (!response.isSuccessful && responseCode != 404) {
                        Log.e("GeminiService", "API failed: $responseCode")
                        return@withContext null
                    }
                } catch (e: Exception) {
                    lastError = e
                    Log.w("GeminiService", "Error with endpoint $baseUrl: ${e.message}")
                    continue
                }
            }
            
            if (responseBody == null || responseCode != 200) {
                Log.e("GeminiService", "All endpoints failed. Check API key and model names.")
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
            
            // Xử lý markdown code blocks và tìm JSON
            var jsonText = content.trim()
            jsonText = jsonText.replace("```json", "").replace("```", "").trim()
            
            // Tìm JSON object trong text
            val jsonStart = jsonText.indexOf('{')
            val jsonEnd = jsonText.lastIndexOf('}')
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                jsonText = jsonText.substring(jsonStart, jsonEnd + 1)
            } else {
                Log.e("GeminiService", "No JSON found in response")
                return@withContext null
            }
            
            val nutritionJson = JSONObject(jsonText)
            val caloriesValue = nutritionJson.optDouble("calories", 0.0).toFloat()
            val proteinValue = nutritionJson.optDouble("protein", 0.0).toFloat()
            val fatValue = nutritionJson.optDouble("fat", 0.0).toFloat()
            val carbValue = nutritionJson.optDouble("carb", 0.0).toFloat()
            
            if (caloriesValue <= 0) {
                Log.w("GeminiService", "Invalid calories value: $caloriesValue")
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
    
    /**
     * Lấy danh sách các model có sẵn từ Gemini API
     * Sử dụng khi cần debug hoặc tìm model name đúng
     */
    suspend fun listAvailableModels(): List<String>? = withContext(Dispatchers.IO) {
        if (apiKey == null || apiKey.isBlank()) {
            Log.e("GeminiService", "API key is null or blank!")
            return@withContext null
        }
        
        try {
            // Thử cả v1beta và v1
            val listUrls = listOf(
                "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey",
                "https://generativelanguage.googleapis.com/v1/models?key=$apiKey"
            )
            
            for (listUrl in listUrls) {
                try {
                    val request = Request.Builder()
                        .url(listUrl)
                        .get()
                        .build()
                    
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    
                    if (response.isSuccessful && responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("models")) {
                            val models = jsonResponse.getJSONArray("models")
                            val modelNames = mutableListOf<String>()
                            
                            for (i in 0 until models.length()) {
                                val model = models.getJSONObject(i)
                                val name = model.getString("name")
                                val supportedMethods = model.optJSONArray("supportedGenerationMethods")
                                
                                // Chỉ lấy các model hỗ trợ generateContent
                                if (supportedMethods != null) {
                                    for (j in 0 until supportedMethods.length()) {
                                        if (supportedMethods.getString(j) == "generateContent") {
                                            modelNames.add(name)
                                            break
                                        }
                                    }
                                }
                            }
                            
                            if (modelNames.isNotEmpty()) {
                                return@withContext modelNames
                            }
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            
            null
        } catch (e: Exception) {
            Log.e("GeminiService", "Error listing models: ${e.message}")
            null
        }
    }
}

data class NutritionInfo(
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carb: Float
)

