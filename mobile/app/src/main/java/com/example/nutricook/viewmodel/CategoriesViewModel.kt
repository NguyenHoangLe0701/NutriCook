package com.example.nutricook.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.repository.CategoryFirestoreRepository
import kotlin.collections.emptyList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ĐỊNH NGHĨA DATA CLASS (phải có)
data class FoodItemUI(
    val id: Long,
    val name: String,
    val calories: String,
    val imageUrl: String
)

data class CategoryUI(
    val id: Long,
    val name: String,
    val icon: String,
    val color: Color
)
// KẾT THÚC DATA CLASS

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: CategoryFirestoreRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<CategoryUI>>(emptyList())
    val categories: StateFlow<List<CategoryUI>> = _categories.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItemUI>>(emptyList())
    val foodItems: StateFlow<List<FoodItemUI>> = _foodItems.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    init {
        fetchCategories()
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            try {
                _categories.value = repository.getCategories()
                _categories.value.firstOrNull()?.let {
                    selectCategory(it.id) // it.id là 1 (kiểu Long)
                }
            } catch (e: Exception) {
                e.printStackTrace() // Xử lý lỗi
            }
        }
    }

    fun selectCategory(categoryId: Long) {
        if (_selectedCategoryId.value == categoryId) return
        _selectedCategoryId.value = categoryId
        fetchFoods(categoryId)
    }

    private fun fetchFoods(categoryId: Long) {
        viewModelScope.launch {
            _foodItems.value = emptyList() // Xóa list cũ
            try {
                _foodItems.value = repository.getFoods(categoryId) // Lấy món ăn cho categoryId=1
            } catch (e: Exception) {
                e.printStackTrace() // Xử lý lỗi
            }
        }
    }
}