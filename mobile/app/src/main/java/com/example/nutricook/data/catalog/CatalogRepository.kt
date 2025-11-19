package com.example.nutricook.data.catalog

interface CatalogRepository {
    suspend fun getCategories(): List<Category>
    suspend fun getFoodsByCategory(categoryId: String): List<Food>
    suspend fun getFood(id: String): Food?
}
