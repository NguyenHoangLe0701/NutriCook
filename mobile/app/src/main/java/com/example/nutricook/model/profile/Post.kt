package com.example.nutricook.model.profile


import com.example.nutricook.model.user.User

data class Post(
    val id: Long,
    val author: User,
    val content: String? = null,
    val images: List<String> = emptyList(),
    val createdAt: Long,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isSaved: Boolean = false
)