package com.example.nutricook.model.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class MacroPrefs(
    val proteinGPerKg: Double = 1.8,
    val fatMode: FatMode = FatMode.PERCENT,
    val fatPercent: Double = 0.30,  // dùng khi PERCENT
    val fatGPerKg: Double = 0.8     // dùng khi PER_KG
)
