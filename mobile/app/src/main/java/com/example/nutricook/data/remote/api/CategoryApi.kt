package com.example.nutricook.data.remote.api

import com.example.nutricook.data.model.CategoryResponse
import com.example.nutricook.data.model.FoodItemResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryApi {
    @GET("api/categories")
    suspend fun getCategories(): List<CategoryResponse>

    @GET("api/foods")
    suspend fun getFoods(@Query("categoryId") categoryId: Long): List<FoodItemResponse>
}