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

    /**
     * [MỚI THÊM TÙY CHỌN] Cần liên kết tài khoản:
     * Xảy ra khi đăng nhập bằng phương thức mới (vd: Facebook),
     * nhưng email đã tồn tại với phương thức khác (vd: Email/Password).
     * credential này cần được lưu để thực hiện bước link tài khoản tiếp theo.
     */
    data class LinkRequired(val credential: com.google.firebase.auth.AuthCredential) : AuthResult()
}