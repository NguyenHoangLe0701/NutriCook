package com.example.nutricook.data.storage

import com.example.nutricook.model.nutrition.NutritionProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Store in-memory cho "My Fat" (NutritionProfile).
 * Dùng StateFlow để ViewModel subscribe realtime.
 */
@Singleton
class NutritionProfileStore @Inject constructor() {

    private val _state = MutableStateFlow(NutritionProfile())

    /** Dòng trạng thái hiện tại (StateFlow cũng là Flow) */
    fun flow(): StateFlow<NutritionProfile> = _state

    /** Cập nhật dựa trên state hiện tại */
    fun update(transform: (NutritionProfile) -> NutritionProfile) {
        _state.value = transform(_state.value)
    }

    /** Gán trực tiếp một giá trị mới */
    fun set(value: NutritionProfile) {
        _state.value = value
    }
}
