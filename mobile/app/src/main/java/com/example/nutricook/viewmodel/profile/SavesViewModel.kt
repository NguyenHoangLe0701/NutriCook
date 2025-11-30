package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.viewmodel.common.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavesViewModel @Inject constructor(
    private val repo: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Lấy userId từ Navigation argument
    private val uid: String = checkNotNull(savedStateHandle["userId"])

    private val _state = MutableStateFlow(ListState<Post>(loading = true))
    val state: StateFlow<ListState<Post>> = _state

    private var nextCursor: String? = null
    private var paging = false

    init {
        refresh()
    }

    /** Load lần đầu hoặc Refresh toàn bộ danh sách */
    fun refresh() = viewModelScope.launch {
        nextCursor = null
        _state.value = _state.value.copy(loading = true, error = null)

        // [SỬA LẠI]: getUserSaves -> getSavedPosts
        runCatching { repo.getSavedPosts(uid, cursor = null) }
            .onSuccess { page ->
                nextCursor = page.nextCursor
                _state.value = _state.value.copy(
                    loading = false,
                    items = page.items,
                    hasMore = page.nextCursor != null
                )
            }
            .onFailure { e ->
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
    }

    /** Tải tiếp trang sau (Pagination) */
    fun loadMore() {
        val cursor = nextCursor ?: return
        if (paging || _state.value.loadingMore) return
        paging = true

        _state.value = _state.value.copy(loadingMore = true, error = null)

        viewModelScope.launch {
            // [SỬA LẠI]: getUserSaves -> getSavedPosts
            runCatching { repo.getSavedPosts(uid, cursor) }
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _state.value = _state.value.copy(
                        items = _state.value.items + page.items,
                        hasMore = page.nextCursor != null,
                        loadingMore = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(loadingMore = false, error = e.message)
                }
            paging = false
        }
    }
}