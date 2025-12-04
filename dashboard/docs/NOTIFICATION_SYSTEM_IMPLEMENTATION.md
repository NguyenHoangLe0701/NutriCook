# Hệ Thống Thông Báo - Triển Khai Chi Tiết

## 📋 Tổng quan

Hệ thống thông báo của NutriCook sử dụng **Firebase Cloud Messaging (FCM)** để gửi push notification từ dashboard đến người dùng mobile. Hệ thống hỗ trợ nhiều cơ chế thông báo khác nhau và tự động quản lý quyền truy cập.

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│                    DASHBOARD (Spring Boot)                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  AdminController.java                                │  │
│  │  - Nhận request từ form gửi thông báo                │  │
│  │  - Gọi NotificationService                           │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────────┐  │
│  │  NotificationService.java                            │  │
│  │  - Lấy FCM tokens từ Firestore                       │  │
│  │  - Gửi notification qua Firebase Cloud Messaging     │  │
│  └──────────────────┬───────────────────────────────────┘  │
└─────────────────────┼───────────────────────────────────────┘
                      │
                      │ Firebase Cloud Messaging (FCM)
                      │
┌─────────────────────▼───────────────────────────────────────┐
│                  MOBILE APP (Android)                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  NutriCookMessagingService.kt                        │  │
│  │  - Nhận FCM message                                  │  │
│  │  - Hiển thị notification                             │  │
│  │  - Xử lý click vào notification                      │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼───────────────────────────────────┐  │
│  │  NotificationScheduler.kt                            │  │
│  │  - Đặt lịch thông báo định kỳ (7h, 12h, 19h)         │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Các File và Vị Trí

### 1. **Dashboard (Backend)**

#### 📂 `dashboard/src/main/java/com/nutricook/dashboard/service/NotificationService.java`
**Nhiệm vụ:** Service chính xử lý gửi notification từ dashboard đến mobile app.

**Các method quan trọng:**
- `sendNotificationToAll()`: Gửi đến tất cả người dùng
- `sendNotificationToActive()`: Gửi đến người dùng hoạt động
- `sendNotificationToNew()`: Gửi đến người dùng mới (30 ngày)
- `sendNotificationToTokens()`: Gửi đến danh sách FCM tokens
- `getAllFcmTokens()`: Lấy tất cả FCM tokens từ Firestore

#### 📂 `dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java`
**Vị trí:** Dòng 2152-2200

**Nhiệm vụ:** Controller xử lý request từ form gửi thông báo.

**Endpoints:**
- `GET /admin/notifications`: Hiển thị form gửi thông báo
- `POST /admin/notifications/send`: Gửi thông báo

**Template HTML:**
- `dashboard/src/main/resources/templates/admin/notifications.html`

---

### 2. **Mobile App (Android)**

#### 📂 `mobile/app/src/main/java/com/example/nutricook/service/NutriCookMessagingService.kt`
**Nhiệm vụ:** Service nhận FCM message và hiển thị notification.

**Các method:**
- `onMessageReceived()`: Nhận message từ FCM
- `sendNotification()`: Tạo và hiển thị notification
- `saveTokenToFirestore()`: Lưu FCM token vào Firestore

#### 📂 `mobile/app/src/main/java/com/example/nutricook/view/notifications/NotificationScheduler.kt`
**Nhiệm vụ:** Đặt lịch thông báo định kỳ (7h sáng, 12h trưa, 19h tối).

#### 📂 `mobile/app/src/main/java/com/example/nutricook/view/notifications/ReminderReceiver.kt`
**Nhiệm vụ:** BroadcastReceiver xử lý thông báo định kỳ từ AlarmManager.

#### 📂 `mobile/app/src/main/java/com/example/nutricook/view/notifications/NotificationUtils.kt`
**Nhiệm vụ:** Utility class tạo notification channel và hiển thị notification.

#### 📂 `mobile/app/src/main/java/com/example/nutricook/MainActivity.kt`
**Nhiệm vụ:** Activity chính - xin quyền notification và khởi tạo hệ thống.

---

## 🔄 Luồng hoạt động chi tiết

### 1. **Dashboard gửi thông báo**

#### Bước 1: Admin nhập form
```
Admin truy cập: http://localhost:8080/admin/notifications
↓
Nhập tiêu đề và nội dung
Chọn đối tượng: "Tất cả người dùng" / "Người dùng hoạt động" / "Người dùng mới"
↓
Click "Gửi thông báo"
```

