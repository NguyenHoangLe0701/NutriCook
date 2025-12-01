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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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
        // Lắng nghe trạng thái đăng nhập (Session)
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
            is AuthEvent.SubmitRegister -> signUpEmailPassword(event.fullName)

            is AuthEvent.SubmitForgotPassword -> forgotPassword(event.email)
            is AuthEvent.SubmitResetNewPassword -> resetNewPassword(event.oobCode, event.newPass)

            is AuthEvent.SendPhoneOtp -> sendPhoneOtp(event.activity, event.phoneNumber)
            is AuthEvent.VerifyPhoneOtp -> verifyPhoneOtp(event.code)
            AuthEvent.ResendEmailVerification -> resendEmailVerification()

            is AuthEvent.GoogleIdToken -> signInWithGoogle(event.idToken)
            AuthEvent.ConsumeMessage -> _uiState.update { it.copy(message = null) }

            // 👇 LOGIC MỚI: Reset cờ isAuthSuccess/isRegisterSuccess sau khi điều hướng
            AuthEvent.ConsumeAuthSuccess -> _uiState.update { it.copy(isAuthSuccess = false, isRegisterSuccess = false) }

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
            // [LOGIC MỚI] Kiểm tra Email Verified ngay sau khi đăng nhập thành công
            val isVerified = verificationRepo.checkEmailVerified()
            if (isVerified) {
                _uiState.update { it.copy(isLoading = false, message = "Đăng nhập thành công", isAuthSuccess = true) }
            } else {
                // Nếu chưa xác thực -> Đăng xuất ngay lập tức
                sessionRepo.signOut()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "Email chưa được xác thực. Vui lòng kiểm tra hộp thư!",
                        isAuthSuccess = false // Chặn không cho vào Home
                    )
                }
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Đăng nhập thất bại") }
        }
    }

    // Hàm đăng ký đã cập nhật logic lưu tên và verify email
    private fun signUpEmailPassword(fullName: String) = viewModelScope.launch {
        val email = _uiState.value.email.trim()
        val pass = _uiState.value.password
        val confirmPass = _uiState.value.confirmPassword

        if (fullName.isBlank()) {
            _uiState.update { it.copy(message = "Vui lòng nhập họ tên") }
            return@launch
        }
        if (email.isEmpty() || pass.length < 6) {
            _uiState.update { it.copy(message = "Mật khẩu >= 6 ký tự & email hợp lệ") }
            return@launch
        }
        if (pass != confirmPass) {
            _uiState.update { it.copy(message = "Mật khẩu không khớp") }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true, message = null) }

        // 1. Gọi Repo tạo tài khoản
        val result = registerRepo.signUp(email, pass)

        result.onSuccess {
            // 2. Cập nhật Display Name lên Firebase ngay lập tức
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build()
                user?.updateProfile(profileUpdates)
            } catch (e: Exception) {
                // Log lỗi cập nhật tên nếu cần
            }

            // 3. Báo thành công về UI -> Chuyển sang màn hình Verify Email
            _uiState.update {
                it.copy(
                    isLoading = false,
                    message = "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt.",
                    isRegisterSuccess = true, // Báo UI chuyển màn hình
                    isAuthSuccess = false     // Không vào Home ngay
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Đăng ký thất bại") }
        }
    }

    // 👇 HÀM FORGOT PASSWORD MỚI: Chỉ gửi email và set cờ chuyển màn hình
    private fun forgotPassword(email: String) = viewModelScope.launch {
        if (email.isBlank()) {
            _uiState.update { it.copy(message = "Vui lòng nhập email") }
            return@launch
        }

        _uiState.update { it.copy(isLoading = true, message = null) }

        val result = forgotPasswordRepo.sendPasswordResetEmail(email)
        result.onSuccess {
            // 👇 QUAN TRỌNG: Dùng isAuthSuccess để trigger chuyển màn hình sang Manual Reset
            _uiState.update {
                it.copy(
                    isLoading = false,
                    // Message này được ForgotPasswordScreen dùng để phân biệt sự kiện thành công
                    message = "Đã gửi email khôi phục. Vui lòng kiểm tra hộp thư.",
                    isAuthSuccess = true
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Gửi email thất bại") }
        }
    }

    // 👇 HÀM RESET NEW PASSWORD MỚI: Xử lý nhập mã thủ công và set cờ chuyển Login
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
                    message = "Đổi mật khẩu thành công!", // ManualResetCodeScreen sẽ đọc message này
                    isAuthSuccess = true // <-- Trigger ManualResetCodeScreen chuyển về Login
                )
            }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, message = e.message ?: "Đổi mật khẩu thất bại") }
        }
    }

    // Hàm tự động kiểm tra trạng thái verify (được gọi từ VerifyEmailScreen)
    fun startEmailVerificationCheck() = viewModelScope.launch {
        while (isActive) {
            val isVerified = verificationRepo.checkEmailVerified()

            if (isVerified) {
                _uiState.update { it.copy(isEmailVerified = true, message = "Xác thực thành công!") }
                break
            }

            delay(3000) // Chờ 3 giây rồi check lại
        }
    }

    private fun sendPhoneOtp(activity: Activity, phoneNumber: String) {
        _uiState.update { it.copy(isLoading = true, message = null) }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
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

    fun signOut() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        sessionRepo.signOut()
        _uiState.update {
            AuthState(message = "Đã đăng xuất")
        }
    }
}