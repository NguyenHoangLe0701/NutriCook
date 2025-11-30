# Hướng dẫn Notification với Logo và Deep Link

## 📱 Công nghệ sử dụng

### 1. **Firebase Cloud Messaging (FCM)**
- **Mục đích**: Gửi push notification từ server đến thiết bị Android
- **Ưu điểm**: 
  - Miễn phí, không giới hạn số lượng notification
  - Hoạt động ngay cả khi app đóng
  - Tự động quản lý connection, battery efficient
  - Hỗ trợ targeting theo user, topic, device group

### 2. **Android Notification System**
- **NotificationChannel**: Quản lý các loại thông báo (Android 8.0+)
- **NotificationCompat**: API tương thích ngược với các phiên bản Android cũ
- **PendingIntent**: Intent được thực thi khi user click vào notification

### 3. **Bitmap & Drawable**
- **setLargeIcon()**: Hiển thị logo lớn trong notification
- **setSmallIcon()**: Icon nhỏ ở góc notification (phải là monochrome)

## 🔧 Cách hoạt động

### Flow hoàn chỉnh:

```
1. Server gửi FCM message
   ↓
2. FirebaseMessagingService.onMessageReceived() nhận message
   ↓
3. sendNotification() tạo notification với:
   - Logo từ drawable (logonutricook.jpg)
   - Title và message
   - PendingIntent để mở MainActivity
   ↓
4. User click notification
   ↓
5. MainActivity được mở (màn hình chính)
```

### Code chi tiết:

#### 1. **Load Logo vào Notification**
```kotlin
// Load logo từ drawable và convert sang Bitmap
val logoBitmap = try {
    val drawable = ContextCompat.getDrawable(this, R.drawable.logonutricook)
    if (drawable != null) {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    } else null
} catch (e: Exception) {
    null
}
```

**Giải thích**:
- `ContextCompat.getDrawable()`: Load drawable resource an toàn
- Convert Drawable → Bitmap để dùng trong `setLargeIcon()`
- Try-catch để tránh crash nếu logo không tồn tại

#### 2. **Tạo Intent để mở MainActivity**
```kotlin
val intent = Intent(this, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    action = Intent.ACTION_MAIN
    addCategory(Intent.CATEGORY_LAUNCHER)
    putExtra("notification", true)
    putExtra("title", title)
    putExtra("message", messageBody)
}
```

**Giải thích**:
- `FLAG_ACTIVITY_NEW_TASK`: Tạo task mới nếu app chưa chạy
- `FLAG_ACTIVITY_CLEAR_TASK`: Xóa các activity cũ trong stack
- `ACTION_MAIN` + `CATEGORY_LAUNCHER`: Mở app như launcher (màn hình chính)
- `putExtra()`: Truyền data nếu cần xử lý trong MainActivity

#### 3. **Tạo PendingIntent**
```kotlin
val pendingIntent = PendingIntent.getActivity(
    this,
    0,
    intent,
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)
```

**Giải thích**:
- `PendingIntent`: Intent được "đóng gói" để dùng sau (khi user click)
- `FLAG_UPDATE_CURRENT`: Cập nhật intent nếu đã tồn tại
- `FLAG_IMMUTABLE`: Bắt buộc từ Android 12+ (API 31+)

#### 4. **Build Notification**
```kotlin
val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
    .setSmallIcon(R.drawable.ic_launcher_foreground) // Icon nhỏ
    .setLargeIcon(logoBitmap) // Logo lớn - QUAN TRỌNG
    .setContentTitle(title)
    .setContentText(messageBody)
    .setContentIntent(pendingIntent) // Mở app khi click
    .setAutoCancel(true) // Tự động đóng khi click
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
```

## 📋 Các bước triển khai

### Bước 1: Chuẩn bị Logo

1. **Đảm bảo file logo tồn tại**:
   - Path: `mobile/app/src/main/res/drawable/logonutricook.jpg`
   - Format: JPG, PNG, hoặc WebP
   - Kích thước khuyến nghị: 256x256px hoặc lớn hơn (Android sẽ tự scale)

2. **Tối ưu logo**:
   - Nén ảnh để giảm kích thước file
   - Đảm bảo logo rõ ràng ở kích thước nhỏ
   - Nên có background trong suốt hoặc màu nền phù hợp

### Bước 2: Kiểm tra Code

