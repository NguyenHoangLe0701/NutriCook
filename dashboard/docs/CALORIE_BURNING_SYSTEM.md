# Hệ Thống Đốt Calories - Triển Khai Chi Tiết

## 📋 Tổng quan

Hệ thống đốt calories của NutriCook theo dõi và tính toán lượng calories đốt cháy khi người dùng tập thể dục. Hệ thống sử dụng **Foreground Service** để chạy timer nền, tính toán calories theo thời gian thực, và hiển thị tiến trình trong notification.

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│              EXERCISE DETAIL SCREEN (UI)                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ExerciseDetailScreen.kt                              │  │
│  │  - Hiển thị exercise info                             │  │
│  │  - Hiển thị timer và calories                         │  │
│  │  - Control buttons (Start/Pause/Reset)                 │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                       │
│                     │ Intent (ACTION_START/PAUSE/RESUME)    │
│                     │                                       │
└─────────────────────┼───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              EXERCISE SERVICE (Background)                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  ExerciseService.kt                                   │  │
│  │  - Chạy timer mỗi giây                                │  │
│  │  - Tính calories theo thời gian thực                  │  │
│  │  - Cập nhật notification                               │  │
│  │  - Lưu state (exerciseName, currentSeconds, etc.)     │  │
│  └──────────────────┬───────────────────────────────────┘  │
│                     │                                       │
│                     │ Update mỗi giây                       │
│                     │                                       │
┌─────────────────────▼───────────────────────────────────────┐
│              NOTIFICATION (Foreground)                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Exercise Notification                                │  │
│  │  - Hiển thị: "05:23 / 15:00 • 35/100 kcal"           │  │
│  │  - Progress bar                                       │  │
│  │  - Actions: Pause/Resume, Stop                        │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Các File và Vị Trí

### 1. **Exercise Service (Background Service)**

#### 📂 `mobile/app/src/main/java/com/example/nutricook/service/ExerciseService.kt`

**Nhiệm vụ:** Service chạy nền để theo dõi timer và tính calories.

**Các biến quan trọng:**
```kotlin
private var totalSeconds = 0        // Tổng thời gian mục tiêu (ví dụ: 900s = 15 phút)
private var currentSeconds = 0      // Thời gian đã tập (tăng mỗi giây)
private var totalCalories = 0       // Tổng calories mục tiêu (ví dụ: 100 kcal)
private var exerciseName = ""       // Tên exercise (ví dụ: "Đạp xe")
private var isRunning = false       // Trạng thái đang chạy/dừng
private var elapsedTime = 0L        // Thời gian đã trôi qua (milliseconds)
```

**Các method quan trọng:**
- `startExercise()`: Bắt đầu exercise và timer
- `pauseExercise()`: Tạm dừng exercise
- `resumeExercise()`: Tiếp tục exercise từ vị trí dừng
- `resetExercise()`: Reset về 0
- `calculateCaloriesBurned()`: Tính calories đã đốt
- `updateNotification()`: Cập nhật notification với tiến trình mới

---

### 2. **Exercise Detail Screen (UI)**

#### 📂 `mobile/app/src/main/java/com/example/nutricook/view/profile/ExerciseDetailScreen.kt`

**Nhiệm vụ:** Màn hình hiển thị thông tin exercise và điều khiển.

**Các state:**
```kotlin
var currentSeconds by remember(exerciseName) { mutableStateOf(0) }
var caloriesBurned by remember(exerciseName) { mutableStateOf(0) }
var isRunning by remember(exerciseName) { mutableStateOf(false) }
```

**Các chức năng:**
- Hiển thị timer: `formatTime(currentSeconds)` / `formatTime(totalSeconds)`
- Hiển thị calories: `caloriesBurned / exerciseCalories kcal`
- Hiển thị progress: Circular progress và linear progress bar
- Control buttons: Start/Pause, Reset

---

## 🔄 Luồng hoạt động chi tiết

