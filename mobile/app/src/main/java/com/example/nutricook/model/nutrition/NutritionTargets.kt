package com.example.nutricook.model.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class NutritionTargets(
    val caloriesTarget: Int = 2200,
    val proteinG: Double = 120.0,
    val fatG: Double = 70.0,
    val carbG: Double = 250.0
)
