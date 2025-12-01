package com.example.nutricook.viewmodel.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.firebase.auth.FirebaseForgotPasswordRepository
import com.example.nutricook.data.firebase.auth.FirebaseVerificationRepository
import com.example.nutricook.model.repository.auth.LoginRepository
import com.example.nutricook.model.repository.auth.RegisterRepository
import com.example.nutricook.model.repository.auth.SessionRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginRepo: LoginRepository,
    private val registerRepo: RegisterRepository,
    private val sessionRepo: SessionRepository,
    private val forgotPasswordRepo: FirebaseForgotPasswordRepository,
    private val verificationRepo: FirebaseVerificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepo.currentUser
                .distinctUntilChanged()
                .collect { user ->
                    _uiState.update {
                        it.copy(
                            currentUser = user,
                            isLoading = false,
                            isAuthSuccess = user != null
                        )
                    }
                }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> _uiState.update { it.copy(email = event.value) }
            is AuthEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value) }
            is AuthEvent.ConfirmPasswordChanged -> _uiState.update { it.copy(confirmPassword = event.value) }

            AuthEvent.SubmitLogin -> signInEmailPassword()
            AuthEvent.SubmitRegister -> signUpEmailPassword()

            is AuthEvent.SubmitForgotPassword -> forgotPassword(event.email)
            is AuthEvent.SubmitResetNewPassword -> resetNewPassword(event.oobCode, event.newPass)

            is AuthEvent.SendPhoneOtp -> sendPhoneOtp(event.activity, event.phoneNumber)
            is AuthEvent.VerifyPhoneOtp -> verifyPhoneOtp(event.code)
            AuthEvent.ResendEmailVerification -> resendEmailVerification()

            is AuthEvent.GoogleIdToken -> signInWithGoogle(event.idToken)
            AuthEvent.ConsumeMessage -> _uiState.update { it.copy(message = null) }
            AuthEvent.Logout -> signOut()
        }
    }

    // ======================== LOGIC XỬ LÝ ========================

    private fun signInEmailPassword() = viewModelScope.launch {
        val email = _uiState.value.email.trim()
        val pass = _uiState.value.password

        if (email.isEmpty() || pass.isEmpty()) {
            _uiState.update { it.copy(message = "Vui lòng nhập email & mật khẩu") }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true, message = null) }

        val result = loginRepo.signIn(email, pass)
        result.onSuccess {
            _uiState.update { it.copy(isLoading = false, message = "Đăng nhập thành công", isAuthSuccess = true) }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Đăng nhập thất bại") }
        }
    }

    private fun signUpEmailPassword() = viewModelScope.launch {
        val email = _uiState.value.email.trim()
        val pass = _uiState.value.password
        val confirmPass = _uiState.value.confirmPassword

        if (email.isEmpty() || pass.length < 6) {
            _uiState.update { it.copy(message = "Mật khẩu >= 6 ký tự & email hợp lệ") }
            return@launch
        }
        if (pass != confirmPass) {
            _uiState.update { it.copy(message = "Mật khẩu xác nhận không khớp") }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true, message = null) }

        val result = registerRepo.signUp(email, pass)
        result.onSuccess {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = "Đăng ký thành công! Đã gửi email xác thực.",
                    isAuthSuccess = true
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Đăng ký thất bại") }
        }
    }

    private fun forgotPassword(email: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _uiState.update { it.copy(message = "Vui lòng nhập email") }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true, message = null) }

        val result = forgotPasswordRepo.sendPasswordResetEmail(email)
        result.onSuccess {
            _uiState.update { it.copy(isLoading = false, message = "Đã gửi email khôi phục. Vui lòng kiểm tra hộp thư.") }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Gửi email thất bại") }
        }
    }

    private fun resetNewPassword(oobCode: String, newPass: String) = viewModelScope.launch {
        val confirmPass = _uiState.value.confirmPassword

        if (newPass.length < 6) {
            _uiState.update { it.copy(message = "Mật khẩu quá yếu") }
            return@launch
        }
        if (newPass != confirmPass) {
            _uiState.update { it.copy(message = "Mật khẩu không khớp") }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true, message = null) }

        val result = forgotPasswordRepo.confirmPasswordReset(oobCode, newPass)
        result.onSuccess {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.",
                    isAuthSuccess = true
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Đổi mật khẩu thất bại") }
        }
    }

    private fun sendPhoneOtp(activity: Activity, phoneNumber: String) {
        _uiState.update { it.copy(isLoading = true, message = null) }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    // [ĐÃ SỬA] Gọi linkPhoneCredential từ repo
                    verificationRepo.linkPhoneCredential(credential)
                    _uiState.update { it.copy(isLoading = false, message = "Tự động xác thực thành công!") }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _uiState.update { it.copy(isLoading = false, message = e.message ?: "Gửi SMS thất bại") }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpSent = true,
                        verificationId = verificationId,
                        message = "Đã gửi mã OTP"
                    )
                }
            }
        }
        verificationRepo.sendPhoneVerification(activity, phoneNumber, callbacks)
    }

    private fun verifyPhoneOtp(code: String) = viewModelScope.launch {
        val verId = _uiState.value.verificationId
        if (verId == null) {
            _uiState.update { it.copy(message = "Lỗi: Mất phiên xác thực.") }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true, message = null) }

        val result = verificationRepo.verifyAndLinkPhone(verId, code)
        result.onSuccess {
            _uiState.update { it.copy(isLoading = false, message = "Liên kết SĐT thành công!") }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = "Mã OTP không đúng hoặc lỗi hệ thống") }
        }
    }

    private fun resendEmailVerification() = viewModelScope.launch {
        val result = verificationRepo.resendEmailVerification()
        result.onSuccess {
            _uiState.update { it.copy(message = "Đã gửi lại email xác thực") }
        }.onFailure { e ->
            _uiState.update { it.copy(message = e.message ?: "Gửi lại thất bại") }
        }
    }

    private fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, message = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        Firebase.auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Đăng nhập Google thành công",
                        isAuthSuccess = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = task.exception?.message ?: "Đăng nhập Google thất bại"
                    )
                }
            }
        }
    }

    // [ĐÃ SỬA] Đổi từ private -> public để NavGraph gọi được
    fun signOut() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        sessionRepo.signOut()
        _uiState.update {
            AuthState(message = "Đã đăng xuất")
        }
    }
}