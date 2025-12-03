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
    val imageUrl: String,
    val categoryId: Long? = null, // ID của category
    val unit: String = "g", // Đơn vị mặc định (g, ml, quả, etc.)
    // Thông tin dinh dưỡng (tính trên 100g)
    val fat: Double = 0.0, // g
    val carbs: Double = 0.0, // g
    val protein: Double = 0.0, // g
    val cholesterol: Double = 0.0, // mg
    val sodium: Double = 0.0, // mg
    val vitamin: Double = 0.0, // % daily value (tổng trung bình)
    // Chi tiết các loại vitamin (% daily value)
    val vitaminA: Double = 0.0,
    val vitaminB1: Double = 0.0,
    val vitaminB2: Double = 0.0,
    val vitaminB3: Double = 0.0,
    val vitaminB6: Double = 0.0,
    val vitaminB9: Double = 0.0,
    val vitaminB12: Double = 0.0,
    val vitaminC: Double = 0.0,
    val vitaminD: Double = 0.0,
    val vitaminE: Double = 0.0,
    val vitaminK: Double = 0.0
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

    // Tất cả foodItems (không filter theo category) - dùng cho tính toán dinh dưỡng
    private val _allFoodItems = MutableStateFlow<List<FoodItemUI>>(emptyList())
    val allFoodItems: StateFlow<List<FoodItemUI>> = _allFoodItems.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    init {
        fetchCategories()
        loadAllFoodItems() // Load tất cả foodItems ngay khi khởi tạo
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
    
    suspend fun getFoodById(foodId: Long): FoodItemUI? {
        return try {
            repository.getFoodById(foodId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Load tất cả foodItems (không filter theo category).
     * Dùng để tính toán dinh dưỡng cho recipe có nguyên liệu từ nhiều categories.
     */
    fun loadAllFoodItems() {
        viewModelScope.launch {
            try {
                _allFoodItems.value = repository.getAllFoods()
                android.util.Log.d("CategoriesViewModel", "Loaded all foodItems: ${_allFoodItems.value.size}")
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("CategoriesViewModel", "Error loading all foodItems: ${e.message}", e)
            }
        }
    }
}