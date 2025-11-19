package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.profile.Post
import com.example.nutricook.model.profile.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// State riêng cho màn hình Public Profile
data class PublicProfileState(
    val loading: Boolean = true,
    val profile: Profile? = null,
    val isFollowing: Boolean = false, // Trạng thái nút Follow
    val posts: List<Post> = emptyList(), // Danh sách bài viết (Recipes)
    val error: String? = null
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    savedStateHandle: SavedStateHandle // Để lấy userId từ Navigation
) : ViewModel() {

    private val _state = MutableStateFlow(PublicProfileState())
    val state = _state.asStateFlow()

    // Giả sử khi navigate bạn truyền tham số tên là "userId"
    private val targetUserId: String? = savedStateHandle["userId"]

    init {
        loadData()
    }

    fun loadData() {
        if (targetUserId == null) {
            _state.update { it.copy(loading = false, error = "User ID not found") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                // 1. Lấy thông tin Profile
                val profile = repo.getProfileByUid(targetUserId)

                // 2. Kiểm tra mình đã follow người này chưa
                val isFollowing = repo.isFollowing(targetUserId)

                // 3. Lấy danh sách bài viết (Recipes)
                val postsPaged = repo.getUserPosts(targetUserId)

                _state.update {
                    it.copy(
                        loading = false,
                        profile = profile,
                        isFollowing = isFollowing,
                        posts = postsPaged.items
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun toggleFollow() {
        val uid = targetUserId ?: return
        val currentFollowState = _state.value.isFollowing

        viewModelScope.launch {
            // Cập nhật UI lạc quan (Optimistic update) để app mượt hơn
            _state.update { it.copy(isFollowing = !currentFollowState) }

            try {
                // Gọi xuống Firebase
                repo.setFollow(uid, !currentFollowState)

                // Tùy chọn: Load lại profile để cập nhật số lượng Follower chính xác
                val updatedProfile = repo.getProfileByUid(uid)
                _state.update { it.copy(profile = updatedProfile) }

            } catch (e: Exception) {
                // Nếu lỗi thì revert lại trạng thái cũ
                _state.update { it.copy(isFollowing = currentFollowState, error = "Lỗi follow: ${e.message}") }
            }
        }
    }
}