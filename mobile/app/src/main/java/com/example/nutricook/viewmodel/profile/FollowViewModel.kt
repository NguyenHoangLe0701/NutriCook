package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.profile.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowUiState(
    val loading: Boolean = false,
    val profile: Profile? = null,
    val isFollowing: Boolean = false,
    val message: String? = null
)

sealed interface FollowEvent {
    /** uid của người cần xem */
    data class Load(val targetUid: String) : FollowEvent
    data class ToggleFollow(val follow: Boolean) : FollowEvent
    data object Consume : FollowEvent
}

@HiltViewModel
class FollowViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(FollowUiState())
    val uiState = _ui.asStateFlow()

    fun onEvent(e: FollowEvent) {
        when (e) {
            is FollowEvent.Load -> viewModelScope.launch {
                _ui.update { it.copy(loading = true, message = null) }
                runCatching {
                    val p = repo.getProfileByUid(e.targetUid)
                    val following = repo.isFollowing(e.targetUid)
                    p to following
                }.onSuccess { (p, following) ->
                    _ui.update { it.copy(loading = false, profile = p, isFollowing = following) }
                }.onFailure { err ->
                    _ui.update {
                        it.copy(
                            loading = false,
                            message = err.message ?: "Không tải được hồ sơ"
                        )
                    }
                }
            }

            is FollowEvent.ToggleFollow -> viewModelScope.launch {
                // Dùng UID (String) – KHÔNG dùng id Int/Long
                val targetUid = _ui.value.profile?.user?.id ?: return@launch
                runCatching { repo.setFollow(targetUid, e.follow) }
                    .onSuccess {
                        _ui.update { cur ->
                            val curFollowers = cur.profile?.followers ?: 0
                            val newFollowers = (curFollowers + if (e.follow) 1 else -1).coerceAtLeast(0)
                            cur.copy(
                                isFollowing = e.follow,
                                profile = cur.profile?.copy(followers = newFollowers)
                            )
                        }
                    }
                    .onFailure { err ->
                        _ui.update { it.copy(message = err.message ?: "Lỗi thao tác theo dõi") }
                    }
            }

            FollowEvent.Consume -> _ui.update { it.copy(message = null) }
        }
    }
}
