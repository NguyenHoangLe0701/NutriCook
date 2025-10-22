package com.example.nutricook.viewmodel.auth

sealed class AuthEvent {
    data class EmailChanged(val value: String) : AuthEvent()
    data class PasswordChanged(val value: String) : AuthEvent()

    data object SubmitLogin : AuthEvent()
    data object SubmitRegister : AuthEvent()

    // Dùng để xoá/snackbar message sau khi hiển thị
    data object ConsumeMessage : AuthEvent()

    // Đăng xuất
    data object Logout : AuthEvent()

    // (Tuỳ chọn) Nếu có Google Sign-In:
    // data class GoogleIdToken(val token: String) : AuthEvent()
}
