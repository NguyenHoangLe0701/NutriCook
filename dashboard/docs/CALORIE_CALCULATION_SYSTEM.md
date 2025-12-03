# Hệ Thống Tính Calories - Triển Khai Chi Tiết

## 📋 Tổng quan

Hệ thống tính calories của NutriCook sử dụng **Google Gemini AI** để tính toán dinh dưỡng tự động từ tên món ăn, kết hợp với database nguyên liệu để tính toán chính xác calories và các thành phần dinh dưỡng.

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│              USER INPUT (Tên món ăn)                        │
│              Ví dụ: "1 quả táo", "100g bơ"                  │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│         GEMINI AI SERVICE (Tính tự động)                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  GeminiNutritionService.kt                           │  │
│  │  - Gọi Gemini API                                    │  │
│  │  - Parse JSON response                               │  │
│  │  - Trả về: calories, protein, fat, carb             │  │
│  └──────────────────┬───────────────────────────────────┘  │
└─────────────────────┼───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│         NUTRITION CALCULATOR (Tính từ nguyên liệu)          │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  NutritionCalculator.kt                              │  │
│  │  - Tính từ database nguyên liệu                      │  │
│  │  - Tính tổng dinh dưỡng                              │  │
│  │  - Chia theo số phần ăn                              │  │
│  └──────────────────┬───────────────────────────────────┘  │
└─────────────────────┼───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│         NUTRITION VIEW MODEL (Quản lý state)                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  NutritionViewModel.kt                               │  │
│  │  - Lưu trữ dữ liệu dinh dưỡng                        │  │
│  │  - Quản lý daily logs                                │  │
│  │  - Tính toán mục tiêu                                │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Các File và Vị Trí

### 1. **Gemini AI Service - Tính tự động từ tên món**

#### 📂 `mobile/app/src/main/java/com/example/nutricook/data/nutrition/GeminiNutritionService.kt`
**Nhiệm vụ:** Service gọi Gemini API để tính calories và dinh dưỡng từ tên món ăn.

**Các method quan trọng:**
- `calculateNutrition(foodName: String)`: Tính dinh dưỡng từ tên món
- `isApiKeyConfigured()`: Kiểm tra API key đã được cấu hình chưa
- `listAvailableModels()`: Lấy danh sách model có sẵn (debug)

**Data class:**
```kotlin
data class NutritionInfo(
    val calories: Float,    // Calories
    val protein: Float,     // Protein (g)
    val fat: Float,         // Fat (g)
    val carb: Float         // Carb (g)
)
```

---

### 2. **Nutrition Calculator - Tính từ nguyên liệu**

#### 📂 `mobile/app/src/main/java/com/example/nutricook/utils/NutritionCalculator.kt`
**Nhiệm vụ:** Utility class tính toán dinh dưỡng từ danh sách nguyên liệu.

**Các method:**
- `calculateNutrition()`: Tính tổng dinh dưỡng từ nguyên liệu
- `parseQuantity()`: Parse số lượng từ string (hỗ trợ phân số)
- `parseCalories()`: Parse calories từ string
- `calculateDailyValue()`: Tính % daily value

**Data classes:**
```kotlin
data class NutritionData(
    val calories: Double,
    val fat: Double,
    val carbs: Double,
    val protein: Double,
    val cholesterol: Double,
    val sodium: Double,
    val vitamin: Double,
    val vitaminDetails: VitaminDetails
)
```

---

### 3. **Nutrition ViewModel - Quản lý state**

#### 📂 `mobile/app/src/main/java/com/example/nutricook/viewmodel/nutrition/NutritionViewModel.kt`
**Nhiệm vụ:** ViewModel quản lý state và logic của màn hình theo dõi dinh dưỡng.

**Các method:**
- `loadData()`: Load dữ liệu dinh dưỡng từ Firestore
- `updateCaloriesTarget()`: Cập nhật mục tiêu calories
- `addMeal()`: Thêm bữa ăn vào log

---

### 4. **Custom Food Calculator Screen**

#### 📂 `mobile/app/src/main/java/com/example/nutricook/view/profile/CustomFoodCalculatorScreen.kt`
**Nhiệm vụ:** Màn hình cho phép user nhập tên món và tự động tính calories.

