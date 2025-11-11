package com.example.nutricook.model.nutrition

enum class Sex { MALE, FEMALE }
enum class ActivityLevel(val factor: Double) {
    SEDENTARY(1.2), LIGHT(1.375), MODERATE(1.55), ACTIVE(1.725), VERY_ACTIVE(1.9)
}

enum class Goal { LOSE, MAINTAIN, GAIN }
enum class BmrFormula { MIFFLIN, HARRIS }
enum class FatMode { PERCENT, PER_KG }