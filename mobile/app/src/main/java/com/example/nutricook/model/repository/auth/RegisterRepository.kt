package com.example.nutricook.model.repository.auth

import com.example.nutricook.model.user.IUser

interface RegisterRepository {
    suspend fun signUp(email: String, password: String): Result<IUser>
}
