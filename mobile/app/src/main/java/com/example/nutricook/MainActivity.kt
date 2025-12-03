package com.example.nutricook

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.nutricook.ui.theme.NutriCookTheme
import com.example.nutricook.view.nav.NavGraph
import com.example.nutricook.view.notifications.NotificationScheduler
import com.example.nutricook.view.notifications.NotificationUtils
import com.example.nutricook.service.ExerciseService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Launcher để xin quyền thông báo
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                NotificationUtils.showNotification(
                    this,
                    "🌿 NutriCook chào bạn",
                    "Cảm ơn bạn đã bật thông báo! Hãy chăm sóc sức khỏe mỗi ngày nhé 💪"
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔹 Xin quyền gửi thông báo (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(permission)
            }
        }

        // 🔹 Tạo kênh thông báo (chỉ cần 1 lần)
        NotificationUtils.createNotificationChannel(this)
        
        // 🔹 Tạo kênh thông báo cho FCM (quan trọng - phải tạo trước khi nhận notification)
        createFcmNotificationChannel(this)
        
        // 🔹 Tạo kênh thông báo cho Exercise Service
        createExerciseNotificationChannel(this)

        // 🔹 Đặt lịch nhắc nhở (7h, 12h, 19h)
        NotificationScheduler.scheduleDailyReminders(this)

        // 🔹 Chỉ hiển thị lời chào khi user mới đăng nhập lần đầu
        val prefs = getSharedPreferences("nutricook_prefs", MODE_PRIVATE)
        val isFirstLogin = prefs.getBoolean("is_first_login", true)
        if (isFirstLogin) {
            NotificationUtils.showNotification(
                this,
                "🌿 NutriCook chào bạn",
                "Hãy dành chút thời gian cho cơ thể và sức khỏe của bạn hôm nay nhé 💫"
            )
            prefs.edit().putBoolean("is_first_login", false).apply()
        }

        // 🔹 Giao diện Compose
        setContent {
            NutriCookTheme {
                val navController = rememberNavController()
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavGraph(navController = navController)
                }
            }
        }
    }
    
    /**
     * Tạo notification channel cho FCM notifications
     * QUAN TRỌNG: Phải tạo channel này trước khi nhận notification từ FCM
     */
    private fun createFcmNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            val CHANNEL_ID = "nutricook_notifications"
            val CHANNEL_NAME = "NutriCook Notifications"
            val CHANNEL_DESCRIPTION = "Thông báo từ NutriCook"
            
            // Kiểm tra channel đã tồn tại chưa
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null || existingChannel.importance != NotificationManager.IMPORTANCE_HIGH) {
                // Xóa channel cũ nếu có (để tạo lại với đúng importance)
                if (existingChannel != null) {
                    notificationManager.deleteNotificationChannel(CHANNEL_ID)
                }
                
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // High để hiển thị notification
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableLights(true)
                    enableVibration(true)
                    setShowBadge(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC // Hiển thị trên lock screen
                }
                notificationManager.createNotificationChannel(channel)
                android.util.Log.d("MainActivity", "FCM notification channel created: $CHANNEL_ID")
            } else {
                android.util.Log.d("MainActivity", "FCM notification channel already exists: $CHANNEL_ID")
            }
        }
    }
    
    private fun createExerciseNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            // Kiểm tra channel đã tồn tại chưa
            val existingChannel = notificationManager.getNotificationChannel(ExerciseService.CHANNEL_ID)
            if (existingChannel == null || existingChannel.importance != NotificationManager.IMPORTANCE_HIGH) {
                // Xóa channel cũ nếu có (để tạo lại với đúng importance)
                if (existingChannel != null) {
                    notificationManager.deleteNotificationChannel(ExerciseService.CHANNEL_ID)
                }
                
                val channel = NotificationChannel(
                    ExerciseService.CHANNEL_ID,
                    "Đang tập thể dục",
                    NotificationManager.IMPORTANCE_HIGH // High để hiển thị trong notification panel
                ).apply {
                    description = "Hiển thị tiến trình tập thể dục"
                    setShowBadge(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    enableVibration(false)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
