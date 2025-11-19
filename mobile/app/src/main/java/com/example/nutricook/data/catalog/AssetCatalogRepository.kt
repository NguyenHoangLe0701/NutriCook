package com.example.nutricook.data.catalog

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetCatalogRepository(
    private val context: Context,
    private val gson: Gson = Gson(),
    private val fileName: String = "foods.json"
) : CatalogRepository {

    @Volatile
    private var cache: CatalogDto? = null

    private fun load(): CatalogDto {
        cache?.let { return it }
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return gson.fromJson(json, CatalogDto::class.java).also { cache = it }
    }

    private fun resId(name: String): Int =
        context.resources.getIdentifier(name, "drawable", context.packageName)

    private fun parseColor(hex: String): Color =
        Color(android.graphics.Color.parseColor(hex))

    override suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        val raw = load()
        raw.categories.map {
            Category(
                id = it.id,
                name = it.name,
                iconRes = resId(it.icon),
                color = parseColor(it.tint)
            )
        }
    }

    override suspend fun getFoodsByCategory(categoryId: String): List<Food> =
        withContext(Dispatchers.IO) {
            val raw = load()
            raw.foods
                .filter { it.categoryId == categoryId }
                .map { it.toDomain() }
        }

    override suspend fun getFood(id: String): Food? = withContext(Dispatchers.IO) {
        val raw = load()
        raw.foods.firstOrNull { it.id == id }?.toDomain()
    }

    // ✅ HÀM MỚI: lấy toàn bộ foods cho NutritionPickerScreen
    suspend fun getAllFoods(): List<Food> = withContext(Dispatchers.IO) {
        val raw = load()
        raw.foods.map { it.toDomain() }
    }

    // map DTO -> domain
    private fun FoodDto.toDomain(): Food =
        Food(
            id = id,
            name = name,
            kcalPer100g = kcalPer100g,
            proteinPer100g = proteinPer100g,
            fatPer100g = fatPer100g,
            carbPer100g = carbPer100g,
            fiberPer100g = fiberPer100g,
            sugarPer100g = sugarPer100g,
            saturatedFatPer100g = saturatedFatPer100g,
            cholesterolMgPer100g = cholesterolMgPer100g,
            sodiumMgPer100g = sodiumMgPer100g,
            servingSizeGrams = servingSizeGrams,
            ediblePortion = ediblePortion ?: 1.0,
            cookedVariants = cookedVariants?.map {
                CookedVariant(
                    type = it.type,
                    lossFactor = it.lossFactor,
                    extraFatGPer100g = it.extraFatGPer100g,
                    note = it.note
                )
            } ?: emptyList(),
            imageRes = resId(image),
            categoryId = categoryId
        )
}
