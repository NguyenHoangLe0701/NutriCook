package com.example.nutricook.data.search

import com.example.nutricook.data.hotnews.HotNewsRepository
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.hotnews.HotNewsArticle
import com.example.nutricook.model.profile.Profile
import com.example.nutricook.model.search.SearchResult
import com.example.nutricook.model.search.SearchType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val profileRepository: ProfileRepository,
    private val hotNewsRepository: HotNewsRepository
) {
    
    /**
     * Tìm kiếm tất cả các loại nội dung
     */
    suspend fun searchAll(
        query: String,
        types: Set<SearchType> = SearchType.values().toSet()
    ): Map<SearchType, List<SearchResult>> = coroutineScope {
        val queryLower = query.lowercase().trim()
        if (queryLower.isBlank()) {
            return@coroutineScope emptyMap()
        }
        
        val results = mutableMapOf<SearchType, List<SearchResult>>()
        
        // Parallel search
        val recipesDeferred = if (types.contains(SearchType.RECIPES)) {
            async { searchRecipes(queryLower) }
        } else null
        
        val foodsDeferred = if (types.contains(SearchType.FOODS)) {
            async { searchFoods(queryLower) }
        } else null
        
        val newsDeferred = if (types.contains(SearchType.NEWS)) {
            async { searchNews(queryLower) }
        } else null
        
        val usersDeferred = if (types.contains(SearchType.USERS)) {
            async { searchUsers(queryLower) }
        } else null
        
        // Await all results
        recipesDeferred?.await()?.let { results[SearchType.RECIPES] = it }
        foodsDeferred?.await()?.let { results[SearchType.FOODS] = it }
        newsDeferred?.await()?.let { results[SearchType.NEWS] = it }
        usersDeferred?.await()?.let { results[SearchType.USERS] = it }
        
        results
    }
    
    /**
     * Tìm kiếm recipes từ Firestore
     */
    private suspend fun searchRecipes(query: String): List<SearchResult.RecipeResult> {
        return try {
            val snapshot = firestore.collection("recipes")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .limit(10)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val name = data["name"] as? String ?: return@mapNotNull null
                
                // Also check if query is in name (case-insensitive)
                if (!name.lowercase().contains(query)) return@mapNotNull null
                
                val imageUrl = data["imageUrl"] as? String
                val calories = (data["calories"] as? Number)?.toInt()
                
                SearchResult.RecipeResult(
                    id = doc.id,
                    title = name,
                    imageUrl = imageUrl,
                    calories = calories,
                    source = "local"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("SearchRepository", "Error searching recipes: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Tìm kiếm foods từ Firestore (foodItems collection)
     * Cải thiện: Lấy tất cả và filter trong memory để hỗ trợ tìm kiếm tốt hơn
     */
    private suspend fun searchFoods(query: String): List<SearchResult.FoodResult> {
        return try {
            val queryLower = query.lowercase().trim()
            val queryWords = queryLower.split(" ").filter { it.isNotBlank() }
            
            // Lấy tất cả foodItems từ collection "foodItems" (giống như CategoriesViewModel)
            val allFoodsSnapshot = firestore.collection("foodItems")
                .get()
                .await()
            
            val allFoods = allFoodsSnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val name = data["name"] as? String ?: return@mapNotNull null
                val nameLower = name.lowercase()
                
                // Kiểm tra nếu tên chứa query hoặc tất cả các từ trong query
                val matches = nameLower.contains(queryLower) || 
                    queryWords.all { word -> nameLower.contains(word) }
                
                if (!matches) return@mapNotNull null
                
                // Parse calories - có thể là String hoặc Number
                val caloriesValue = when (val cal = data["calories"]) {
                    is Number -> cal.toFloat()
                    is String -> cal.replace(" kcal", "").replace(" ", "").toFloatOrNull() ?: 0f
                    else -> 0f
                }
                
                val protein = (data["protein"] as? Number)?.toDouble()?.toFloat() ?: 0f
                val fat = (data["fat"] as? Number)?.toDouble()?.toFloat() ?: 0f
                val carbs = (data["carbs"] as? Number)?.toDouble()?.toFloat() ?: 
                           (data["carb"] as? Number)?.toDouble()?.toFloat() ?: 0f
                
                SearchResult.FoodResult(
                    id = doc.id,
                    title = name,
                    imageUrl = data["imageUrl"] as? String,
                    calories = caloriesValue,
                    protein = protein,
                    fat = fat,
                    carb = carbs
                )
            }
            
            // Sort by relevance (exact match first, then starts with, then contains)
            val sorted = allFoods.sortedWith(compareBy<SearchResult.FoodResult> { result ->
                val nameLower = result.title.lowercase()
                when {
                    nameLower == queryLower -> 0 // Exact match
                    nameLower.startsWith(queryLower) -> 1 // Starts with
                    else -> 2 // Contains
                }
            })
            
            sorted.take(10) // Limit total results
        } catch (e: Exception) {
            android.util.Log.e("SearchRepository", "Error searching foods: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Tìm kiếm hot news
     */
    private suspend fun searchNews(query: String): List<SearchResult.NewsResult> {
        return try {
            val allNews = hotNewsRepository.getAllHotNews()
            allNews.filter { article ->
                article.title.contains(query, ignoreCase = true) ||
                article.content.contains(query, ignoreCase = true) ||
                article.category.contains(query, ignoreCase = true)
            }.take(10).map { article ->
                SearchResult.NewsResult(
                    id = article.id,
                    title = article.title,
                    imageUrl = article.thumbnailUrl,
                    category = article.category,
                    content = article.content
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("SearchRepository", "Error searching news: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Tìm kiếm users
     */
    private suspend fun searchUsers(query: String): List<SearchResult.UserResult> {
        return try {
            val profiles = profileRepository.searchProfiles(query)
            profiles.take(10).map { profile ->
                val user = profile.user
                SearchResult.UserResult(
                    id = user.id,
                    title = user.displayName ?: user.email ?: "Unknown",
                    imageUrl = user.avatarUrl,
                    email = user.email ?: "",
                    displayName = user.displayName ?: ""
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("SearchRepository", "Error searching users: ${e.message}")
            emptyList()
        }
    }
}

