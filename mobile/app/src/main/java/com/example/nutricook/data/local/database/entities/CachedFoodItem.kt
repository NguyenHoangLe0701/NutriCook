package com.example.nutricook.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity để cache food item data từ Firestore
 */
@Entity(tableName = "cached_food_items")
data class CachedFoodItem(
    @PrimaryKey
    val id: Long,
    val name: String,
    val calories: String,
    val imageUrl: String?,
    val unit: String,
    val fat: Double,
    val carbs: Double,
    val protein: Double,
    val cholesterol: Double,
    val sodium: Double,
    val vitamin: Double,
    val vitaminA: Double = 0.0,
    val vitaminB1: Double = 0.0,
    val vitaminB2: Double = 0.0,
    val vitaminB3: Double = 0.0,
    val vitaminB6: Double = 0.0,
    val vitaminB9: Double = 0.0,
    val vitaminB12: Double = 0.0,
    val vitaminC: Double = 0.0,
    val vitaminD: Double = 0.0,
    val vitaminE: Double = 0.0,
    val vitaminK: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)





