package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.nutricook.model.nutrition.*

class NutritionViewModel : ViewModel() {

    private val _state = MutableStateFlow(NutritionUiState())
    val state: StateFlow<NutritionUiState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        // TODO: nếu có DataStore, load ở đây. Tạm khởi tạo nhanh:
        val prof = NutritionProfile().recalculate()
        _state.value = NutritionUiState(
            loading = false,
            profile = prof,
            targets = prof.targets,
            macroPct = prof.targets.percentages()
        )
    }

    /** Update input (metrics/prefs/goal/delta) + recalc ngay */
    fun updateInputs(
        metrics: BodyMetrics? = null,
        prefs: MacroPrefs? = null,
        goal: Goal? = null,
        delta: Int? = null
    ) {
        val cur = _state.value
        val newProf = cur.profile
            .withInputs(metrics = metrics, prefs = prefs, goal = goal, deltaKcalPerDay = delta)
            .recalculate()
        _state.value = cur.copy(
            profile = newProf,
            targets = newProf.targets,
            macroPct = newProf.targets.percentages(),
            dirty = true
        )
    }

    /** Nếu bạn có nút Save: lưu DataStore/Firestore rồi clear cờ dirty */
    fun save() = viewModelScope.launch {
        // TODO: persist profile.nutrition -> DataStore/Firestore
        _state.update { it.copy(dirty = false) }
    }

    /** Reset về mặc định (tuỳ chọn) */
    fun resetDefaults() {
        val prof = NutritionProfile().recalculate()
        _state.value = _state.value.copy(
            profile = prof,
            targets = prof.targets,
            macroPct = prof.targets.percentages(),
            dirty = true
        )
    }
}

