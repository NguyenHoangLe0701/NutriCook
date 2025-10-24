package com.example.nutricook.model.repository.auth

import com.example.nutricook.model.user.IUser

interface LoginRepository {
    suspend fun signIn(email: String, password: String): Result<IUser>
}
