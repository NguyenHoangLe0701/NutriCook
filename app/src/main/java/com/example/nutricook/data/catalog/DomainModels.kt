package com.example.nutricook.data.catalog

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class CookedVariant(
    val type: String,
    // mặc định: không thất thoát khối lượng, không hút thêm dầu
    val lossFactor: Double = 1.0,          // 0.0–1.0 (1.0 = giữ nguyên)
    val extraFatGPer100g: Double = 0.0,    // gram mỡ hút thêm / 100g
    val note: String? = null
)

data class Category(
    val id: String,
    val name: String,
    @DrawableRes val iconRes: Int,
    val color: Color
)

data class Food(
    val id: String,
    val name: String,
    val kcalPer100g: Double,

    // macro & vi chất (nullable cho an toàn)
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
    val cookedVariants: List<CookedVariant> = emptyList(),

    @DrawableRes val imageRes: Int,
    val categoryId: String
)
