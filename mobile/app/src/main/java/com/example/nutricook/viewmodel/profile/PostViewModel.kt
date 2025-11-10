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

    private val _state = MutableStateFlow(ListState<Post>())
    val state: StateFlow<ListState<Post>> = _state

    private var currentUid: String? = null
    private var nextCursor: String? = null
    private var pagingInFlight = false

    /** Gọi ở ProfileScreen: LaunchedEffect(uid) { postVm.loadInitial(uid) } */
    fun loadInitial(uid: String) {
        // tránh nạp lại khi đã có dữ liệu đúng uid
        if (currentUid == uid && _state.value.items.isNotEmpty()) return

        currentUid = uid
        nextCursor = null
        _state.value = ListState(loading = true)

        viewModelScope.launch {
            runCatching { repo.getUserPosts(uid, cursor = null) }
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _state.value = ListState(
                        loading = false,
                        items = page.items,
                        hasMore = page.nextCursor != null
                    )
                }
                .onFailure { e ->
                    _state.value = ListState(
                        loading = false,
                        items = emptyList(),
                        error = e.message
                    )
                }
        }
    }

    /** Gọi từ UI khi nhấn “Tải thêm…” */
    fun loadMore() {
        val uid = currentUid ?: return
        val cursor = nextCursor ?: return
        if (pagingInFlight) return

        pagingInFlight = true
        _state.value = _state.value.copy(loadingMore = true)

        viewModelScope.launch {
            runCatching { repo.getUserPosts(uid, cursor) }
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _state.value = _state.value.copy(
                        items = _state.value.items + page.items,
                        hasMore = page.nextCursor != null,
                        loadingMore = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loadingMore = false,
                        error = e.message
                    )
                }
            pagingInFlight = false
        }
    }
}
