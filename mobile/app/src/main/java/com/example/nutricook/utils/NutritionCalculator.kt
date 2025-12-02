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
     * @return NutritionData chứa tổng dinh dưỡng
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
        
        // Chi tiết vitamin
        var totalVitaminA = 0.0
        var totalVitaminB1 = 0.0
        var totalVitaminB2 = 0.0
        var totalVitaminB3 = 0.0
        var totalVitaminB6 = 0.0
        var totalVitaminB9 = 0.0
        var totalVitaminB12 = 0.0
        var totalVitaminC = 0.0
        var totalVitaminD = 0.0
        var totalVitaminE = 0.0
        var totalVitaminK = 0.0
        
        ingredients.forEach { ingredient ->
            if (ingredient.name.isNotBlank() && ingredient.quantity.isNotBlank() && ingredient.foodItemId != null) {
                val foodItem = foodItemsMap[ingredient.foodItemId]
                if (foodItem != null) {
                    // Parse số lượng từ string (hỗ trợ phân số như "1/2", "1.5", "2")
                    val quantityInUnits = parseQuantity(ingredient.quantity)
                    
                    // Chuyển đổi từ đơn vị của nguyên liệu sang gram
                    // Ví dụ: 2 quả trứng = 2 * 100g = 200g, 500ml nước = 500g
                    val quantityInGrams = ingredient.unit.toGrams(quantityInUnits)
                    
                    // Tính dinh dưỡng dựa trên số lượng (giá trị trong FoodItemUI là trên 100g)
                    val multiplier = quantityInGrams / 100.0
                    
                    // Parse calories từ string (ví dụ: "100 kcal" -> 100.0)
                    val caloriesValue = parseCalories(foodItem.calories)
                    
                    // Tính dinh dưỡng trực tiếp từ nguyên liệu (không áp dụng phương pháp nấu)
                    val calories = caloriesValue * multiplier
                    val fat = foodItem.fat * multiplier
                    val carbs = foodItem.carbs * multiplier
                    val protein = foodItem.protein * multiplier
                    val cholesterol = foodItem.cholesterol * multiplier
                    val sodium = foodItem.sodium * multiplier
                    val vitamin = foodItem.vitamin * multiplier
                    
                    // Tính chi tiết vitamin
                    val vitaminA = foodItem.vitaminA * multiplier
                    val vitaminB1 = foodItem.vitaminB1 * multiplier
                    val vitaminB2 = foodItem.vitaminB2 * multiplier
                    val vitaminB3 = foodItem.vitaminB3 * multiplier
                    val vitaminB6 = foodItem.vitaminB6 * multiplier
                    val vitaminB9 = foodItem.vitaminB9 * multiplier
                    val vitaminB12 = foodItem.vitaminB12 * multiplier
                    val vitaminC = foodItem.vitaminC * multiplier
                    val vitaminD = foodItem.vitaminD * multiplier
                    val vitaminE = foodItem.vitaminE * multiplier
                    val vitaminK = foodItem.vitaminK * multiplier
                    
                    totalCalories += calories
                    totalFat += fat
                    totalCarbs += carbs
                    totalProtein += protein
                    totalCholesterol += cholesterol
                    totalSodium += sodium
                    totalVitamin += vitamin
                    
                    // Tổng hợp chi tiết vitamin
                    totalVitaminA += vitaminA
                    totalVitaminB1 += vitaminB1
                    totalVitaminB2 += vitaminB2
                    totalVitaminB3 += vitaminB3
                    totalVitaminB6 += vitaminB6
                    totalVitaminB9 += vitaminB9
                    totalVitaminB12 += vitaminB12
                    totalVitaminC += vitaminC
                    totalVitaminD += vitaminD
                    totalVitaminE += vitaminE
                    totalVitaminK += vitaminK
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
        
        // Tính tổng vitamin từ chi tiết
        val vitaminDetails = VitaminDetails(
            vitaminA = totalVitaminA,
            vitaminB1 = totalVitaminB1,
            vitaminB2 = totalVitaminB2,
            vitaminB3 = totalVitaminB3,
            vitaminB6 = totalVitaminB6,
            vitaminB9 = totalVitaminB9,
            vitaminB12 = totalVitaminB12,
            vitaminC = totalVitaminC,
            vitaminD = totalVitaminD,
            vitaminE = totalVitaminE,
            vitaminK = totalVitaminK
        )
        
        // Sử dụng tổng vitamin từ chi tiết nếu có, nếu không thì dùng totalVitamin
        val finalVitamin = if (vitaminDetails.getTotalPercent() > 0) {
            vitaminDetails.getTotalPercent()
        } else {
            totalVitamin
        }
        
        return NutritionData(
            calories = totalCalories,
            fat = totalFat,
            carbs = totalCarbs,
            protein = totalProtein,
            cholesterol = totalCholesterol,
            sodium = totalSodium,
            vitamin = finalVitamin,
            vitaminDetails = vitaminDetails
        )
    }
    
    /**
     * Parse số lượng từ string (hỗ trợ phân số và số thập phân)
     * Ví dụ: "2" -> 2.0, "1.5" -> 1.5, "1/2" -> 0.5, "1 1/2" -> 1.5, "200" -> 200.0
     * Đơn vị được xử lý riêng thông qua IngredientUnit
     */
    private fun parseQuantity(quantityStr: String): Double {
        val cleaned = quantityStr.trim()
        
        // Xử lý phân số: "1/2", "3/4", "1 1/2"
        val fractionRegex = Regex("""(\d+)?\s*(\d+)/(\d+)""")
        val fractionMatch = fractionRegex.find(cleaned)
        if (fractionMatch != null) {
            val wholePart = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val numerator = fractionMatch.groupValues[2].toDoubleOrNull() ?: 0.0
            val denominator = fractionMatch.groupValues[3].toDoubleOrNull() ?: 1.0
            if (denominator > 0) {
                return wholePart + (numerator / denominator)
            }
        }
        
        // Xử lý số thập phân: "1.5", "2,5", "200"
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
/**
 * Chi tiết các loại vitamin (% daily value)
 */
data class VitaminDetails(
    val vitaminA: Double = 0.0,
    val vitaminB1: Double = 0.0, // Thiamin
    val vitaminB2: Double = 0.0, // Riboflavin
    val vitaminB3: Double = 0.0, // Niacin
    val vitaminB6: Double = 0.0,
    val vitaminB9: Double = 0.0, // Folate
    val vitaminB12: Double = 0.0,
    val vitaminC: Double = 0.0,
    val vitaminD: Double = 0.0,
    val vitaminE: Double = 0.0,
    val vitaminK: Double = 0.0
) {
    /**
     * Tính tổng % vitamin (trung bình của các loại vitamin)
     */
    fun getTotalPercent(): Double {
        val values = listOf(
            vitaminA, vitaminB1, vitaminB2, vitaminB3, vitaminB6,
            vitaminB9, vitaminB12, vitaminC, vitaminD, vitaminE, vitaminK
        )
        val nonZeroValues = values.filter { it > 0 }
        return if (nonZeroValues.isNotEmpty()) {
            nonZeroValues.average()
        } else {
            0.0
        }
    }
}

data class NutritionData(
    val calories: Double = 0.0,
    val fat: Double = 0.0, // g
    val carbs: Double = 0.0, // g
    val protein: Double = 0.0, // g
    val cholesterol: Double = 0.0, // mg
    val sodium: Double = 0.0, // mg
    val vitamin: Double = 0.0, // % daily value (tổng trung bình)
    val vitaminDetails: VitaminDetails = VitaminDetails() // Chi tiết các loại vitamin
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

