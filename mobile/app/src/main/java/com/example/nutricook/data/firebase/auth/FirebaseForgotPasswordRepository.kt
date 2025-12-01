package com.example.nutricook.data.firebase.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseForgotPasswordRepository @Inject constructor(
    private val ds: FirebaseAuthDataSource
) {
    // Gửi email reset (Màn hình 1)
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        runCatching { ds.sendPasswordResetEmail(email) }

    // Đổi mật khẩu mới từ mã code (Màn hình 2)
    suspend fun confirmPasswordReset(code: String, newPass: String): Result<Unit> =
        runCatching { ds.confirmPasswordReset(code, newPass) }
}