---

## 🔄 Luồng hoạt động chi tiết

### 1. **Tính calories tự động bằng Gemini AI**

#### Bước 1: User nhập tên món ăn
```
User nhập: "1 quả táo"
hoặc: "100g bơ"
hoặc: "1 bát cơm"
```

#### Bước 2: Gọi Gemini API
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/nutrition/GeminiNutritionService.kt
suspend fun calculateNutrition(foodName: String): NutritionInfo? = withContext(Dispatchers.IO) {
    // 1. Kiểm tra API key
    if (apiKey == null || apiKey.isBlank()) {
        Log.e("GeminiService", "API key not configured")
        return@withContext null
    }
    
    // 2. Tạo prompt cho Gemini
    val prompt = """Bạn là chuyên gia dinh dưỡng. Tính calories và dinh dưỡng cho món ăn: "$foodName". 
Trả về CHỈ JSON với format này, không có text khác:
{"calories": số_calories, "protein": số_gam_protein, "fat": số_gam_fat, "carb": số_gam_carb}"""
    
    // 3. Tạo request body
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
    
    // 4. Gửi request đến Gemini API
    val request = Request.Builder()
        .url("$baseUrl?key=$apiKey")
        .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
        .build()
    
    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()
    
    // ...
}
```

#### Bước 3: Parse JSON response
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/nutrition/GeminiNutritionService.kt
// Parse JSON từ response
val jsonResponse = JSONObject(responseBody)
val candidates = jsonResponse.getJSONArray("candidates")
val content = candidates.getJSONObject(0)
    .getJSONObject("content")
    .getJSONArray("parts")
    .getJSONObject(0)
    .getString("text")

// Tìm JSON object trong text (loại bỏ markdown)
var jsonText = content.trim()
jsonText = jsonText.replace("```json", "").replace("```", "").trim()

// Extract JSON
val jsonStart = jsonText.indexOf('{')
val jsonEnd = jsonText.lastIndexOf('}')
val nutritionJson = JSONObject(jsonText.substring(jsonStart, jsonEnd + 1))

// Parse values
val caloriesValue = nutritionJson.optDouble("calories", 0.0).toFloat()
val proteinValue = nutritionJson.optDouble("protein", 0.0).toFloat()
val fatValue = nutritionJson.optDouble("fat", 0.0).toFloat()
val carbValue = nutritionJson.optDouble("carb", 0.0).toFloat()

// Trả về NutritionInfo
return NutritionInfo(
    calories = caloriesValue,
    protein = proteinValue,
    fat = fatValue,
    carb = carbValue
)
```

#### Bước 4: Hiển thị kết quả
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/profile/CustomFoodCalculatorScreen.kt
// Gọi Gemini service
val nutrition = geminiService.calculateNutrition(foodName.trim())

if (nutrition != null && nutrition.calories > 0) {
    // Tự động điền vào các field
    calories = nutrition.calories.toInt().toString()
    protein = String.format("%.1f", nutrition.protein)
    fat = String.format("%.1f", nutrition.fat)
    carb = String.format("%.1f", nutrition.carb)
    
    showSuccess = true
    hasAutoCalculated = true
} else {
    geminiError = "Không thể tính calories tự động. Vui lòng nhập thủ công."
}
```

---

### 2. **Tính calories từ nguyên liệu**

#### Bước 1: User chọn nguyên liệu
```
User tạo công thức với các nguyên liệu:
- 2 quả trứng (200g)
- 100g bơ
- 500ml sữa
```

#### Bước 2: Tính dinh dưỡng từng nguyên liệu
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/utils/NutritionCalculator.kt
fun calculateNutrition(
    ingredients: List<IngredientItem>,
    foodItemsMap: Map<Long, FoodItemUI>,
    servings: Int = 1
): NutritionData {
    var totalCalories = 0.0
    var totalFat = 0.0
    var totalCarbs = 0.0
    var totalProtein = 0.0
    
    ingredients.forEach { ingredient ->
        if (ingredient.foodItemId != null) {
            val foodItem = foodItemsMap[ingredient.foodItemId]
            if (foodItem != null) {
                // 1. Parse số lượng
                val quantityInUnits = parseQuantity(ingredient.quantity) // "2" → 2.0
                
                // 2. Chuyển đổi sang gram
                val quantityInGrams = ingredient.unit.toGrams(quantityInUnits)
                // Ví dụ: 2 quả trứng → 200g
                
                // 3. Tính multiplier (giá trị trong FoodItemUI là trên 100g)
                val multiplier = quantityInGrams / 100.0
                // Ví dụ: 200g → multiplier = 2.0
                
                // 4. Tính dinh dưỡng
                val caloriesValue = parseCalories(foodItem.calories)
                val calories = caloriesValue * multiplier
                val fat = foodItem.fat * multiplier
                val carbs = foodItem.carbs * multiplier
                val protein = foodItem.protein * multiplier
                
                // 5. Tổng hợp
                totalCalories += calories
                totalFat += fat
                totalCarbs += carbs
                totalProtein += protein
            }
        }
    }
    
    // ...
}
```

