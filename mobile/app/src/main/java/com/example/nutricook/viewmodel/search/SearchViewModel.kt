package com.example.nutricook.viewmodel.search

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.search.SearchRepository
import com.example.nutricook.model.search.SearchResult
import com.example.nutricook.model.search.SearchType
import com.example.nutricook.model.search.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val selectedTypes: Set<SearchType> = setOf(
        SearchType.RECIPES,
        SearchType.FOODS,
        SearchType.NEWS
    ),
    val selectedCategory: String? = null,
    val caloriesRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    val sortBy: SortOption = SortOption.RELEVANCE,
    val results: Map<SearchType, List<SearchResult>> = emptyMap(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showFilters: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    private val prefs: SharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)
    
    init {
        loadRecentSearches()
    }
    
    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        
        // Debounce: Hủy tìm kiếm cũ nếu người dùng gõ tiếp nhanh quá
        searchJob?.cancel()
        
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(results = emptyMap(), isLoading = false) }
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(500) // Đợi 500ms sau khi ngừng gõ mới gọi API
            
            val currentState = _uiState.value
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val results = repository.searchAll(
                    query = newQuery,
                    types = currentState.selectedTypes
                )
                
                // Apply filters
                val filteredResults = applyFilters(results, currentState)
                
                // Sort results
                val sortedResults = sortResults(filteredResults, currentState.sortBy)
                
                _uiState.update { 
                    it.copy(
                        results = sortedResults,
                        isLoading = false,
                        error = null
                    )
                }
                
                // Save to recent searches
                if (newQuery.isNotBlank()) {
                    saveRecentSearch(newQuery)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Lỗi khi tìm kiếm"
                    )
                }
            }
        }
    }
    
    fun toggleSearchType(type: SearchType) {
        _uiState.update { currentState ->
            val newTypes = if (currentState.selectedTypes.contains(type)) {
                currentState.selectedTypes - type
            } else {
                currentState.selectedTypes + type
            }
            currentState.copy(selectedTypes = newTypes)
        }
        
        // Re-search với types mới
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank()) {
            onQueryChange(currentQuery)
        }
    }
    
    fun setCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank()) {
            onQueryChange(currentQuery)
        }
    }
    
    fun setCaloriesRange(range: ClosedFloatingPointRange<Float>) {
        _uiState.update { it.copy(caloriesRange = range) }
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank()) {
            onQueryChange(currentQuery)
        }
    }
    
    fun setSortBy(sortOption: SortOption) {
        _uiState.update { it.copy(sortBy = sortOption) }
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank()) {
            onQueryChange(currentQuery)
        }
    }
    
    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }
    
    fun clearSearch() {
        _uiState.update { 
            SearchUiState(
                recentSearches = _uiState.value.recentSearches
            )
        }
    }
    
    fun selectRecentSearch(query: String) {
        onQueryChange(query)
    }
    
    fun clearRecentSearches() {
        viewModelScope.launch {
            prefs.edit().clear().apply()
            _uiState.update { it.copy(recentSearches = emptyList()) }
        }
    }
    
    private fun applyFilters(
        results: Map<SearchType, List<SearchResult>>,
        state: SearchUiState
    ): Map<SearchType, List<SearchResult>> {
        var filtered = results
        
        // Filter by category (for news)
        if (state.selectedCategory != null) {
            filtered = filtered.mapValues { (type, items) ->
                if (type == SearchType.NEWS) {
                    items.filter { result ->
                        (result as? SearchResult.NewsResult)?.category == state.selectedCategory
                    }
                } else {
                    items
                }
            }
        }
        
        // Filter by calories range (for recipes and foods)
        filtered = filtered.mapValues { (type, items) ->
            items.filter { result ->
                when (result) {
                    is SearchResult.RecipeResult -> {
                        val calories = result.calories?.toFloat() ?: 0f
                        calories in state.caloriesRange
                    }
                    is SearchResult.FoodResult -> {
                        result.calories in state.caloriesRange
                    }
                    else -> true
                }
            }
        }
        
        return filtered
    }
    
    private fun sortResults(
        results: Map<SearchType, List<SearchResult>>,
        sortBy: SortOption
    ): Map<SearchType, List<SearchResult>> {
        return when (sortBy) {
            SortOption.RELEVANCE -> results // Already sorted by relevance from Firestore
            SortOption.NEWEST -> results.mapValues { (_, items) ->
                items.sortedByDescending { 
                    when (it) {
                        is SearchResult.NewsResult -> it.id // Use ID as proxy for date
                        else -> it.id
                    }
                }
            }
            SortOption.POPULAR -> results // TODO: Implement popularity sorting
            SortOption.CALORIES_LOW_TO_HIGH -> results.mapValues { (_, items) ->
                items.sortedBy {
                    when (it) {
                        is SearchResult.RecipeResult -> it.calories?.toFloat() ?: Float.MAX_VALUE
                        is SearchResult.FoodResult -> it.calories
                        else -> Float.MAX_VALUE
                    }
                }
            }
            SortOption.CALORIES_HIGH_TO_LOW -> results.mapValues { (_, items) ->
                items.sortedByDescending {
                    when (it) {
                        is SearchResult.RecipeResult -> it.calories?.toFloat() ?: 0f
                        is SearchResult.FoodResult -> it.calories
                        else -> 0f
                    }
                }
            }
        }
    }
    
    private fun saveRecentSearch(query: String) {
        viewModelScope.launch {
            try {
                val current = prefs.getStringSet("recent_searches", mutableSetOf())?.toMutableList() ?: mutableListOf()
                
                // Remove if exists and add to front
                current.remove(query)
                current.add(0, query)
                
                // Keep only last 10
                val limited = current.take(10)
                
                prefs.edit().putStringSet("recent_searches", limited.toSet()).apply()
                
                _uiState.update { it.copy(recentSearches = limited) }
            } catch (e: Exception) {
                android.util.Log.e("SearchViewModel", "Error saving recent search: ${e.message}")
            }
        }
    }
    
    private fun loadRecentSearches() {
        viewModelScope.launch {
            try {
                val recent = prefs.getStringSet("recent_searches", emptySet())?.toList() ?: emptyList()
                _uiState.update { it.copy(recentSearches = recent) }
            } catch (e: Exception) {
                android.util.Log.e("SearchViewModel", "Error loading recent searches: ${e.message}")
            }
        }
    }
}

