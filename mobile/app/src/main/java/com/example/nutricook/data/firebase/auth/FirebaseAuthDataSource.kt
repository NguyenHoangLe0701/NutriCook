package com.example.nutricook.data.firebase.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth
) {
    // Flow theo dõi trạng thái User (đăng nhập/đăng xuất)
    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa -> trySend(fa.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    // --- Đăng nhập / Đăng ký ---

    suspend fun signIn(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: error("User is null after signIn")
    }

    suspend fun signUp(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        // Tùy chọn: Gửi email xác thực ngay khi đăng ký thành công
        result.user?.sendEmailVerification()?.await()
        return result.user ?: error("User is null after signUp")
    }

    fun signOut() = auth.signOut()

    // --- Chức năng: Quên mật khẩu ---

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    // Xác nhận đổi mật khẩu mới từ mã code (Deep Link)
    suspend fun confirmPasswordReset(code: String, newPass: String) {
        auth.confirmPasswordReset(code, newPass).await()
    }

    // --- Chức năng: Xác thực Email ---

    suspend fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    // Reload user để cập nhật trạng thái isEmailVerified
    suspend fun reloadUser() {
        auth.currentUser?.reload()?.await()
    }

    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    // --- Chức năng: Xác thực Số điện thoại (Phone Auth) ---

    // Gửi mã OTP
    // Lưu ý: Cần truyền Activity để Firebase hiển thị reCAPTCHA
    fun sendPhoneVerification(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // SĐT cần verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout
            .setActivity(activity)             // Activity bắt buộc
            .setCallbacks(callbacks)           // Callback trả về kết quả
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Liên kết SĐT vào tài khoản hiện tại sau khi có mã OTP (credential)
    suspend fun linkPhoneCredential(credential: PhoneAuthCredential): FirebaseUser {
        val user = auth.currentUser ?: error("No user logged in to link phone")
        val result = user.linkWithCredential(credential).await()
        return result.user ?: error("User is null after linking")
    }
}