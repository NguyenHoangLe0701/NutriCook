package com.example.nutricook.viewmodel.auth

import com.example.nutricook.model.user.IUser

data class AuthState(
    // --- Dữ liệu nhập liệu (Input) ---
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "", // Dùng cho Đăng ký hoặc Đặt mật khẩu mới

    // --- Trạng thái UI (Loading/Error) ---
    val isLoading: Boolean = false,
    val message: String? = null, // Thông báo lỗi hoặc thành công (để hiện Toast/Snackbar)

    // --- Trạng thái Logic ---
    val currentUser: IUser? = null,     // User hiện tại (nếu đã login)
    val isAuthSuccess: Boolean = false, // Cờ báo hiệu login/register thành công -> Chuyển màn hình

    // --- Trạng thái Phone Auth (Xác thực SĐT) ---
    val isOtpSent: Boolean = false,     // True = đã gửi SMS thành công -> Hiển thị ô nhập OTP
    val verificationId: String? = null  // ID định danh phiên gửi SMS (Firebase trả về)
)