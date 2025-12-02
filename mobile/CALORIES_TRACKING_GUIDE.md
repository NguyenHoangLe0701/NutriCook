# 📊 Hướng Dẫn Triển Khai Tính Năng Theo Dõi Calories

## 📋 Tổng Quan

Tính năng theo dõi calories cho phép người dùng:
- Thêm món ăn và tự động tính calories bằng Gemini API
- Nhập thủ công thông tin dinh dưỡng
- Theo dõi calories hàng ngày với biểu đồ trực quan
- Reset dữ liệu khi cần thiết
- Hiển thị cảnh báo khi vượt quá mục tiêu
- **Bổ sung món ăn cho các ngày trước** (tính năng mới)

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

### 1. **Jetpack Compose** 📱
- `TextField`, `OutlinedTextField`: Input fields
- `Button`, `OutlinedButton`: Nút thao tác
- `Card`: Container cho UI components
- `Canvas`: Vẽ biểu đồ và progress circle
- `LazyColumn`, `LazyRow`: Hiển thị danh sách

### 2. **Architecture Components** 🏗️
- **ViewModel**: `NutritionViewModel` quản lý state
- **Repository**: `NutritionRepository` xử lý data
- **StateFlow**: Quản lý UI state reactive
- **Hilt**: Dependency injection

### 3. **Firebase Firestore** 🔥
- Collection: `users/{uid}/daily_logs`
- Document ID: Format `yyyy-MM-dd` (ví dụ: "2025-11-30")
- Fields: `calories`, `protein`, `fat`, `carb`, `dateId`

### 4. **Gemini API** 🤖
- Tự động tính calories từ tên món ăn
- Thư viện: `okhttp3` cho HTTP requests
- Model: `gemini-1.5-flash`

## 📐 Kiến Trúc

```
ProfileScreen
    └── CaloriesTrackingCard
        ├── Date Picker (chọn ngày để xem/bổ sung)
        ├── Progress Circle (hiển thị calories ngày được chọn)
        ├── Macronutrient Breakdown (Protein, Fat, Carb)
        ├── 7-Day Chart (biểu đồ lịch sử)
        ├── Button "Thêm" → Navigate to AddMealScreen (lưu vào ngày được chọn)
        ├── Button "Reset" → Reset today's data (chỉ khi xem hôm nay)
        └── Button "Quay về hôm nay" → Reset về xem dữ liệu hôm nay

AddMealScreen
    ├── Title hiển thị ngày (nếu đang bổ sung cho ngày trước)
    ├── Search Bar (tìm món ăn)
    ├── Quick Suggestions (QuickFoodChip)
    ├── Gemini Auto Calculation
    └── Manual Input Fields
        └── Button "Lưu" → updateTodayNutrition() hoặc updateNutritionForDate()

CustomFoodCalculatorScreen
    ├── Food Name Input (với Gemini icon)
    ├── Nutrition Fields (Calories, Protein, Fat, Carb)
    └── Button "Lưu món ăn" → updateTodayNutrition()

NutritionViewModel
    ├── loadData() → getTodayLog() + getWeeklyHistory()
    ├── loadDataForDate(dateId) → Load dữ liệu cho ngày cụ thể
    ├── selectDate(dateId) → Chọn ngày và load dữ liệu
    ├── updateTodayNutrition() → cộng dồn vào database (hôm nay)
    ├── updateNutritionForDate(dateId, ...) → cộng dồn vào database (ngày cụ thể)
    ├── resetTodayNutrition() → reset về 0
    └── resetToToday() → Quay về xem dữ liệu hôm nay

NutritionRepository
    ├── getTodayLog() → Lấy dữ liệu ngày hôm nay
    ├── getLogForDate(dateId) → Lấy dữ liệu cho một ngày cụ thể
    ├── updateTodayNutrition() → Cộng dồn calories cho hôm nay
    ├── updateNutritionForDate(dateId, ...) → Cộng dồn calories cho ngày cụ thể
    ├── resetTodayNutrition() → Reset về 0
    ├── getWeeklyHistory() → Lấy 7 ngày gần nhất
    └── dateToDateId() / dateIdToDate() → Helper chuyển đổi Date ↔ dateId
```

## 🎯 Các Bước Triển Khai

### Bước 1: Tạo Data Model

**File**: `mobile/app/src/main/java/com/example/nutricook/model/nutrition/DailyLog.kt`

```kotlin
data class DailyLog(
    val dateId: String = "", // Format "2025-11-30"
    val calories: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val carb: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
)
```

### Bước 2: Tạo NutritionRepository

**File**: `mobile/app/src/main/java/com/example/nutricook/data/nutrition/NutritionRepository.kt`

**Chức năng chính**:

1. **getTodayLog()**: Lấy dữ liệu ngày hôm nay
   - Sử dụng `getTodayDateId()` để tạo ID (format "yyyy-MM-dd")
   - Query: `users/{uid}/daily_logs/{todayId}`

2. **updateTodayNutrition()**: Cộng dồn calories
   - **QUAN TRỌNG**: Chỉ cộng dồn phần tăng thêm, không phải tổng
   - Sử dụng Firestore Transaction để đảm bảo atomicity
   - Validation: Giới hạn calories 0-10000 kcal
   - Logging để debug

