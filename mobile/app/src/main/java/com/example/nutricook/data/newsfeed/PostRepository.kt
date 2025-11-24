package com.example.nutricook.data.newsfeed

import android.net.Uri
import android.util.Log
import com.example.nutricook.data.Page
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IPostRepository {

    override suspend fun getNewsFeed(cursor: String?): Page<Post> {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("DEBUG_FEED", "Repository: User is NULL. Không thể tải bài viết do Rules chặn!")
            return Page(emptyList(), null)
        } else {
            Log.d("DEBUG_FEED", "Repository: User OK (${currentUser.email}). Bắt đầu query Firestore...")
        }

        return try {
            val pageSize = 10L

            var query = firestore.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize)

            if (cursor != null) {
                val lastDocRef = firestore.collection("posts").document(cursor).get().await()
                if (lastDocRef.exists()) {
                    query = query.startAfter(lastDocRef)
                }
            }

            val snapshot = query.get().await()
            val items = snapshot.toObjects(Post::class.java)

            Log.d("DEBUG_FEED", "Repository: Tải thành công ${items.size} bài viết.")

            val nextCursor = if (items.size >= pageSize) items.last().id else null

            Page(items = items, nextCursor = nextCursor)
        } catch (e: Exception) {
            Log.e("DEBUG_FEED", "Repository Lỗi: ${e.message}")
            e.printStackTrace()
            Page(emptyList(), null)
        }
    }

    override suspend fun createPost(content: String, imageUrl: String?): Post {
        val currentUser = auth.currentUser ?: throw Exception("Bạn chưa đăng nhập")

        // Tạo Document Reference trước để lấy ID
        val docRef = firestore.collection("posts").document()

        val newPost = Post(
            id = docRef.id,
            content = content,
            imageUrl = imageUrl,
            author = User(
                id = currentUser.uid,
                email = currentUser.email ?: "Anonymous"
            ),
            createdAt = System.currentTimeMillis(),
            likes = emptyList(), // Khởi tạo rỗng
            saves = emptyList(), // Khởi tạo rỗng
            commentCount = 0
        )

        // Lưu lên Firestore
        docRef.set(newPost).await()
        Log.d("DEBUG_FEED", "Repository: Đã đăng bài mới thành công (ID: ${docRef.id})")

        return newPost
    }

    override suspend fun deletePost(postId: String) {
        try {
            firestore.collection("posts").document(postId).delete().await()
            Log.d("DEBUG_FEED", "Repository: Đã xóa bài viết $postId")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- CÁC HÀM BỔ SUNG MỚI ---

    override suspend fun uploadImageToStorage(imageUri: Uri): String? {
        return try {
            // Lưu ý: Dùng getInstance() trực tiếp để tránh lỗi Hilt nếu chưa config module Storage
            val storageRef = FirebaseStorage.getInstance().reference
            // Tạo tên file ngẫu nhiên: images/posts/UUID.jpg
            val imageRef = storageRef.child("images/posts/${java.util.UUID.randomUUID()}.jpg")

            // Upload
            imageRef.putFile(imageUri).await()

            // Lấy link download
            val url = imageRef.downloadUrl.await().toString()
            Log.d("DEBUG_FEED", "Upload ảnh thành công: $url")
            url
        } catch (e: Exception) {
            Log.e("DEBUG_FEED", "Lỗi upload ảnh: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun toggleLike(postId: String, currentUserId: String) {
        val postRef = firestore.collection("posts").document(postId)
        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.get("likes") as? List<String> ?: emptyList()

                val newLikes = if (currentLikes.contains(currentUserId)) {
                    currentLikes - currentUserId // Bỏ like
                } else {
                    currentLikes + currentUserId // Thêm like
                }

                transaction.update(postRef, "likes", newLikes)
            }.await()
        } catch (e: Exception) {
            Log.e("DEBUG_FEED", "Lỗi toggle like: ${e.message}")
        }
    }

    override suspend fun toggleSave(postId: String, currentUserId: String) {
        val postRef = firestore.collection("posts").document(postId)
        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentSaves = snapshot.get("saves") as? List<String> ?: emptyList()

                val newSaves = if (currentSaves.contains(currentUserId)) {
                    currentSaves - currentUserId
                } else {
                    currentSaves + currentUserId
                }
                transaction.update(postRef, "saves", newSaves)
            }.await()
        } catch (e: Exception) {
            Log.e("DEBUG_FEED", "Lỗi toggle save: ${e.message}")
        }
    }
}