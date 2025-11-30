package com.example.nutricook.data.newsfeed

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.nutricook.data.Page
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume

class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val mediaManager: MediaManager,
    @ApplicationContext private val context: Context
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
        return suspendCancellableCoroutine { continuation ->
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    android.util.Log.e("PostRepo", "User not logged in")
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }
                
                // Generate unique public_id for Cloudinary
                val timestamp = System.currentTimeMillis()
                val random = (0..9999).random()
                val publicId = "posts/${currentUser.uid}/${timestamp}_${random}"
                
                android.util.Log.d("PostRepo", "Uploading image to Cloudinary: $publicId")
                
                // Upload to Cloudinary
                val requestId = mediaManager.upload(imageUri)
                    .option("public_id", publicId)
                    .option("folder", "nutricook/posts")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            android.util.Log.d("PostRepo", "Upload started: $requestId")
                        }
                        
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            android.util.Log.d("PostRepo", "Upload progress: $progress%")
                        }
                        
                        override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                            val secureUrl = resultData["secure_url"] as? String
                            val url = resultData["url"] as? String
                            val imageUrl = secureUrl ?: url
                            
                            if (imageUrl != null) {
                                android.util.Log.d("PostRepo", "Image uploaded successfully: $imageUrl")
                                continuation.resume(imageUrl)
                            } else {
                                android.util.Log.e("PostRepo", "Upload succeeded but no URL returned")
                                continuation.resume(null)
                            }
                        }
                        
                        override fun onError(requestId: String, error: ErrorInfo) {
                            android.util.Log.e("PostRepo", "Upload error: ${error.description}")
                            continuation.resume(null)
                        }
                        
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            android.util.Log.w("PostRepo", "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()
                
                android.util.Log.d("PostRepo", "Upload request dispatched: $requestId")
            } catch (e: Exception) {
                android.util.Log.e("PostRepo", "Error uploading image: ${e.message}")
                continuation.resume(null)
            }
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