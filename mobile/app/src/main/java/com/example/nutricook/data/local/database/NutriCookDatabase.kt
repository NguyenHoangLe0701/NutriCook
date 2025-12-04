package com.example.nutricook.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nutricook.data.local.database.dao.CategoryDao
import com.example.nutricook.data.local.database.dao.FoodItemDao
import com.example.nutricook.data.local.database.dao.RecipeDao
import com.example.nutricook.data.local.database.entities.CachedCategory
import com.example.nutricook.data.local.database.entities.CachedFoodItem
import com.example.nutricook.data.local.database.entities.CachedRecipe

/**
 * Room Database cho offline caching
 * Version: 1 - Initial version
 */
@Database(
    entities = [
        CachedCategory::class,
        CachedFoodItem::class,
        CachedRecipe::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NutriCookDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun foodItemDao(): FoodItemDao
    abstract fun recipeDao(): RecipeDao
}





