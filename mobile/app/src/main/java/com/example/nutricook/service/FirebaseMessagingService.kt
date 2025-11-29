package com.example.nutricook.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.nutricook.MainActivity
import com.example.nutricook.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore

class NutriCookMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "NutriCookFCM"
        private const val CHANNEL_ID = "nutricook_notifications"
        private const val CHANNEL_NAME = "NutriCook Notifications"
        private const val CHANNEL_DESCRIPTION = "Thông báo từ NutriCook"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Lưu token vào Firestore
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Kiểm tra xem message có data payload không
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        // Kiểm tra xem message có notification payload không
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // Hiển thị notification
            sendNotification(it.title ?: "NutriCook", it.body ?: "")
        }

        // Nếu không có notification payload, tạo từ data
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "NutriCook"
            val message = remoteMessage.data["message"] ?: ""
            sendNotification(title, message)
        }
    }

    /**
     * Tạo notification channel cho Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                // QUAN TRỌNG: Hiển thị trên lock screen
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Gửi notification đến người dùng
     */
    private fun sendNotification(title: String, messageBody: String) {
        // Intent để mở MainActivity khi click vào notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Thêm flags để đảm bảo mở đúng màn hình chính
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            // Truyền thông tin notification nếu cần
            putExtra("notification", true)
            putExtra("title", title)
            putExtra("message", messageBody)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Load logo từ drawable và convert sang Bitmap với kích thước lớn hơn
        val logoBitmap = try {
            val drawable = ContextCompat.getDrawable(this, R.drawable.logonutricook)
            if (drawable != null) {
                // Tăng kích thước logo lên 1.5x hoặc 2x để hiển thị lớn hơn
                // Kích thước lớn icon trong notification thường là 256x256dp
                // Nhân với density để có pixel thực tế
                val scaleFactor = 2.0f // Tăng gấp đôi kích thước
                val originalWidth = drawable.intrinsicWidth
                val originalHeight = drawable.intrinsicHeight
                
                val newWidth = (originalWidth * scaleFactor).toInt()
                val newHeight = (originalHeight * scaleFactor).toInt()
                
                // Tạo bitmap với kích thước mới
                val bitmap = android.graphics.Bitmap.createBitmap(
                    newWidth,
                    newHeight,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                
                // Vẽ logo với kích thước mới
                drawable.setBounds(0, 0, newWidth, newHeight)
                drawable.draw(canvas)
                
                bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading logo for notification", e)
            null
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Icon nhỏ ở góc
            .setLargeIcon(logoBitmap) // Logo lớn trong notification - QUAN TRỌNG
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true) // Tự động đóng khi click
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(pendingIntent) // Mở app khi click
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority để hiển thị trên lock screen
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // QUAN TRỌNG: Hiển thị trên lock screen
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody)) // Style cho text dài
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sound, vibration, lights

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * Lưu FCM token vào Firestore
     */
    private fun saveTokenToFirestore(token: String) {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(currentUser.uid)
                
                userRef.update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token saved to Firestore for user: ${currentUser.uid}")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error saving FCM token to Firestore", e)
                    }
            } else {
                Log.w(TAG, "User not logged in, cannot save FCM token")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving FCM token", e)
        }
    }
}

