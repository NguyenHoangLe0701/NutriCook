package com.example.nutricook.data.newsfeed

import com.example.nutricook.data.Page
import com.example.nutricook.model.newsfeed.Post

interface IPostRepository {
    suspend fun getNewsFeed(cursor: String?): Page<Post>

    // [CẬP NHẬT] Thêm tham số imageUrl (có thể null)
    suspend fun createPost(content: String, imageUrl: String?): Post

    suspend fun deletePost(postId: String)
}