### 1. **Bắt đầu Exercise**

#### Bước 1: User chọn exercise và bấm "Tiếp tục"

```kotlin
// File: ExerciseDetailScreen.kt
Button(onClick = {
    val intent = Intent(context, ExerciseService::class.java).apply {
        action = ExerciseService.ACTION_START
        putExtra(ExerciseService.EXTRA_EXERCISE_NAME, exerciseName)      // "Đạp xe"
        putExtra(ExerciseService.EXTRA_TOTAL_SECONDS, totalSeconds)      // 900 (15 phút)
        putExtra(ExerciseService.EXTRA_TOTAL_CALORIES, exerciseCalories) // 100 kcal
        putExtra(ExerciseService.EXTRA_CURRENT_SECONDS, currentSeconds)  // 0 (bắt đầu từ đầu)
    }
    context.startForegroundService(intent)
})
```

#### Bước 2: Service nhận ACTION_START

```kotlin
// File: ExerciseService.kt
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
        ACTION_START -> {
            exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Bài tập"
            totalSeconds = intent.getIntExtra(EXTRA_TOTAL_SECONDS, 900)
            totalCalories = intent.getIntExtra(EXTRA_TOTAL_CALORIES, 150)
            
            // Reset về 0 nếu bắt đầu mới
            if (currentSeconds == 0) {
                elapsedTime = 0L
                startTime = SystemClock.elapsedRealtime()
            }
            
            startExercise()
        }
    }
    return START_STICKY
}
```

#### Bước 3: Service bắt đầu timer

```kotlin
// File: ExerciseService.kt
private fun startExercise() {
    isRunning = true
    
    // Tạo và hiển thị foreground notification
    val notification = createNotification()
    startForeground(NOTIFICATION_ID, notification)
    
    // Bắt đầu timer
    startTimer()
}

private fun startTimer() {
    job?.cancel() // Cancel timer cũ nếu có
    job = serviceScope.launch(Dispatchers.Default) {
        while (isRunning && currentSeconds < totalSeconds) {
            delay(1000) // Đợi 1 giây
            if (isRunning) {
                currentSeconds++ // Tăng thời gian
                elapsedTime = SystemClock.elapsedRealtime() - startTime
                
                // Cập nhật notification mỗi giây
                updateNotification()
            }
        }
        
        // Hoàn thành exercise
        if (currentSeconds >= totalSeconds) {
            isRunning = false
            updateNotification()
        }
    }
}
```

---

### 2. **Tính toán Calories**

#### Công thức tính calories:

```kotlin
// File: ExerciseService.kt
private fun calculateCaloriesBurned(): Int {
    return if (totalSeconds > 0) {
        // Calories = (Total Calories / Total Seconds) * Current Seconds
        (totalCalories.toFloat() / totalSeconds * currentSeconds).toInt()
    } else 0
}
```

**Ví dụ:**
- **Exercise:** Đạp xe
- **Mục tiêu:** 15 phút (900s) = 100 kcal
- **Đã tập:** 5 phút (300s)

**Tính toán:**
```
Calories đã đốt = (100 kcal / 900s) * 300s = 33.33 kcal ≈ 33 kcal
```

#### Cập nhật calories trong UI:

```kotlin
// File: ExerciseDetailScreen.kt
LaunchedEffect(Unit) {
    while (true) {
        delay(500) // Update mỗi 500ms
        if (isServiceBound && service != null) {
            val newSeconds = service!!.getCurrentSeconds()
            val newCalories = service!!.getCaloriesBurned() // Tính từ service
            val newRunning = service!!.getIsRunning()
            
            // Update state
            currentSeconds = newSeconds
            caloriesBurned = newCalories
            isRunning = newRunning
        }
    }
}
```

---

### 3. **Tạm dừng và Tiếp tục**

#### Tạm dừng (Pause):

