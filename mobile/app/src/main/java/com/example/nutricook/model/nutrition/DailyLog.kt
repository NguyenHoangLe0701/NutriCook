package com.example.nutricook.model.nutrition

data class DailyLog(
    val dateId: String = "", // Quan trọng: Format "2025-11-20"
    val calories: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val carb: Float = 0f,
    val updatedAt: Long = System.currentTimeMillis()
)