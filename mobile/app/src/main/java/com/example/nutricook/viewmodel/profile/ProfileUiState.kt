package com.example.nutricook.viewmodel.profile

import com.example.nutricook.model.profile.Profile
import com.example.nutricook.model.nutrition.NutritionProfile
import com.example.nutricook.model.nutrition.recalculate

data class ProfileUiState(
    val loading: Boolean = true,
    val updating: Boolean = false,
    val message: String? = null,
    val profile: Profile? = null,
    val nutri: NutritionProfile = NutritionProfile().recalculate()
)
