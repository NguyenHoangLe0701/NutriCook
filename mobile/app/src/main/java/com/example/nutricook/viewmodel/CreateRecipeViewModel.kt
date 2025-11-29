package com.example.nutricook.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.view.recipes.CookingStep
import com.example.nutricook.view.recipes.IngredientItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.example.nutricook.utils.NutritionData

data class RecipeCreationState(
    // Step 1 data
    val recipeName: String = "",
    val estimatedTime: String = "",
    val servings: String = "",
    val selectedImageUris: List<Uri> = emptyList(),
    val ingredients: List<IngredientItem> = emptyList(),
    
    // Step 2 data
    val cookingSteps: List<CookingStep> = emptyList(),
    
    // Step 3 data
    val description: String = "",
    val notes: String = "",
    val tips: String = "",
    
    // Calculated nutrition data
    val nutritionData: NutritionData? = null
)

@HiltViewModel
class CreateRecipeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val gson = Gson()
    
    // Load initial state from SavedStateHandle
    private val _state = MutableStateFlow(
        RecipeCreationState(
            recipeName = savedStateHandle.get<String>("recipeName") ?: "",
            estimatedTime = savedStateHandle.get<String>("estimatedTime") ?: "",
            servings = savedStateHandle.get<String>("servings") ?: "",
            selectedImageUris = loadUriList("selectedImageUris"),
            ingredients = loadIngredients("ingredients"),
            cookingSteps = loadCookingSteps("cookingSteps"),
            description = savedStateHandle.get<String>("description") ?: "",
            notes = savedStateHandle.get<String>("notes") ?: "",
            tips = savedStateHandle.get<String>("tips") ?: ""
        )
    )
    val state: StateFlow<RecipeCreationState> = _state.asStateFlow()
    
    // Helper functions to load complex data
    private fun loadUriList(key: String): List<Uri> {
        val uriStrings = savedStateHandle.get<List<String>>(key) ?: return emptyList()
        return uriStrings.mapNotNull { Uri.parse(it) }
    }
    
    private fun loadIngredients(key: String): List<IngredientItem> {
        val json = savedStateHandle.get<String>(key) ?: return emptyList()
        return try {
            // Parse JSON manually vì IngredientItem chứa enum và có thể có vấn đề với Gson
            val type = object : TypeToken<List<IngredientItemJson>>() {}.type
            val jsonList: List<IngredientItemJson> = gson.fromJson(json, type) ?: return emptyList()
            jsonList.mapNotNull { jsonItem ->
                try {
                    IngredientItem(
                        name = jsonItem.name ?: "",
                        quantity = jsonItem.quantity ?: "",
                        unit = jsonItem.unit?.let { 
                            try {
                                com.example.nutricook.utils.IngredientUnit.valueOf(it)
                            } catch (e: Exception) {
                                com.example.nutricook.utils.IngredientUnit.GRAMS
                            }
                        } ?: com.example.nutricook.utils.IngredientUnit.GRAMS,
                        foodItemId = jsonItem.foodItemId,
                        categoryId = jsonItem.categoryId,
                        cookingMethod = jsonItem.cookingMethod?.let {
                            try {
                                com.example.nutricook.utils.CookingMethod.valueOf(it)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CreateRecipeVM", "Error loading ingredients: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun loadCookingSteps(key: String): List<CookingStep> {
        val json = savedStateHandle.get<String>(key) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CookingStepJson>>() {}.type
            val jsonList: List<CookingStepJson> = gson.fromJson(json, type) ?: return emptyList()
            jsonList.mapNotNull { jsonStep ->
                try {
                    CookingStep(
                        description = jsonStep.description ?: "",
                        imageUri = jsonStep.imageUri?.let { Uri.parse(it) }
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("CreateRecipeVM", "Error loading cooking steps: ${e.message}", e)
            emptyList()
        }
    }
    
    // Helper data classes for JSON serialization
    private data class IngredientItemJson(
        val name: String?,
        val quantity: String?,
        val unit: String?,
        val foodItemId: Long?,
        val categoryId: Long?,
        val cookingMethod: String?
    )
    
    private data class CookingStepJson(
        val description: String?,
        val imageUri: String?
    )
    
    // Step 1 setters
    fun setStep1Data(
        recipeName: String,
        estimatedTime: String,
        servings: String,
        selectedImageUris: List<Uri>,
        ingredients: List<IngredientItem>
    ) {
        // Save to SavedStateHandle
        savedStateHandle["recipeName"] = recipeName
        savedStateHandle["estimatedTime"] = estimatedTime
        savedStateHandle["servings"] = servings
        savedStateHandle["selectedImageUris"] = selectedImageUris.map { it.toString() }
        
        // Convert ingredients to JSON-safe format
        val ingredientsJson = ingredients.map { item ->
            IngredientItemJson(
                name = item.name,
                quantity = item.quantity,
                unit = item.unit.name,
                foodItemId = item.foodItemId,
                categoryId = item.categoryId,
                cookingMethod = item.cookingMethod?.name
            )
        }
        savedStateHandle["ingredients"] = gson.toJson(ingredientsJson)
        
        _state.update { current ->
            current.copy(
                recipeName = recipeName,
                estimatedTime = estimatedTime,
                servings = servings,
                selectedImageUris = selectedImageUris,
                ingredients = ingredients
            )
        }
    }
    
    // Step 2 setter
    fun setStep2Data(cookingSteps: List<CookingStep>) {
        // Convert cooking steps to JSON-safe format
        val stepsJson = cookingSteps.map { step ->
            CookingStepJson(
                description = step.description,
                imageUri = step.imageUri?.toString()
            )
        }
        savedStateHandle["cookingSteps"] = gson.toJson(stepsJson)
        _state.update { current ->
            current.copy(cookingSteps = cookingSteps)
        }
    }
    
    // Step 3 setters
    fun setStep3Data(description: String, notes: String, tips: String) {
        savedStateHandle["description"] = description
        savedStateHandle["notes"] = notes
        savedStateHandle["tips"] = tips
        _state.update { current ->
            current.copy(
                description = description,
                notes = notes,
                tips = tips
            )
        }
    }
    
    // Set calculated nutrition data
    fun setNutritionData(nutritionData: NutritionData) {
        _state.update { current ->
            current.copy(nutritionData = nutritionData)
        }
    }
    
    // Clear all data
    fun clearAll() {
        savedStateHandle.remove<String>("recipeName")
        savedStateHandle.remove<String>("estimatedTime")
        savedStateHandle.remove<String>("servings")
        savedStateHandle.remove<String>("selectedImageUris")
        savedStateHandle.remove<String>("ingredients")
        savedStateHandle.remove<String>("cookingSteps")
        savedStateHandle.remove<String>("description")
        savedStateHandle.remove<String>("notes")
        savedStateHandle.remove<String>("tips")
        _state.value = RecipeCreationState()
    }
}

