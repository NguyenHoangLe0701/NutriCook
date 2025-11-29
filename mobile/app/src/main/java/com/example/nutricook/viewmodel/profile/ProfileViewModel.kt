package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.newsfeed.Post // Import đúng
import com.example.nutricook.model.profile.Profile
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
    private val repo: ProfileRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _ui.asStateFlow()

    private val _savedPosts = MutableStateFlow<List<Post>>(emptyList())
    val savedPosts: StateFlow<List<Post>> = _savedPosts.asStateFlow()

    init {
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

        viewModelScope.launch {
            if (_ui.value.profile == null) refreshOnce()
        }
    }

    fun loadSavedPosts() = viewModelScope.launch {
        val uid = _ui.value.profile?.user?.id ?: return@launch
        runCatching {
            repo.getUserSaves(uid)
        }
            .onSuccess { pagedResult ->
                _savedPosts.value = pagedResult.items
            }
            .onFailure { e ->
                e.printStackTrace()
            }
    }

    fun refreshOnce() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        runCatching { repo.getMyProfile() }
            .onSuccess { p ->
                val chartData = generateChartData(p)
                _ui.update {
                    it.copy(
                        loading = false,
                        profile = p,
                        chartData = chartData
                    )
                }
            }
            .onFailure { e ->
                _ui.update {
                    it.copy(
                        loading = false,
                        message = e.message ?: "Không tải được hồ sơ"
                    )
                }
            }
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

    fun updateProfile(fullName: String?, bio: String?) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching {
            repo.updateProfile(
                fullName = fullName,
                email = null,
                dayOfBirth = null,
                gender = null,
                bio = bio
            )
        }
            .onSuccess {
                refreshOnce()
                _ui.update { it.copy(updating = false, message = "Đã lưu hồ sơ") }
            }
            .onFailure { e ->
                _ui.update {
                    it.copy(
                        updating = false,
                        message = e.message ?: "Không thể lưu hồ sơ"
                    )
                }
            }
    }

    fun updateAvatar(localUri: String) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching { repo.updateAvatar(localUri) }
            .onSuccess { url ->
                _ui.update { st ->
                    val newProfile = st.profile?.copy(
                        user = st.profile.user.copy(avatarUrl = url)
                    )
                    st.copy(
                        updating = false,
                        profile = newProfile ?: st.profile,
                        message = "Đã cập nhật ảnh đại diện"
                    )
                }
                refreshOnce()
            }
            .onFailure { e ->
                _ui.update {
                    it.copy(
                        updating = false,
                        message = e.message ?: "Không thể cập nhật ảnh"
                    )
                }
            }
    }

    fun setFollow(targetUid: String, follow: Boolean) = viewModelScope.launch {
        runCatching { repo.setFollow(targetUid, follow) }
            .onFailure { e ->
                _ui.update {
                    it.copy(message = e.message ?: "Lỗi thao tác theo dõi")
                }
            }
    }

    suspend fun isFollowing(targetUid: String): Boolean =
        runCatching { repo.isFollowing(targetUid) }.getOrElse { false }

    fun changePassword(oldPassword: String, newPassword: String) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching { repo.changePassword(oldPassword, newPassword) }
            .onSuccess {
                _ui.update { it.copy(updating = false, message = "Đã đổi mật khẩu") }
            }
            .onFailure { e ->
                _ui.update {
                    it.copy(
                        updating = false,
                        message = e.message ?: "Đổi mật khẩu thất bại"
                    )
                }
            }
    }

    fun consumeMessage() {
        _ui.update { it.copy(message = null) }
    }
}