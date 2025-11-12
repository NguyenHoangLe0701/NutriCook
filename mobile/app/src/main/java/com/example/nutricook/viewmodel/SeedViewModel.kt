package com.example.nutricook.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutricook.data.SampleData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SeedViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _status = mutableStateOf("Idle")
    val status: State<String> get() = _status

    suspend fun seedReviews() {
        _status.value = "Seeding reviews..."
        try {
            SampleData.reviews.forEach { r ->
                val data = mapOf(
                    "userName" to r.userName,
                    "date" to r.date,
                    "text" to r.text,
                    "rating" to r.rating,
                    "likes" to r.likes,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("reviews").add(data).await()
            }
            _status.value = "Reviews seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedCategories() {
        _status.value = "Seeding categories..."
        try {
            SampleData.categories.forEach { c ->
                val data = mapOf(
                    "category" to c.category,
                    "title" to c.title,
                    "userCount" to c.userCount,
                    "additionalUsers" to c.additionalUsers,
                    "imageRes" to c.imageRes,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("categories").add(data).await()
            }
            _status.value = "Categories seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedRecipes() {
        _status.value = "Seeding recipes..."
        try {
            SampleData.todayRecipes.forEach { r ->
                val data = mapOf(
                    "name" to r.name,
                    "description" to r.description,
                    "rating" to r.rating,
                    "reviews" to r.reviews,
                    "imageRes" to r.imageRes,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("recipes").add(data).await()
            }
            _status.value = "Recipes seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedVegetables() {
        _status.value = "Seeding vegetables..."
        try {
            SampleData.vegetables.forEach { v ->
                db.collection("foods").document("vegetables").collection("items").add(v).await()
            }
            _status.value = "Vegetables seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedFruits() {
        _status.value = "Seeding fruits..."
        try {
            SampleData.fruits.forEach { f ->
                db.collection("foods").document("fruits").collection("items").add(f).await()
            }
            _status.value = "Fruits seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedSeafood() {
        _status.value = "Seeding seafood..."
        try {
            SampleData.fishAndSeafood.forEach { s ->
                db.collection("foods").document("seafood").collection("items").add(s).await()
            }
            _status.value = "Seafood seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedAllFoods() {
        _status.value = "Seeding all foods..."
        try {
            seedVegetables()
            seedFruits()
            seedSeafood()
            _status.value = "All foods seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedIngredients() {
        _status.value = "Seeding ingredients..."
        try {
            SampleData.allIngredients.forEach { (letter, items) ->
                items.forEach { item ->
                    db.collection("ingredients").add(
                        mapOf(
                            "name" to item,
                            "letter" to letter,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            _status.value = "Ingredients seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedMealTypes() {
        _status.value = "Seeding meal types..."
        try {
            SampleData.mealTypes.forEach { mealType ->
                db.collection("meal_types").add(
                    mapOf(
                        "name" to mealType,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            _status.value = "Meal types seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedDietTypes() {
        _status.value = "Seeding diet types..."
        try {
            SampleData.dietTypes.forEach { dietType ->
                db.collection("diet_types").add(
                    mapOf(
                        "name" to dietType,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            _status.value = "Diet types seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedCookingTips() {
        _status.value = "Seeding cooking tips..."
        try {
            SampleData.cookingTips.forEach { tip ->
                db.collection("cooking_tips").add(
                    tip + ("createdAt" to FieldValue.serverTimestamp())
                ).await()
            }
            _status.value = "Cooking tips seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }

    suspend fun seedCalorieInfo() {
        _status.value = "Seeding calorie info..."
        try {
            SampleData.calorieInfo.forEach { info ->
                db.collection("calorie_info").add(info).await()
            }
            _status.value = "Calorie info seeded ✓"
        } catch (e: Exception) {
            _status.value = "Error: ${e.message}"
        }
    }
}
