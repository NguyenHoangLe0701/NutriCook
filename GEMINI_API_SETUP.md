# Hướng dẫn cấu hình Gemini API cho tính năng tự động tính calories

## Tổng quan
Tính năng này cho phép người dùng nhập tên món ăn (ví dụ: "1 quả táo", "100g bơ") và hệ thống sẽ tự động tính calories và các chất dinh dưỡng thông qua Google Gemini API.

## Các bước cấu hình

### 1. Lấy API Key từ Google AI Studio

1. Truy cập: https://makersuite.google.com/app/apikey
2. Đăng nhập bằng tài khoản Google
3. Tạo API key mới
4. Copy API key

### 2. Cấu hình API Key trong project

Có 2 cách:

#### Cách 1: Thêm vào local.properties (Khuyến nghị)

1. Mở file `mobile/local.properties`
2. Thêm dòng:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```

3. Cập nhật `GeminiNutritionService.kt` để đọc từ BuildConfig:
   ```kotlin
   private val apiKey: String? = BuildConfig.GEMINI_API_KEY
   ```

4. Thêm vào `build.gradle.kts`:
   ```kotlin
   android {
       defaultConfig {
           val geminiApiKey = project.findProperty("GEMINI_API_KEY") as String? ?: ""
           buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
       }
   }
   ```

#### Cách 2: Hardcode tạm thời (Chỉ dùng cho testing)

1. Mở file `mobile/app/src/main/java/com/example/nutricook/data/nutrition/GeminiNutritionService.kt`
2. Tìm dòng:
   ```kotlin
   private val apiKey: String? = null
   ```
3. Thay bằng:
   ```kotlin
   private val apiKey: String? = "YOUR_API_KEY_HERE"
   ```

⚠️ **Lưu ý**: Không commit API key vào Git!

### 3. Kiểm tra hoạt động

1. Build và chạy app
2. Vào màn hình Profile > Calories Tracking
3. Bấm "Thêm bữa ăn"
4. Bấm nút "+" để thêm món tùy chỉnh
5. Nhập tên món ăn (ví dụ: "1 quả táo")
6. Bấm icon ✨ (AutoAwesome) để tự động tính calories

## Tính năng

- ✅ Tự động tính calories từ tên món ăn
- ✅ Tính protein, fat, carb
- ✅ Hỗ trợ tiếng Việt
- ✅ Hỗ trợ các đơn vị (1 quả, 100g, 1 tô, v.v.)
- ✅ Fallback: Nếu API không hoạt động, người dùng có thể nhập thủ công

## Giới hạn

- API có giới hạn số request miễn phí
- Cần kết nối internet
- Độ chính xác phụ thuộc vào cách mô tả món ăn

## Troubleshooting

### Lỗi: "Không thể tính calories tự động"
- Kiểm tra API key đã được cấu hình chưa
- Kiểm tra kết nối internet
- Kiểm tra API key còn hợp lệ không

### Icon ✨ không hiển thị
- API key chưa được cấu hình
- Kiểm tra `isApiKeyConfigured()` trả về `true`

## Tài liệu tham khảo

- [Google Gemini API Documentation](https://ai.google.dev/docs)
- [Gemini API Pricing](https://ai.google.dev/pricing)

