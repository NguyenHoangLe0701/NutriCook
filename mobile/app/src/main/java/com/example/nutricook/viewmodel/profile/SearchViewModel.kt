package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.profile.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Profile> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(SearchUiState())
    val uiState = _ui.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _ui.update { it.copy(query = newQuery) }

        // Debounce: Hủy tìm kiếm cũ nếu người dùng gõ tiếp nhanh quá
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            _ui.update { it.copy(results = emptyList(), loading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Đợi 500ms sau khi ngừng gõ mới gọi API
            _ui.update { it.copy(loading = true, error = null) }

            runCatching { repo.searchProfiles(newQuery) }
                .onSuccess { profiles ->
                    _ui.update { it.copy(loading = false, results = profiles) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message) }
                }
        }
    }
}