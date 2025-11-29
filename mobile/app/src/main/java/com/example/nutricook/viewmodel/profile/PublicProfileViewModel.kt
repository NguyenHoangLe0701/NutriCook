package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.newsfeed.Post // IMPORT QUAN TRỌNG: Sử dụng Model thống nhất
import com.example.nutricook.model.profile.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// State cho màn hình Public Profile
data class PublicProfileState(
    val loading: Boolean = true,
    val profile: Profile? = null,
    val isFollowing: Boolean = false, // Trạng thái nút Follow
    val posts: List<Post> = emptyList(), // Danh sách bài viết (Model thống nhất)
    val error: String? = null
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    savedStateHandle: SavedStateHandle // Để lấy userId từ Navigation
) : ViewModel() {

    private val _state = MutableStateFlow(PublicProfileState())
    val state = _state.asStateFlow()

    // Lấy tham số "userId" được truyền từ NavGraph
    private val targetUserId: String? = savedStateHandle["userId"]

    init {
        loadData()
    }

    fun loadData() {
        val uid = targetUserId
        if (uid == null) {
            _state.update { it.copy(loading = false, error = "Không tìm thấy User ID") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                // 1. Lấy thông tin Profile
                val profile = repo.getProfileByUid(uid)

                // 2. Kiểm tra mình đã follow người này chưa
                val isFollowing = repo.isFollowing(uid)

                // 3. Lấy danh sách bài viết
                // Vì Repo trả về Paged<Post> (model newsfeed) và State cũng dùng model newsfeed
                // nên ta gán trực tiếp .items mà không cần map lại.
                val postsPaged = repo.getUserPosts(uid)

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
        val currentProfile = _state.value.profile ?: return
        val currentFollowState = _state.value.isFollowing
        val newFollowState = !currentFollowState

        viewModelScope.launch {
            // --- OPTIMISTIC UPDATE (Cập nhật giao diện trước khi gọi API) ---

            // 1. Tính toán số follower mới giả định
            // Nếu follow -> +1, nếu unfollow -> -1 (nhưng không nhỏ hơn 0)
            val currentCount = currentProfile.followers
            val newCount = if (newFollowState) currentCount + 1 else (currentCount - 1).coerceAtLeast(0)

            // 2. Tạo profile object mới với số follower đã cập nhật
            val updatedProfile = currentProfile.copy(followers = newCount)

            // 3. Cập nhật UI ngay lập tức
            _state.update {
                it.copy(
                    isFollowing = newFollowState,
                    profile = updatedProfile
                )
            }

            try {
                // --- GỌI API ---
                repo.setFollow(uid, newFollowState)

            } catch (e: Exception) {
                // --- ROLLBACK (Nếu API lỗi thì trả về trạng thái cũ) ---
                _state.update {
                    it.copy(
                        isFollowing = currentFollowState, // Trả về trạng thái cũ
                        profile = currentProfile,         // Trả về profile cũ (số follower cũ)
                        error = "Lỗi thao tác: ${e.message}"
                    )
                }
            }
        }
    }
}