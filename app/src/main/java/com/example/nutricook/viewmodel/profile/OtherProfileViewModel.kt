package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid: String = checkNotNull(savedStateHandle["userId"])

    private val _ui = MutableStateFlow(OtherProfileUiState())
    val ui: StateFlow<OtherProfileUiState> = _ui

    // quản lý cursor phân trang nội bộ (không đẩy lên UI)
    private var nextCursor: String? = null

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        _ui.update { it.copy(loading = true, error = null) }

        runCatching { repo.getProfileByUid(uid) }
            .onSuccess { profile ->
                val following = runCatching { repo.isFollowing(uid) }.getOrDefault(false)
                val firstPage = runCatching { repo.getUserPosts(uid, null) }.getOrNull()
                nextCursor = firstPage?.nextCursor

                _ui.update {
                    it.copy(
                        loading = false,
                        profile = profile,
                        isFollowing = following,
                        posts = firstPage?.items.orEmpty(),
                        hasMore = !firstPage?.nextCursor.isNullOrBlank(),
                        loadingMore = false,
                        error = null
                    )
                }
            }
            .onFailure { e ->
                _ui.update { it.copy(loading = false, error = e.message) }
            }
    }

    fun toggleFollow() = viewModelScope.launch {
        if (_ui.value.togglingFollow || _ui.value.profile == null) return@launch
        val now = !_ui.value.isFollowing

        // optimistic + lock nút
        _ui.update { it.copy(isFollowing = now, togglingFollow = true) }

        runCatching { repo.setFollow(uid, now) }
            .onFailure { e ->
                _ui.update { it.copy(isFollowing = !now, error = e.message, togglingFollow = false) }
            }
            .onSuccess {
                _ui.update { it.copy(togglingFollow = false) }
            }
    }

    fun loadMore() = viewModelScope.launch {
        val cursor = nextCursor ?: return@launch
        if (_ui.value.loadingMore) return@launch

        _ui.update { it.copy(loadingMore = true) }

        runCatching { repo.getUserPosts(uid, cursor) }
            .onSuccess { page ->
                nextCursor = page.nextCursor
                _ui.update {
                    it.copy(
                        loadingMore = false,
                        posts = it.posts + page.items,
                        hasMore = !page.nextCursor.isNullOrBlank()
                    )
                }
            }
            .onFailure { e ->
                _ui.update { it.copy(loadingMore = false, error = e.message) }
            }
    }
}
