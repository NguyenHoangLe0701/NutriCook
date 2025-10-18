package com.example.nutricook.data.auth

import kotlinx.coroutines.delay

class AuthManager {
    companion object {
        private const val ADMIN_EMAIL = "admin@nutricook.com"
        private const val ADMIN_PASSWORD = "12345"
        
        // Simulate user database
        private val users = mutableMapOf<String, String>()
        
        init {
            // Add admin user
            users[ADMIN_EMAIL] = ADMIN_PASSWORD
        }
        
        suspend fun login(email: String, password: String): AuthResult {
            delay(1000) // Simulate network delay
            
            return when {
                email.isEmpty() || password.isEmpty() -> AuthResult.Error("Vui lòng nhập đầy đủ thông tin")
                email == ADMIN_EMAIL && password == ADMIN_PASSWORD -> AuthResult.Success("admin")
                users[email] == password -> AuthResult.Success("user")
                else -> AuthResult.Error("Email hoặc mật khẩu không đúng")
            }
        }
        
        suspend fun register(fullName: String, email: String, password: String, confirmPassword: String): AuthResult {
            delay(1000) // Simulate network delay
            
            return when {
                fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> 
                    AuthResult.Error("Vui lòng nhập đầy đủ thông tin")
                password != confirmPassword -> 
                    AuthResult.Error("Mật khẩu xác nhận không khớp")
                users.containsKey(email) -> 
                    AuthResult.Error("Email này đã được sử dụng")
                password.length < 6 -> 
                    AuthResult.Error("Mật khẩu phải có ít nhất 6 ký tự")
                else -> {
                    users[email] = password
                    AuthResult.Success("user")
                }
            }
        }
    }
}

sealed class AuthResult {
    data class Success(val userType: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
