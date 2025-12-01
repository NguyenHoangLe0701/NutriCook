package com.example.nutricook.data.local.database.dao

import androidx.room.*
import com.example.nutricook.data.local.database.entities.CachedFoodItem
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho FoodItem operations
 */
@Dao
interface FoodItemDao {
    @Query("SELECT * FROM cached_food_items ORDER BY name ASC")
    fun getAllFoodItems(): Flow<List<CachedFoodItem>>

    @Query("SELECT * FROM cached_food_items WHERE id = :id")
    suspend fun getFoodItemById(id: Long): CachedFoodItem?

    @Query("SELECT * FROM cached_food_items ORDER BY lastUpdated DESC LIMIT :limit")
    suspend fun getRecentFoodItems(limit: Int): List<CachedFoodItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: CachedFoodItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(foodItems: List<CachedFoodItem>)

    @Query("DELETE FROM cached_food_items")
    suspend fun deleteAllFoodItems()

    @Query("SELECT COUNT(*) FROM cached_food_items")
    suspend fun getFoodItemCount(): Int
}

