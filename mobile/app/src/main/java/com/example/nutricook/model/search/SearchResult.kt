package com.example.nutricook.model.search

sealed class SearchResult {
    abstract val id: String
    abstract val title: String
    abstract val imageUrl: String?
    
    data class RecipeResult(
        override val id: String,
        override val title: String,
        override val imageUrl: String?,
        val calories: Int?,
        val source: String = "local" // "local" hoặc "spoonacular"
    ) : SearchResult()
    
    data class FoodResult(
        override val id: String,
        override val title: String,
        override val imageUrl: String?,
        val calories: Float,
        val protein: Float,
        val fat: Float,
        val carb: Float
    ) : SearchResult()
    
    data class NewsResult(
        override val id: String,
        override val title: String,
        override val imageUrl: String?,
        val category: String,
        val content: String? = null
    ) : SearchResult()
}

enum class SearchType {
    RECIPES,
    FOODS,
    NEWS
}

enum class SortOption {
    RELEVANCE,
    NEWEST,
    POPULAR,
    CALORIES_LOW_TO_HIGH,
    CALORIES_HIGH_TO_LOW
}