#### Bước 3: Chia theo số phần ăn
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/utils/NutritionCalculator.kt
// Chia cho số phần ăn
if (servings > 0) {
    totalCalories /= servings
    totalFat /= servings
    totalCarbs /= servings
    totalProtein /= servings
}

// Trả về NutritionData
return NutritionData(
    calories = totalCalories,
    fat = totalFat,
    carbs = totalCarbs,
    protein = totalProtein,
    // ...
)
```

---

### 3. **Parse số lượng (hỗ trợ phân số)**

Hệ thống hỗ trợ nhiều định dạng số lượng:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/utils/NutritionCalculator.kt
private fun parseQuantity(quantityStr: String): Double {
    val cleaned = quantityStr.trim()
    
    // 1. Xử lý phân số: "1/2", "3/4", "1 1/2"
    val fractionRegex = Regex("""(\d+)?\s*(\d+)/(\d+)""")
    val fractionMatch = fractionRegex.find(cleaned)
    if (fractionMatch != null) {
        val wholePart = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        val numerator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 0.0
        val denominator = fractionMatch.groupValues[3].toDoubleOrNull() ?: 1.0
        if (denominator > 0) {
            return wholePart + (numerator / denominator)
        }
    }
    
    // 2. Xử lý số thập phân: "1.5", "2,5", "200"
    val numberPart = cleaned.filter { it.isDigit() || it == '.' || it == ',' }
        .replace(',', '.')
        .toDoubleOrNull() ?: 0.0
    
    return numberPart
}
```

**Ví dụ:**
- `"2"` → `2.0`
- `"1.5"` → `1.5`
- `"1/2"` → `0.5`
- `"1 1/2"` → `1.5`
- `"200"` → `200.0`

---

### 4. **Chuyển đổi đơn vị sang gram**

```kotlin
// Extension function để chuyển đổi đơn vị
fun IngredientUnit.toGrams(quantity: Double): Double {
    return when (this) {
        IngredientUnit.GRAM -> quantity
        IngredientUnit.KILOGRAM -> quantity * 1000.0
        IngredientUnit.MILLILITER -> quantity // 1ml ≈ 1g (nước)
        IngredientUnit.LITER -> quantity * 1000.0
        IngredientUnit.PIECE -> quantity * 100.0 // Mặc định: 1 cái = 100g
        // ...
    }
}
```

**Ví dụ:**
- `2 quả trứng` → `2 * 100g = 200g`
- `500ml sữa` → `500g`
- `100g bơ` → `100g`

---

## 🎯 Các Màn Hình Sử Dụng Tính Calories

### 1. **Custom Food Calculator Screen**

**File:** `mobile/app/src/main/java/com/example/nutricook/view/profile/CustomFoodCalculatorScreen.kt`

**Chức năng:**
- User nhập tên món ăn
- Click icon ✨ để tự động tính bằng Gemini AI
- Hoặc nhập thủ công calories, protein, fat, carb

