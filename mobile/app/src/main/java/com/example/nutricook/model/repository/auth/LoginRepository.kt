package com.example.nutricook.model.repository.auth

import com.example.nutricook.model.user.IUser

interface LoginRepository {
    suspend fun signIn(email: String, password: String): Result<IUser>

    // 👇 Thêm dòng này vào để sửa lỗi "overrides nothing"
    suspend fun signInWithFacebook(accessToken: String): Result<IUser>
}