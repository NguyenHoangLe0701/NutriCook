package com.example.nutricook.data.profile

import com.example.nutricook.model.profile.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun myProfileFlow(): Flow<Profile?>
    suspend fun getMyProfile(): Profile
    suspend fun getProfileByUid(uid: String): Profile

    // Khớp với ViewModel.SaveSettings
    suspend fun updateProfile(
        fullName: String?,
        email: String?,        // đổi email (có thể cần re-auth)
        dayOfBirth: String?,
        gender: String?,
        bio: String?
    )

    suspend fun updateAvatar(localUri: String): String

    // Follow
    suspend fun setFollow(targetUid: String, follow: Boolean)
    suspend fun isFollowing(targetUid: String): Boolean

    // Đổi mật khẩu
    suspend fun changePassword(oldPassword: String, newPassword: String)
}
