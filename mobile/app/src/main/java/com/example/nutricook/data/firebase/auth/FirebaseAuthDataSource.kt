package com.example.nutricook.data.firebase.auth

import android.app.Activity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FacebookAuthProvider // Import mới cần thiết cho Facebook Sign-in
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

    /**
     * [MỚI THÊM] Đăng nhập Firebase bằng Facebook Access Token.
     */
    suspend fun signInWithFacebook(accessToken: String): FirebaseUser {
        // 1. Tạo Firebase Credential từ Facebook Access Token
        val credential = FacebookAuthProvider.getCredential(accessToken)

        // 2. Đăng nhập vào Firebase bằng Credential
        val result = auth.signInWithCredential(credential).await()

        return result.user ?: error("User is null after Facebook sign-in")
    }

    fun signOut() = auth.signOut()

    // --- Chức năng: Quên mật khẩu (CÓ SỬA ĐỔI) ---

    suspend fun sendPasswordResetEmail(email: String) {
        // Cấu hình để khi bấm link sẽ mở App thay vì mở Web
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            // URL này phải là link Hosting của Firebase hoặc Dynamic Link
            // Khi bấm vào, nó sẽ chuyển tiếp kèm theo oobCode
            .setUrl("https://nutricook-fff8f.firebaseapp.com/reset_password")
            .setHandleCodeInApp(true) // Quan trọng: Báo Firebase là xử lý trong App
            .setAndroidPackageName(
                "com.example.nutricook", // Package name của app bạn
                true, // installIfNotAvailable
                "1"   // minimumVersion
            )
            .build()

        auth.sendPasswordResetEmail(email, actionCodeSettings).await()
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