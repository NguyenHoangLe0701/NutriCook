package com.example.nutricook.model.newsfeed

import com.example.nutricook.model.user.User

data class Post(
    val id: String = "",
    val userId: String = "", // Giữ để tương thích dữ liệu cũ
    val author: User = User(),

    // Thêm title để hỗ trợ hiển thị tiêu đề (từ phiên bản Profile cũ)
    val title: String = "",

    val content: String = "",
    val imageUrl: String? = null,

    // Dữ liệu gốc từ Firestore
    val likes: List<String> = emptyList(),
    val saves: List<String> = emptyList(),

    val commentCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    // --- Helper Properties cho UI ---

    val likeCount: Int
        get() = likes.size

    // Hàm kiểm tra xem user hiện tại có lưu bài này không
    fun isSaved(currentUid: String?): Boolean {
        return currentUid != null && saves.contains(currentUid)
    }
}