```kotlin
// File: ExerciseService.kt
private fun pauseExercise() {
    if (!isRunning) return
    isRunning = false
    
    // Lưu thời gian đã trôi qua
    elapsedTime = SystemClock.elapsedRealtime() - startTime
    
    // Dừng timer
    job?.cancel()
    
    // Cập nhật notification (hiển thị "⏸️ Đã tạm dừng")
    updateNotification()
}
```

**Kết quả:**
- Timer dừng lại
- `currentSeconds` giữ nguyên (ví dụ: 300s = 5 phút)
- Calories giữ nguyên (ví dụ: 33 kcal)
- Notification hiển thị: "⏸️ Đã tạm dừng"

#### Tiếp tục (Resume):

```kotlin
// File: ExerciseService.kt
private fun resumeExercise() {
    if (isRunning) return
    isRunning = true
    
    // Tính lại startTime để tiếp tục từ vị trí dừng
    startTime = SystemClock.elapsedRealtime() - elapsedTime
    
    // Tiếp tục timer
    startTimer()
}
```

**Kết quả:**
- Timer tiếp tục từ vị trí dừng (ví dụ: từ 5 phút)
- Calories tiếp tục tính từ vị trí dừng
- Notification hiển thị: "▶️ Đang chạy..."

---

### 4. **Reset Exercise**

```kotlin
// File: ExerciseService.kt
private fun resetExercise() {
    isRunning = false
    currentSeconds = 0
    elapsedTime = 0L
    startTime = SystemClock.elapsedRealtime()
    updateNotification()
}
```

**Kết quả:**
- Timer về 0:00
- Calories về 0 kcal
- Progress về 0%

---

## 📊 Hiển thị trong Notification

### Notification Content:

```kotlin
// File: ExerciseService.kt
private fun createNotification(): Notification {
    val caloriesBurned = calculateCaloriesBurned()
    val progress = if (totalSeconds > 0) (currentSeconds.toFloat() / totalSeconds * 100).toInt() else 0
    val timeElapsed = formatTime(currentSeconds)  // "05:23"
    val timeTotal = formatTime(totalSeconds)       // "15:00"
    val statusText = if (isRunning) "▶️ Đang chạy..." else "⏸️ Đã tạm dừng"
    
    return NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("$exerciseName • $statusText")
        .setContentText("$timeElapsed / $timeTotal • $caloriesBurned/$totalCalories kcal")
        .setProgress(100, progress, false) // Progress bar
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
        .setOngoing(true) // Không thể swipe away
        .build()
}
```

**Ví dụ notification:**
```
┌─────────────────────────────────────┐
│ Đạp xe • ▶️ Đang chạy...            │
│ 05:23 / 15:00 • 35/100 kcal         │
│ ████████░░░░░░░░░░ 35%              │
│ [⏸ Tạm dừng] [⏹ Dừng]              │
└─────────────────────────────────────┘
```

---

## 🔄 Cơ chế Resume từ Notification

### Vấn đề đã giải quyết:

**Trước đây:** Khi user đang tập "Đạp xe" (5 phút), bấm dừng, rồi chuyển sang màn hình "Bơi lội" và bấm "Tiếp tục" → "Bơi lội" bắt đầu từ 0:00 (sai).

**Hiện tại:** Khi user bấm "Tiếp tục" từ notification hoặc màn hình khác:
- Service kiểm tra xem có exercise nào đang dừng không
- Nếu có exercise đang dừng (ví dụ "Đạp xe" ở 5 phút), resume exercise đó
- Exercise mới (ví dụ "Bơi lội") không bắt đầu nếu có exercise cũ đang dừng

### Code Implementation:

```kotlin
// File: ExerciseService.kt
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

### UI Logic:

```kotlin
// File: ExerciseDetailScreen.kt
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

### Luồng hoạt động:

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

---

## 📈 Công thức tính Calories

### Công thức cơ bản:

```
Calories đã đốt = (Total Calories / Total Seconds) × Current Seconds
```

### Ví dụ tính toán:

#### Ví dụ 1: Đạp xe
- **Mục tiêu:** 15 phút (900s) = 100 kcal
- **Đã tập:** 5 phút (300s)

```
Calories = (100 kcal / 900s) × 300s = 33.33 kcal ≈ 33 kcal
```

#### Ví dụ 2: Đi bộ nhanh
- **Mục tiêu:** 20 phút (1200s) = 100 kcal
- **Đã tập:** 10 phút (600s)

```
Calories = (100 kcal / 1200s) × 600s = 50 kcal
```

#### Ví dụ 3: Yoga nhẹ
- **Mục tiêu:** 30 phút (1800s) = 100 kcal
- **Đã tập:** 15 phút (900s)

```
Calories = (100 kcal / 1800s) × 900s = 50 kcal
```

### Code Implementation:

```kotlin
// File: ExerciseService.kt
private fun calculateCaloriesBurned(): Int {
    return if (totalSeconds > 0) {
        // Tính calories theo tỉ lệ thời gian
        (totalCalories.toFloat() / totalSeconds * currentSeconds).toInt()
    } else 0
}
```

**Lưu ý:**
- Calories được tính **tuyến tính** theo thời gian
- Làm tròn xuống (`.toInt()`) để hiển thị số nguyên
- Cập nhật mỗi giây khi timer chạy

---

## 🎯 Các Exercise Types

### Danh sách exercises:

| Exercise Name | Duration | Calories | Difficulty |
|--------------|----------|----------|------------|
| Đạp xe | 15 phút | 100 kcal | Trung bình |
| Đi bộ nhanh | 20 phút | 100 kcal | Thấp |
| Yoga nhẹ | 30 phút | 100 kcal | Thấp |
| Bơi lội nhẹ | 15 phút | 100 kcal | Trung bình |
| Chạy bộ | 20 phút | 200 kcal | Cao |
| Nhảy dây | 10 phút | 150 kcal | Trung bình |

### Cấu hình exercise:

```kotlin
// File: ExerciseSuggestionsScreen.kt hoặc HomeScreen.kt
data class Exercise(
    val name: String,        // "Đạp xe"
    val duration: String,   // "15 phút"
    val calories: Int,      // 100
    val imageRes: Int,      // R.drawable.cycling
    val difficulty: String  // "Trung bình"
)
```

---

## 🔧 Service Actions

### Các action constants:

```kotlin
// File: ExerciseService.kt
companion object {
    const val ACTION_START = "com.example.nutricook.ACTION_START"
    const val ACTION_PAUSE = "com.example.nutricook.ACTION_PAUSE"
    const val ACTION_RESUME = "com.example.nutricook.ACTION_RESUME"
    const val ACTION_RESET = "com.example.nutricook.ACTION_RESET"
    const val ACTION_STOP = "com.example.nutricook.ACTION_STOP"
    
    const val EXTRA_EXERCISE_NAME = "extra_exercise_name"
    const val EXTRA_TOTAL_SECONDS = "extra_total_seconds"
    const val EXTRA_TOTAL_CALORIES = "extra_total_calories"
    const val EXTRA_CURRENT_SECONDS = "extra_current_seconds"
}
```

### Sử dụng actions:

```kotlin
// Start exercise
val intent = Intent(context, ExerciseService::class.java).apply {
    action = ExerciseService.ACTION_START
    putExtra(ExerciseService.EXTRA_EXERCISE_NAME, "Đạp xe")
    putExtra(ExerciseService.EXTRA_TOTAL_SECONDS, 900)
    putExtra(ExerciseService.EXTRA_TOTAL_CALORIES, 100)
}
context.startForegroundService(intent)

// Pause exercise
val pauseIntent = Intent(context, ExerciseService::class.java).apply {
    action = ExerciseService.ACTION_PAUSE
}
context.startService(pauseIntent)

// Resume exercise
val resumeIntent = Intent(context, ExerciseService::class.java).apply {
    action = ExerciseService.ACTION_RESUME
}
context.startService(resumeIntent)

// Reset exercise
val resetIntent = Intent(context, ExerciseService::class.java).apply {
    action = ExerciseService.ACTION_RESET
}
context.startService(resetIntent)

// Stop exercise
val stopIntent = Intent(context, ExerciseService::class.java).apply {
    action = ExerciseService.ACTION_STOP
}
context.startService(stopIntent)
```