#### Bước 2: Controller xử lý request
```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java
@PostMapping("/notifications/send")
public String sendNotification(
    @RequestParam String title,
    @RequestParam String message,
    @RequestParam(required = false, defaultValue = "all") String target,
    RedirectAttributes redirectAttributes
) {
    // Gọi NotificationService để gửi
    int sentCount = notificationService.sendNotificationToAll(title, message);
    
    // Trả về kết quả
    redirectAttributes.addFlashAttribute("success", 
        "Đã gửi thông báo thành công đến " + sentCount + " người dùng!");
    return "redirect:/admin/notifications";
}
```

#### Bước 3: NotificationService lấy FCM tokens
```java
// File: dashboard/src/main/java/com/nutricook/dashboard/service/NotificationService.java
private List<String> getAllFcmTokens() throws ExecutionException, InterruptedException {
    List<String> tokens = new ArrayList<>();
    
    // Lấy tất cả users từ Firestore
    CollectionReference users = firestore.collection("users");
    QuerySnapshot snapshot = users.get().get();
    
    // Lấy FCM token của mỗi user
    snapshot.getDocuments().forEach(doc -> {
        Map<String, Object> data = doc.getData();
        if (data != null && data.containsKey("fcmToken")) {
            Object token = data.get("fcmToken");
            if (token != null && !token.toString().isEmpty()) {
                tokens.add(token.toString());
            }
        }
    });
    
    return tokens;
}
```

#### Bước 4: Gửi notification qua FCM
```java
// File: dashboard/src/main/java/com/nutricook/dashboard/service/NotificationService.java
private int sendNotificationToTokens(List<String> fcmTokens, String title, String message) {
    for (String token : fcmTokens) {
        // Tạo Android notification config
        AndroidConfig androidConfig = AndroidConfig.builder()
            .setPriority(AndroidConfig.Priority.HIGH)
            .setNotification(AndroidNotification.builder()
                .setTitle(title)
                .setBody(message)
                .setSound("default")
                .setChannelId("nutricook_notifications")
                .setVisibility(AndroidNotification.Visibility.PUBLIC) // Hiển thị trên lock screen
                .setPriority(AndroidNotification.Priority.HIGH)
                .build())
            .build();
        
        // Tạo FCM message
        Message fcmMessage = Message.builder()
            .setToken(token)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(message)
                .build())
            .setAndroidConfig(androidConfig)
            .putData("type", "admin_notification") // Đánh dấu là thông báo từ admin
            .build();
        
        // Gửi message
        firebaseMessaging.send(fcmMessage);
    }
}
```

**Giải thích:**
- `setChannelId("nutricook_notifications")`: Kênh thông báo trên Android
- `setVisibility(PUBLIC)`: Hiển thị đầy đủ trên lock screen
- `setPriority(HIGH)`: Ưu tiên cao để notification hiển thị ngay

---

### 2. **Mobile app nhận và hiển thị thông báo**

#### Bước 1: FCM Service nhận message
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/service/NutriCookMessagingService.kt
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    // Kiểm tra notification payload
    remoteMessage.notification?.let {
        sendNotification(it.title ?: "NutriCook", it.body ?: "")
    }
    
    // Hoặc tạo từ data payload
    if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
        val title = remoteMessage.data["title"] ?: "NutriCook"
        val message = remoteMessage.data["message"] ?: ""
        sendNotification(title, message)
    }
}
```

#### Bước 2: Tạo và hiển thị notification
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/service/NutriCookMessagingService.kt
private fun sendNotification(title: String, messageBody: String) {
    // 1. Tạo Intent để mở app khi click vào notification
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
        putExtra("notification", true) // Đánh dấu mở từ notification
    }
    
    // 2. Tạo PendingIntent
    val pendingIntent = PendingIntent.getActivity(
        this, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    // 3. Load logo để hiển thị trong notification
    val logoBitmap = try {
        val drawable = ContextCompat.getDrawable(this, R.drawable.logonutricook)
        // Convert Drawable → Bitmap
        // ...
    } catch (e: Exception) { null }
    
    // 4. Tạo notification
    val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setLargeIcon(logoBitmap) // Logo lớn trong notification
        .setContentTitle(title)
        .setContentText(messageBody)
        .setAutoCancel(true) // Tự động đóng khi click
        .setContentIntent(pendingIntent) // Mở app khi click
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Hiển thị trên lock screen
        .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
    
    // 5. Hiển thị notification
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
}
```