**Code example:**
```kotlin
// Khi user click icon ✨
IconButton(onClick = {
    isLoadingGemini = true
    scope.launch {
        try {
            val nutrition = geminiService.calculateNutrition(foodName.trim())
            
            if (nutrition != null && nutrition.calories > 0) {
                // Tự động điền
                calories = nutrition.calories.toInt().toString()
                protein = String.format("%.1f", nutrition.protein)
                fat = String.format("%.1f", nutrition.fat)
                carb = String.format("%.1f", nutrition.carb)
            } else {
                geminiError = "Không thể tính calories tự động."
            }
        } catch (e: Exception) {
            geminiError = "Lỗi: ${e.message}"
        } finally {
            isLoadingGemini = false
        }
    }
}) {
    Icon(Icons.Outlined.AutoAwesome, "Tự động tính")
}
```

---

### 2. **Add Meal Screen**

**File:** `mobile/app/src/main/java/com/example/nutricook/view/profile/AddMealScreen.kt`

**Chức năng:**
- User thêm bữa ăn vào daily log
- Tính tổng calories đã tiêu thụ trong ngày

---

### 3. **Recipe Nutrition Calculator**

**File:** `mobile/app/src/main/java/com/example/nutricook/utils/NutritionCalculator.kt`

**Chức năng:**
- Tính dinh dưỡng từ danh sách nguyên liệu trong công thức
- Hỗ trợ chia theo số phần ăn (servings)

---

## 📊 Cấu Trúc Dữ Liệu

### NutritionInfo (Gemini AI)
```kotlin
data class NutritionInfo(
    val calories: Float,    // Calories
    val protein: Float,     // Protein (g)
    val fat: Float,         // Fat (g)
    val carb: Float         // Carb (g)
)
```

### NutritionData (Từ nguyên liệu)
```kotlin
data class NutritionData(
    val calories: Double,           // Calories
    val fat: Double,                // Fat (g)
    val carbs: Double,              // Carbs (g)
    val protein: Double,            // Protein (g)
    val cholesterol: Double,        // Cholesterol (mg)
    val sodium: Double,             // Sodium (mg)
    val vitamin: Double,            // Vitamin (% daily value)
    val vitaminDetails: VitaminDetails
)

data class VitaminDetails(
    val vitaminA: Double,
    val vitaminB1: Double,
    val vitaminB2: Double,
    val vitaminB3: Double,
    val vitaminB6: Double,
    val vitaminB9: Double,
    val vitaminB12: Double,
    val vitaminC: Double,
    val vitaminD: Double,
    val vitaminE: Double,
    val vitaminK: Double
)
```

---

## 🔧 Cấu Hình Gemini API

### 1. **Lấy API Key**

1. Truy cập: https://makersuite.google.com/app/apikey
2. Tạo API key mới
3. Copy API key

### 2. **Thêm vào Project**

#### Mobile (local.properties):
```properties
GEMINI_API_KEY=your_api_key_here
```

#### BuildConfig (build.gradle.kts):
```kotlin
android {
    buildFeatures {
        buildConfig = true
    }
}

android {
    defaultConfig {
        val geminiApiKey = project.findProperty("GEMINI_API_KEY") as? String ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }
}
```

### 3. **Sử dụng trong code**
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/nutrition/GeminiNutritionService.kt
private val apiKey: String? = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() }
```

---

## 🎯 Các Model Gemini được hỗ trợ

Hệ thống tự động thử các model theo thứ tự ưu tiên:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/nutrition/GeminiNutritionService.kt
private val baseUrls = listOf(
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent",
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-preview:generateContent"
)
```

**Thứ tự ưu tiên:**
1. `gemini-2.5-flash` - Nhanh nhất, đủ dùng
2. `gemini-2.5-pro` - Chính xác hơn
3. `gemini-3-pro-preview` - Mới nhất

---

## 📝 Prompt Engineering

### Prompt được sử dụng:

```kotlin
val prompt = """Bạn là chuyên gia dinh dưỡng. Tính calories và dinh dưỡng cho món ăn: "$foodName". 
Trả về CHỈ JSON với format này, không có text khác:
{"calories": số_calories, "protein": số_gam_protein, "fat": số_gam_fat, "carb": số_gam_carb}"""
```

**Đặc điểm:**
- Yêu cầu trả về JSON thuần (không có markdown, text khác)
- Format cố định để dễ parse
- Chỉ yêu cầu 4 giá trị cơ bản: calories, protein, fat, carb

