package com.example.nutricook.model.nutrition

import kotlinx.serialization.Serializable

@Serializable
data class BodyMetrics(
    val sex: Sex = Sex.MALE,
    val age: Int = 20,
    val heightCm: Double = 170.0,
    val weightKg: Double = 65.0,
    val activity: ActivityLevel = ActivityLevel.LIGHT,
    val bmrFormula: BmrFormula = BmrFormula.MIFFLIN
)
