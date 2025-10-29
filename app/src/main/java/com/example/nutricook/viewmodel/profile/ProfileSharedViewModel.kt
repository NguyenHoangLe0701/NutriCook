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
    val fullName: String = "",
    val email: String = "",
    val dayOfBirth: String = "",
    val gender: String = "Male",
    val saving: Boolean = false
)

sealed interface ProfileSharedEvent {
    data object Refresh : ProfileSharedEvent
    data object SaveSettings : ProfileSharedEvent
    data class FullNameChanged(val v: String): ProfileSharedEvent
    data class EmailChanged(val v: String): ProfileSharedEvent
    data class DobChanged(val v: String): ProfileSharedEvent
    data class GenderChanged(val v: String): ProfileSharedEvent
    data class ChangePassword(val old: String, val new: String): ProfileSharedEvent
    data object Consume : ProfileSharedEvent
}

@HiltViewModel
class ProfileSharedViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileSharedUiState())
    val uiState = _ui.asStateFlow()

    init { onEvent(ProfileSharedEvent.Refresh) }

    fun onEvent(e: ProfileSharedEvent) {
        when (e) {
            ProfileSharedEvent.Refresh -> viewModelScope.launch {
                _ui.value = _ui.value.copy(loading = true, message = null)
                try {
                    val p = repo.getMyProfile()
                    _ui.value = _ui.value.copy(
                        loading = false,
                        myProfile = p,
                        fullName = p.user.displayName?.ifBlank { p.user.email.substringBefore("@") }
                            ?: p.user.email.substringBefore("@"),
                        email = p.user.email,
                        dayOfBirth = p.dayOfBirth.orEmpty(),
                        gender = p.gender ?: "Male"
                    )
                } catch (ex: Exception) {
                    _ui.value = _ui.value.copy(loading = false, message = ex.message ?: "Lỗi tải hồ sơ")
                }
            }

            ProfileSharedEvent.SaveSettings -> viewModelScope.launch {
                _ui.value = _ui.value.copy(saving = true, message = null)
                val s = _ui.value
                try {
                    repo.updateProfile(
                        fullName = s.fullName,
                        email = s.email,
                        dayOfBirth = s.dayOfBirth.ifBlank { null },
                        gender = s.gender.ifBlank { null },
                        bio = s.myProfile?.bio // giữ bio hiện tại (hoặc UI cho phép sửa thì truyền từ state)
                    )
                    _ui.value = _ui.value.copy(saving = false, message = "Đã lưu thay đổi")
                    onEvent(ProfileSharedEvent.Refresh)
                } catch (ex: Exception) {
                    _ui.value = _ui.value.copy(saving = false, message = ex.message ?: "Không thể lưu")
                }
            }

            is ProfileSharedEvent.FullNameChanged -> _ui.value = _ui.value.copy(fullName = e.v)
            is ProfileSharedEvent.EmailChanged    -> _ui.value = _ui.value.copy(email = e.v)
            is ProfileSharedEvent.DobChanged      -> _ui.value = _ui.value.copy(dayOfBirth = e.v)
            is ProfileSharedEvent.GenderChanged   -> _ui.value = _ui.value.copy(gender = e.v)

            is ProfileSharedEvent.ChangePassword -> viewModelScope.launch {
                _ui.value = _ui.value.copy(saving = true, message = null)
                try {
                    repo.changePassword(e.old, e.new)
                    _ui.value = _ui.value.copy(saving = false, message = "Đổi mật khẩu thành công")
                } catch (ex: Exception) {
                    _ui.value = _ui.value.copy(saving = false, message = ex.message ?: "Đổi mật khẩu thất bại")
                }
            }

            ProfileSharedEvent.Consume -> _ui.value = _ui.value.copy(message = null)
        }
    }
}
