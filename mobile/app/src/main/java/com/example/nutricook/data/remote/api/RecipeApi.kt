package com.example.nutricook.data.remote.api

import com.example.nutricook.data.model.Recipe
import retrofit2.http.GET
import retrofit2.http.Query

interface RecipeApi {
    @GET("recipes/complexSearch")
    suspend fun getSuggestions(@Query("query") query: String): List<Recipe>
}


