package com.example.nutricook.data.profile

import com.example.nutricook.model.common.Paged
import com.example.nutricook.model.newsfeed.Post // CHỈ DÙNG POST NÀY
import com.example.nutricook.model.profile.ActivityItem
import com.example.nutricook.model.profile.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    // --- USER & PROFILE ---
    fun myProfileFlow(): Flow<Profile?>
    suspend fun getMyProfile(): Profile
    suspend fun getProfileByUid(uid: String): Profile

    suspend fun updateProfile(
        fullName: String?,
        email: String?,
        dayOfBirth: String?,
        gender: String?,
        bio: String?
    )

    suspend fun updateAvatar(localUri: String): String
    suspend fun changePassword(oldPassword: String, newPassword: String)

    /** Cập nhật mục tiêu calories. */
    suspend fun updateCaloriesTarget(caloriesTarget: Float)

    // --- SOCIAL ACTIONS ---
    suspend fun setFollow(targetUid: String, follow: Boolean)
    suspend fun isFollowing(targetUid: String): Boolean

    // --- LIST DATA ---
    /** Lấy danh sách bài viết của một user (phân trang qua cursor). */
    suspend fun getUserPosts(uid: String, cursor: String? = null): Paged<Post>
    suspend fun getUserSaves(uid: String, cursor: String? = null): Paged<Post>
    suspend fun getUserActivities(uid: String, cursor: String? = null): Paged<ActivityItem>

    // --- SEARCH ---
    suspend fun searchProfiles(query: String): List<Profile>
}