#### Bước 3: User click vào notification → Mở app
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/service/NutriCookMessagingService.kt
val intent = Intent(this, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    // → Mở app và xóa các activity cũ trong stack
    
    action = Intent.ACTION_MAIN
    addCategory(Intent.CATEGORY_LAUNCHER)
    // → Mở app như launcher (màn hình chính)
    
    putExtra("notification", true)
    // → Đánh dấu mở từ notification (có thể dùng để điều hướng)
}
```

**Kết quả:** MainActivity được mở → NavGraph hiển thị màn hình chính (Home screen)

---

## 🔔 Các Cơ Chế Thông Báo

### 1. **Thông báo từ Dashboard (Admin)**

#### Cách hoạt động:
- Admin gửi thông báo thủ công qua form
- Gửi đến tất cả người dùng hoặc nhóm người dùng cụ thể
- Sử dụng Firebase Cloud Messaging

#### Đối tượng nhận:
- **Tất cả người dùng**: Tất cả users có FCM token
- **Người dùng hoạt động**: Users có FCM token (tạm thời = tất cả)
- **Người dùng mới**: Users đăng ký trong 30 ngày gần đây

#### Code location:
```java
// Dashboard: AdminController.java (dòng 2160-2200)
// Dashboard: NotificationService.java (toàn bộ file)
```

---

### 2. **Thông báo định kỳ (Scheduled Notifications)**

#### Cách hoạt động:
- Sử dụng `AlarmManager` để đặt lịch
- Thông báo tự động vào các giờ cố định: **7h sáng, 12h trưa, 19h tối**
- Chạy mỗi ngày (repeating)

#### Code implementation:
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/notifications/NotificationScheduler.kt
object NotificationScheduler {
    fun scheduleDailyReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val messages = listOf(
            "Buổi sáng rồi! Hãy ăn sáng để có năng lượng bắt đầu ngày mới ☀️",
            "Giờ trưa đến rồi! Ghi lại bữa ăn của bạn nhé 🍚",
            "Buổi tối đến rồi! Cùng xem hôm nay bạn đã đạt được mục tiêu chưa 🌙"
        )
        val hours = listOf(7, 12, 19) // 7h sáng, 12h trưa, 19h tối
        
        for (i in messages.indices) {
            // Tạo Intent cho mỗi thông báo
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("message", messages[i])
            }
            
            // Tạo PendingIntent
            val pendingIntent = PendingIntent.getBroadcast(
                context, i, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Đặt giờ thông báo
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hours[i]) // 7, 12, hoặc 19
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                
                // Nếu giờ đã qua, đặt cho ngày mai
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            // Đặt lịch lặp lại hàng ngày
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP, // Đánh thức thiết bị
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, // Lặp lại mỗi ngày
                pendingIntent
            )
        }
    }
}
```

#### BroadcastReceiver xử lý:
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/notifications/ReminderReceiver.kt
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val message = intent?.getStringExtra("message") ?: "Đừng quên ghi lại bữa ăn hôm nay nhé 🍱"
        
        // Hiển thị notification
        NotificationUtils.showNotification(
            context,
            "NutriCook nhắc nhở",
            message
        )
    }
}
```

#### Khi nào được đặt lịch:
- Khi app khởi động lần đầu (MainActivity.onCreate)
- User có thể bật/tắt trong Settings (nếu có)

#### Code location:
```kotlin
// MainActivity.kt (dòng 60-61)
NotificationScheduler.scheduleDailyReminders(this)

// NotificationScheduler.kt (toàn bộ file)
// ReminderReceiver.kt (toàn bộ file)
```

#### Đăng ký trong AndroidManifest:
```xml
<!-- File: mobile/app/src/main/AndroidManifest.xml -->
<receiver
    android:name=".view.notifications.ReminderReceiver"
    android:exported="true" />
```

---

### 3. **Thông báo chào mừng (Welcome Notification)**

#### Cách hoạt động:
- Hiển thị khi user đăng nhập lần đầu
- Chỉ hiển thị 1 lần (lưu trong SharedPreferences)

#### Code implementation:
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/MainActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    
    // Kiểm tra xem đây có phải lần đăng nhập đầu tiên không
    val prefs = getSharedPreferences("nutricook_prefs", MODE_PRIVATE)
    val isFirstLogin = prefs.getBoolean("is_first_login", true)
    
    if (isFirstLogin) {
        // Hiển thị thông báo chào mừng
        NotificationUtils.showNotification(
            this,
            "🌿 NutriCook chào bạn",
            "Hãy dành chút thời gian cho cơ thể và sức khỏe của bạn hôm nay nhé 💫"
        )
        
        // Đánh dấu đã hiển thị
        prefs.edit().putBoolean("is_first_login", false).apply()
    }
}
```