3. **resetTodayNutrition()**: Reset về 0
   - Set tất cả giá trị về 0
   - Tự động reload sau khi reset

4. **getWeeklyHistory()**: Lấy 7 ngày gần nhất
   - Query với `orderBy("dateId", DESCENDING)`
   - Limit 7 documents
   - Reverse để hiển thị từ cũ đến mới

**Code mẫu**:

```kotlin
suspend fun updateTodayNutrition(calories: Float, protein: Float, fat: Float, carb: Float) {
    // Validation
    val validCalories = calories.coerceIn(0f, 10000f)
    val validProtein = protein.coerceIn(0f, 1000f)
    val validFat = fat.coerceIn(0f, 1000f)
    val validCarb = carb.coerceIn(0f, 2000f)
    
    val todayId = getTodayDateId()
    val docRef = logsCol().document(todayId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(docRef)
        if (snapshot.exists()) {
            // CỘNG DỒN phần tăng thêm
            val current = snapshot.toObject(DailyLog::class.java)!!
            transaction.update(docRef, mapOf(
                "calories" to (current.calories + validCalories),
                "protein" to (current.protein + validProtein),
                "fat" to (current.fat + validFat),
                "carb" to (current.carb + validCarb)
            ))
        } else {
            // Tạo mới nếu chưa có
            transaction.set(docRef, DailyLog(
                dateId = todayId,
                calories = validCalories,
                protein = validProtein,
                fat = validFat,
                carb = validCarb
            ))
        }
    }.await()
}
```

### Bước 3: Tạo NutritionViewModel

**File**: `mobile/app/src/main/java/com/example/nutricook/viewmodel/nutrition/NutritionViewModel.kt`

**Chức năng**:

1. **loadData()**: Load dữ liệu ngày hôm nay và lịch sử
2. **updateTodayNutrition()**: Gọi repository và reload
3. **resetTodayNutrition()**: Reset và reload

**Code mẫu**:

```kotlin
fun updateTodayNutrition(cal: Float, pro: Float, fat: Float, carb: Float) = viewModelScope.launch {
    try {
        repo.updateTodayNutrition(cal, pro, fat, carb)
        loadData() // Reload để cập nhật UI
        _ui.update { it.copy(message = "Đã cập nhật dinh dưỡng!") }
    } catch (e: Exception) {
        _ui.update { it.copy(message = "Lỗi: ${e.message}") }
    }
}

fun resetTodayNutrition() = viewModelScope.launch {
    try {
        repo.resetTodayNutrition()
        loadData() // Reload để cập nhật biểu đồ
        _ui.update { it.copy(message = "Đã reset dữ liệu hôm nay!") }
    } catch (e: Exception) {
        _ui.update { it.copy(message = "Lỗi khi reset: ${e.message}") }
    }
}
```

### Bước 4: Tạo AddMealScreen

**File**: `mobile/app/src/main/java/com/example/nutricook/view/profile/AddMealScreen.kt`

**QUAN TRỌNG - Logic cộng dồn**:

- **KHÔNG** khởi tạo với `initialCalories` (tổng hiện tại)
- Form bắt đầu rỗng, chỉ nhập **phần tăng thêm**
- Khi click QuickFoodChip: Cộng vào giá trị hiện tại của form
- Khi save: Chỉ truyền phần nhập thêm vào `updateTodayNutrition()`

**Code mẫu**:

```kotlin
// KHÔNG khởi tạo với initial values
var cal by remember { mutableStateOf("") }
var pro by remember { mutableStateOf("") }
var fat by remember { mutableStateOf("") }
var carb by remember { mutableStateOf("") }

// Tính tổng để hiển thị (chỉ để hiển thị, không dùng để save)
val totalCalories = (initialCalories + (cal.toFloatOrNull() ?: 0f))

// Khi click QuickFoodChip
QuickFoodChip(food) {
    val currentCal = cal.toFloatOrNull() ?: 0f
    cal = (currentCal + food.calories).toString() // Chỉ cộng vào form
    // ...
}

// Khi save - CHỈ truyền phần tăng thêm
Button(onClick = {
    onSave(
        cal.toFloatOrNull() ?: 0f,  // Chỉ phần nhập thêm
        pro.toFloatOrNull() ?: 0f,
        fat.toFloatOrNull() ?: 0f,
        carb.toFloatOrNull() ?: 0f
    )
})
```

**Tính năng**:

1. **Search Bar**: Tìm món ăn trong danh sách
2. **Gemini Auto Calculation**: Tự động tính calories nếu không tìm thấy
3. **Quick Suggestions**: Click để thêm nhanh
4. **Manual Input**: Nhập thủ công với validation
5. **Reset Button**: Xóa form để nhập lại

### Bước 5: Tạo CustomFoodCalculatorScreen

**File**: `mobile/app/src/main/java/com/example/nutricook/view/profile/CustomFoodCalculatorScreen.kt`

**Tính năng**:

1. **Food Name Input**: 
   - Placeholder: "Ví dụ: Cá ngừ 200gr, 1 quả táo..."
   - Icon ✨ để tự động tính bằng Gemini API
   - Chỉ cập nhật nếu field trống (bảo vệ giá trị nhập thủ công)

