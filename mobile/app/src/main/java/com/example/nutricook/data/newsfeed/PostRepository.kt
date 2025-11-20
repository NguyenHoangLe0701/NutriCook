package com.example.nutricook.data.newsfeed

import com.example.nutricook.data.Page
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IPostRepository {

    override suspend fun getNewsFeed(cursor: String?): Page<Post> {
        return try {
            val pageSize = 10L

            // Query cơ bản: lấy bài mới nhất
            var query = firestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize)

            // Nếu có cursor (id bài cuối của trang trước), startAfter document đó
            if (cursor != null) {
                val lastDoc = firestore.collection("posts").document(cursor).get().await()
                if (lastDoc.exists()) {
                    query = query.startAfter(lastDoc)
                }
            }

            val snapshot = query.get().await()
            val items = snapshot.toObjects(Post::class.java)

            // Tính toán nextCursor cho trang sau
            val nextCursor = if (items.size >= pageSize) items.last().id else null

            Page(items = items, nextCursor = nextCursor)
        } catch (e: Exception) {
            e.printStackTrace()
            Page(emptyList(), null)
        }
    }

    // [CẬP NHẬT] Nhận imageUrl và lưu vào Post
    override suspend fun createPost(content: String, imageUrl: String?): Post {
        val currentUser = auth.currentUser ?: throw Exception("Bạn chưa đăng nhập")

        // Tạo Document Reference trước để lấy ID
        val docRef = firestore.collection("posts").document()

        val newPost = Post(
            id = docRef.id,
            content = content,
            imageUrl = imageUrl, // Lưu link ảnh vào đây
            author = User(
                id = currentUser.uid,
                email = currentUser.email ?: "Anonymous"
            ),
            createdAt = System.currentTimeMillis()
        )

        // Lưu lên Firestore
        docRef.set(newPost).await()

        return newPost
    }

    override suspend fun deletePost(postId: String) {
        try {
            firestore.collection("posts").document(postId).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}