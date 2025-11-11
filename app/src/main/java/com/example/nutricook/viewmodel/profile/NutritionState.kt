package com.example.nutricook.viewmodel.profile

import com.example.nutricook.model.nutrition.*

data class NutritionUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val profile: NutritionProfile = NutritionProfile(),
    val targets: NutritionTargets = NutritionTargets(),
    val macroPct: MacroPercent = MacroPercent(0, 0, 0),
    val dirty: Boolean = false // user vừa chỉnh input nhưng chưa lưu (nếu bạn có nút Save)
)

data class MacroPercent(val proteinPct: Int, val fatPct: Int, val carbPct: Int)

fun NutritionTargets.percentages(): MacroPercent {
    val total = caloriesTarget.coerceAtLeast(1)
    val p = (proteinG * 4.0 / total * 100).toInt()
    val f = (fatG * 9.0 / total * 100).toInt()
    val c = (100 - p - f).coerceAtLeast(0)
    return MacroPercent(p, f, c)
}