package com.example.nutricook.view.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object NotificationScheduler {
    fun scheduleDailyReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val messages = listOf(
            "Buổi sáng rồi! Hãy ăn sáng để có năng lượng bắt đầu ngày mới ☀️",
            "Giờ trưa đến rồi! Ghi lại bữa ăn của bạn nhé 🍚",
            "Buổi tối đến rồi! Cùng xem hôm nay bạn đã đạt được mục tiêu chưa 🌙"
        )
        val hours = listOf(7, 12, 19)

        for (i in messages.indices) {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("message", messages[i])
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hours[i])
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
}
