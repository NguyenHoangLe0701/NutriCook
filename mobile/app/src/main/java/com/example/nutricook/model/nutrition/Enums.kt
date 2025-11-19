package com.example.nutricook.model.nutrition

import kotlinx.serialization.Serializable

@Serializable
enum class Sex { MALE, FEMALE }

@Serializable
enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2), LIGHT(1.375), MODERATE(1.55), ACTIVE(1.725), VERY_ACTIVE(1.9)
}

@Serializable
enum class Goal { LOSE, MAINTAIN, GAIN }

@Serializable
enum class BmrFormula { MIFFLIN, HARRIS }

@Serializable
enum class FatMode { PERCENT, PER_KG }
