package com.example.nutricook.data.profile

import com.example.nutricook.model.common.Paged
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.profile.ActivityItem
import com.example.nutricook.model.profile.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    // --- 1. LOCAL DATA & STATE ---
    fun myProfileFlow(): Flow<Profile?>

    /** * [SỬA LẠI TÊN Ở ĐÂY]: Đổi từ refreshMyProfile thành getMyProfile
     * để khớp với code Implementation
     */
    suspend fun getMyProfile(): Profile

    // --- 2. PUBLIC INFO ---
    suspend fun getProfileByUid(uid: String): Profile

    // --- 3. UPDATES ---
    suspend fun updateProfile(
        fullName: String?,
        dayOfBirth: String?,
        gender: String?,
        bio: String?
    ): Profile

    suspend fun updateAvatar(localUri: String): String

    suspend fun updateCaloriesTarget(caloriesTarget: Float): Profile

    // --- 4. ACCOUNT SECURITY ---
    suspend fun changePassword(oldPassword: String, newPassword: String)

    // --- 5. SOCIAL ACTIONS ---
    suspend fun setFollow(targetUid: String, follow: Boolean)

    suspend fun isFollowing(targetUid: String): Boolean

    // --- 6. LIST DATA (PAGING) ---
    suspend fun getUserPosts(uid: String, cursor: String? = null): Paged<Post>

    /** * Đảm bảo tên này khớp với bên Impl là getSavedPosts (không phải getUserSaves)
     */
    suspend fun getSavedPosts(uid: String, cursor: String? = null): Paged<Post>

    suspend fun getUserActivities(uid: String, cursor: String? = null): Paged<ActivityItem>

    // --- 7. SEARCH ---
    suspend fun searchProfiles(query: String): List<Profile>
}