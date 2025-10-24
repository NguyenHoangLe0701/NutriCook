package com.example.nutricook.viewmodel.auth

import com.example.nutricook.model.user.IUser

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val currentUser: IUser? = null
)