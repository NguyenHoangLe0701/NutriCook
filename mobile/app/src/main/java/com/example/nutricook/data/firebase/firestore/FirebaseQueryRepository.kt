package com.example.nutricook.data.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseQueryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getReviews(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("reviews").get().await()
            val reviews = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("categories").get().await()
            val categories = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecipes(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("recipes").get().await()
            val recipes = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(recipes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVegetables(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("foods").document("vegetables")
                .collection("items").get().await()
            val vegetables = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(vegetables)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFruits(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("foods").document("fruits")
                .collection("items").get().await()
            val fruits = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(fruits)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSeafood(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("foods").document("seafood")
                .collection("items").get().await()
            val seafood = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(seafood)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIngredients(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("ingredients").get().await()
            val ingredients = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMealTypes(): Result<List<String>> {
        return try {
            val snapshot = firestore.collection("meal_types").get().await()
            val mealTypes = snapshot.documents.mapNotNull { 
                it.getString("name")
            }
            Result.success(mealTypes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDietTypes(): Result<List<String>> {
        return try {
            val snapshot = firestore.collection("diet_types").get().await()
            val dietTypes = snapshot.documents.mapNotNull { 
                it.getString("name")
            }
            Result.success(dietTypes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCookingTips(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("cooking_tips").get().await()
            val tips = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(tips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCalorieInfo(): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = firestore.collection("calorie_info").get().await()
            val info = snapshot.documents.map { it.data ?: emptyMap() }
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
