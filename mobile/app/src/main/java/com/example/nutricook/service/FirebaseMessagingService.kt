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
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority để hiển thị trên lock screen
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // QUAN TRỌNG: Hiển thị trên lock screen
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
            .setDefaults(NotificationCompat.DEFAULT_ALL)

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