---

## 🔐 Quyền Thông Báo (Notification Permission)

### Android 13+ (API 33+)

Từ Android 13, app cần xin quyền `POST_NOTIFICATIONS` để hiển thị thông báo.

#### Code xin quyền:
```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/MainActivity.kt
// Launcher để xin quyền thông báo
private val requestPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Quyền được cấp - hiển thị thông báo cảm ơn
            NotificationUtils.showNotification(
                this,
                "🌿 NutriCook chào bạn",
                "Cảm ơn bạn đã bật thông báo! Hãy chăm sóc sức khỏe mỗi ngày nhé 💪"
            )
        }
        // Nếu từ chối, không làm gì (user sẽ không nhận thông báo)
    }

override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    
    // Xin quyền gửi thông báo (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Chưa có quyền → xin quyền
            requestPermissionLauncher.launch(permission)
        }
    }
    
    // Android < 13: Không cần xin quyền, tự động có quyền
}
```

#### Đăng ký trong AndroidManifest:
```xml
<!-- File: mobile/app/src/main/AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Lưu ý:**
- Android 12 trở xuống: Không cần xin quyền, tự động có
- Android 13+: Bắt buộc xin quyền, nếu user từ chối thì không thể hiển thị thông báo

---

## 📱 Xử Lý Click Vào Notification

### Khi user click vào notification:

1. **PendingIntent được kích hoạt**
   ```kotlin
   // File: mobile/app/src/main/java/com/example/nutricook/service/NutriCookMessagingService.kt
   val intent = Intent(this, MainActivity::class.java).apply {
       flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
       // → Xóa các activity cũ, mở app mới
       
       action = Intent.ACTION_MAIN
       addCategory(Intent.CATEGORY_LAUNCHER)
       // → Mở app như launcher (màn hình chính)
       
       putExtra("notification", true)
       putExtra("title", title)
       putExtra("message", messageBody)
   }
   ```

2. **MainActivity được mở**
   ```kotlin
   // File: mobile/app/src/main/java/com/example/nutricook/MainActivity.kt
   override fun onCreate(savedInstanceState: Bundle?) {
       // ...
       
       // Intent có thể chứa thông tin từ notification
       val fromNotification = intent.getBooleanExtra("notification", false)
       if (fromNotification) {
           // Có thể điều hướng đến màn hình cụ thể nếu cần
           // Ví dụ: navController.navigate(Routes.HOME)
       }
       
       // Hiển thị NavGraph (màn hình chính)
       setContent {
           NutriCookTheme {
               val navController = rememberNavController()
               Surface(modifier = Modifier.fillMaxSize()) {
                   NavGraph(navController = navController)
               }
           }
       }
   }
   ```

3. **Kết quả:** User thấy màn hình chính của app (Home screen)

---

## 🎯 Notification Channel (Android 8.0+)

### Tại sao cần Notification Channel?

Từ Android 8.0 (API 26), mỗi notification phải thuộc một channel. User có thể tắt/bật từng channel riêng.

### Tạo Notification Channel:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/service/NutriCookMessagingService.kt
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID, // "nutricook_notifications"
            CHANNEL_NAME, // "NutriCook Notifications"
            NotificationManager.IMPORTANCE_HIGH // Ưu tiên cao
        ).apply {
            description = CHANNEL_DESCRIPTION // "Thông báo từ NutriCook"
            enableLights(true) // Bật đèn LED
            enableVibration(true) // Rung
            setShowBadge(true) // Hiển thị badge
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC // Hiển thị trên lock screen
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
```

**Các channel trong hệ thống:**
1. `nutricook_notifications` - Thông báo từ FCM (Admin)
2. `nutricook_reminder_channel` - Thông báo nhắc nhở định kỳ

---

## 🔄 FCM Token Management

### Lưu FCM Token vào Firestore:

Khi app được cài đặt hoặc token được refresh, token được lưu vào Firestore.

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/service/NutriCookMessagingService.kt
override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM token: $token")
    
    // Lưu token vào Firestore
    saveTokenToFirestore(token)
}

