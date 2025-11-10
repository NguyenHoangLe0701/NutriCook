package com.example.nutricook.data.repository

import com.example.nutricook.data.remote.api.RecipeApi
import javax.inject.Inject

class RecipeRepository @Inject constructor(private val api: RecipeApi) {
    suspend fun getSuggestions(query: String) = api.getSuggestions(query)
}