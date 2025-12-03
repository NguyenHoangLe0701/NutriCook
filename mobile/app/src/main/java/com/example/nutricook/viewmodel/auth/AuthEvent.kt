package com.example.nutricook.viewmodel.auth

import android.app.Activity

sealed class AuthEvent {
    // --- Các sự kiện thay đổi dữ liệu nhập liệu (Input) ---
    data class EmailChanged(val value: String) : AuthEvent()
    data class PasswordChanged(val value: String) : AuthEvent()

    // Nhập lại mật khẩu (Dùng cho Đăng ký & Đổi mật khẩu mới)
    data class ConfirmPasswordChanged(val value: String) : AuthEvent()

    // --- Các hành động Submit (Nút bấm) ---
    data object SubmitLogin : AuthEvent()

    // ĐÃ SỬA: Thêm tham số fullName để truyền tên người dùng vào ViewModel
    data class SubmitRegister(val fullName: String) : AuthEvent()

    // --- Chức năng Quên Mật Khẩu ---
    // Màn hình 1: Gửi yêu cầu reset qua email
    data class SubmitForgotPassword(val email: String) : AuthEvent()

    // Màn hình 2: Đặt lại mật khẩu mới (cần mã oobCode từ email và mật khẩu mới user nhập)
    data class SubmitResetNewPassword(val oobCode: String, val newPass: String) : AuthEvent()

    // --- Chức năng Xác thực (Phone & Email) ---
    // Gửi OTP (Cần Activity để Firebase hiển thị Captcha)
    data class SendPhoneOtp(val activity: Activity, val phoneNumber: String) : AuthEvent()

    // Xác thực mã OTP người dùng nhập vào
    data class VerifyPhoneOtp(val code: String) : AuthEvent()

    // Gửi lại email xác thực
    data object ResendEmailVerification : AuthEvent()

    // --- Các sự kiện khác (Đăng nhập bên thứ ba) ---

    // Google Login
    data class GoogleIdToken(val idToken: String) : AuthEvent()

    /**
     * [MỚI THÊM] Facebook Login:
     * Cần truyền Access Token nhận được từ Facebook SDK ở tầng UI.
     */
    data class FacebookAccessToken(val accessToken: String) : AuthEvent()

    // Xóa thông báo lỗi sau khi đã hiển thị (Toast/Snackbar)
    data object ConsumeMessage : AuthEvent()

    // 👇 THÊM: Xóa trạng thái isAuthSuccess sau khi đã điều hướng
    data object ConsumeAuthSuccess : AuthEvent()

    // Đăng xuất
    data object Logout : AuthEvent()
}