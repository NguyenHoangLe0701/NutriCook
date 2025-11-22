package com.example.nutricook.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.nutricook.MainActivity
import com.example.nutricook.R
import kotlinx.coroutines.*

class ExerciseService : Service() {
    
    private val binder = ExerciseBinder()
    private var totalSeconds = 0
    private var currentSeconds = 0
    private var totalCalories = 0
    private var exerciseName = ""
    private var isRunning = false
    private var startTime = 0L
    private var elapsedTime = 0L
    
    private var job: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }
    
    inner class ExerciseBinder : Binder() {
        fun getService(): ExerciseService = this@ExerciseService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Bài tập"
                totalSeconds = intent.getIntExtra(EXTRA_TOTAL_SECONDS, 900)
                totalCalories = intent.getIntExtra(EXTRA_TOTAL_CALORIES, 150)
                // Nhận current state từ UI nếu có (để resume từ vị trí cũ)
                val savedSeconds = intent.getIntExtra(EXTRA_CURRENT_SECONDS, 0)
                if (savedSeconds > 0 && currentSeconds == 0) {
                    currentSeconds = savedSeconds
                    elapsedTime = (savedSeconds * 1000).toLong()
                }
                startExercise()
            }
            ACTION_PAUSE -> pauseExercise()
            ACTION_RESUME -> resumeExercise()
            ACTION_RESET -> resetExercise()
            ACTION_STOP -> stopExercise()
            ACTION_UPDATE -> {
                // Update state từ UI (optional)
                val newSeconds = intent.getIntExtra(EXTRA_CURRENT_SECONDS, currentSeconds)
                if (newSeconds != currentSeconds && !isRunning) {
                    currentSeconds = newSeconds
                    updateNotification()
                }
            }
        }
        // START_STICKY: Service tự động restart nếu bị kill
        // START_NOT_STICKY: Service không restart nếu bị kill
        // START_REDELIVER_INTENT: Service restart và nhận lại intent cuối cùng
        return START_STICKY // Dùng START_STICKY để service tự restart nếu bị kill, chạy nền ngay cả khi app tắt
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Kiểm tra channel đã tồn tại chưa
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                // Chỉ tạo channel nếu chưa có
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Đang tập thể dục",
                    NotificationManager.IMPORTANCE_HIGH // High importance để hiển thị trong notification panel
                ).apply {
                    description = "Hiển thị tiến trình tập thể dục"
                    setShowBadge(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC // Hiển thị đầy đủ trên lock screen
                    enableVibration(false)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
    
    private fun startExercise() {
        if (isRunning) {
            // Nếu đang chạy rồi, chỉ update notification
            updateNotification()
            return
        }
        try {
            // Reset elapsedTime nếu bắt đầu mới (currentSeconds = 0)
            if (currentSeconds == 0) {
                elapsedTime = 0L
                startTime = SystemClock.elapsedRealtime()
            } else {
                // Resume từ vị trí cũ
                elapsedTime = (currentSeconds * 1000).toLong()
                startTime = SystemClock.elapsedRealtime() - elapsedTime
            }
            
            isRunning = true
            
            // Tạo và start foreground notification
            val notification = createNotification()
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                e.printStackTrace()
                // Nếu startForeground fail, vẫn chạy timer
            }
            
            // Start timer
            startTimer()
        } catch (e: Exception) {
            e.printStackTrace()
            isRunning = false
        }
    }
    
    private fun startTimer() {
        job?.cancel() // Cancel timer cũ nếu có
        job = serviceScope.launch(Dispatchers.Default) {
            while (isRunning && currentSeconds < totalSeconds) {
                delay(1000)
                if (isRunning) {
                    currentSeconds++
                    elapsedTime = SystemClock.elapsedRealtime() - startTime
                    // Update notification mỗi giây
                    updateNotification()
                }
            }
            if (currentSeconds >= totalSeconds) {
                // Hoàn thành bài tập
                isRunning = false
                updateNotification()
            }
        }
    }
    
    private fun pauseExercise() {
        if (!isRunning) return
        isRunning = false
        elapsedTime = SystemClock.elapsedRealtime() - startTime
        job?.cancel()
        updateNotification()
    }
    
    private fun resumeExercise() {
        if (isRunning) return
        isRunning = true
        startTime = SystemClock.elapsedRealtime() - elapsedTime
        startTimer() // Resume timer
    }
    
    private fun resetExercise() {
        isRunning = false
        currentSeconds = 0
        elapsedTime = 0L
        startTime = SystemClock.elapsedRealtime()
        updateNotification()
    }
    
    private fun stopExercise() {
        isRunning = false
        stopForeground(true)
        stopSelf()
    }
    
    private fun calculateCaloriesBurned(): Int {
        return if (totalSeconds > 0) {
            (totalCalories.toFloat() / totalSeconds * currentSeconds).toInt()
        } else 0
    }
    
    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_exercise", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, ExerciseService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val pauseResumeIntent = Intent(this, ExerciseService::class.java).apply {
            action = if (isRunning) ACTION_PAUSE else ACTION_RESUME
        }
        val pauseResumePendingIntent = PendingIntent.getService(
            this,
            2,
            pauseResumeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val caloriesBurned = calculateCaloriesBurned()
        val progress = if (totalSeconds > 0) (currentSeconds.toFloat() / totalSeconds * 100).toInt() else 0
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🔥 $exerciseName")
            .setContentText("${formatTime(currentSeconds)} / ${formatTime(totalSeconds)} • $caloriesBurned/$totalCalories kcal")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Notification không thể swipe away - QUAN TRỌNG để chạy nền
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority để hiển thị trong notification panel
            .setCategory(NotificationCompat.CATEGORY_WORKOUT) // Category workout để hệ thống biết đây là workout
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // QUAN TRỌNG: Hiển thị đầy đủ trên lock screen
            .setProgress(100, progress, false) // Progress bar hiển thị tiến trình
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false) // Không tự động xóa khi click
            .setOnlyAlertOnce(true) // Chỉ alert một lần, không vibrate mỗi update
            .setDefaults(0) // Không có sound/vibration mặc định
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // Foreground service chạy ngay
            .setChronometerCountDown(false) // Không dùng countdown
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
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("⏱️ Thời gian: ${formatTime(currentSeconds)} / ${formatTime(totalSeconds)}\n" +
                            "🔥 Calo: $caloriesBurned / $totalCalories kcal\n" +
                            "📊 Tiến trình: $progress%\n" +
                            (if (isRunning) "▶️ Đang chạy..." else "⏸️ Đã tạm dừng") +
                            "\n💡 Notification này sẽ hiển thị trên màn hình khóa")
            )
            .build()
    }
    
    private fun updateNotification() {
        // Luôn update notification để hiển thị trong notification panel và lock screen
        try {
            val notification = createNotification()
            // QUAN TRỌNG: Luôn dùng startForeground khi service đang chạy để:
            // 1. Service không bị kill khi app tắt
            // 2. Notification hiển thị trên lock screen
            // 3. Timer tiếp tục chạy nền
            if (isRunning || currentSeconds > 0) {
                try {
                    startForeground(NOTIFICATION_ID, notification)
                } catch (e: Exception) {
                    // Fallback nếu startForeground fail
                    e.printStackTrace()
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            } else {
                // Nếu không chạy, chỉ update notification thường
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Public methods để UI có thể lấy thông tin
    fun getCurrentSeconds(): Int = currentSeconds
    fun getTotalSeconds(): Int = totalSeconds
    fun getCaloriesBurned(): Int = calculateCaloriesBurned()
    fun getTotalCalories(): Int = totalCalories
    fun getIsRunning(): Boolean = isRunning
    fun getProgress(): Float = if (totalSeconds > 0) currentSeconds.toFloat() / totalSeconds else 0f
    
    override fun onDestroy() {
        super.onDestroy()
        // Chỉ cancel job nếu user dừng exercise, không cancel khi service bị kill
        if (!isRunning) {
            job?.cancel()
        }
        // Không cancel serviceScope để service có thể tiếp tục nếu restart
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Khi task bị remove (app swipe away), vẫn giữ service chạy
        // Service sẽ tự động restart với START_STICKY
        if (isRunning) {
            // Đảm bảo notification vẫn hiển thị
            updateNotification()
        }
    }
    
    companion object {
        const val CHANNEL_ID = "exercise_channel"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "com.example.nutricook.ACTION_START"
        const val ACTION_PAUSE = "com.example.nutricook.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.nutricook.ACTION_RESUME"
        const val ACTION_RESET = "com.example.nutricook.ACTION_RESET"
        const val ACTION_STOP = "com.example.nutricook.ACTION_STOP"
        
        const val EXTRA_EXERCISE_NAME = "extra_exercise_name"
        const val EXTRA_TOTAL_SECONDS = "extra_total_seconds"
        const val EXTRA_TOTAL_CALORIES = "extra_total_calories"
        const val EXTRA_CURRENT_SECONDS = "extra_current_seconds"
        
        const val ACTION_UPDATE = "com.example.nutricook.ACTION_UPDATE"
    }
}

