package com.example.nutricook.data.firebase.auth

import com.example.nutricook.model.user.IUser
import com.example.nutricook.model.repository.auth.RegisterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRegisterRepository @Inject constructor(
    private val ds: FirebaseAuthDataSource
) : RegisterRepository {
    override suspend fun signUp(email: String, password: String): Result<IUser> =
        runCatching { ds.signUp(email, password).toDomain() }

    /**
     * [MỚI THÊM] Đăng ký hoặc Đăng nhập bằng Facebook.
     * Khi gọi hàm này với Access Token, Firebase sẽ tự động
     * tạo tài khoản mới nếu chưa tồn tại, hoặc đăng nhập nếu đã tồn tại.
     */
    suspend fun signUpWithFacebook(accessToken: String): Result<IUser> =
        runCatching { ds.signInWithFacebook(accessToken).toDomain() }
}