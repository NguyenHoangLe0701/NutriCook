package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.profile.Post
import com.example.nutricook.viewmodel.common.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ListState<Post>(loading = true))
    val state: StateFlow<ListState<Post>> = _state

    private var userId: String? = null
    private var nextCursor: String? = null
    private var initialLoaded = false

    fun loadInitial(uid: String) {
        // Tránh reload nếu đã có dữ liệu cho cùng uid
        if (uid == userId && initialLoaded) return

        userId = uid
        nextCursor = null
        initialLoaded = false

        viewModelScope.launch {
            _state.value = ListState(loading = true)
            runCatching {
                // Nếu API hỗ trợ, truyền limit = 3 ở đây
                // repo.getUserPosts(uid, cursor = null, limit = 3)
                repo.getUserPosts(uid, cursor = null)
            }.onSuccess { page ->
                nextCursor = page.nextCursor
                initialLoaded = true
                _state.value = ListState(
                    loading = false,
                    items = page.items,
                    hasMore = nextCursor != null,
                    loadingMore = false
                )
            }.onFailure { e ->
                _state.value = ListState(
                    loading = false,
                    items = emptyList(),
                    error = e.message
                )
            }
        }
    }

    fun loadMore() {
        val uid = userId ?: return
        val cursor = nextCursor ?: return
        if (_state.value.loadingMore) return

        viewModelScope.launch {
            _state.value = _state.value.copy(loadingMore = true)
            runCatching {
                // Nếu API hỗ trợ, truyền limit = 3 ở đây
                // repo.getUserPosts(uid, cursor, limit = 3)
                repo.getUserPosts(uid, cursor)
            }.onSuccess { page ->
                nextCursor = page.nextCursor
                _state.value = _state.value.copy(
                    items = _state.value.items + page.items,
                    hasMore = nextCursor != null,
                    loadingMore = false
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loadingMore = false,
                    error = e.message
                )
            }
        }
    }
}