2. **Nutrition Fields**:
   - Calories, Protein, Fat, Carb
   - **Validation**: Chỉ cho phép số dương (0-9 và dấu chấm)
   - Tự động lọc: Loại bỏ chữ cái, số âm, ký tự đặc biệt
   - Hiển thị lỗi: Border đỏ nếu giá trị không hợp lệ

3. **Reset Button**:
   - OutlinedButton màu đỏ
   - Xóa tất cả fields và thông báo

**Code validation với DecimalInputHelper**:

```kotlin
OutlinedTextField(
    value = calories,
    onValueChange = { newValue ->
        // Normalize: hỗ trợ cả dấu phẩy và dấu chấm, GIỮ NGUYÊN theo sở thích người dùng
        calories = DecimalInputHelper.normalizeDecimalInput(newValue)
    },
    isError = !DecimalInputHelper.isValid(calories) // Báo đỏ nếu ".9" hoặc ",9"
)
```

### Bước 6: Tạo CaloriesTrackingCard

**File**: `mobile/app/src/main/java/com/example/nutricook/view/profile/ProfileScreens.kt`

**Tính năng**:

1. **Progress Circle**:
   - Hiển thị calories hiện tại
   - Màu đỏ nếu vượt quá target
   - Hiển thị phần trăm vượt quá
   - Vòng tròn cảnh báo khi vượt quá

2. **Macronutrient Breakdown**:
   - Protein (màu xanh dương)
   - Carb (màu xanh lá)
   - Fat (màu cam)
   - Progress bars với màu tương ứng

3. **7-Day Chart**:
   - Line graph với gradient fill
   - Màu đỏ nếu vượt quá target
   - Dấu cảnh báo trên các điểm vượt quá
   - Horizontal line chỉ target

4. **Reset Button**:
   - Chỉ hiển thị khi có dữ liệu (calories > 0)
   - OutlinedButton màu đỏ
   - Gọi `nutritionVm.resetTodayNutrition()`

**Code mẫu - Progress Circle**:

```kotlin
val progress = todayCalories / caloriesTarget // Không giới hạn ở 1f
val isOverTarget = progress > 1f
val displayProgress = if (isOverTarget) 1f else progress.coerceIn(0f, 1f)

Canvas {
    // Vẽ background circle
    drawArc(color = Color(0xFFE5E7EB), ...)
    
    // Vẽ progress - màu đỏ nếu vượt quá
    drawArc(
        color = if(isOverTarget) Color(0xFFEF4444) else TealPrimary,
        sweepAngle = displayProgress * 360f,
        ...
    )
    
    // Vòng tròn cảnh báo nếu vượt quá
    if (isOverTarget) {
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = 0.2f),
            radius = radius + strokeWidth / 2 + 4.dp.toPx(),
            style = Stroke(width = 3f)
        )
    }
}
```

### Bước 7: Tạo DecimalInputHelper (Tối ưu UX)

**File**: `mobile/app/src/main/java/com/example/nutricook/utils/DecimalInputHelper.kt`

**Tính năng**:
- Hỗ trợ cả dấu phẩy (,) và dấu chấm (.) cho decimal separator
- **GIỮ NGUYÊN** dấu phẩy hoặc dấu chấm theo sở thích người dùng (không tự động chuyển)
- Báo đỏ nếu bắt đầu bằng dấu decimal (".9", ",9") - bắt buộc phải nhập "0.9" hoặc "0,9"
- Chỉ cho phép số và 1 dấu decimal separator
- Tự động thêm "0" chỉ khi parse (khi save), không thêm ngay khi nhập

**Code mẫu**:

