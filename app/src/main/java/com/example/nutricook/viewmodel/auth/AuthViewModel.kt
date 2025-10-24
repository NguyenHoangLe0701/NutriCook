package com.example.nutricook.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.model.repository.auth.LoginRepository
import com.example.nutricook.model.repository.auth.RegisterRepository
import com.example.nutricook.model.repository.auth.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginRepo: LoginRepository,
    private val registerRepo: RegisterRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepo.currentUser
                .distinctUntilChanged()
                .collect { user ->
                    _uiState.update { it.copy(currentUser = user, isLoading = false) }
                }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> _uiState.update { it.copy(email = event.value) }
            is AuthEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value) }
            AuthEvent.SubmitLogin -> signIn()
            AuthEvent.SubmitRegister -> signUp()
            AuthEvent.ConsumeMessage -> _uiState.update { it.copy(message = null) }
            AuthEvent.Logout -> signOut()
        }
    }

    private fun signIn() = viewModelScope.launch {
        val email = _uiState.value.email.trim()
        val pass = _uiState.value.password
        if (email.isEmpty() || pass.isEmpty()) {
            _uiState.update { it.copy(message = "Vui lòng nhập email & mật khẩu") }
            return@launch
        }
        _uiState.update { it.copy(isLoading = true, message = null) }
        val result = loginRepo.signIn(email, pass)
        result.onSuccess {
            _uiState.update { it.copy(isLoading = false, message = "Đăng nhập thành công") }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Đăng nhập thất bại") }
        }
    }

    private fun signUp() = viewModelScope.launch {
        val email = _uiState.value.email.trim()
        val pass = _uiState.value.password
        if (email.isEmpty() || pass.length < 6) {
            _uiState.update { it.copy(message = "Mật khẩu >= 6 ký tự & email hợp lệ") }
            return@launch
        }
        _uiState.update { it.copy(isLoading = true, message = null) }
        val result = registerRepo.signUp(email, pass)
        result.onSuccess {
            _uiState.update { it.copy(isLoading = false, message = "Đăng ký thành công") }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Đăng ký thất bại") }
        }
    }

    private fun signOut() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        sessionRepo.signOut()
        _uiState.update {
            AuthState(
                message = "Đã đăng xuất",
                currentUser = null // Đảm bảo currentUser là null
            )
        }
    }
}