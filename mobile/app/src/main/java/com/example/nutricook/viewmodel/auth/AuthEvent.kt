package com.example.nutricook.viewmodel.auth

sealed class AuthEvent {
    data class EmailChanged(val value: String) : AuthEvent()
    data class PasswordChanged(val value: String) : AuthEvent()

    data object SubmitLogin : AuthEvent()
    data object SubmitRegister : AuthEvent()

    // Google only
    data class GoogleIdToken(val idToken: String) : AuthEvent()

    data object ConsumeMessage : AuthEvent()
    data object Logout : AuthEvent()
}