```kotlin
object DecimalInputHelper {
    /**
     * Normalize decimal input - GIỮ NGUYÊN dấu phẩy hoặc dấu chấm theo sở thích người dùng
     * KHÔNG tự động thêm "0" ngay (để hiển thị error state)
     */
    fun normalizeDecimalInput(input: String): String {
        if (input.isBlank()) return ""
        
        // Cho phép số, dấu chấm và dấu phẩy
        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
        
        // Chỉ cho phép 1 dấu decimal separator
        // Nếu có cả 2, giữ dấu đầu tiên xuất hiện
        val dotIndex = filtered.indexOf('.')
        val commaIndex = filtered.indexOf(',')
        
        val normalized = when {
            dotIndex >= 0 && commaIndex >= 0 -> {
                // Nếu có cả 2, giữ dấu xuất hiện trước
                if (dotIndex < commaIndex) {
                    filtered.replace(",", "") // Giữ dấu chấm
                } else {
                    filtered.replace(".", "") // Giữ dấu phẩy
                }
            }
            else -> filtered // Giữ nguyên dấu phẩy hoặc dấu chấm
        }
        
        // KHÔNG tự động thêm "0" ở đây - để hiển thị error state
        return normalized
    }
    
    /**
     * Normalize và tự động thêm "0" (cho parse khi save)
     */
    fun normalizeForParse(input: String): String {
        val normalized = normalizeDecimalInput(input)
        
        // Tự động thêm "0" trước dấu decimal nếu bắt đầu bằng dấu đó
        return when {
            normalized.startsWith(".") -> "0$normalized"
            normalized.startsWith(",") -> "0$normalized"
            normalized == "." -> "0."
            normalized == "," -> "0,"
            else -> normalized
        }
    }
    
    /**
     * Parse string thành Float, hỗ trợ cả dấu phẩy và dấu chấm
     * Chuyển dấu phẩy thành dấu chấm khi parse (vì Float.parseFloat() chỉ nhận dấu chấm)
     */
    fun parseToFloat(input: String): Float? {
        if (input.isBlank()) return null
        
        val normalized = normalizeForParse(input)
        // Chuyển dấu phẩy thành dấu chấm để parse
        val forParse = normalized.replace(",", ".")
        return forParse.toFloatOrNull()
    }
    
    /**
     * Kiểm tra giá trị có hợp lệ không
     * Trả về false nếu bắt đầu bằng dấu decimal (".9", ",9")
     */
    fun isValid(value: String): Boolean {
        if (value.isBlank()) return true
        
        val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }
        
        // Kiểm tra nếu bắt đầu bằng dấu decimal (".9", ",9") - KHÔNG hợp lệ
        val startsWithDecimal = filtered.startsWith(".") || filtered.startsWith(",")
        val hasDigitsAfter = filtered.drop(1).any { it.isDigit() }
        
        if (startsWithDecimal && hasDigitsAfter) {
            // Bắt đầu bằng dấu decimal - bắt buộc phải nhập "0.9" hoặc "0,9"
            return false
        }
        
        val floatValue = parseToFloat(value)
        return floatValue != null && floatValue >= 0
    }
}
```

**Sử dụng trong TextField**:

```kotlin
OutlinedTextField(
    value = calories,
    onValueChange = { newValue ->
        // Normalize: hỗ trợ cả dấu phẩy và dấu chấm, GIỮ NGUYÊN theo sở thích người dùng
        calories = DecimalInputHelper.normalizeDecimalInput(newValue)
    },
    isError = !DecimalInputHelper.isValid(calories) // Báo đỏ nếu ".9" hoặc ",9"
)
```

**Ví dụ hoạt động**:
- Nhập "." → Giữ nguyên "." và hiển thị viền đỏ (error) - chưa có số
- Nhập "," → Giữ nguyên "," và hiển thị viền đỏ (error) - chưa có số
- Nhập ".9" → Giữ nguyên ".9" và hiển thị viền đỏ (error) - bắt buộc phải nhập "0.9"
- Nhập ",9" → Giữ nguyên ",9" và hiển thị viền đỏ (error) - bắt buộc phải nhập "0,9"
- Nhập "0.9" → Giữ nguyên "0.9" (không viền đỏ, hợp lệ)
- Nhập "0,9" → Giữ nguyên "0,9" (không viền đỏ, hợp lệ)
- Nhập "25,5" → Giữ nguyên "25,5" (hợp lệ, người dùng thích dấu phẩy)
- Nhập "25.5" → Giữ nguyên "25.5" (hợp lệ, người dùng thích dấu chấm)
- Nhập "abc" → Tự động lọc, chỉ còn số
- Nhập "-10" → Tự động lọc, không cho số âm

**Lưu ý**: 
- Khi chỉ nhập dấu decimal (".", ",") → Hiển thị viền đỏ để cảnh báo
- Khi bắt đầu bằng dấu decimal (".9", ",9") → Hiển thị viền đỏ, bắt buộc phải nhập "0.9" hoặc "0,9"
- Dấu phẩy (,) KHÔNG tự động chuyển thành dấu chấm (.) - giữ nguyên theo sở thích người dùng
- Hệ thống chấp nhận cả dấu phẩy và dấu chấm khi parse (chuyển dấu phẩy thành dấu chấm chỉ khi parse)
- Tự động thêm "0" chỉ khi parse (khi save), không thêm ngay khi nhập

### Bước 8: Validation Input

**Nguyên tắc**:

1. **Hỗ trợ cả dấu phẩy và dấu chấm**:
   - Sử dụng `DecimalInputHelper.normalizeDecimalInput()`
   - Tự động chuyển dấu phẩy thành dấu chấm
   - Tự động thêm "0" nếu bắt đầu bằng dấu decimal

2. **Chỉ cho phép số dương**:
   - Filter: `it.isDigit() || it == '.' || it == ','`
   - Không cho phép: Chữ cái, số âm, ký tự đặc biệt
   - Tối đa 1 dấu decimal separator

3. **Range validation**:
   - Calories: 0-10000 kcal
   - Protein: 0-1000 g
   - Fat: 0-1000 g
   - Carb: 0-2000 g

4. **Error state**:
   - `isError = !DecimalInputHelper.isValid(value)`
   - Border đỏ khi có lỗi

### Bước 9: Reset Functionality

**Có 2 loại Reset**:

1. **Reset Form** (trong AddMealScreen/CustomFoodCalculatorScreen):
   - Chỉ xóa dữ liệu trong form
   - Không ảnh hưởng dữ liệu đã lưu
   - Cho phép nhập lại từ đầu

