package com.example.nutricook.data.catalog

data class CategoryDto(
    val id: String,
    val name: String,
    val icon: String,
    val tint: String
)

data class CookedVariantDto(
    val type: String,
    val lossFactor: Double,
    val extraFatGPer100g: Double,
    val note: String? = null
)

data class FoodDto(
    val id: String,
    val name: String,
    val kcalPer100g: Double,
    val proteinPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val carbPer100g: Double? = null,
    val fiberPer100g: Double? = null,
    val sugarPer100g: Double? = null,
    val saturatedFatPer100g: Double? = null,
    val cholesterolMgPer100g: Int? = null,
    val sodiumMgPer100g: Int? = null,
    val servingSizeGrams: Int? = null,
    val ediblePortion: Double? = 1.0,
    val cookedVariants: List<CookedVariantDto>? = null,
    val image: String,
    val categoryId: String
)

data class CatalogDto(
    val categories: List<CategoryDto>,
    val foods: List<FoodDto>
)
