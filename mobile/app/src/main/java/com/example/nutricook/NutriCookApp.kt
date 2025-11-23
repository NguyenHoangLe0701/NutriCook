package com.example.nutricook

import android.app.Application
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NutriCookApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Request FCM token khi app khởi động
        requestFCMToken()
    }
    
    private fun requestFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("NutriCookApp", "Failed to get FCM token", task.exception)
                return@addOnCompleteListener
            }
            
            // Token sẽ được lưu tự động trong NutriCookMessagingService.onNewToken()
            val token = task.result
            Log.d("NutriCookApp", "FCM Token: $token")
        }
    }
}
