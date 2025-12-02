# Hướng dẫn Triển Khai Gemini API - Tính Năng Tự Động Tính Calories

## 📋 Mục lục

1. [Tổng quan](#tổng-quan)
2. [Các bước cấu hình](#các-bước-cấu-hình)
3. [Các bước triển khai code](#các-bước-triển-khai-code)
4. [Luồng xử lý dữ liệu](#luồng-xử-lý-dữ-liệu)
5. [Troubleshooting](#troubleshooting)

## Tổng quan

Tính năng này cho phép người dùng nhập tên món ăn (ví dụ: "1 quả táo", "100g bơ") và hệ thống sẽ **tự động** tính calories và các chất dinh dưỡng thông qua Google Gemini API sau 1.5 giây ngừng gõ.

### Tính năng chính

- ✅ **Tự động tính calories từ tên món ăn** - Không cần click, tự động sau 1.5 giây
- ✅ **Tính protein, fat, carb** - Tự động tính đầy đủ thông tin dinh dưỡng
- ✅ **Hỗ trợ tiếng Việt** - Nhập tên món ăn bằng tiếng Việt
- ✅ **Hỗ trợ các đơn vị** (1 quả, 100g, 1 tô, v.v.)
- ✅ **Manual trigger** - Click icon ✨ để tính lại bất cứ lúc nào
- ✅ **Smart logic** - Chỉ tự động tính khi calories chưa được nhập thủ công

## Các bước cấu hình

### Bước 1: Lấy API Key từ Google AI Studio

1. Truy cập: https://makersuite.google.com/app/apikey
2. Đăng nhập bằng tài khoản Google
3. Tạo API key mới (nếu chưa có)
4. Copy API key (thường bắt đầu bằng `AIza...`)

### Bước 2: Cấu hình API Key trong project

#### Cách 1: Thêm vào local.properties (Khuyến nghị)

1. Mở file `mobile/local.properties`
2. Thêm dòng:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```
   Ví dụ:
   ```
   GEMINI_API_KEY=AIzaSyClCw...
   ```

3. **Rebuild project:**
   ```bash
   cd mobile
   ./gradlew clean
   ./gradlew build
   ```

#### Cách 2: Thêm vào .env (Root project)

1. Tạo file `.env` ở root project (cùng cấp với `mobile/`, `dashboard/`)
2. Thêm dòng:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```

3. **Rebuild project:**
   ```bash
   cd mobile
   ./gradlew clean
   ./gradlew build
   ```

⚠️ **Lưu ý**: Không commit API key vào Git! File `local.properties` và `.env` đã được thêm vào `.gitignore`.

## Các bước triển khai code

### Bước 1: Tạo GeminiNutritionService

**File:** `mobile/app/src/main/java/com/example/nutricook/data/nutrition/GeminiNutritionService.kt`

**Chức năng:**
- Gọi Gemini API để tính calories và dinh dưỡng
- Hỗ trợ nhiều model names (fallback)
- Parse JSON response và trả về `NutritionInfo`

**Code mẫu:**

```kotlin
@Singleton
class GeminiNutritionService @Inject constructor() {
    private val apiKey: String? = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() }
    private val client = OkHttpClient()
    
    private val baseUrls = listOf(
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent",
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-preview:generateContent"
    )
    
    suspend fun calculateNutrition(foodName: String): NutritionInfo? {
        // Implementation...
    }
    
    fun isApiKeyConfigured(): Boolean = apiKey != null && apiKey.isNotBlank()
}
```

### Bước 2: Tạo CustomFoodCalculatorScreen

**File:** `mobile/app/src/main/java/com/example/nutricook/view/profile/CustomFoodCalculatorScreen.kt`

**Tính năng chính:**

1. **Auto-trigger Gemini:**
   - Khi người dùng nhập tên món ăn (ít nhất 3 ký tự)
   - Sau 1.5 giây ngừng gõ → Tự động gọi Gemini
   - Chỉ tự động tính khi calories chưa được nhập thủ công
   - Sử dụng debouncing để tránh gọi API quá nhiều

2. **Manual trigger:**
   - Icon ✨ (AutoAwesome) để tính lại bất cứ lúc nào
   - Click icon sẽ ghi đè giá trị hiện tại

## Luồng xử lý dữ liệu

### 1. Lấy dữ liệu (Input từ User)

**Bước 1.1: Người dùng nhập tên món ăn**
```kotlin
// Trong CustomFoodCalculatorScreen
OutlinedTextField(
    value = foodName,
    onValueChange = { newValue ->
        foodName = newValue
        // Trigger auto-calculate sau 1.5 giây
    }
)
```

**Bước 1.2: Kiểm tra điều kiện để trigger auto-calculate**
```kotlin
// Điều kiện:
- foodName.trim().length >= 3  // Ít nhất 3 ký tự
- calories.isBlank() || calories == "0"  // Calories chưa nhập thủ công
- geminiService != null && geminiService.isApiKeyConfigured()  // Service có sẵn
- !isLoadingGemini  // Không đang tính
```

**Bước 1.3: Debounce 1.5 giây**
```kotlin
autoCalculateJob?.cancel()  // Hủy job cũ nếu có
autoCalculateJob = coroutineScope.launch {
    delay(1500)  // Đợi 1.5 giây
    // Nếu người dùng gõ tiếp → job sẽ bị cancel
    // Nếu ngừng gõ → tiếp tục gọi API
}
```

### 2. Đẩy tính (Gọi API)

**Bước 2.1: Tạo prompt cho Gemini**
```kotlin
val prompt = """Bạn là chuyên gia dinh dưỡng. Tính calories và dinh dưỡng cho món ăn: "$foodName". 
Trả về CHỈ JSON với format này, không có text khác:
{"calories": số_calories, "protein": số_gam_protein, "fat": số_gam_fat, "carb": số_gam_carb}"""
```

**Bước 2.2: Tạo request body**
```kotlin
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
```

**Bước 2.3: Gửi request đến Gemini API**
```kotlin
// Thử các endpoint theo thứ tự ưu tiên
for (baseUrl in baseUrls) {
    val request = Request.Builder()
        .url("$baseUrl?key=$apiKey")
        .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
        .addHeader("Content-Type", "application/json")
        .build()
    
    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()
    
    if (response.isSuccessful && responseBody != null) {
        // Parse response
        break
    } else {
        // Thử endpoint tiếp theo
        continue
    }
}
```

**Bước 2.4: Xử lý response**
```kotlin
val jsonResponse = JSONObject(responseBody)
val candidates = jsonResponse.getJSONArray("candidates")
val content = candidates.getJSONObject(0)
    .getJSONObject("content")
    .getJSONArray("parts")
    .getJSONObject(0)
    .getString("text")
```

### 3. Dữ liệu tính toán (Parse và Update UI)

**Bước 3.1: Extract JSON từ response**
```kotlin
// Xử lý markdown code blocks
var jsonText = content.trim()
jsonText = jsonText.replace("```json", "").replace("```", "").trim()

// Tìm JSON object trong text
val jsonStart = jsonText.indexOf('{')
val jsonEnd = jsonText.lastIndexOf('}')
if (jsonStart >= 0 && jsonEnd > jsonStart) {
    jsonText = jsonText.substring(jsonStart, jsonEnd + 1)
}
```

**Bước 3.2: Parse JSON thành NutritionInfo**
```kotlin
val nutritionJson = JSONObject(jsonText)
val caloriesValue = nutritionJson.optDouble("calories", 0.0).toFloat()
val proteinValue = nutritionJson.optDouble("protein", 0.0).toFloat()
val fatValue = nutritionJson.optDouble("fat", 0.0).toFloat()
val carbValue = nutritionJson.optDouble("carb", 0.0).toFloat()

// Validation
if (caloriesValue <= 0) {
    return null  // Invalid data
}

return NutritionInfo(
    calories = caloriesValue,
    protein = proteinValue,
    fat = fatValue,
    carb = carbValue
)
```

**Bước 3.3: Cập nhật UI với dữ liệu đã tính**
```kotlin
// Trong CustomFoodCalculatorScreen
if (nutrition != null && nutrition.calories > 0) {
    calories = nutrition.calories.toInt().toString()
    protein = String.format("%.1f", nutrition.protein)
    fat = String.format("%.1f", nutrition.fat)
    carb = String.format("%.1f", nutrition.carb)
    hasAutoCalculated = true
    showSuccess = true
}
```

**Bước 3.4: Lưu vào database**
```kotlin
// Khi người dùng click "Lưu món ăn"
onSave(
    foodName = foodName,
    calories = calories.toFloatOrNull() ?: 0f,
    protein = protein.toFloatOrNull() ?: 0f,
    fat = fat.toFloatOrNull() ?: 0f,
    carb = carb.toFloatOrNull() ?: 0f
)

// Trong ViewModel/Repository
nutritionVm.updateNutritionForDate(
    dateId = selectedDateId,
    calories = calories,
    protein = protein,
    fat = fat,
    carb = carb
)
```

### Sơ đồ luồng xử lý

```
┌─────────────────────────────────────────────────────────────┐
│ 1. LẤY DỮ LIỆU (Input từ User)                              │
├─────────────────────────────────────────────────────────────┤
│ User nhập tên món ăn → Kiểm tra điều kiện → Debounce 1.5s   │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. ĐẨY TÍNH (Gọi API)                                        │
├─────────────────────────────────────────────────────────────┤
│ Tạo prompt → Tạo request body → Gửi request → Nhận response │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. DỮ LIỆU TÍNH TOÁN (Parse và Update UI)                   │
├─────────────────────────────────────────────────────────────┤
│ Extract JSON → Parse thành NutritionInfo → Update UI        │
│ → Lưu vào database                                           │
└─────────────────────────────────────────────────────────────┘
```

## Troubleshooting

### Lỗi: "API key not configured"

**Nguyên nhân:** API key chưa được cấu hình hoặc chưa rebuild project.

**Giải pháp:**
1. Kiểm tra `local.properties` hoặc `.env` có API key không
2. Rebuild project: `./gradlew clean && ./gradlew build`
3. Kiểm tra log: `API key not configured`

### Lỗi: "All endpoints failed" - 404 Model Not Found

**Nguyên nhân:** Model names không đúng hoặc đã bị ngừng hỗ trợ.

**Giải pháp:**

1. **Kiểm tra model names hiện tại:**
   - Mở `GeminiNutritionService.kt`
   - Xem `baseUrls` có đúng không

2. **Lấy danh sách model có sẵn:**
   ```bash
   # Sử dụng curl
   curl "https://generativelanguage.googleapis.com/v1beta/models?key=YOUR_API_KEY"
   ```

3. **Cập nhật model names:**
   - Mở `GeminiNutritionService.kt`
   - Cập nhật `baseUrls` với model names mới nhất

4. **Model names hiện tại đang sử dụng:**
   - `gemini-2.5-flash` (v1beta) - Model mới nhất, nhanh
   - `gemini-2.5-pro` (v1beta) - Model mới nhất, mạnh
   - `gemini-3-pro-preview` (v1beta) - Preview version

### Lỗi: "Permission denied" - 403

**Nguyên nhân:** API key không có quyền truy cập Gemini API.

**Giải pháp:**
1. Kiểm tra API key tại: https://makersuite.google.com/app/apikey
2. Đảm bảo API key không bị restrict
3. Tạo API key mới nếu cần

### Icon ✨ không hiển thị

**Nguyên nhân:** API key chưa được cấu hình.

**Giải pháp:**
- Kiểm tra `isApiKeyConfigured()` trả về `true`
- Rebuild project sau khi thêm API key

## Tài liệu tham khảo

- [Google Gemini API Documentation](https://ai.google.dev/docs)
- [Gemini API Pricing](https://ai.google.dev/pricing)
- [List Models API](https://ai.google.dev/api/rest/generativelanguage/models)