private fun saveTokenToFirestore(token: String) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    if (currentUser != null) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(currentUser.uid)
        
        // Lưu token vào field "fcmToken"
        userRef.update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving FCM token", e)
            }
    }
}
```

**Cấu trúc Firestore:**
```
users/
  └── {userId}/
      ├── email: "user@example.com"
      ├── displayName: "Nguyễn Văn A"
      └── fcmToken: "dK3jK...xyz"  ← Token để gửi notification
```

---

## 📊 Tóm Tắt Luồng Hoạt Động

### **Dashboard → Mobile (Push Notification):**

```
1. Admin nhập form trên dashboard
   ↓
2. AdminController nhận request
   ↓
3. NotificationService lấy FCM tokens từ Firestore
   ↓
4. Gửi message qua Firebase Cloud Messaging
   ↓
5. NutriCookMessagingService nhận message
   ↓
6. Hiển thị notification trên mobile
   ↓
7. User click → Mở MainActivity (màn hình chính)
```

### **Scheduled Notification (Định kỳ):**

```
1. App khởi động (MainActivity.onCreate)
   ↓
2. NotificationScheduler.scheduleDailyReminders()
   ↓
3. AlarmManager đặt lịch (7h, 12h, 19h)
   ↓
4. Đến giờ → ReminderReceiver.onReceive()
   ↓
5. NotificationUtils.showNotification()
   ↓
6. Hiển thị notification
```

---

## 🛠️ Cấu Hình và Setup

### 1. **Firebase Configuration**

#### Dashboard (Spring Boot):
```java
// File: dashboard/src/main/resources/application.properties
firebase.enabled=true

// File: dashboard/src/main/resources/serviceAccountKey.json
// Service account key để kết nối Firebase
```

#### Mobile (Android):
```xml
<!-- File: mobile/app/google-services.json -->
<!-- Firebase configuration file -->
```

### 2. **Dependencies**

#### Dashboard (pom.xml):
```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

#### Mobile (build.gradle.kts):
```kotlin
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-messaging-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
```

---

## ✅ Checklist Triển Khai

### Dashboard:
- [x] ✅ NotificationService.java - Service gửi notification
- [x] ✅ AdminController.java - Controller xử lý form
- [x] ✅ notifications.html - Form gửi thông báo
- [x] ✅ Firebase configuration - Service account key

### Mobile:
- [x] ✅ NutriCookMessagingService.kt - Nhận FCM message
- [x] ✅ NotificationScheduler.kt - Đặt lịch thông báo định kỳ
- [x] ✅ ReminderReceiver.kt - Xử lý thông báo định kỳ
- [x] ✅ NotificationUtils.kt - Utility tạo notification
- [x] ✅ MainActivity.kt - Xin quyền và khởi tạo
- [x] ✅ AndroidManifest.xml - Đăng ký service và receiver
- [x] ✅ Firebase configuration - google-services.json

---

---

## 🏃 Thông Báo Exercise (Foreground Service Notification)

### Tổng quan

Khi user bắt đầu tập thể dục, app sử dụng **Foreground Service** để chạy timer nền và hiển thị notification với tiến trình tập luyện. Notification này cho phép user:
- Xem tiến trình tập luyện (thời gian, calories)
- Tạm dừng/Tiếp tục từ notification
- Dừng exercise hoàn toàn

### Cơ chế Resume Exercise từ Notification

#### Vấn đề đã giải quyết:

**Trước đây:** Khi user đang tập "Đạp xe" (5 phút), bấm dừng, rồi chuyển sang màn hình "Bơi lội" và bấm "Tiếp tục" → "Bơi lội" bắt đầu từ 0:00 (sai).

**Hiện tại:** Khi user bấm "Tiếp tục" từ notification hoặc màn hình khác:
- Service kiểm tra xem có exercise nào đang dừng không
- Nếu có exercise đang dừng (ví dụ "Đạp xe" ở 5 phút), resume exercise đó
- Exercise mới (ví dụ "Bơi lội") không bắt đầu nếu có exercise cũ đang dừng

#### Code Implementation:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/service/ExerciseService.kt

// Service lưu trữ exercise đang chạy/dừng
private var exerciseName = ""
private var currentSeconds = 0
private var isRunning = false

// Method kiểm tra có exercise đang active không
fun hasActiveExercise(): Boolean = exerciseName.isNotEmpty() && (isRunning || currentSeconds > 0)
fun getExerciseName(): String = exerciseName