2. **Reset Data** (trong CaloriesTrackingCard):
   - Reset dữ liệu ngày hôm nay về 0 trong database
   - Tự động reload để cập nhật biểu đồ
   - Hiển thị thông báo "Đã reset dữ liệu hôm nay!"

**Code mẫu**:

```kotlin
// Reset Form
OutlinedButton(
    onClick = {
        cal = ""
        pro = ""
        fat = ""
        carb = ""
    }
) {
    Icon(Icons.Filled.Refresh, ...)
    Text("Reset")
}

// Reset Data
OutlinedButton(
    onClick = { nutritionVm.resetTodayNutrition() }
) {
    Icon(Icons.Filled.Refresh, ...)
    Text("Reset")
}
```

## ⚠️ Lưu Ý Quan Trọng

### 1. Logic Cộng Dồn Calories

**SAI** ❌:
```kotlin
// AddMealScreen khởi tạo với initialCalories
var cal = initialCalories.toString() // 100

// Click "quả táo" (25 kcal)
cal = (100 + 25).toString() // 125

// Save
updateTodayNutrition(125, ...) // Truyền tổng

// Repository cộng dồn
current.calories + 125 = 100 + 125 = 225 // SAI!
```

**ĐÚNG** ✅:
```kotlin
// AddMealScreen KHÔNG khởi tạo với initialCalories
var cal = "" // Rỗng

// Click "quả táo" (25 kcal)
cal = "25" // Chỉ phần tăng thêm

// Save
updateTodayNutrition(25, ...) // Chỉ truyền phần tăng thêm

// Repository cộng dồn
current.calories + 25 = 100 + 25 = 125 // ĐÚNG!
```

### 2. Validation Input

- **Luôn validate** trước khi save
- **Chỉ cho phép số dương** (filter chữ cái, số âm)
- **Giới hạn range** để tránh giá trị bất thường
- **Hiển thị lỗi** rõ ràng cho người dùng

### 3. Biểu Đồ Khi Vượt Quá

- **Progress không giới hạn ở 1f**: Cho phép hiển thị > 100%
- **Màu đỏ** khi vượt quá target
- **Hiển thị phần trăm vượt quá**
- **Vòng tròn cảnh báo** để dễ nhận biết

## 🧪 Testing

### Test Cases

1. **Thêm món ăn từ Quick Suggestions**:
   - Click "Cơm trắng" → Calories tăng đúng 130 kcal
   - Click thêm "Trứng ốp la" → Calories tăng thêm 90 kcal
   - Tổng: 220 kcal (đúng)

2. **Tính calories tự động với Gemini**:
   - Nhập "1 quả táo" → Click icon ✨
   - Calories tự động điền (ví dụ: 25 kcal)
   - Save → Calories tăng đúng 25 kcal

3. **Nhập thủ công**:
   - Nhập "25" vào Calories field
   - Save → Calories tăng đúng 25 kcal
   - Không bị gấp đôi

4. **Validation và Decimal Input**:
   - Nhập "abc" → Tự động lọc, chỉ còn số
   - Nhập ".9" → Hiển thị viền đỏ (error), bắt buộc phải nhập "0.9"
   - Nhập ",9" → Hiển thị viền đỏ (error), bắt buộc phải nhập "0,9"
   - Nhập "0.9" → Hợp lệ, không viền đỏ
   - Nhập "0,9" → Hợp lệ, không viền đỏ (giữ nguyên dấu phẩy)
   - Nhập "25.5" → Hợp lệ, giữ nguyên dấu chấm
   - Nhập "25,5" → Hợp lệ, giữ nguyên dấu phẩy
   - Protein nhập "0.9", Fat nhập "0,9" → Cả 2 đều hợp lệ và được parse đúng
   - Nhập "-10" → Tự động lọc, không cho số âm
   - Nhập "25.5" → Cho phép decimal

5. **Reset**: 
   - Bấm Reset trong form → Form xóa, dữ liệu vẫn giữ
   - Bấm Reset trong CaloriesTrackingCard → Dữ liệu về 0, biểu đồ cập nhật

6. **Bổ sung món ăn cho ngày trước**:
   - Click vào ngày/icon calendar → Mở DatePickerDialog
   - Chọn ngày trước → Hiển thị dữ liệu ngày đó
   - Click "Thêm" → Mở AddMealScreen với title "Bổ sung món ăn - [ngày]"
   - Thêm món ăn → Lưu vào ngày đã chọn (không phải hôm nay)
   - Click "Quay về hôm nay" → Quay lại xem dữ liệu hôm nay

### Bước 10: Triển Khai Tính Năng Bổ Sung Món Ăn Cho Ngày Trước

**Mục đích**: Cho phép người dùng bổ sung món ăn đã quên nhập cho các ngày trước.

#### 10.1. Cập Nhật NutritionRepository

**File**: `mobile/app/src/main/java/com/example/nutricook/data/nutrition/NutritionRepository.kt`

**Thêm các hàm mới**:

