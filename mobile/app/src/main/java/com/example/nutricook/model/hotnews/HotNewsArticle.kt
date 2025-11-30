package com.example.nutricook.model.hotnews

import com.example.nutricook.model.user.User

data class HotNewsArticle(
    val id: String = "",
    val title: String = "",
    val content: String = "", // Nội dung bài đăng
    val category: String = "",
    val author: User = User(),
    val thumbnailUrl: String? = null,
    val link: String? = null, // Optional link
    val createdAt: Long = System.currentTimeMillis(),
    // Thêm các trường tương tác giống Post
    val likes: List<String> = emptyList(),
    val saves: List<String> = emptyList(),
    val commentCount: Int = 0
) {
    // Helper để format date
    fun getFormattedDate(): String {
        val date = java.util.Date(createdAt)
        val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ENGLISH)
        return sdf.format(date)
    }
    
    // Helper properties cho UI
    val likeCount: Int
        get() = likes.size
    
    // Hàm kiểm tra xem user hiện tại có lưu bài này không
    fun isSaved(currentUid: String?): Boolean {
        return currentUid != null && saves.contains(currentUid)
    }
}

