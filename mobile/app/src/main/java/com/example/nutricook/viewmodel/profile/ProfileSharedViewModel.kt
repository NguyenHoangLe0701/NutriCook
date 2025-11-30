package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import com.example.nutricook.model.profile.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSharedUiState(
    val loading: Boolean = true,
    val message: String? = null,
    val myProfile: Profile? = null,

    // Các field binding lên SettingsScreen
    val fullName: String = "",
    val email: String = "",
    val dayOfBirth: String = "",
    val gender: String = "Male",
    val bio: String = "", // [MỚI] Thêm Bio để chỉnh sửa trong Settings

    val saving: Boolean = false
)

sealed interface ProfileSharedEvent {
    data object Refresh : ProfileSharedEvent
    data object SaveSettings : ProfileSharedEvent

    data class FullNameChanged(val v: String) : ProfileSharedEvent
    data class EmailChanged(val v: String) : ProfileSharedEvent
    data class DobChanged(val v: String) : ProfileSharedEvent
    data class GenderChanged(val v: String) : ProfileSharedEvent
    data class BioChanged(val v: String) : ProfileSharedEvent // [MỚI] Sự kiện sửa Bio

    // Đổi mật khẩu
    data class ChangePassword(val old: String, val new: String) : ProfileSharedEvent

    // Đổi avatar từ Settings
    data class UpdateAvatar(val localUri: String) : ProfileSharedEvent

    data object Consume : ProfileSharedEvent
}

@HiltViewModel
class ProfileSharedViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileSharedUiState())
    val uiState = _ui.asStateFlow()

    init {
        onEvent(ProfileSharedEvent.Refresh)
    }

    fun onEvent(e: ProfileSharedEvent) {
        when (e) {
            ProfileSharedEvent.Refresh -> viewModelScope.launch {
                _ui.value = _ui.value.copy(loading = true, message = null)
                runCatching { repo.getMyProfile() }
                    .onSuccess { p ->
                        updateStateFromProfile(p)
                    }
                    .onFailure { ex ->
                        _ui.value = _ui.value.copy(
                            loading = false,
                            message = ex.message ?: "Lỗi tải hồ sơ"
                        )
                    }
            }

            ProfileSharedEvent.SaveSettings -> viewModelScope.launch {
                _ui.value = _ui.value.copy(saving = true, message = null)
                val s = _ui.value
                runCatching {
                    // [CHỈNH SỬA] Bỏ tham số email (vì Repo mới đã bỏ), thêm bio
                    repo.updateProfile(
                        fullName = s.fullName,
                        dayOfBirth = s.dayOfBirth.ifBlank { null },
                        gender = s.gender.ifBlank { null },
                        bio = s.bio
                    )
                }
                    .onSuccess { updatedProfile ->
                        // Repo trả về Profile mới, update thẳng vào State luôn
                        updateStateFromProfile(updatedProfile)
                        _ui.value = _ui.value.copy(
                            saving = false,
                            message = "Đã lưu thay đổi"
                        )
                    }
                    .onFailure { ex ->
                        _ui.value = _ui.value.copy(
                            saving = false,
                            message = ex.message ?: "Không thể lưu"
                        )
                    }
            }

            is ProfileSharedEvent.FullNameChanged ->
                _ui.value = _ui.value.copy(fullName = e.v)

            is ProfileSharedEvent.EmailChanged ->
                _ui.value = _ui.value.copy(email = e.v)

            is ProfileSharedEvent.DobChanged ->
                _ui.value = _ui.value.copy(dayOfBirth = e.v)

            is ProfileSharedEvent.GenderChanged ->
                _ui.value = _ui.value.copy(gender = e.v)

            is ProfileSharedEvent.BioChanged ->
                _ui.value = _ui.value.copy(bio = e.v)

            is ProfileSharedEvent.ChangePassword -> viewModelScope.launch {
                _ui.value = _ui.value.copy(saving = true, message = null)
                runCatching { repo.changePassword(e.old, e.new) }
                    .onSuccess {
                        _ui.value = _ui.value.copy(
                            saving = false,
                            message = "Đổi mật khẩu thành công"
                        )
                    }
                    .onFailure { ex ->
                        _ui.value = _ui.value.copy(
                            saving = false,
                            message = ex.message ?: "Đổi mật khẩu thất bại"
                        )
                    }
            }

            // [QUAN TRỌNG] Xử lý đổi Avatar
            is ProfileSharedEvent.UpdateAvatar -> viewModelScope.launch {
                if (e.localUri.isEmpty()) return@launch

                _ui.value = _ui.value.copy(saving = true, message = null)
                runCatching { repo.updateAvatar(e.localUri) }
                    .onSuccess { newUrl ->
                        // Cập nhật URL mới vào State ngay lập tức
                        val currentP = _ui.value.myProfile
                        if (currentP != null) {
                            val newP = currentP.copy(user = currentP.user.copy(avatarUrl = newUrl))
                            _ui.value = _ui.value.copy(
                                saving = false,
                                myProfile = newP,
                                message = "Đã đổi ảnh đại diện"
                            )
                        } else {
                            // Nếu profile chưa có thì load lại
                            onEvent(ProfileSharedEvent.Refresh)
                        }
                    }
                    .onFailure { ex ->
                        _ui.value = _ui.value.copy(
                            saving = false,
                            message = ex.message ?: "Không thể đổi ảnh"
                        )
                    }
            }

            ProfileSharedEvent.Consume ->
                _ui.value = _ui.value.copy(message = null)
        }
    }

    // Helper function để đồng bộ Profile vào UI State
    private fun updateStateFromProfile(p: Profile) {
        _ui.value = _ui.value.copy(
            loading = false,
            myProfile = p,
            fullName = p.user.displayName ?: "",
            email = p.user.email,
            dayOfBirth = p.dayOfBirth.orEmpty(),
            gender = p.gender ?: "Male",
            bio = p.bio ?: "" // Đồng bộ Bio
        )
    }
}