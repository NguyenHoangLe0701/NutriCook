package com.example.nutricook.data.firebase.auth

// Kết quả auth đơn giản để dùng ở UI
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}
