package com.example.nutricook.viewmodel.profile

import com.example.nutricook.model.profile.Post
import com.example.nutricook.model.profile.Profile

data class OtherProfileUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val profile: Profile? = null,
    val isFollowing: Boolean = false,
    val posts: List<Post> = emptyList(),
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val togglingFollow: Boolean = false // chặn spam follow/unfollow
)
