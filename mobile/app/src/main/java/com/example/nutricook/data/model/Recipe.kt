package com.example.nutricook.data.model

data class Recipe(val id: Int, val name: String, val ingredients: List<String>, val instructions: String, val nutrition: NutritionInfo)
data class NutritionInfo(val calories: Int, val protein: Int)