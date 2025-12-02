package com.example.nutricook.data.local.database.dao

import androidx.room.*
import com.example.nutricook.data.local.database.entities.CachedRecipe
import kotlinx.coroutines.flow.Flow

/**
 * DAO cho Recipe operations
 */
@Dao
interface RecipeDao {
    @Query("SELECT * FROM cached_recipes ORDER BY lastUpdated DESC")
    fun getAllRecipes(): Flow<List<CachedRecipe>>

    @Query("SELECT * FROM cached_recipes WHERE recipeId = :recipeId")
    suspend fun getRecipeById(recipeId: String): CachedRecipe?

    @Query("SELECT * FROM cached_recipes ORDER BY lastUpdated DESC LIMIT :limit")
    suspend fun getRecentRecipes(limit: Int): List<CachedRecipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: CachedRecipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<CachedRecipe>)

    @Query("DELETE FROM cached_recipes")
    suspend fun deleteAllRecipes()

    @Query("SELECT COUNT(*) FROM cached_recipes")
    suspend fun getRecipeCount(): Int
}



