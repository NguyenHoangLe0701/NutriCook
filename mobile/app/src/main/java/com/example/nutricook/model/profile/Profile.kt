package com.example.nutricook.model.profile

import com.example.nutricook.model.nutrition.NutritionProfile
import com.example.nutricook.model.user.User

data class Profile(
    val user: User,
    val posts: Int = 0,
    val following: Int = 0,
    val followers: Int = 0,
    val saves: Int = 0,
    val activities: Int = 0,
    val bio: String? = null,
    val dayOfBirth: String? = null,
    val gender: String? = null,

    val nutrition: NutritionProfile = NutritionProfile()
)