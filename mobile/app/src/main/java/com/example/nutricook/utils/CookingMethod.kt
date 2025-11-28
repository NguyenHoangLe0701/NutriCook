package com.example.nutricook.utils

/**
 * Enum định nghĩa các phương pháp nấu ăn và tỷ lệ tiêu hao dinh dưỡng
 * Dựa trên nghiên cứu về ảnh hưởng của phương pháp nấu đến dinh dưỡng
 */
enum class CookingMethod(
    val displayName: String,
    val vitaminLossPercent: Double = 0.0, // % mất đi của vitamin
    val mineralLossPercent: Double = 0.0, // % mất đi của khoáng chất
    val proteinLossPercent: Double = 0.0, // % mất đi của protein
    val fatLossPercent: Double = 0.0, // % mất đi của chất béo
    val carbsLossPercent: Double = 0.0 // % mất đi của carbs
) {
    // Luộc: mất nhiều vitamin tan trong nước (B, C), khoáng chất
    BOILING(
        displayName = "Luộc",
        vitaminLossPercent = 40.0, // Mất nhiều vitamin tan trong nước
        mineralLossPercent = 30.0, // Khoáng chất hòa tan vào nước
        proteinLossPercent = 5.0, // Protein ít bị mất
        fatLossPercent = 0.0,
        carbsLossPercent = 5.0
    ),
    
    // Xào: mất ít dinh dưỡng hơn, nhưng có thể tăng chất béo
    STIR_FRYING(
        displayName = "Xào",
        vitaminLossPercent = 20.0,
        mineralLossPercent = 10.0,
        proteinLossPercent = 5.0,
        fatLossPercent = 0.0, // Có thể tăng do dầu mỡ
        carbsLossPercent = 5.0
    ),
    
    // Hấp: giữ được nhiều dinh dưỡng nhất
    STEAMING(
        displayName = "Hấp",
        vitaminLossPercent = 10.0,
        mineralLossPercent = 5.0,
        proteinLossPercent = 2.0,
        fatLossPercent = 0.0,
        carbsLossPercent = 2.0
    ),
    
    // Nướng: mất một số vitamin nhạy cảm với nhiệt
    BAKING(
        displayName = "Nướng",
        vitaminLossPercent = 25.0,
        mineralLossPercent = 15.0,
        proteinLossPercent = 8.0,
        fatLossPercent = 5.0,
        carbsLossPercent = 10.0
    ),
    
    // Chiên: mất nhiều vitamin, có thể tăng chất béo
    FRYING(
        displayName = "Chiên",
        vitaminLossPercent = 35.0,
        mineralLossPercent = 20.0,
        proteinLossPercent = 10.0,
        fatLossPercent = 0.0, // Có thể tăng do dầu mỡ
        carbsLossPercent = 15.0
    ),
    
    // Nướng than: mất nhiều dinh dưỡng do nhiệt độ cao
    GRILLING(
        displayName = "Nướng than",
        vitaminLossPercent = 30.0,
        mineralLossPercent = 20.0,
        proteinLossPercent = 12.0,
        fatLossPercent = 10.0,
        carbsLossPercent = 12.0
    ),
    
    // Không nấu (ăn sống): giữ nguyên dinh dưỡng
    RAW(
        displayName = "Ăn sống",
        vitaminLossPercent = 0.0,
        mineralLossPercent = 0.0,
        proteinLossPercent = 0.0,
        fatLossPercent = 0.0,
        carbsLossPercent = 0.0
    ),
    
    // Hầm: mất một số vitamin nhưng giữ được khoáng chất trong nước
    STEWING(
        displayName = "Hầm",
        vitaminLossPercent = 30.0,
        mineralLossPercent = 15.0, // Một phần khoáng chất ở trong nước
        proteinLossPercent = 5.0,
        fatLossPercent = 0.0,
        carbsLossPercent = 5.0
    );
    
    /**
     * Tính toán dinh dưỡng sau khi nấu
     * @param originalValue Giá trị dinh dưỡng ban đầu
     * @param nutrientType Loại dinh dưỡng (VITAMIN, MINERAL, PROTEIN, FAT, CARBS)
     * @return Giá trị dinh dưỡng sau khi nấu
     */
    fun calculateAfterCooking(originalValue: Double, nutrientType: NutrientType): Double {
        val lossPercent = when (nutrientType) {
            NutrientType.VITAMIN -> vitaminLossPercent
            NutrientType.MINERAL -> mineralLossPercent
            NutrientType.PROTEIN -> proteinLossPercent
            NutrientType.FAT -> fatLossPercent
            NutrientType.CARBS -> carbsLossPercent
        }
        return originalValue * (1 - lossPercent / 100.0)
    }
}

/**
 * Enum định nghĩa các loại dinh dưỡng
 */
enum class NutrientType {
    VITAMIN,
    MINERAL,
    PROTEIN,
    FAT,
    CARBS
}