```kotlin
// Helper: Chuyển đổi Date thành dateId (format "yyyy-MM-dd")
fun dateToDateId(date: Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(date)
}

// Helper: Chuyển đổi dateId thành Date
fun dateIdToDate(dateId: String): Date? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.parse(dateId)
    } catch (e: Exception) {
        null
    }
}

// Lấy dữ liệu cho một ngày cụ thể (không chỉ hôm nay)
suspend fun getLogForDate(dateId: String): DailyLog? {
    if (auth.currentUser == null) return null
    return try {
        val snap = logsCol().document(dateId).get().await()
        snap.toObject(DailyLog::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Cập nhật dinh dưỡng cho một ngày cụ thể (cộng dồn)
suspend fun updateNutritionForDate(
    dateId: String, 
    calories: Float, 
    protein: Float, 
    fat: Float, 
    carb: Float
) {
    if (auth.currentUser == null) return
    
    // Validation giống như updateTodayNutrition
    val validCalories = calories.coerceIn(0f, 10000f)
    val validProtein = protein.coerceIn(0f, 1000f)
    val validFat = fat.coerceIn(0f, 1000f)
    val validCarb = carb.coerceIn(0f, 2000f)
    
    val docRef = logsCol().document(dateId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(docRef)
        if (snapshot.exists()) {
            // Đã có dữ liệu -> CỘNG DỒN
            val current = snapshot.toObject(DailyLog::class.java)!!
            transaction.update(docRef, mapOf(
                "calories" to (current.calories + validCalories),
                "protein" to (current.protein + validProtein),
                "fat" to (current.fat + validFat),
                "carb" to (current.carb + validCarb)
            ))
        } else {
            // Chưa có -> TẠO MỚI
            val newLog = DailyLog(
                dateId = dateId,
                calories = validCalories,
                protein = validProtein,
                fat = validFat,
                carb = validCarb
            )
            transaction.set(docRef, newLog)
        }
    }.await()
}
```

**Cập nhật hàm cũ**:

```kotlin
// updateTodayNutrition() giờ gọi updateNutritionForDate()
suspend fun updateTodayNutrition(calories: Float, protein: Float, fat: Float, carb: Float) {
    updateNutritionForDate(getTodayDateId(), calories, protein, fat, carb)
}

// getTodayLog() giờ gọi getLogForDate()
suspend fun getTodayLog(): DailyLog? {
    return getLogForDate(getTodayDateId())
}
```

#### 10.2. Cập Nhật NutritionViewModel

**File**: `mobile/app/src/main/java/com/example/nutricook/viewmodel/nutrition/NutritionViewModel.kt`

**Cập nhật UI State**:

```kotlin
data class NutritionUiState(
    val loading: Boolean = false,
    val history: List<DailyLog> = emptyList(),
    val todayLog: DailyLog? = null,
    val selectedDateLog: DailyLog? = null, // Dữ liệu ngày được chọn
    val selectedDateId: String? = null,    // ID ngày được chọn (format "yyyy-MM-dd")
    val message: String? = null
)
```

**Thêm các hàm mới**:

```kotlin
// Chọn ngày và load dữ liệu cho ngày đó
fun selectDate(dateId: String) = viewModelScope.launch {
    _ui.update { it.copy(selectedDateId = dateId, loading = true) }
    loadDataForDate(dateId)
}

// Load dữ liệu cho một ngày cụ thể
fun loadDataForDate(dateId: String) = viewModelScope.launch {
    try {
        val log = repo.getLogForDate(dateId)
        _ui.update { 
            it.copy(
                selectedDateLog = log ?: DailyLog(
                    dateId = dateId, 
                    calories = 0f, 
                    protein = 0f, 
                    fat = 0f, 
                    carb = 0f
                ),
                loading = false
            )
        }
    } catch (e: Exception) {
        _ui.update { 
            it.copy(
                loading = false, 
                message = e.message,
                selectedDateLog = DailyLog(dateId = dateId, calories = 0f, protein = 0f, fat = 0f, carb = 0f)
            )
        }
    }
}

// Cập nhật dinh dưỡng cho ngày được chọn
fun updateNutritionForDate(
    dateId: String, 
    cal: Float, 
    pro: Float, 
    fat: Float, 
    carb: Float
) = viewModelScope.launch {
    try {
        repo.updateNutritionForDate(dateId, cal, pro, fat, carb)
        loadData() // Reload dữ liệu hôm nay và lịch sử
        
        // Nếu đang xem ngày này, reload dữ liệu ngày đó
        if (_ui.value.selectedDateId == dateId) {
            loadDataForDate(dateId)
        }
        
        _ui.update { it.copy(message = "Đã cập nhật dinh dưỡng!") }
    } catch (e: Exception) {
        _ui.update { it.copy(message = "Lỗi: ${e.message}") }
    }
}

// Quay về xem dữ liệu hôm nay
fun resetToToday() = viewModelScope.launch {
    val todayId = repo.dateToDateId(Date())
    _ui.update { 
        it.copy(
            selectedDateId = null, 
            selectedDateLog = null
        ) 
    }
    loadData()
}
```

#### 10.3. Thêm Date Picker

**File**: `mobile/app/src/main/java/com/example/nutricook/view/profile/ProfileScreens.kt`

**Thêm hàm showDatePicker**:

