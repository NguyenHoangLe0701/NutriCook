package com.example.nutricook.model.profile

import com.example.nutricook.model.user.User

data class Post(
    val id: String = "",

    val author: User,

    val title: String = "",

    val content: String? = null,

    val images: List<String> = emptyList(),

    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isSaved: Boolean = false
) {
    val imageUrl: String?
        get() = images.firstOrNull()
}