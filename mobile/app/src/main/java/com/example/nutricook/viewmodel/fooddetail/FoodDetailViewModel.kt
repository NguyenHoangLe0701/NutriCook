package com.example.nutricook.viewmodel.fooddetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.catalog.AssetCatalogRepository
import com.example.nutricook.data.catalog.CatalogRepository
import com.example.nutricook.data.catalog.CookedVariant
import com.example.nutricook.data.catalog.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class FoodDetailState(
    val loading: Boolean = true,
    val error: String? = null,
    val food: Food? = null,
    val grams: Int = 100,               // khối lượng người dùng chọn (g)
    val method: CookedVariant? = null,  // cách chế biến đang chọn (có thể null)

    // KẾT QUẢ đã tính cho "grams" hiện tại
    val kcalTotal: Int = 0,
    val proteinG: Double? = null,
    val fatG: Double? = null,
    val carbG: Double? = null
)

class FoodDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: CatalogRepository = AssetCatalogRepository(app)

    private val _state = MutableStateFlow(FoodDetailState())
    val state: StateFlow<FoodDetailState> = _state

    fun load(foodId: String, defaultGrams: Int = 100) = viewModelScope.launch {
        try {
            _state.value = _state.value.copy(loading = true, error = null)
            val f = repo.getFood(foodId)
            if (f == null) {
                _state.value = FoodDetailState(loading = false, error = "Không tìm thấy món")
                return@launch
            }
            _state.value = _state.value.copy(loading = false, food = f, grams = defaultGrams)
            recalc()
        } catch (e: Exception) {
            _state.value = FoodDetailState(loading = false, error = e.message ?: "Lỗi tải dữ liệu")
        }
    }

    fun setMethod(m: CookedVariant?) {
        _state.value = _state.value.copy(method = m)
        recalc()
    }

    fun setGrams(g: Int) {
        _state.value = _state.value.copy(grams = g.coerceIn(0, 5000))
        recalc()
    }

    // -------------------- TÍNH TOÁN --------------------
    private fun recalc() {
        val cur = _state.value
        val f = cur.food ?: return

        // 1) Khối lượng ăn được (áp ediblePortion)
        val edible = (f.ediblePortion ?: 1.0).coerceIn(0.0, 1.0)
        val gramsNet = (cur.grams * edible).coerceAtLeast(0.0)

        // 2) Thất thoát theo cách chế biến (0.0–1.0; null -> 1.0)
        val lossFactor = (cur.method?.lossFactor ?: 1.0).coerceIn(0.0, 1.0)

        // 3) Dầu mỡ hấp thụ thêm theo 100g (nếu có)
        val extraFat = (cur.method?.extraFatGPer100g ?: 0.0).coerceAtLeast(0.0) * (gramsNet / 100.0)

        // 4) Kcal = kcal/100g * gramsNet * loss + extraFat*9
        val kcalBase = f.kcalPer100g * (gramsNet / 100.0) * lossFactor
        val kcal = kcalBase + extraFat * 9.0

        // 5) Macros = macro/100g * gramsNet * loss; riêng fat cộng thêm extraFat
        val protein = f.proteinPer100g?.let { it * (gramsNet / 100.0) * lossFactor }
        val carb    = f.carbPer100g?.let    { it * (gramsNet / 100.0) * lossFactor }
        val fat0    = f.fatPer100g?.let     { it * (gramsNet / 100.0) * lossFactor }
        val fat     = if (fat0 != null) fat0 + extraFat else null

        _state.value = cur.copy(
            kcalTotal = kcal.roundToInt(),
            proteinG = protein,
            carbG = carb,
            fatG = fat
        )
    }
}
