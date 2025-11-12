package com.example.nutricook.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.nutricook.data.firebase.firestore.FirebaseQueryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QueryViewModel @Inject constructor(
    private val queryRepo: FirebaseQueryRepository
) : ViewModel() {

    private val _reviews = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val reviews: State<List<Map<String, Any>>> get() = _reviews

    private val _categories = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val categories: State<List<Map<String, Any>>> get() = _categories

    private val _recipes = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val recipes: State<List<Map<String, Any>>> get() = _recipes

    private val _vegetables = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val vegetables: State<List<Map<String, Any>>> get() = _vegetables

    private val _fruits = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val fruits: State<List<Map<String, Any>>> get() = _fruits

    private val _seafood = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val seafood: State<List<Map<String, Any>>> get() = _seafood

    private val _ingredients = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val ingredients: State<List<Map<String, Any>>> get() = _ingredients

    private val _mealTypes = mutableStateOf<List<String>>(emptyList())
    val mealTypes: State<List<String>> get() = _mealTypes

    private val _dietTypes = mutableStateOf<List<String>>(emptyList())
    val dietTypes: State<List<String>> get() = _dietTypes

    private val _cookingTips = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val cookingTips: State<List<Map<String, Any>>> get() = _cookingTips

    private val _calorieInfo = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val calorieInfo: State<List<Map<String, Any>>> get() = _calorieInfo

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> get() = _isLoading

    private val _error = mutableStateOf("")
    val error: State<String> get() = _error

    // Query individual collections
    suspend fun loadReviews() {
        _isLoading.value = true
        queryRepo.getReviews().onSuccess { _reviews.value = it }
            .onFailure { _error.value = it.message ?: "Error loading reviews" }
        _isLoading.value = false
    }

    suspend fun loadCategories() {
        _isLoading.value = true
        queryRepo.getCategories().onSuccess { _categories.value = it }
            .onFailure { _error.value = it.message ?: "Error loading categories" }
        _isLoading.value = false
    }

    suspend fun loadRecipes() {
        _isLoading.value = true
        queryRepo.getRecipes().onSuccess { _recipes.value = it }
            .onFailure { _error.value = it.message ?: "Error loading recipes" }
        _isLoading.value = false
    }

    suspend fun loadVegetables() {
        _isLoading.value = true
        queryRepo.getVegetables().onSuccess { _vegetables.value = it }
            .onFailure { _error.value = it.message ?: "Error loading vegetables" }
        _isLoading.value = false
    }

    suspend fun loadFruits() {
        _isLoading.value = true
        queryRepo.getFruits().onSuccess { _fruits.value = it }
            .onFailure { _error.value = it.message ?: "Error loading fruits" }
        _isLoading.value = false
    }

    suspend fun loadSeafood() {
        _isLoading.value = true
        queryRepo.getSeafood().onSuccess { _seafood.value = it }
            .onFailure { _error.value = it.message ?: "Error loading seafood" }
        _isLoading.value = false
    }

    suspend fun loadIngredients() {
        _isLoading.value = true
        queryRepo.getIngredients().onSuccess { _ingredients.value = it }
            .onFailure { _error.value = it.message ?: "Error loading ingredients" }
        _isLoading.value = false
    }

    suspend fun loadMealTypes() {
        _isLoading.value = true
        queryRepo.getMealTypes().onSuccess { _mealTypes.value = it }
            .onFailure { _error.value = it.message ?: "Error loading meal types" }
        _isLoading.value = false
    }

    suspend fun loadDietTypes() {
        _isLoading.value = true
        queryRepo.getDietTypes().onSuccess { _dietTypes.value = it }
            .onFailure { _error.value = it.message ?: "Error loading diet types" }
        _isLoading.value = false
    }

    suspend fun loadCookingTips() {
        _isLoading.value = true
        queryRepo.getCookingTips().onSuccess { _cookingTips.value = it }
            .onFailure { _error.value = it.message ?: "Error loading cooking tips" }
        _isLoading.value = false
    }

    suspend fun loadCalorieInfo() {
        _isLoading.value = true
        queryRepo.getCalorieInfo().onSuccess { _calorieInfo.value = it }
            .onFailure { _error.value = it.message ?: "Error loading calorie info" }
        _isLoading.value = false
    }

    // Load all data at once
    suspend fun loadAllData() {
        _isLoading.value = true
        try {
            loadReviews()
            loadCategories()
            loadRecipes()
            loadVegetables()
            loadFruits()
            loadSeafood()
            loadIngredients()
            loadMealTypes()
            loadDietTypes()
            loadCookingTips()
            loadCalorieInfo()
            _error.value = ""
        } catch (e: Exception) {
            _error.value = e.message ?: "Error loading all data"
        }
        _isLoading.value = false
    }
}
