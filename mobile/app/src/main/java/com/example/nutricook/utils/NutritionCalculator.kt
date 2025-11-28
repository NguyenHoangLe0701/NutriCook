package com.example.nutricook.utils

import com.example.nutricook.view.recipes.IngredientItem
import com.example.nutricook.viewmodel.FoodItemUI

/**
 * Utility class để tính toán dinh dưỡng từ danh sách nguyên liệu
 */
object NutritionCalculator {
    
    /**
     * Tính toán tổng dinh dưỡng từ danh sách nguyên liệu
     * @param ingredients Danh sách nguyên liệu với tên và số lượng
     * @param foodItemsMap Map từ tên nguyên liệu đến FoodItemUI (từ database)
     * @param servings Số phần ăn (để chia đều dinh dưỡng)
     * @return NutritionData chứa tổng dinh dưỡng
     */
    fun calculateNutrition(
        ingredients: List<IngredientItem>,
        foodItemsMap: Map<String, FoodItemUI>,
        servings: Int = 1
    ): NutritionData {
        var totalCalories = 0.0
        var totalFat = 0.0
        var totalCarbs = 0.0
        var totalProtein = 0.0
        var totalCholesterol = 0.0
        var totalSodium = 0.0
        var totalVitamin = 0.0
        
        ingredients.forEach { ingredient ->
            if (ingredient.name.isNotBlank() && ingredient.quantity.isNotBlank()) {
                val foodItem = foodItemsMap[ingredient.name.lowercase().trim()]
                if (foodItem != null) {
                    // Parse số lượng (ví dụ: "200g" -> 200.0)
                    val quantity = parseQuantity(ingredient.quantity)
                    
                    // Tính dinh dưỡng dựa trên số lượng
                    // Giả sử giá trị trong FoodItemUI là trên 100g
                    val multiplier = quantity / 100.0
                    
                    // Parse calories từ string (ví dụ: "100 kcal" -> 100.0)
                    val caloriesValue = parseCalories(foodItem.calories)
                    
                    totalCalories += caloriesValue * multiplier
                    totalFat += foodItem.fat * multiplier
                    totalCarbs += foodItem.carbs * multiplier
                    totalProtein += foodItem.protein * multiplier
                    totalCholesterol += foodItem.cholesterol * multiplier
                    totalSodium += foodItem.sodium * multiplier
                    totalVitamin += foodItem.vitamin * multiplier
                }
            }
        }
        
        // Chia cho số phần ăn
        if (servings > 0) {
            totalCalories /= servings
            totalFat /= servings
            totalCarbs /= servings
            totalProtein /= servings
            totalCholesterol /= servings
            totalSodium /= servings
            totalVitamin /= servings
        }
        
        return NutritionData(
            calories = totalCalories,
            fat = totalFat,
            carbs = totalCarbs,
            protein = totalProtein,
            cholesterol = totalCholesterol,
            sodium = totalSodium,
            vitamin = totalVitamin
        )
    }
    
    /**
     * Parse số lượng từ string (ví dụ: "200g" -> 200.0, "1.5kg" -> 1500.0)
     */
    private fun parseQuantity(quantityStr: String): Double {
        val cleaned = quantityStr.lowercase().trim()
        val numberPart = cleaned.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
            .toDoubleOrNull() ?: 0.0
        
        return when {
            cleaned.contains("kg") -> numberPart * 1000.0
            cleaned.contains("g") -> numberPart
            cleaned.contains("ml") -> numberPart // Giả sử 1ml = 1g cho nước
            cleaned.contains("l") -> numberPart * 1000.0
            else -> numberPart // Mặc định là gram
        }
    }
    
    /**
     * Parse calories từ string (ví dụ: "100 kcal" -> 100.0)
     */
    private fun parseCalories(caloriesStr: String): Double {
        val cleaned = caloriesStr.lowercase().trim()
        val numberPart = cleaned.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
            .toDoubleOrNull() ?: 0.0
        return numberPart
    }
    
    /**
     * Tính % daily value
     */
    fun calculateDailyValue(value: Double, dailyValue: Double): Int {
        return if (dailyValue > 0) {
            ((value / dailyValue) * 100).toInt().coerceAtMost(100)
        } else {
            0
        }
    }
}

/**
 * Data class chứa thông tin dinh dưỡng
 */
data class NutritionData(
    val calories: Double = 0.0,
    val fat: Double = 0.0, // g
    val carbs: Double = 0.0, // g
    val protein: Double = 0.0, // g
    val cholesterol: Double = 0.0, // mg
    val sodium: Double = 0.0, // mg
    val vitamin: Double = 0.0 // % daily value
) {
    // Daily values theo FDA (cho người trưởng thành)
    companion object {
        const val DAILY_CALORIES = 2000.0
        const val DAILY_FAT = 65.0 // g
        const val DAILY_CARBS = 300.0 // g
        const val DAILY_PROTEIN = 50.0 // g
        const val DAILY_CHOLESTEROL = 300.0 // mg
        const val DAILY_SODIUM = 2300.0 // mg
        const val DAILY_VITAMIN = 100.0 // %
    }
    
    fun getCaloriesPercent(): Int {
        return NutritionCalculator.calculateDailyValue(calories, DAILY_CALORIES)
    }
    
    fun getFatPercent(): Int {
        return NutritionCalculator.calculateDailyValue(fat, DAILY_FAT)
    }
    
    fun getCarbsPercent(): Int {
        return NutritionCalculator.calculateDailyValue(carbs, DAILY_CARBS)
    }
    
    fun getProteinPercent(): Int {
        return NutritionCalculator.calculateDailyValue(protein, DAILY_PROTEIN)
    }
    
    fun getCholesterolPercent(): Int {
        return NutritionCalculator.calculateDailyValue(cholesterol, DAILY_CHOLESTEROL)
    }
    
    fun getSodiumPercent(): Int {
        return NutritionCalculator.calculateDailyValue(sodium, DAILY_SODIUM)
    }
    
    fun getVitaminPercent(): Int {
        return vitamin.toInt().coerceAtMost(100)
    }
}

