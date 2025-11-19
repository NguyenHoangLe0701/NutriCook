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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class NutritionUiState(
    val loading: Boolean = false,
    val history: List<DailyLog> = emptyList(), // Dữ liệu cho biểu đồ
    val todayLog: DailyLog? = null, // Dữ liệu hôm nay để hiển thị input
    val message: String? = null
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repo: NutritionRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(NutritionUiState())
    val ui = _ui.asStateFlow()

    // Format ngày chuẩn: yyyy-MM-dd
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val todayId: String get() = dateFormat.format(Date())

    init {
        loadData()
    }

    fun loadData() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        try {
            val weekHistory = repo.getWeeklyHistory()
            val today = repo.getLogByDate(todayId) ?: DailyLog(dateId = todayId)

            _ui.update {
                it.copy(loading = false, history = weekHistory, todayLog = today)
            }
        } catch (e: Exception) {
            _ui.update { it.copy(loading = false, message = e.message) }
        }
    }

    // Hàm gọi từ UI khi người dùng nhập số liệu mới
    fun updateTodayNutrition(cal: Float, pro: Float, fat: Float, carb: Float) = viewModelScope.launch {
        val log = DailyLog(
            dateId = todayId,
            calories = cal,
            protein = pro,
            fat = fat,
            carb = carb
        )

        try {
            repo.saveDailyLog(log)
            loadData() // Reload lại biểu đồ ngay lập tức
            _ui.update { it.copy(message = "Đã cập nhật dinh dưỡng hôm nay!") }
        } catch (e: Exception) {
            _ui.update { it.copy(message = "Lỗi: ${e.message}") }
        }
    }
}