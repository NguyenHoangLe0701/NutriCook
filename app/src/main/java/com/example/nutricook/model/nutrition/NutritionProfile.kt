package com.example.nutricook.model.nutrition

data class NutritionProfile(
    val metrics: BodyMetrics = BodyMetrics(),
    val goal: Goal = Goal.MAINTAIN,
    // âm: thâm hụt, dương: dư (kcal/ngày)
    val calorieDeltaPerDay: Int = 0,
    val prefs: MacroPrefs = MacroPrefs(),
    val targets: NutritionTargets = NutritionTargets()
)

// --- Helpers: cập nhật input & tính lại targets ---
fun NutritionProfile.withInputs(
    metrics: BodyMetrics? = null,
    prefs: MacroPrefs? = null,
    goal: Goal? = null,
    deltaKcalPerDay: Int? = null
): NutritionProfile = copy(
    metrics = metrics ?: this.metrics,
    prefs = prefs ?: this.prefs,
    goal = goal ?: this.goal,
    calorieDeltaPerDay = deltaKcalPerDay ?: this.calorieDeltaPerDay
)

fun NutritionProfile.recalculate(): NutritionProfile {
    val bmr = when (metrics.bmrFormula) {
        BmrFormula.MIFFLIN -> NutritionCalc.bmrMifflin(
            sex = metrics.sex.name.lowercase(),
            weightKg = metrics.weightKg, heightCm = metrics.heightCm, age = metrics.age
        )
        BmrFormula.HARRIS -> NutritionCalc.bmrHarris(
            sex = metrics.sex.name.lowercase(),
            weightKg = metrics.weightKg, heightCm = metrics.heightCm, age = metrics.age
        )
    }

    val tdee = NutritionCalc.tdee(bmr, metrics.activity.factor)
    val kcal = NutritionCalc.caloriesTarget(
        tdee = tdee, goal = goal.name.lowercase(), deltaKcalPerDay = calorieDeltaPerDay
    )

    val m = NutritionCalc.buildMacroTargets(
        calories = kcal,
        weightKg = metrics.weightKg,
        proteinGPerKg = prefs.proteinGPerKg,
        fatMode = when (prefs.fatMode) { FatMode.PER_KG -> "perKg" else -> "percent" },
        fatPercent = prefs.fatPercent,
        fatGPerKg = prefs.fatGPerKg
    ).rounded()

    return copy(targets = NutritionTargets(
        caloriesTarget = m.calories, proteinG = m.proteinG, fatG = m.fatG, carbG = m.carbG
    ))
}