1. **File đã được cập nhật**:
   - `FirebaseMessagingService.kt` đã có code load logo
   - Intent đã được cấu hình để mở MainActivity

2. **Kiểm tra imports**:
   ```kotlin
   import androidx.core.content.ContextCompat
   ```

### Bước 3: Test Local

1. **Build và chạy app**:
   ```bash
   cd mobile
   ./gradlew assembleDebug
   ```

2. **Gửi test notification từ Firebase Console**:
   - Vào [Firebase Console](https://console.firebase.google.com)
   - Chọn project → Cloud Messaging
   - Compose notification → Send test message
   - Nhập FCM token (lấy từ logcat: `New FCM token: ...`)

3. **Kiểm tra**:
   - ✅ Notification hiển thị với logo
   - ✅ Click vào notification mở MainActivity
   - ✅ App mở đúng màn hình chính

### Bước 4: Gửi từ Server (Dashboard)

#### Cách 1: Sử dụng Firebase Admin SDK (Java/Spring Boot)

```java
// Trong NotificationService.java
public void sendNotification(String fcmToken, String title, String message) {
    Message message = Message.builder()
        .setToken(fcmToken)
        .setNotification(Notification.builder()
            .setTitle(title)
            .setBody(message)
            .build())
        .setAndroidConfig(AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .build())
        .build();
    
    try {
        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Successfully sent message: " + response);
    } catch (Exception e) {
        System.err.println("Error sending message: " + e.getMessage());
    }
}
```

#### Cách 2: Sử dụng REST API

```bash
curl -X POST https://fcm.googleapis.com/v1/projects/nutricook-fff8f/messages:send \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": {
      "token": "USER_FCM_TOKEN",
      "notification": {
        "title": "NutriCook",
        "body": "Xin chào"
      }
    }
  }'
```

### Bước 5: Xử lý khi App mở từ Notification

Trong `MainActivity.kt`, bạn có thể xử lý data từ notification:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Kiểm tra nếu mở từ notification
    if (intent.getBooleanExtra("notification", false)) {
        val title = intent.getStringExtra("title")
        val message = intent.getStringExtra("message")
        
        // Có thể hiển thị dialog, navigate đến màn hình cụ thể, etc.
        Log.d("MainActivity", "Opened from notification: $title - $message")
    }
    
    // ... rest of code
}
```

## 🐛 Troubleshooting

### Logo không hiển thị

**Nguyên nhân**:
- File logo không tồn tại hoặc tên sai
- Bitmap conversion lỗi
- Logo quá lớn (Android giới hạn ~512x512px)

**Giải pháp**:
1. Kiểm tra file tồn tại: `R.drawable.logonutricook`
2. Xem logcat: `Error loading logo for notification`
3. Resize logo về 256x256px hoặc nhỏ hơn

### Click notification không mở app

**Nguyên nhân**:
- Intent flags không đúng
- MainActivity không được khai báo trong AndroidManifest

**Giải pháp**:
1. Kiểm tra AndroidManifest.xml:
   ```xml
   <activity
       android:name=".MainActivity"
       android:exported="true">
       <intent-filter>
           <action android:name="android.intent.action.MAIN" />
           <category android:name="android.intent.category.LAUNCHER" />
       </intent-filter>
   </activity>
   ```

2. Kiểm tra logcat khi click notification

### Notification không hiển thị

**Nguyên nhân**:
- Chưa xin quyền (Android 13+)
- Notification channel chưa được tạo
- App bị kill bởi hệ thống

**Giải pháp**:
1. Kiểm tra quyền: Settings → Apps → NutriCook → Notifications
2. Xem logcat: `createNotificationChannel()`
3. Test với app đang chạy trước, sau đó test khi app đóng

## 📚 Tài liệu tham khảo

- [Firebase Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
- [Android Notification Guide](https://developer.android.com/develop/ui/views/notifications)
- [NotificationCompat API](https://developer.android.com/reference/androidx/core/app/NotificationCompat)

## ✅ Checklist triển khai

- [x] Logo đã được thêm vào notification
- [x] Intent mở MainActivity khi click
- [x] Code đã được cập nhật
- [ ] Test local notification
- [ ] Test từ Firebase Console
- [ ] Test từ Dashboard server
- [ ] Kiểm tra trên các phiên bản Android khác nhau
- [ ] Tối ưu logo size nếu cần

