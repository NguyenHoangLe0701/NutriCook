package com.example.nutricook.view.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val message = intent?.getStringExtra("message") ?: "Đừng quên ghi lại bữa ăn hôm nay nhé 🍱"
        NotificationUtils.showNotification(
            context,
            "NutriCook nhắc nhở",
            message
        )
    }
}
