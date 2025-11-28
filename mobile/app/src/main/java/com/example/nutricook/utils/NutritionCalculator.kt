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
     * @param foodItemsMap Map từ foodItemId đến FoodItemUI (từ database)
     * @param servings Số phần ăn (để chia đều dinh dưỡng)
     * @return NutritionData chứa tổng dinh dưỡng (sau khi áp dụng độ tiêu hao)
     */
    fun calculateNutrition(
        ingredients: List<IngredientItem>,
        foodItemsMap: Map<Long, FoodItemUI>,
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
            if (ingredient.name.isNotBlank() && ingredient.quantity.isNotBlank() && ingredient.foodItemId != null) {
                val foodItem = foodItemsMap[ingredient.foodItemId]
                if (foodItem != null) {
                    // Parse số lượng từ string
                    val quantityInUnits = parseQuantity(ingredient.quantity)
                    
                    // Chuyển đổi từ đơn vị của nguyên liệu sang gram
                    // Ví dụ: 2 quả trứng = 2 * 100g = 200g, 500ml nước = 500g
                    val quantityInGrams = ingredient.unit.toGrams(quantityInUnits)
                    
                    // Tính dinh dưỡng dựa trên số lượng (giá trị trong FoodItemUI là trên 100g)
                    val multiplier = quantityInGrams / 100.0
                    
                    // Parse calories từ string (ví dụ: "100 kcal" -> 100.0)
                    val caloriesValue = parseCalories(foodItem.calories)
                    
                    // Tính dinh dưỡng ban đầu
                    var calories = caloriesValue * multiplier
                    var fat = foodItem.fat * multiplier
                    var carbs = foodItem.carbs * multiplier
                    var protein = foodItem.protein * multiplier
                    var cholesterol = foodItem.cholesterol * multiplier
                    var sodium = foodItem.sodium * multiplier
                    var vitamin = foodItem.vitamin * multiplier
                    
                    // Áp dụng độ tiêu hao dinh dưỡng dựa trên phương pháp nấu
                    val cookingMethod = ingredient.cookingMethod
                    if (cookingMethod != null) {
                        // Tính toán dinh dưỡng sau khi nấu
                        // Vitamin và khoáng chất bị ảnh hưởng nhiều nhất
                        vitamin = cookingMethod.calculateAfterCooking(vitamin, NutrientType.VITAMIN)
                        
                        // Protein, carbs, fat cũng bị ảnh hưởng
                        protein = cookingMethod.calculateAfterCooking(protein, NutrientType.PROTEIN)
                        carbs = cookingMethod.calculateAfterCooking(carbs, NutrientType.CARBS)
                        fat = cookingMethod.calculateAfterCooking(fat, NutrientType.FAT)
                        
                        // Sodium và cholesterol ít bị ảnh hưởng, nhưng có thể mất một phần
                        sodium = cookingMethod.calculateAfterCooking(sodium, NutrientType.MINERAL)
                        cholesterol = cookingMethod.calculateAfterCooking(cholesterol, NutrientType.FAT)
                        
                        // Calories được tính lại dựa trên các thành phần còn lại
                        // 1g protein = 4 kcal, 1g carbs = 4 kcal, 1g fat = 9 kcal
                        calories = (protein * 4.0) + (carbs * 4.0) + (fat * 9.0)
                    }
                    
                    totalCalories += calories
                    totalFat += fat
                    totalCarbs += carbs
                    totalProtein += protein
                    totalCholesterol += cholesterol
                    totalSodium += sodium
                    totalVitamin += vitamin
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
     * Parse số lượng từ string (chỉ lấy số, không xử lý đơn vị)
     * Ví dụ: "2" -> 2.0, "1.5" -> 1.5, "200" -> 200.0
     * Đơn vị được xử lý riêng thông qua IngredientUnit
     */
    private fun parseQuantity(quantityStr: String): Double {
        val cleaned = quantityStr.trim()
        val numberPart = cleaned.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
            .toDoubleOrNull() ?: 0.0
        
        return numberPart
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

