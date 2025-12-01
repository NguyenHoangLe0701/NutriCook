package com.example.nutricook.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.nutricook.data.local.database.converters.ListConverter

/**
 * Entity để cache recipe data từ Firestore
 */
@Entity(tableName = "cached_recipes")
@TypeConverters(ListConverter::class)
data class CachedRecipe(
    @PrimaryKey
    val recipeId: String,
    val recipeName: String,
    val userEmail: String,
    val estimatedTime: String,
    val servings: String,
    val imageUrls: List<String>?,
    val description: String?,
    val notes: String?,
    val tips: String?,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val createdAt: Long?,
    val lastUpdated: Long = System.currentTimeMillis()
)

