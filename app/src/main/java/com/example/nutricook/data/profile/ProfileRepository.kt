package com.example.nutricook.data.profile

import com.example.nutricook.model.common.Paged
import com.example.nutricook.model.profile.ActivityItem
import com.example.nutricook.model.profile.Post
import com.example.nutricook.model.profile.Profile
import com.example.nutricook.model.user.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    /** Lắng nghe realtime hồ sơ của chính mình (có thể null nếu chưa đăng nhập). */
    fun myProfileFlow(): Flow<Profile?>

    /** Lấy hồ sơ hiện tại, nếu chưa có sẽ tạo. */
    suspend fun getMyProfile(): Profile

    /** Lấy hồ sơ của user khác theo UID. */
    suspend fun getProfileByUid(uid: String): Profile

    /** Cập nhật thông tin cơ bản (dùng cho màn Settings). */
    suspend fun updateProfile(
        fullName: String?,
        email: String?,
        dayOfBirth: String?,
        gender: String?,
        bio: String?
    )

    /** Upload avatar và trả về URL đã cập nhật. */
    suspend fun updateAvatar(localUri: String): String

    /** Follow / Unfollow user khác. */
    suspend fun setFollow(targetUid: String, follow: Boolean)

    /** Kiểm tra đã follow user khác chưa. */
    suspend fun isFollowing(targetUid: String): Boolean

    /** Đổi mật khẩu (đòi hỏi re-auth). */
    suspend fun changePassword(oldPassword: String, newPassword: String)

    // ---------- Danh sách phân trang ----------
    /** Lấy danh sách bài viết của một user (phân trang qua cursor = millis). */
    suspend fun getUserPosts(uid: String, cursor: String? = null): Paged<Post>

    /** Lấy danh sách bài đã lưu (saved/bookmarked) của một user (phân trang). */
    suspend fun getUserSaves(uid: String, cursor: String? = null): Paged<Post>

    /** Lấy activity gần đây của một user (phân trang). */
    suspend fun getUserActivities(uid: String, cursor: String? = null): Paged<ActivityItem>

    /** Tìm người dùng theo tên hoặc email (dùng name_lower / email_lower). */
    suspend fun searchUsers(query: String, limit: Long = 20L): List<User>
}
