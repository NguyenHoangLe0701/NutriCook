package com.example.nutricook.data.newsfeed

import android.net.Uri
import android.util.Log
import com.example.nutricook.data.Page
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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
            return Page(emptyList(), null)
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
            val nextCursor = if (items.size >= pageSize) items.last().id else null

            Page(items = items, nextCursor = nextCursor)
        } catch (e: Exception) {
            e.printStackTrace()
            Page(emptyList(), null)
        }
    }

    // [CẬP NHẬT] Nhận title và lưu vào object Post
    override suspend fun createPost(title: String, content: String, imageUrl: String?): Post {
        val currentUser = auth.currentUser ?: throw Exception("Bạn chưa đăng nhập")
        val docRef = firestore.collection("posts").document()

        val newPost = Post(
            id = docRef.id,
            title = title, // Lưu title
            content = content,
            imageUrl = imageUrl,
            author = User(
                id = currentUser.uid,
                email = currentUser.email ?: "Anonymous",
                displayName = currentUser.displayName,
                avatarUrl = currentUser.photoUrl?.toString()
            ),
            createdAt = System.currentTimeMillis(),
            likes = emptyList(),
            saves = emptyList(),
            commentCount = 0
        )

        docRef.set(newPost).await()

        // Tăng số lượng bài viết trong Profile
        firestore.collection("users").document(currentUser.uid)
            .update("posts", FieldValue.increment(1))

        return newPost
    }

    override suspend fun deletePost(postId: String) {
        try {
            val currentUser = auth.currentUser ?: return
            firestore.collection("posts").document(postId).delete().await()

            firestore.collection("users").document(currentUser.uid)
                .update("posts", FieldValue.increment(-1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun uploadImageToStorage(imageUri: Uri): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/posts/${java.util.UUID.randomUUID()}.jpg")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
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

                if (currentLikes.contains(currentUserId)) {
                    transaction.update(postRef, "likes", FieldValue.arrayRemove(currentUserId))
                } else {
                    transaction.update(postRef, "likes", FieldValue.arrayUnion(currentUserId))
                }
            }.await()
        } catch (e: Exception) {
            Log.e("DEBUG_FEED", "Lỗi toggle like: ${e.message}")
        }
    }

    override suspend fun toggleSave(postId: String, currentUserId: String) {
        val db = firestore
        val postRef = db.collection("posts").document(postId)
        val userSaveRef = db.collection("users").document(currentUserId)
            .collection("saves").document(postId)

        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                @Suppress("UNCHECKED_CAST")
                val currentSaves = snapshot.get("saves") as? List<String> ?: emptyList()

                if (currentSaves.contains(currentUserId)) {
                    transaction.update(postRef, "saves", FieldValue.arrayRemove(currentUserId))
                    transaction.delete(userSaveRef)
                } else {
                    transaction.update(postRef, "saves", FieldValue.arrayUnion(currentUserId))
                    val saveData = mapOf(
                        "postId" to postId,
                        "at" to FieldValue.serverTimestamp()
                    )
                    transaction.set(userSaveRef, saveData)
                }
            }.await()
        } catch (e: Exception) {
            Log.e("DEBUG_FEED", "Lỗi toggle save: ${e.message}")
        }
    }
}