// Khi nhận ACTION_START
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
        ACTION_START -> {
            val newExerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Bài tập"
            val hasActiveExercise = exerciseName.isNotEmpty() && (isRunning || currentSeconds > 0)
            val isDifferentExercise = exerciseName != newExerciseName && exerciseName.isNotEmpty()
            
            // Nếu có exercise đang dừng và khác với exercise mới, KHÔNG start exercise mới
            if (hasActiveExercise && isDifferentExercise && !isRunning) {
                return START_STICKY // Không start exercise mới
            }
            
            // ... logic start exercise mới hoặc resume exercise hiện tại
        }
        ACTION_RESUME -> {
            // Resume exercise đang dừng (bất kể màn hình nào)
            resumeExercise()
        }
    }
}
```

#### UI Logic (ExerciseDetailScreen.kt):

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/profile/ExerciseDetailScreen.kt

Button(onClick = {
    if (isRunning) {
        // Pause exercise
        val intent = Intent(context, ExerciseService::class.java).apply {
            action = ExerciseService.ACTION_PAUSE
        }
        context.startService(intent)
    } else {
        // Kiểm tra xem có exercise khác đang dừng không
        if (isServiceBound && service != null) {
            val serviceExerciseName = service!!.getExerciseName()
            val serviceHasActive = service!!.hasActiveExercise()
            val serviceIsRunning = service!!.getIsRunning()
            
            // Nếu có exercise khác đang dừng, resume exercise đó
            if (serviceHasActive && serviceExerciseName != exerciseName && !serviceIsRunning) {
                // Resume exercise cũ thay vì start exercise mới
                val resumeIntent = Intent(context, ExerciseService::class.java).apply {
                    action = ExerciseService.ACTION_RESUME
                }
                context.startService(resumeIntent)
                return@onClick
            }
        }
        
        // Start exercise mới hoặc resume exercise hiện tại
        val intent = Intent(context, ExerciseService::class.java).apply {
            action = ExerciseService.ACTION_START
            putExtra(ExerciseService.EXTRA_EXERCISE_NAME, exerciseName)
            // ...
        }
        context.startForegroundService(intent)
    }
})
```

#### Notification Actions:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/service/ExerciseService.kt

private fun createNotification(): Notification {
    // Pause/Resume button
    val pauseResumeIntent = Intent(this, ExerciseService::class.java).apply {
        action = if (isRunning) ACTION_PAUSE else ACTION_RESUME
    }
    val pauseResumePendingIntent = PendingIntent.getService(
        this, 2, pauseResumeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .addAction(
            R.drawable.ic_launcher_foreground,
            if (isRunning) "⏸ Tạm dừng" else "▶ Tiếp tục",
            pauseResumePendingIntent
        )
        .addAction(
            R.drawable.ic_launcher_foreground,
            "⏹ Dừng",
            stopPendingIntent
        )
        // ...
}
```

#### Luồng hoạt động:

```
1. User tập "Đạp xe" 5 phút → Bấm dừng
   ↓
2. Service lưu: exerciseName = "Đạp xe", currentSeconds = 300, isRunning = false
   ↓
3. User chuyển sang màn hình "Bơi lội" (hiển thị 00:00)
   ↓
4. User bấm "Tiếp tục" từ notification hoặc màn hình "Bơi lội"
   ↓
5. Service kiểm tra: hasActiveExercise() = true, exerciseName = "Đạp xe"
   ↓
6. Service resume "Đạp xe" từ 5 phút (không start "Bơi lội")
   ↓
7. Notification hiển thị: "Đạp xe • 05:00 / 15:00 • 33/100 kcal"
```

#### File locations:

- **Service:** `mobile/app/src/main/java/com/example/nutricook/service/ExerciseService.kt`
- **UI:** `mobile/app/src/main/java/com/example/nutricook/view/profile/ExerciseDetailScreen.kt`
- **Notification Channel:** `exercise_channel` (IMPORTANCE_HIGH)

---

## 🎉 Kết Luận

Hệ thống thông báo của NutriCook hoạt động hoàn chỉnh với:
- ✅ Gửi thông báo từ dashboard đến tất cả người dùng
- ✅ Thông báo định kỳ (7h, 12h, 19h)
- ✅ Tự động xin quyền notification (Android 13+)
- ✅ Click vào notification → Mở app
- ✅ Hiển thị logo trong notification
- ✅ Quản lý FCM tokens trong Firestore
- ✅ **Foreground Service Notification cho exercise** (mới)
- ✅ **Resume exercise từ notification** (mới)
- ✅ **Kiểm tra và resume exercise đang dừng** (mới)

Tất cả các file đã được triển khai và sẵn sàng sử dụng! 🚀

