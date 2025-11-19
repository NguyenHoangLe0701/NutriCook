package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
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

    init {
        // Lắng nghe realtime; distinctUntilChanged để tránh render lại khi data không đổi
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

        // Kéo 1 lần để chắc chắn/tạo doc nếu chưa có
        viewModelScope.launch {
            if (_ui.value.profile == null) refreshOnce()
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

    /**
     * Hàm helper: Tạo dữ liệu biểu đồ lịch sử Calo (7 ngày)
     * Dựa trên Calories Target và biến thiên ngẫu nhiên cố định (Deterministic Random)
     */
    private fun generateChartData(p: Profile?): List<Float> {
        if (p?.nutrition != null && p.nutrition.caloriesTarget > 0) {
            val target = p.nutrition.caloriesTarget
            // Tạo 7 điểm dữ liệu
            return List(7) { index ->
                // Dùng ID user + index làm seed để biểu đồ không bị nhảy lung tung mỗi lần load
                val seed = (p.user.id.hashCode() + index).toLong()
                // Random biến thiên từ -20% đến +20% quanh target
                val randomFactor = Random(seed).nextFloat() * 0.4f - 0.2f
                target + (target * randomFactor)
            }
        }
        // Dữ liệu mặc định nếu chưa có nutrition
        return listOf(1500f, 1800f, 1600f, 1900f, 1700f, 2000f, 1800f)
    }

    /** Dùng ở các màn chỉnh profile đơn giản (không phải settings) */
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
                // Nếu backend có stream, onEach sẽ bắn về; vẫn refresh để chắc đồng bộ
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

    /** Đổi ảnh đại diện */
    fun updateAvatar(localUri: String) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching { repo.updateAvatar(localUri) }
            .onSuccess { url ->
                // Cập nhật lạc quan để UI phản hồi tức thì
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
                // Đồng bộ lại với server
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

    /** Follow/Unfollow */
    fun setFollow(targetUid: String, follow: Boolean) = viewModelScope.launch {
        runCatching { repo.setFollow(targetUid, follow) }
            .onFailure { e ->
                _ui.update {
                    it.copy(message = e.message ?: "Lỗi thao tác theo dõi")
                }
            }
    }

    /** Kiểm tra trạng thái follow */
    suspend fun isFollowing(targetUid: String): Boolean =
        runCatching { repo.isFollowing(targetUid) }.getOrElse { false }

    /** Đổi mật khẩu */
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