---

## 🧮 Ví Dụ Tính Toán

### Ví dụ 1: Tính từ tên món (Gemini AI)

**Input:** `"1 quả táo"`

**Process:**
1. Gọi Gemini API với prompt: "Tính calories cho 1 quả táo"
2. Gemini trả về JSON:
   ```json
   {
     "calories": 95,
     "protein": 0.5,
     "fat": 0.3,
     "carb": 25
   }
   ```
3. Parse và hiển thị kết quả

**Output:**
- Calories: 95 kcal
- Protein: 0.5g
- Fat: 0.3g
- Carb: 25g

---

### Ví dụ 2: Tính từ nguyên liệu

**Input:**
- 2 quả trứng (200g) - Calories: 140/100g
- 100g bơ - Calories: 717/100g
- 500ml sữa - Calories: 42/100g

**Process:**
```
Trứng: 140 * (200/100) = 140 * 2 = 280 kcal
Bơ: 717 * (100/100) = 717 * 1 = 717 kcal
Sữa: 42 * (500/100) = 42 * 5 = 210 kcal

Tổng: 280 + 717 + 210 = 1207 kcal
```

**Nếu chia 4 phần:**
```
1207 / 4 = 301.75 kcal/phần
```

---

## 🔄 Luồng Hoàn Chỉnh

### **Tính tự động (Gemini):**

```
1. User nhập tên món: "1 quả táo"
   ↓
2. Click icon ✨ (AutoAwesome)
   ↓
3. GeminiNutritionService.calculateNutrition()
   ↓
4. Gửi request đến Gemini API
   ↓
5. Parse JSON response
   ↓
6. Tự động điền calories, protein, fat, carb
   ↓
7. User xem kết quả và có thể chỉnh sửa
```

### **Tính từ nguyên liệu:**

```
1. User tạo công thức với nguyên liệu
   ↓
2. NutritionCalculator.calculateNutrition()
   ↓
3. Parse số lượng từng nguyên liệu
   ↓
4. Chuyển đổi đơn vị sang gram
   ↓
5. Tính dinh dưỡng từ database
   ↓
6. Tổng hợp dinh dưỡng
   ↓
7. Chia theo số phần ăn (nếu có)
   ↓
8. Hiển thị kết quả
```

---

## 📊 Database Schema

### Firestore Collections:

```
nutrition/
  └── dailyLogs/
      └── {userId}/
          └── {dateId}/
              ├── date: "2024-01-15"
              ├── calories: 240
              ├── protein: 10.5
              ├── fat: 8.2
              ├── carb: 30
              └── meals: []
                  ├── {mealId}/
                  │   ├── name: "Bữa trưa"
                  │   ├── calories: 500
                  │   ├── protein: 20
                  │   ├── fat: 15
                  │   └── carb: 60
```

---

## ✅ Checklist Triển Khai

### Core Services:
- [x] ✅ GeminiNutritionService.kt - Gọi Gemini API
- [x] ✅ NutritionCalculator.kt - Tính từ nguyên liệu
- [x] ✅ NutritionViewModel.kt - Quản lý state

### UI Screens:
- [x] ✅ CustomFoodCalculatorScreen.kt - Màn hình tính calories tự động
- [x] ✅ AddMealScreen.kt - Thêm bữa ăn
- [x] ✅ ProfileScreens.kt - Hiển thị theo dõi calories

### Configuration:
- [x] ✅ Gemini API key configuration
- [x] ✅ BuildConfig setup
- [x] ✅ Error handling

---

## 🎉 Kết Luận

Hệ thống tính calories của NutriCook hoạt động với 2 phương pháp:

1. **Tự động bằng Gemini AI:**
   - ✅ Nhập tên món → Tự động tính
   - ✅ Nhanh chóng, tiện lợi
   - ✅ Hỗ trợ nhiều định dạng tên món

2. **Tính từ nguyên liệu:**
   - ✅ Chính xác từ database
   - ✅ Hỗ trợ nhiều đơn vị
   - ✅ Tính được vitamin, cholesterol, sodium

Cả hai phương pháp đều được tích hợp hoàn chỉnh và sẵn sàng sử dụng! 🚀

