package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSearchViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    data class UiState(
        val query: String = "",
        val loading: Boolean = false,
        val results: List<User> = emptyList(),
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    private var searchJob: Job? = null

    fun updateQuery(q: String) {
        _ui.update { it.copy(query = q) }
        // debounce 300ms
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val key = q.trim()
            if (key.isEmpty()) {
                _ui.update { it.copy(results = emptyList(), loading = false, error = null) }
                return@launch
            }
            delay(300)
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.searchUsers(key, limit = 30) }
                .onSuccess { list ->
                    _ui.update { it.copy(loading = false, results = list, error = null) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message, results = emptyList()) }
                }
        }
    }
}