---

## 📱 Notification Channel

### Tạo notification channel:

```kotlin
// File: ExerciseService.kt
private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID, // "exercise_channel"
            "Đang tập thể dục",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Hiển thị tiến trình tập thể dục"
            setShowBadge(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(false)
            enableLights(true)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
```

**Đăng ký trong AndroidManifest:**

```xml
<!-- File: mobile/app/src/main/AndroidManifest.xml -->
<service
    android:name=".service.ExerciseService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="health" />
```

---

## 🎨 UI Components

### Circular Progress Indicator:

```kotlin
// File: ExerciseDetailScreen.kt
Canvas(modifier = Modifier.size(200.dp)) {
    val strokeWidth = 12.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = Offset(size.width / 2, size.height / 2)
    
    // Background circle
    drawCircle(
        color = Color(0xFFE0E0E0),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
    )
    
    // Progress circle
    val progress = currentSeconds.toFloat() / totalSeconds
    val sweepAngle = 360f * progress
    drawArc(
        color = Color(0xFF20B2AA),
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
}
```

### Linear Progress Bar:

```kotlin
// File: ExerciseDetailScreen.kt
LinearProgressIndicator(
    progress = { currentSeconds.toFloat() / totalSeconds },
    modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
        .clip(RoundedCornerShape(5.dp)),
    color = Color(0xFF20B2AA),
    trackColor = Color(0xFFE0E0E0)
)
```

---

## ✅ Checklist Triển Khai

### Service:
- [x] ✅ ExerciseService.kt - Foreground service chạy timer
- [x] ✅ calculateCaloriesBurned() - Tính calories theo thời gian
- [x] ✅ startExercise() - Bắt đầu exercise
- [x] ✅ pauseExercise() - Tạm dừng exercise
- [x] ✅ resumeExercise() - Tiếp tục exercise
- [x] ✅ resetExercise() - Reset về 0
- [x] ✅ updateNotification() - Cập nhật notification
- [x] ✅ hasActiveExercise() - Kiểm tra exercise đang active
- [x] ✅ getExerciseName() - Lấy tên exercise hiện tại

### UI:
- [x] ✅ ExerciseDetailScreen.kt - Màn hình hiển thị exercise
- [x] ✅ Circular progress indicator
- [x] ✅ Linear progress bar
- [x] ✅ Timer display (MM:SS)
- [x] ✅ Calories display
- [x] ✅ Control buttons (Start/Pause, Reset)
- [x] ✅ Sync state với service

### Notification:
- [x] ✅ Notification channel (exercise_channel)
- [x] ✅ Foreground notification với progress
- [x] ✅ Pause/Resume action button
- [x] ✅ Stop action button
- [x] ✅ Update notification mỗi giây

### Logic:
- [x] ✅ Resume exercise từ notification
- [x] ✅ Kiểm tra exercise đang dừng trước khi start mới
- [x] ✅ Reset state khi chuyển exercise mới (nếu không có exercise đang dừng)

---

## 🎉 Kết Luận

Hệ thống đốt calories của NutriCook hoạt động hoàn chỉnh với:
- ✅ Timer chạy nền (Foreground Service)
- ✅ Tính calories theo thời gian thực
- ✅ Hiển thị tiến trình trong notification
- ✅ Tạm dừng/Tiếp tục từ notification
- ✅ Resume exercise đang dừng (không start exercise mới)
- ✅ Reset exercise về 0
- ✅ UI sync với service mỗi 500ms

Tất cả các file đã được triển khai và sẵn sàng sử dụng! 🚀

