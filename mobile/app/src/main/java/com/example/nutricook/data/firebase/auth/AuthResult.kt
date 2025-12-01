package com.example.nutricook.data.firebase.auth

// Kết quả auth đơn giản để dùng ở UI
sealed class AuthResult {
    // Đăng ký, Đăng nhập, Link tài khoản thành công
    data object Success : AuthResult()

    // Có lỗi xảy ra
    data class Error(val message: String) : AuthResult()

    // [MỚI] Đã gửi mã OTP thành công (Dùng cho bước verify SĐT)
    // verificationId cần được lưu lại để dùng cho bước xác thực sau đó
    data class CodeSent(val verificationId: String) : AuthResult()
}