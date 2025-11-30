package com.example.nutricook.viewmodel.hotnews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.hotnews.HotNewsRepository
import com.example.nutricook.model.hotnews.HotNewsArticle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HotNewsUiState(
    val articles: List<HotNewsArticle> = emptyList(),
    val filteredArticles: List<HotNewsArticle> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class HotNewsViewModel @Inject constructor(
    private val repository: HotNewsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HotNewsUiState())
    val uiState: StateFlow<HotNewsUiState> = _uiState.asStateFlow()
    
    init {
        loadHotNews()
    }
    
    fun loadHotNews() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val articles = repository.getAllHotNews()
                _uiState.value = _uiState.value.copy(
                    articles = articles,
                    filteredArticles = articles,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Lỗi khi tải tin tức"
                )
            }
        }
    }
    
    fun filterByCategory(category: String?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val filtered = if (category.isNullOrBlank()) {
                currentState.articles
            } else {
                currentState.articles.filter { 
                    it.category.equals(category, ignoreCase = true) 
                }
            }
            
            // Apply search filter if exists
            val finalFiltered = if (currentState.searchQuery.isNotBlank()) {
                filtered.filter { 
                    it.title.contains(currentState.searchQuery, ignoreCase = true) ||
                    it.category.contains(currentState.searchQuery, ignoreCase = true)
                }
            } else {
                filtered
            }
            
            _uiState.value = currentState.copy(
                selectedCategory = category,
                filteredArticles = finalFiltered
            )
        }
    }
    
    fun search(query: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val filtered = if (query.isBlank()) {
                if (currentState.selectedCategory.isNullOrBlank()) {
                    currentState.articles
                } else {
                    currentState.articles.filter { 
                        it.category.equals(currentState.selectedCategory, ignoreCase = true) 
                    }
                }
            } else {
                val categoryFiltered = if (currentState.selectedCategory.isNullOrBlank()) {
                    currentState.articles
                } else {
                    currentState.articles.filter { 
                        it.category.equals(currentState.selectedCategory, ignoreCase = true) 
                    }
                }
                
                categoryFiltered.filter { 
                    it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            }
            
            _uiState.value = currentState.copy(
                searchQuery = query,
                filteredArticles = filtered
            )
        }
    }
    
    fun createArticle(
        title: String,
        content: String,
        category: String,
        thumbnailUri: android.net.Uri?,
        link: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.createHotNewsArticle(title, content, category, thumbnailUri, link)
                result.fold(
                    onSuccess = {
                        onSuccess()
                        loadHotNews() // Reload list
                    },
                    onFailure = { error ->
                        onError(error.message ?: "Lỗi khi tạo bài đăng")
                    }
                )
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi khi tạo bài đăng")
            }
        }
    }
    
    fun getAvailableCategories(): List<String> {
        return _uiState.value.articles.map { it.category }.distinct().sorted()
    }
}

