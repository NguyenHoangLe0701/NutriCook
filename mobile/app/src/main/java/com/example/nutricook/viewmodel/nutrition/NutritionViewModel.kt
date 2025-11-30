package com.example.nutricook.viewmodel.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.nutrition.NutritionRepository
import com.example.nutricook.model.nutrition.DailyLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NutritionUiState(
    val loading: Boolean = false,
    val history: List<DailyLog> = emptyList(), // Dữ liệu cho biểu đồ
    val todayLog: DailyLog? = null, // Dữ liệu hôm nay (nếu null nghĩa là chưa có gì)
    val message: String? = null
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repo: NutritionRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(NutritionUiState())
    val ui = _ui.asStateFlow()

    init {
        loadData()
    }

    fun loadData() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        try {
            // 1. Lấy lịch sử tuần (để vẽ biểu đồ)
            val weekHistory = repo.getWeeklyHistory()

            // 2. Lấy dữ liệu hôm nay
            // Hàm này trong Repo đã tự check ngày. Nếu qua ngày mới -> trả về null
            val today = repo.getTodayLog()

            _ui.update {
                it.copy(
                    loading = false,
                    history = weekHistory,
                    // Nếu today là null (qua ngày mới), tạo object rỗng để UI hiển thị 0
                    todayLog = today ?: DailyLog(calories = 0f, protein = 0f, fat = 0f, carb = 0f)
                )
            }
        } catch (e: Exception) {
            _ui.update { it.copy(loading = false, message = e.message) }
        }
    }

    // Hàm này gọi từ Dialog nhập liệu
    fun updateTodayNutrition(cal: Float, pro: Float, fat: Float, carb: Float) = viewModelScope.launch {
        try {
            // Gọi Repo để cộng dồn số liệu vào ngày hôm nay
            repo.updateTodayNutrition(cal, pro, fat, carb)

            // Reload lại để cập nhật UI ngay lập tức
            loadData()
            _ui.update { it.copy(message = "Đã cập nhật dinh dưỡng!") }
        } catch (e: Exception) {
            _ui.update { it.copy(message = "Lỗi: ${e.message}") }
        }
    }
}