```kotlin
import android.app.DatePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

fun showDatePicker(
    context: android.content.Context,
    currentDateId: String?,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    
    // Nếu có currentDateId, parse nó để set ngày ban đầu
    if (currentDateId != null) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(currentDateId)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            // Nếu parse lỗi, dùng ngày hôm nay
        }
    }
    
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    
    DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateId = sdf.format(selectedCalendar.time)
            onDateSelected(dateId)
        },
        year,
        month,
        day
    ).show()
}
```

#### 10.4. Cập Nhật CaloriesTrackingCard

**File**: `mobile/app/src/main/java/com/example/nutricook/view/profile/ProfileScreens.kt`

**Thêm tham số mới**:

```kotlin
fun CaloriesTrackingCard(
    modifier: Modifier = Modifier,
    todayCalories: Float,
    caloriesTarget: Float,
    todayLog: DailyLog?,
    weeklyData: List<Float>,
    onAddClick: () -> Unit,
    onTargetChange: ((Float) -> Unit)? = null,
    onResetClick: (() -> Unit)? = null,
    selectedDateId: String? = null,        // ID ngày được chọn
    selectedDateLog: DailyLog? = null,     // Dữ liệu ngày được chọn
    onDateSelected: ((String) -> Unit)? = null,  // Callback khi chọn ngày
    onResetToToday: (() -> Unit)? = null   // Callback quay về hôm nay
) {
    val context = LocalContext.current
    
    // Xác định dữ liệu hiển thị
    val displayLog = if (selectedDateId != null) selectedDateLog else todayLog
    val displayCalories = displayLog?.calories ?: 0f
    val isToday = selectedDateId == null
    
    // Format ngày để hiển thị
    val displayDateText = if (selectedDateId != null) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(selectedDateId)
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            selectedDateId
        }
    } else {
        "Hôm nay"
    }
    
    // ... UI code ...
    
    // Thay đổi phần hiển thị ngày
    Column {
        Text("Theo dõi Calories", ...)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                if (onDateSelected != null) {
                    showDatePicker(context, selectedDateId) { dateId ->
                        onDateSelected(dateId)
                    }
                }
            }
        ) {
            Text(displayDateText, ...)
            if (onDateSelected != null) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = "Chọn ngày",
                    modifier = Modifier.size(16.dp),
                    tint = TextGray
                )
            }
        }
        // Nút quay về hôm nay nếu đang xem ngày khác
        if (!isToday && onResetToToday != null) {
            TextButton(onClick = onResetToToday) {
                Text("Quay về hôm nay", ...)
            }
        }
    }
    
    // Sử dụng displayCalories thay vì todayCalories
    // Sử dụng displayLog thay vì todayLog
    // Nút Reset chỉ hiển thị khi đang xem hôm nay
    if (isToday && displayCalories > 0 && onResetClick != null) {
        // ... Reset button ...
    }
}
```

#### 10.5. Cập Nhật ProfileScreen

**File**: `mobile/app/src/main/java/com/example/nutricook/view/profile/ProfileScreens.kt`

**Truyền tham số mới vào CaloriesTrackingCard**:

```kotlin
CaloriesTrackingCard(
    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
    todayCalories = displayCalories,
    caloriesTarget = caloriesTarget,
    todayLog = displayLog,
    weeklyData = historyData,
    onAddClick = { onNavigateToCalculator() },
    onTargetChange = { newTarget -> vm.updateCaloriesTarget(newTarget) },
    onResetClick = { nutritionVm.resetTodayNutrition() },
    selectedDateId = nutritionState.selectedDateId,
    selectedDateLog = nutritionState.selectedDateLog,
    onDateSelected = { dateId -> nutritionVm.selectDate(dateId) },
    onResetToToday = { nutritionVm.resetToToday() }
)
```

#### 10.6. Cập Nhật AddMealScreen

**File**: `mobile/app/src/main/java/com/example/nutricook/view/profile/AddMealScreen.kt`

**Thêm tham số selectedDateId**:

```kotlin
@Composable
fun AddMealScreen(
    navController: NavController,
    initialCalories: Float = 0f,
    initialProtein: Float = 0f,
    initialFat: Float = 0f,
    initialCarb: Float = 0f,
    caloriesTarget: Float = 2000f,
    selectedDateId: String? = null,  // Thêm tham số mới
    onSave: (Float, Float, Float, Float) -> Unit
) {
    // Format ngày để hiển thị
    val displayDateText = if (selectedDateId != null) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(selectedDateId)
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            "Bổ sung món ăn - ${displayFormat.format(date ?: Date())}"
        } catch (e: Exception) {
            "Bổ sung món ăn"
        }
    } else {
        "Thêm bữa ăn"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(displayDateText, ...)
                        if (selectedDateId != null) {
                            Text(
                                "Bổ sung cho ngày đã chọn",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                },
                // ...
            )
        }
    ) {
        // ... UI code ...
    }
}
```

#### 10.7. Cập Nhật NavGraph

**File**: `mobile/app/src/main/java/com/example/nutricook/view/nav/NavGraph.kt`

**Truyền selectedDateId vào AddMealScreen**:

