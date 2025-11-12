package com.example.nutricook.model.nutrition

data class DailyTotals(
    val date: String,        // yyyy-MM-dd
    val caloriesIn: Int,
    val proteinG: Double,
    val fatG: Double,
    val carbG: Double
)
