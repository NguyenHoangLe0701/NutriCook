package com.example.nutricook.data.model

// Khớp với Category entity từ backend
data class CategoryResponse(
    val id: Long,
    val name: String,
    val icon: String,
    val color: String
)

// Khớp với FoodItem entity từ backend
data class FoodItemResponse(
    val id: Long,
    val name: String,
    val calories: String,
    val imageUrl: String?
)