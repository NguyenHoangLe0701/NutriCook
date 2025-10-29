package com.example.nutricook.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.profile.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val uiState = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.myProfileFlow().collect { p ->
                _ui.update { it.copy(loading = false, profile = p) }
            }
        }
        refreshOnce()
    }

    fun refreshOnce() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        runCatching { repo.getMyProfile() }
            .onSuccess { p -> _ui.update { it.copy(loading = false, profile = p) } }
            .onFailure { e -> _ui.update { it.copy(loading = false, message = e.message ?: "Không tải được hồ sơ") } }
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
        }.onSuccess {
            _ui.update { it.copy(updating = false, message = "Đã lưu hồ sơ") }
        }.onFailure { e ->
            _ui.update { it.copy(updating = false, message = e.message ?: "Không thể lưu hồ sơ") }
        }
    }

    fun updateAvatar(localUri: String) = viewModelScope.launch {
        _ui.update { it.copy(updating = true, message = null) }
        runCatching { repo.updateAvatar(localUri) }
            .onSuccess { _ui.update { it.copy(updating = false, message = "Đã cập nhật ảnh đại diện") } }
            .onFailure { e -> _ui.update { it.copy(updating = false, message = e.message ?: "Không thể cập nhật ảnh") } }
    }

    fun setFollow(targetUid: String, follow: Boolean) = viewModelScope.launch {
        runCatching { repo.setFollow(targetUid, follow) }
            .onFailure { e -> _ui.update { it.copy(message = e.message ?: "Lỗi thao tác theo dõi") } }
    }

    fun consumeMessage() = _ui.update { it.copy(message = null) }
}
