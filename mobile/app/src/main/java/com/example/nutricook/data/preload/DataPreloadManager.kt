package com.example.nutricook.data.preload

import android.util.Log
import com.example.nutricook.data.local.database.NutriCookDatabase
import com.example.nutricook.data.local.database.dao.CategoryDao
import com.example.nutricook.data.local.database.dao.FoodItemDao
import com.example.nutricook.data.local.database.dao.RecipeDao
import com.example.nutricook.data.local.database.entities.CachedCategory
import com.example.nutricook.data.local.database.entities.CachedFoodItem
import com.example.nutricook.data.local.database.entities.CachedRecipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataPreloadManager: Quản lý việc preload dữ liệu từ Firestore vào Room database
 * 
 * Chức năng:
 * - Load categories, food items, recipes từ Firestore
 * - Cache vào Room database để sử dụng offline
 * - Cung cấp progress callback để hiển thị trên UI
 */
@Singleton
class DataPreloadManager @Inject constructor(
    private val database: NutriCookDatabase,
    private val firestore: FirebaseFirestore
) {
    private val categoryDao: CategoryDao = database.categoryDao()
    private val foodItemDao: FoodItemDao = database.foodItemDao()
    private val recipeDao: RecipeDao = database.recipeDao()

    /**
     * Preload tất cả dữ liệu cần thiết
     * @param onProgress Callback với progress (0-100)
     * @param onComplete Callback khi hoàn thành
     * @param onError Callback khi có lỗi
     */
    suspend fun preloadAllData(
        onProgress: (Int, String) -> Unit = { _, _ -> },
        onComplete: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress(0, "Đang khởi tạo...")
            
            // Kiểm tra cache cũ (nếu có data trong 24h thì vẫn sync một phần để đảm bảo data mới)
            val hasRecentCache = checkRecentCache()
            if (hasRecentCache) {
                Log.d("DataPreloadManager", "Found recent cache, doing quick sync")
                onProgress(5, "Đang kiểm tra dữ liệu cũ...")
                delay(1500) // Delay để hiển thị message
                
                // Vẫn sync một phần để đảm bảo có data mới nhất
                // Nhưng nhanh hơn vì đã có cache
                onProgress(20, "Đang đồng bộ danh mục...")
                delay(800) // Delay để hiển thị progress
                val categories = loadCategories()
                categoryDao.insertCategories(categories)
                
                onProgress(50, "Đang đồng bộ nguyên liệu...")
                delay(1000)
                val foodItems = loadFoodItems(limit = 200) // Load ít hơn khi có cache
                foodItemDao.insertFoodItems(foodItems)
                
                onProgress(80, "Đang đồng bộ công thức...")
                delay(1000)
                val recipes = loadRecipes(limit = 100) // Load ít hơn khi có cache
                recipeDao.insertRecipes(recipes)
                
                onProgress(100, "Đã đồng bộ dữ liệu!")
                delay(700) // Delay cuối cùng
                onComplete()
                return@withContext
            }

            // Load categories (20%)
            onProgress(10, "Đang tải danh mục...")
            val categories = loadCategories()
            categoryDao.insertCategories(categories)
            onProgress(20, "Đã tải ${categories.size} danh mục")

            // Load food items (50%) - Tăng limit để tải nhiều hơn
            onProgress(25, "Đang tải nguyên liệu...")
            val foodItems = loadFoodItems(limit = 500) // Tăng từ 200 lên 500
            foodItemDao.insertFoodItems(foodItems)
            onProgress(50, "Đã tải ${foodItems.size} nguyên liệu")

            // Load recipes (30%) - Tăng limit để tải nhiều hơn
            onProgress(60, "Đang tải công thức...")
            val recipes = loadRecipes(limit = 200) // Tăng từ 100 lên 200
            recipeDao.insertRecipes(recipes)
            onProgress(90, "Đã tải ${recipes.size} công thức")

            onProgress(100, "Hoàn tất!")
            onComplete()
        } catch (e: Exception) {
            Log.e("DataPreloadManager", "Error preloading data", e)
            onError(e)
        }
    }

    /**
     * Kiểm tra xem có cache gần đây không (trong 24h)
     */
    private suspend fun checkRecentCache(): Boolean {
        val categoryCount = categoryDao.getCategoryCount()
        val foodItemCount = foodItemDao.getFoodItemCount()
        val recipeCount = recipeDao.getRecipeCount()
        
        // Nếu có đủ data cơ bản thì coi như có cache
        return categoryCount > 0 && foodItemCount > 10 && recipeCount > 0
    }

    /**
     * Load categories từ Firestore
     */
    private suspend fun loadCategories(): List<CachedCategory> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("categories")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    CachedCategory(
                        id = (data["id"] as? Number)?.toLong() ?: 0L,
                        name = data["name"] as? String ?: "",
                        icon = data["icon"] as? String ?: "🍽️"
                    )
                } catch (e: Exception) {
                    Log.e("DataPreloadManager", "Error parsing category: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DataPreloadManager", "Error loading categories: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load food items từ Firestore
     */
    private suspend fun loadFoodItems(limit: Int = 500): List<CachedFoodItem> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("foodItems")
                .limit(limit.toLong()) // Giới hạn để không load quá nhiều
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    // Parse calories
                    val caloriesValue = when {
                        data["calories"] is String -> {
                            val calStr = data["calories"] as String
                            if (calStr.contains("kcal", ignoreCase = true)) calStr else "$calStr kcal"
                        }
                        data["calories"] is Number -> {
                            "${(data["calories"] as Number).toDouble().toInt()} kcal"
                        }
                        else -> "0 kcal"
                    }
                    
                    CachedFoodItem(
                        id = (data["id"] as? Number)?.toLong() ?: 0L,
                        name = data["name"] as? String ?: "",
                        calories = caloriesValue,
                        imageUrl = data["imageUrl"] as? String,
                        unit = data["unit"] as? String ?: "g",
                        fat = (data["fat"] as? Number)?.toDouble() ?: 0.0,
                        carbs = (data["carbs"] as? Number)?.toDouble() ?: 0.0,
                        protein = (data["protein"] as? Number)?.toDouble() ?: 0.0,
                        cholesterol = (data["cholesterol"] as? Number)?.toDouble() ?: 0.0,
                        sodium = (data["sodium"] as? Number)?.toDouble() ?: 0.0,
                        vitamin = (data["vitamin"] as? Number)?.toDouble() ?: 0.0,
                        vitaminA = (data["vitaminA"] as? Number)?.toDouble() ?: 0.0,
                        vitaminB1 = (data["vitaminB1"] as? Number)?.toDouble() ?: 0.0,
                        vitaminB2 = (data["vitaminB2"] as? Number)?.toDouble() ?: 0.0,
                        vitaminB3 = (data["vitaminB3"] as? Number)?.toDouble() ?: 0.0,
                        vitaminB6 = (data["vitaminB6"] as? Number)?.toDouble() ?: 0.0,
                        vitaminB9 = (data["vitaminB9"] as? Number)?.toDouble() ?: 0.0,
                        vitaminB12 = (data["vitaminB12"] as? Number)?.toDouble() ?: 0.0,
                        vitaminC = (data["vitaminC"] as? Number)?.toDouble() ?: 0.0,
                        vitaminD = (data["vitaminD"] as? Number)?.toDouble() ?: 0.0,
                        vitaminE = (data["vitaminE"] as? Number)?.toDouble() ?: 0.0,
                        vitaminK = (data["vitaminK"] as? Number)?.toDouble() ?: 0.0
                    )
                } catch (e: Exception) {
                    Log.e("DataPreloadManager", "Error parsing food item: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DataPreloadManager", "Error loading food items: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load recipes từ Firestore
     */
    private suspend fun loadRecipes(limit: Int = 200): List<CachedRecipe> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("userRecipes")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong()) // Giới hạn recipes mới nhất
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val imageUrls = data["imageUrls"] as? List<*>
                    
                    CachedRecipe(
                        recipeId = doc.id,
                        recipeName = data["recipeName"] as? String ?: "",
                        userEmail = data["userEmail"] as? String ?: "",
                        estimatedTime = data["estimatedTime"] as? String ?: "0",
                        servings = data["servings"] as? String ?: "1",
                        imageUrls = imageUrls?.mapNotNull { it as? String },
                        description = data["description"] as? String,
                        notes = data["notes"] as? String,
                        tips = data["tips"] as? String,
                        rating = (data["rating"] as? Number)?.toDouble() ?: 0.0,
                        reviewCount = (data["reviewCount"] as? Number)?.toInt() ?: 0,
                        createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.seconds?.times(1000)
                    )
                } catch (e: Exception) {
                    Log.e("DataPreloadManager", "Error parsing recipe: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DataPreloadManager", "Error loading recipes: ${e.message}")
            emptyList()
        }
    }
}

