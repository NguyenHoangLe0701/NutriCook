package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.newsfeed.Post // [QUAN TRỌNG] Thêm import này
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

    // Lấy userId từ Navigation argument (đảm bảo không null)
    private val uid: String = checkNotNull(savedStateHandle["userId"])

    private val _state = MutableStateFlow(ListState<Post>(loading = true))
    val state: StateFlow<ListState<Post>> = _state

    // Giữ cursor trong ViewModel để xử lý Load More
    private var nextCursor: String? = null
    private var paging = false

    init {
        refresh()
    }

    /** Load lần đầu hoặc Refresh toàn bộ danh sách */
    fun refresh() = viewModelScope.launch {
        nextCursor = null
        // Reset state về loading, xóa lỗi cũ nếu có (nhưng giữ items cũ nếu muốn UX mượt hơn, ở đây reset loading=true thì UI thường hiện loading spinner toàn màn hình)
        _state.value = _state.value.copy(loading = true, error = null)

        runCatching { repo.getUserSaves(uid, cursor = null) }
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
        if (paging || _state.value.loadingMore) return // Kiểm tra thêm flag loadingMore trong state để chắc chắn
        paging = true

        _state.value = _state.value.copy(loadingMore = true, error = null)

        viewModelScope.launch {
            runCatching { repo.getUserSaves(uid, cursor) }
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