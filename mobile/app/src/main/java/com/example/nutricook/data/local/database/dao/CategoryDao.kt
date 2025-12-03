package com.example.nutricook.data.local.database.dao

import androidx.room.*
import com.example.nutricook.data.local.database.entities.CachedCategory
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho Category operations
 */
@Dao
interface CategoryDao {
    @Query("SELECT * FROM cached_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CachedCategory>>

    @Query("SELECT * FROM cached_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CachedCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CachedCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CachedCategory>)

    @Query("DELETE FROM cached_categories")
    suspend fun deleteAllCategories()

    @Query("SELECT COUNT(*) FROM cached_categories")
    suspend fun getCategoryCount(): Int
}




