package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUserUiState(
    val query: String = "",
    val loading: Boolean = false,
    val results: List<User> = emptyList(),
    val error: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchUserViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(SearchUserUiState())
    val ui: StateFlow<SearchUserUiState> = _ui

    init {
        viewModelScope.launch {
            // tự động search khi người dùng gõ
            MutableStateFlow("").apply {
                // pipe từ _ui.query
            }
        }
        // Cách đơn giản: theo dõi field query trong _ui
        viewModelScope.launch {
            _ui
                .collect { state ->
                    // no-op; dùng hàm setQuery để debounce riêng
                }
        }
    }

    fun setQuery(q: String) {
        _ui.update { it.copy(query = q) }
        searchDebounced(q)
    }

    private var lastJob: kotlinx.coroutines.Job? = null

    private fun searchDebounced(q: String) {
        lastJob?.cancel()
        lastJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            val key = q.trim()
            if (key.isBlank()) {
                _ui.update { it.copy(results = emptyList(), loading = false, error = null) }
                return@launch
            }
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.searchUsers(key, 20) }
                .onSuccess { list ->
                    _ui.update { it.copy(loading = false, results = list) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message, results = emptyList()) }
                }
        }
    }
}
