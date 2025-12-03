package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.data.repository.UserRecipeRepository
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.profile.Profile
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    private val userRecipeRepo: UserRecipeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _ui.asStateFlow()

    // Danh sách bài đã lưu
    private val _savedPosts = MutableStateFlow<List<Post>>(emptyList())
    val savedPosts: StateFlow<List<Post>> = _savedPosts.asStateFlow()

    // [MỚI] Danh sách bài viết của chính tôi
    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

    // [MỚI] Danh sách công thức của chính tôi
    private val _userRecipes = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val userRecipes: StateFlow<List<Map<String, Any>>> = _userRecipes.asStateFlow()

    init {
        // 1. Tự động lắng nghe thay đổi từ Firestore (Real-time)
        repo.myProfileFlow()
            .distinctUntilChanged()
            .onEach { p ->
                val chartData = generateChartData(p)
                _ui.update {
                    it.copy(
                        loading = false,
                        profile = p,
                        chartData = chartData
                    )
                }
            }
            .catch { e ->
                _ui.update { it.copy(message = e.message ?: "Lỗi luồng hồ sơ") }
            }
            .launchIn(viewModelScope)

        // 2. Load Profile lần đầu
        viewModelScope.launch {
            if (_ui.value.profile == null) {
                runCatching { repo.getMyProfile() }
            }
        }

        // 3. Tự động load dữ liệu bài viết
        loadSavedPosts()
        loadUserPosts() // [MỚI] Load bài viết của mình
    }

    // --- CÁC HÀM UPDATE ---

    fun updateAvatar(localUri: String) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }

        runCatching {
            repo.updateAvatar(localUri)
        }
            .onSuccess { newUrl ->
                _ui.update { state ->
                    val updatedUser = state.profile?.user?.copy(avatarUrl = newUrl)
                    val updatedProfile = state.profile?.copy(user = updatedUser!!)

                    state.copy(
                        updating = false,
                        profile = updatedProfile ?: state.profile,
                        message = "Đã cập nhật ảnh đại diện"
                    )
                }
            }
            .onFailure { e ->
                _ui.update {
                    it.copy(
                        updating = false,
                        message = "Lỗi đổi ảnh: ${e.message}"
                    )
                }
            }
    }

    fun updateProfile(fullName: String?, bio: String?) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching {
            repo.updateProfile(
                fullName = fullName,
                dayOfBirth = null,
                gender = null,
                bio = bio
            )
        }
            .onSuccess {
                _ui.update { it.copy(updating = false, message = "Đã lưu hồ sơ") }
            }
            .onFailure { e ->
                _ui.update { it.copy(updating = false, message = e.message ?: "Lỗi lưu hồ sơ") }
            }
    }

    fun updateCaloriesTarget(target: Float) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching {
            repo.updateCaloriesTarget(target)
        }
            .onSuccess {
                _ui.update { it.copy(updating = false, message = "Đã cập nhật mục tiêu") }
            }
            .onFailure { e ->
                _ui.update { it.copy(updating = false, message = e.message ?: "Lỗi cập nhật") }
            }
    }

    // --- CÁC HÀM KHÁC ---

    // Hàm lấy bài viết đã lưu
    fun loadSavedPosts() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch

        runCatching { repo.getSavedPosts(uid) }
            .onSuccess { paged ->
                _savedPosts.value = paged.items
            }
            .onFailure { e ->
                e.printStackTrace()
            }
    }

    // [MỚI] Hàm lấy bài viết của chính mình
    fun loadUserPosts() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch

        runCatching { repo.getUserPosts(uid) }
            .onSuccess { paged ->
                _userPosts.value = paged.items
            }
            .onFailure { e ->
                e.printStackTrace()
            }
    }

    // [MỚI] Hàm lấy công thức của chính mình
    fun loadUserRecipes() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        
        android.util.Log.d("ProfileViewModel", "Loading user recipes for userId: $uid")

        runCatching { userRecipeRepo.getUserRecipes(uid) }
            .onSuccess { recipes ->
                android.util.Log.d("ProfileViewModel", "Loaded ${recipes.size} recipes")
                _userRecipes.value = recipes
            }
            .onFailure { e ->
                android.util.Log.e("ProfileViewModel", "Error loading user recipes: ${e.message}", e)
                e.printStackTrace()
                _userRecipes.value = emptyList()
            }
    }

    fun setFollow(targetUid: String, follow: Boolean) = viewModelScope.launch {
        runCatching { repo.setFollow(targetUid, follow) }
            .onFailure { _ui.update { it.copy(message = "Lỗi thao tác theo dõi") } }
    }

    suspend fun isFollowing(targetUid: String): Boolean =
        runCatching { repo.isFollowing(targetUid) }.getOrElse { false }

    fun changePassword(old: String, new: String) = viewModelScope.launch {
        _ui.update { it.copy(updating = true) }
        runCatching { repo.changePassword(old, new) }
            .onSuccess { _ui.update { it.copy(updating = false, message = "Đã đổi mật khẩu") } }
            .onFailure { e -> _ui.update { it.copy(updating = false, message = e.message) } }
    }

    fun consumeMessage() {
        _ui.update { it.copy(message = null) }
    }

    private fun generateChartData(p: Profile?): List<Float> {
        if (p?.nutrition != null && p.nutrition.caloriesTarget > 0) {
            val target = p.nutrition.caloriesTarget
            return List(7) { index ->
                val seed = (p.user.id.hashCode() + index).toLong()
                val randomFactor = Random(seed).nextFloat() * 0.4f - 0.2f
                target + (target * randomFactor)
            }
        }
        return listOf(1500f, 1800f, 1600f, 1900f, 1700f, 2000f, 1800f)
    }
}