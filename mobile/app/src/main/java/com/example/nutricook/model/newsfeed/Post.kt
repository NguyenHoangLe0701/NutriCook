package com.example.nutricook.model.newsfeed

import com.example.nutricook.model.user.User

data class Post(
    val id: String = "",
    val content: String = "",
    val imageUrl: String? = null, // Thêm trường này (có thể null nếu bài không có ảnh)
    val author: User = User(id = "", email = ""),
    val createdAt: Long = 0L,
    val likes: List<String> = emptyList(), // Danh sách UID người like
    val saves: List<String> = emptyList(), // Danh sách UID người save
    val commentCount: Int = 0 // Số lượng comment
)