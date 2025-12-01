package com.example.nutricook.data.firebase.auth

import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseVerificationRepository @Inject constructor(
    private val ds: FirebaseAuthDataSource
) {
    // --- EMAIL ---

    suspend fun resendEmailVerification(): Result<Unit> =
        runCatching { ds.sendEmailVerification() }

    suspend fun checkEmailVerified(): Boolean {
        return try {
            ds.reloadUser()
            ds.isEmailVerified()
        } catch (e: Exception) {
            false
        }
    }

    // --- PHONE (OTP) ---

    fun sendPhoneVerification(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        ds.sendPhoneVerification(activity, phoneNumber, callbacks)
    }

    /**
     * Hàm này dùng cho trường hợp nhập mã OTP thủ công
     */
    suspend fun verifyAndLinkPhone(verificationId: String, code: String): Result<FirebaseUser> =
        runCatching {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            ds.linkPhoneCredential(credential)
        }

    /**
     * [MỚI THÊM] Hàm này dùng cho trường hợp Firebase tự động verify (onVerificationCompleted)
     * ViewModel đang gọi hàm này nên báo lỗi thiếu
     */
    suspend fun linkPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> =
        runCatching {
            ds.linkPhoneCredential(credential)
        }
}