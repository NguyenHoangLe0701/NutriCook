package com.example.nutricook.model.profile


import com.example.nutricook.model.user.User

data class Nutrition(
    val caloriesTarget: Float = 0f,
    val proteinG: Float = 0f,
    val fatG: Float = 0f,
    val carbG: Float = 0f
)

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
    val nutrition: Nutrition? = null
)