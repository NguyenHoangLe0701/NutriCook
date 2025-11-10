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
}
