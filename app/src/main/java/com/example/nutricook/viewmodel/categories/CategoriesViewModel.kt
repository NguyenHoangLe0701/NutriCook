package com.example.nutricook.viewmodel.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.catalog.AssetCatalogRepository
import com.example.nutricook.data.catalog.CatalogRepository
import com.example.nutricook.data.catalog.Category
import com.example.nutricook.data.catalog.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoriesState(
    val loading: Boolean = true,
    val error: String? = null,
    val categories: List<Category> = emptyList(),
    val selectedIndex: Int = 0,
    val foods: List<Food> = emptyList()
)

class CategoriesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: CatalogRepository = AssetCatalogRepository(app)

    private val _state = MutableStateFlow(CategoriesState())
    val state: StateFlow<CategoriesState> = _state

    init { load() }

    fun load() = viewModelScope.launch {
        try {
            val cats = repo.getCategories()
            if (cats.isEmpty()) {
                _state.value = _state.value.copy(loading = false, error = "Không có danh mục")
                return@launch
            }
            val foods = repo.getFoodsByCategory(cats.first().id)
            _state.value = CategoriesState(
                loading = false,
                categories = cats,
                foods = foods,
                selectedIndex = 0
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(loading = false, error = e.message ?: "Lỗi tải dữ liệu")
        }
    }

    fun select(index: Int) = viewModelScope.launch {
        val cats = _state.value.categories
        if (index !in cats.indices) return@launch
        _state.value = _state.value.copy(loading = true, selectedIndex = index)
        val foods = repo.getFoodsByCategory(cats[index].id)
        _state.value = _state.value.copy(loading = false, foods = foods)
    }
}
