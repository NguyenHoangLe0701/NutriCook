package com.example.nutricook.data.profile

import com.example.nutricook.model.profile.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    /** lắng nghe realtime hồ sơ của chính mình (có thể null nếu chưa đăng nhập) */
    fun myProfileFlow(): Flow<Profile?>

    /** lấy hồ sơ hiện tại, nếu chưa có sẽ tạo */
    suspend fun getMyProfile(): Profile

    /** lấy hồ sơ của user khác */
    suspend fun getProfileByUid(uid: String): Profile

    /** cập nhật thông tin cơ bản (dùng cho màn Settings) */
    suspend fun updateProfile(
        fullName: String?,
        email: String?,
        dayOfBirth: String?,
        gender: String?,
        bio: String?
    )

    /** upload avatar và trả về url */
    suspend fun updateAvatar(localUri: String): String

    /** follow / unfollow user khác */
    suspend fun setFollow(targetUid: String, follow: Boolean)

    /** kiểm tra đã follow chưa */
    suspend fun isFollowing(targetUid: String): Boolean

    /** đổi password (đòi hỏi re-auth) */
    suspend fun changePassword(oldPassword: String, newPassword: String)
}
