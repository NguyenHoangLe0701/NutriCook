package com.example.nutricook.model.nutrition

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Bộ hàm tính dinh dưỡng thuần Kotlin.
 * Không phụ thuộc Firebase/Room/UI.
 *
 * Cách dùng nhanh:
 *  val bmr = NutritionCalc.bmrMifflin(sex = "male", weightKg = 70.0, heightCm = 175.0, age = 22)
 *  val tdee = NutritionCalc.tdee(bmr, NutritionCalc.activityFactor("moderate"))
 *  val target = NutritionCalc.buildMacroTargets(
 *      calories = NutritionCalc.caloriesTarget(tdee, goal = "lose", deltaKcalPerDay = -300),
 *      weightKg = 70.0,
 *      proteinGPerKg = 1.8,
 *      fatMode = "percent",   // "percent" | "perKg"
 *      fatPercent = 0.30,     // dùng khi percent
 *      fatGPerKg = 0.8        // dùng khi perKg
 *  )
 */
object NutritionCalc {

    // -------------------- BMR --------------------

    /** Mifflin-St Jeor */
    fun bmrMifflin(
        sex: String,         // "male" | "female"
        weightKg: Double,
        heightCm: Double,
        age: Int
    ): Double {
        return if (sex.equals("female", ignoreCase = true)) {
            10 * weightKg + 6.25 * heightCm - 5 * age - 161
        } else {
            10 * weightKg + 6.25 * heightCm - 5 * age + 5
        }
    }

    /** Harris-Benedict (revised) */
    fun bmrHarris(
        sex: String,         // "male" | "female"
        weightKg: Double,
        heightCm: Double,
        age: Int
    ): Double {
        return if (sex.equals("female", ignoreCase = true)) {
            655.1 + 9.563 * weightKg + 1.850 * heightCm - 4.676 * age
        } else {
            66.47 + 13.75 * weightKg + 5.003 * heightCm - 6.755 * age
        }
    }

    // -------------------- Activity & TDEE --------------------

    /** Hệ số hoạt động từ chuỗi: sedentary/light/moderate/active/very_active */
    fun activityFactor(level: String): Double = when (level.lowercase()) {
        "sedentary"   -> 1.2
        "light"       -> 1.375
        "moderate"    -> 1.55
        "active"      -> 1.725
        "very_active" -> 1.9
        else          -> 1.2
    }

    fun tdee(bmr: Double, activityFactor: Double): Int =
        (bmr * activityFactor).roundToInt()

    // -------------------- Calories Target --------------------

    /**
     * goal: "lose" | "maintain" | "gain"
     * deltaKcalPerDay: âm khi giảm, dương khi tăng (ví dụ -300 hoặc +250)
     */
    fun caloriesTarget(tdee: Int, goal: String, deltaKcalPerDay: Int): Int {
        val delta = when (goal.lowercase()) {
            "lose"     -> minOf(0, deltaKcalPerDay)   // chỉ nhận giá trị âm
            "gain"     -> maxOf(0, deltaKcalPerDay)   // chỉ nhận giá trị dương
            else       -> 0
        }
        // chặn đáy an toàn
        return max(1200, tdee + delta)
    }

    // -------------------- Fat helpers --------------------

    fun fatFromPercent(calories: Int, fatPercent: Double): Double =
        calories * fatPercent / 9.0

    fun fatFromPerKg(weightKg: Double, gPerKg: Double): Double =
        weightKg * gPerKg

    // -------------------- Macro Targets --------------------

    data class MacroTargets(
        val calories: Int,
        val proteinG: Double,
        val fatG: Double,
        val carbG: Double
    ) {
        /** Làm tròn đẹp để hiển thị nhưng giữ số thô khi lưu nếu muốn chính xác hơn. */
        fun rounded(): MacroTargets = copy(
            proteinG = (proteinG * 10).roundToInt() / 10.0,
            fatG     = (fatG * 10).roundToInt() / 10.0,
            carbG    = (carbG * 10).roundToInt() / 10.0
        )
    }

    /**
     * Xây macro từ Calories + prefs.
     * fatMode: "percent" | "perKg"
     */
    fun buildMacroTargets(
        calories: Int,
        weightKg: Double,
        proteinGPerKg: Double = 1.8,
        fatMode: String = "percent",
        fatPercent: Double = 0.30,
        fatGPerKg: Double = 0.8
    ): MacroTargets {
        val proteinG = weightKg * proteinGPerKg
        val fatG = when (fatMode.lowercase()) {
            "perkg", "per_kg" -> fatFromPerKg(weightKg, fatGPerKg)
            else               -> fatFromPercent(calories, fatPercent)
        }

        val proteinKcal = proteinG * 4.0
        val fatKcal     = fatG * 9.0
        val carbKcal    = (calories - proteinKcal - fatKcal).coerceAtLeast(0.0)
        val carbG       = carbKcal / 4.0

        return MacroTargets(
            calories = calories,
            proteinG = proteinG,
            fatG = fatG,
            carbG = carbG
        )
    }

    // -------------------- Tiện ích all-in-one --------------------

    /**
     * Tính trọn gói từ thông tin cơ bản.
     *
     * @param formula   "mifflin" | "harris"
     * @param sex       "male" | "female"
     * @param activity  "sedentary" | "light" | "moderate" | "active" | "very_active"
     * @param goal      "lose" | "maintain" | "gain"
     */
    fun computeAll(
        formula: String = "mifflin",
        sex: String,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activity: String,
        goal: String = "maintain",
        deltaKcalPerDay: Int = 0,
        proteinGPerKg: Double = 1.8,
        fatMode: String = "percent",
        fatPercent: Double = 0.30,
        fatGPerKg: Double = 0.8
    ): ResultBundle {
        val bmr = when (formula.lowercase()) {
            "harris" -> bmrHarris(sex, weightKg, heightCm, age)
            else     -> bmrMifflin(sex, weightKg, heightCm, age)
        }
        val tdee = tdee(bmr, activityFactor(activity))
        val kcal = caloriesTarget(tdee, goal, deltaKcalPerDay)
        val macros = buildMacroTargets(
            calories = kcal,
            weightKg = weightKg,
            proteinGPerKg = proteinGPerKg,
            fatMode = fatMode,
            fatPercent = fatPercent,
            fatGPerKg = fatGPerKg
        )
        return ResultBundle(
            bmr = bmr,
            tdee = tdee,
            caloriesTarget = kcal,
            targets = macros
        )
    }

    data class ResultBundle(
        val bmr: Double,
        val tdee: Int,
        val caloriesTarget: Int,
        val targets: MacroTargets
    )
}
