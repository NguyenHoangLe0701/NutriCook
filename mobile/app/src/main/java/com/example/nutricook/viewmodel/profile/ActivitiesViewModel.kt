package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.profile.ActivityItem
import com.example.nutricook.viewmodel.common.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val repo: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val uid: String = checkNotNull(savedStateHandle["userId"])

    private val _state = MutableStateFlow(ListState<ActivityItem>(loading = true))
    val state: StateFlow<ListState<ActivityItem>> = _state

    // giữ cursor trong VM, không để trong state
    private var nextCursor: String? = null
    private var paging = false

    init { load() }

    /** load lần đầu hoặc refresh */
    fun load() = viewModelScope.launch {
        nextCursor = null
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { repo.getUserActivities(uid, cursor = null) }
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

    /** tải thêm trang kế tiếp */
    fun loadMore() {
        val cursor = nextCursor ?: return
        if (paging) return
        paging = true

        _state.value = _state.value.copy(loadingMore = true, error = null)

        viewModelScope.launch {
            runCatching { repo.getUserActivities(uid, cursor) }
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