```kotlin
composable("add_meal") {
    val nutritionVm: NutritionViewModel = hiltViewModel()
    val profileVm: ProfileViewModel = hiltViewModel()
    val nutritionState by nutritionVm.ui.collectAsState()
    val profileState by profileVm.uiState.collectAsState()
    
    // Lấy dateId từ state (nếu đang xem ngày khác)
    val selectedDateId = nutritionState.selectedDateId
    val displayLog = if (selectedDateId != null) {
        nutritionState.selectedDateLog
    } else {
        nutritionState.todayLog
    }
    
    val caloriesTarget = profileState.profile?.nutrition?.caloriesTarget ?: 2000f

    AddMealScreen(
        navController = navController,
        initialCalories = displayLog?.calories ?: 0f,
        initialProtein = displayLog?.protein ?: 0f,
        initialFat = displayLog?.fat ?: 0f,
        initialCarb = displayLog?.carb ?: 0f,
        caloriesTarget = caloriesTarget,
        selectedDateId = selectedDateId,  // Truyền selectedDateId
        onSave = { cal, pro, fat, carb ->
            if (selectedDateId != null) {
                // Lưu vào ngày được chọn
                nutritionVm.updateNutritionForDate(selectedDateId, cal, pro, fat, carb)
            } else {
                // Lưu vào hôm nay
                nutritionVm.updateTodayNutrition(cal, pro, fat, carb)
            }
        }
    )
}
```

## 🎯 Luồng Hoạt Động

1. **Người dùng chọn ngày**:
   - Click vào ngày/icon calendar trong CaloriesTrackingCard
   - DatePickerDialog hiển thị
   - Chọn ngày → `onDateSelected(dateId)` được gọi
   - ViewModel gọi `selectDate(dateId)` → Load dữ liệu ngày đó

2. **Hiển thị dữ liệu ngày được chọn**:
   - CaloriesTrackingCard hiển thị dữ liệu từ `selectedDateLog`
   - Hiển thị ngày đã chọn thay vì "Hôm nay"
   - Nút "Quay về hôm nay" xuất hiện

3. **Bổ sung món ăn**:
   - Click "Thêm" → Navigate to AddMealScreen
   - AddMealScreen nhận `selectedDateId`
   - Title hiển thị "Bổ sung món ăn - [ngày]"
   - Khi save → Gọi `updateNutritionForDate(dateId, ...)`
   - Dữ liệu được lưu vào ngày đã chọn

4. **Quay về hôm nay**:
   - Click "Quay về hôm nay"
   - ViewModel gọi `resetToToday()`
   - `selectedDateId` = null → Hiển thị dữ liệu hôm nay

## ⚠️ Lưu Ý Quan Trọng

### Vấn đề: Calories bị tính gấp đôi

**Nguyên nhân**: AddMealScreen khởi tạo với `initialCalories` và truyền tổng vào `updateTodayNutrition()`

**Giải pháp**: 
- Không khởi tạo form với `initialCalories`
- Chỉ truyền phần tăng thêm vào `updateTodayNutrition()`

### Vấn đề: Biểu đồ không cập nhật sau khi reset

**Nguyên nhân**: Không reload data sau khi reset

**Giải pháp**: 
- Gọi `loadData()` sau `resetTodayNutrition()`
- Hoặc reload trong ViewModel

### Vấn đề: Người dùng nhập số âm hoặc chữ cái

**Nguyên nhân**: Không có validation

**Giải pháp**: 
- Sử dụng `DecimalInputHelper.normalizeDecimalInput()` để filter và normalize
- Set `isError = !DecimalInputHelper.isValid(value)`

### Vấn đề: Người dùng nhập ".7" hoặc ",7" thay vì "0.7"

**Nguyên nhân**: Không tự động thêm "0" trước dấu decimal

**Giải pháp**: 
- `DecimalInputHelper.normalizeDecimalInput()` tự động thêm "0" nếu bắt đầu bằng dấu decimal
- Hỗ trợ cả dấu phẩy (,) và dấu chấm (.) theo sở thích người dùng

## 📝 Notes

1. **Performance**: 
   - Sử dụng Firestore Transaction để đảm bảo atomicity
   - Cache dữ liệu trong ViewModel để tránh query nhiều lần

2. **Error Handling**:
   - Try-catch trong repository
   - Hiển thị thông báo lỗi rõ ràng
   - Logging để debug

3. **User Experience**:
   - Validation real-time
   - Hỗ trợ cả dấu phẩy (,) và dấu chấm (.) cho decimal
   - Tự động thêm "0" trước dấu decimal (".7" → "0.7")
   - Thông báo success/error
   - Loading indicator khi đang xử lý
   - Reset button dễ nhận biết

## 🚀 Next Steps

1. ✅ **Bổ sung món ăn cho ngày trước** - Đã triển khai
2. Thêm tính năng xóa từng món ăn đã thêm
3. Lịch sử chi tiết các món ăn đã thêm trong ngày
4. Export dữ liệu ra PDF/Excel
5. So sánh với các ngày trước
6. Gợi ý món ăn dựa trên calories còn lại
7. Thêm tính năng chỉnh sửa món ăn đã thêm
8. Thống kê theo tuần/tháng

