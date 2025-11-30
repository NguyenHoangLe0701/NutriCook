package com.example.nutricook.model.newsfeed

import com.example.nutricook.model.user.User

data class Comment(
    val id: String = "",
    val postId: String = "", // ID của Post hoặc Hot News
    val postType: String = "post", // "post" hoặc "hotnews"
    val author: User = User(),
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

