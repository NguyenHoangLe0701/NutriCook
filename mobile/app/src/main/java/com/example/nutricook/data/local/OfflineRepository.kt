package com.example.nutricook.data.local

import com.example.nutricook.data.local.database.dao.CategoryDao
import com.example.nutricook.data.local.database.dao.FoodItemDao
import com.example.nutricook.data.local.database.dao.RecipeDao
import com.example.nutricook.data.local.database.entities.CachedCategory
import com.example.nutricook.data.local.database.entities.CachedFoodItem
import com.example.nutricook.data.local.database.entities.CachedRecipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OfflineRepository: Repository pattern với offline-first approach
 * 
 * Ưu tiên đọc từ Room database (offline), sau đó sync với Firestore nếu cần
 * Giúp ứng dụng hoạt động offline và giảm số lần gọi API
 */
@Singleton
class OfflineRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val foodItemDao: FoodItemDao,
    private val recipeDao: RecipeDao
) {
    /**
     * Lấy tất cả categories từ cache
     */
    fun getAllCategories(): Flow<List<CachedCategory>> {
        return categoryDao.getAllCategories()
    }

    /**
     * Lấy category theo ID từ cache
     */
    suspend fun getCategoryById(id: Long): CachedCategory? {
        return categoryDao.getCategoryById(id)
    }

    /**
     * Lấy tất cả food items từ cache
     */
    fun getAllFoodItems(): Flow<List<CachedFoodItem>> {
        return foodItemDao.getAllFoodItems()
    }

    /**
     * Lấy food item theo ID từ cache
     */
    suspend fun getFoodItemById(id: Long): CachedFoodItem? {
        return foodItemDao.getFoodItemById(id)
    }

    /**
     * Lấy các food items mới nhất từ cache
     */
    suspend fun getRecentFoodItems(limit: Int = 10): List<CachedFoodItem> {
        return foodItemDao.getRecentFoodItems(limit)
    }

    /**
     * Lấy tất cả recipes từ cache
     */
    fun getAllRecipes(): Flow<List<CachedRecipe>> {
        return recipeDao.getAllRecipes()
    }

    /**
     * Lấy recipe theo ID từ cache
     */
    suspend fun getRecipeById(recipeId: String): CachedRecipe? {
        return recipeDao.getRecipeById(recipeId)
    }

    /**
     * Lấy các recipes mới nhất từ cache
     */
    suspend fun getRecentRecipes(limit: Int = 20): List<CachedRecipe> {
        return recipeDao.getRecentRecipes(limit)
    }

    /**
     * Kiểm tra xem có data trong cache không
     */
    suspend fun hasCachedData(): Boolean {
        val categoryCount = categoryDao.getCategoryCount()
        val foodItemCount = foodItemDao.getFoodItemCount()
        val recipeCount = recipeDao.getRecipeCount()
        return categoryCount > 0 && foodItemCount > 0 && recipeCount > 0
    }
}




