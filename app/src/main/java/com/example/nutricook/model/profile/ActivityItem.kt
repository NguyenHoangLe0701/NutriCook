package com.example.nutricook.model.profile


import com.example.nutricook.model.user.User

enum class ActivityType { FOLLOWED_YOU, LIKED_POST, COMMENTED_POST, NEW_POST }

data class ActivityItem(
    val id: Long,
    val actor: User, // ai tạo activity
    val type: ActivityType,
    val targetPostId: Long? = null, // post liên quan (nếu có)
    val createdAt: Long
)