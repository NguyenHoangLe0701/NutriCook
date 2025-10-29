package com.example.nutricook.data.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

/**
 * Wrapper đơn giản cho FirebaseAuth để UI gọi trực tiếp.
 * (Nếu theo MVVM chuẩn thì nên gọi qua Repository + ViewModel)
 */
object AuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            AuthResult.Success
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("Email không tồn tại.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Sai email hoặc mật khẩu.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Đăng nhập thất bại.")
        }
    }

    suspend fun register(email: String, password: String): AuthResult {
        return try {
            auth.createUserWithEmailAndPassword(email.trim(), password).await()
            // Có thể gửi email verify nếu muốn:
            // auth.currentUser?.sendEmailVerification()?.await()
            AuthResult.Success
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("Email đã được đăng ký.")
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Error("Mật khẩu quá yếu (>= 6 ký tự).")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Email không hợp lệ.")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Đăng ký thất bại.")
        }
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        auth.signOut()
    }

    val currentUserId: String? get() = auth.currentUser?.uid
    val currentEmail: String? get() = auth.currentUser?.email
}
