package com.example.nutricook.data.newsfeed

import android.net.Uri
import com.example.nutricook.data.Page
import com.example.nutricook.model.newsfeed.Post

interface IPostRepository {
    // Lấy danh sách bài viết
    suspend fun getNewsFeed(cursor: String?): Page<Post>

    // Tạo bài viết (Lưu data vào Firestore)
    suspend fun createPost(content: String, imageUrl: String?): Post

    // Xóa bài viết
    suspend fun deletePost(postId: String)

    // [MỚI] Upload ảnh lên Storage -> Trả về URL
    suspend fun uploadImageToStorage(imageUri: Uri): String?

    // [MỚI] Thả tim / Bỏ tim
    suspend fun toggleLike(postId: String, currentUserId: String)

    // [MỚI] Lưu bài / Bỏ lưu
    suspend fun toggleSave(postId: String, currentUserId: String)
}