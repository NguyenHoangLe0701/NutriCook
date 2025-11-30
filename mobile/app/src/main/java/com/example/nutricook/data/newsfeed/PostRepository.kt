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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
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

            // 1. Tải danh sách bài viết (Dữ liệu Author có thể bị cũ)
            val snapshot = query.get().await()
            // Use custom mapper to avoid likeCount warning (likeCount is computed from likes.size)
            val rawPosts = snapshot.documents.mapNotNull { it.toPostDomain() }

            // 2. [QUAN TRỌNG] "Join" dữ liệu User mới nhất vào Post
            // Lấy danh sách ID các tác giả trong trang này (tối đa 10 người)
            val authorIds = rawPosts.map { it.author.id }.distinct().take(10)

            val finalPosts = if (authorIds.isNotEmpty()) {
                // Tải thông tin mới nhất của các tác giả từ collection 'users'
                val usersSnapshot = firestore.collection("users")
                    .whereIn(FieldPath.documentId(), authorIds)
                    .get()
                    .await()

                // Tạo Map để tra cứu nhanh: ID -> User Object
                val usersMap = usersSnapshot.documents.associate { doc ->
                    // Map thủ công để đảm bảo lấy đúng field
                    val uid = doc.id
                    val email = doc.getString("email") ?: ""
                    val name = doc.getString("displayName")
                    val avatar = doc.getString("avatarUrl")

                    uid to User(id = uid, email = email, displayName = name, avatarUrl = avatar)
                }

                // Gán lại Author mới nhất vào bài viết
                rawPosts.map { post ->
                    val liveAuthor = usersMap[post.author.id]
                    if (liveAuthor != null) {
                        // Copy bài viết nhưng thay thế author bằng thông tin mới nhất
                        post.copy(author = liveAuthor)
                    } else {
                        post
                    }
                }
            } else {
                rawPosts
            }

            val nextCursor = if (finalPosts.size >= pageSize) finalPosts.last().id else null

            Page(items = finalPosts, nextCursor = nextCursor)
        } catch (e: Exception) {
            e.printStackTrace()
            Page(emptyList(), null)
        }
    }

    // [CẬP NHẬT] Lấy thông tin User từ Firestore thay vì Auth để đảm bảo Avatar đúng
    override suspend fun createPost(title: String, content: String, imageUrl: String?): Post {
        val currentUser = auth.currentUser ?: throw Exception("Bạn chưa đăng nhập")

        // 1. Lấy thông tin User mới nhất từ Firestore (Nơi chứa Avatar thật)
        val userDoc = firestore.collection("users").document(currentUser.uid).get().await()

        // Ưu tiên lấy từ Firestore, nếu không có thì fallback về Auth
        val currentAvatarUrl = userDoc.getString("avatarUrl") ?: currentUser.photoUrl?.toString()
        val currentDisplayName = userDoc.getString("displayName") ?: currentUser.displayName
        val currentEmail = userDoc.getString("email") ?: currentUser.email ?: "Anonymous"

        val docRef = firestore.collection("posts").document()

        val newPost = Post(
            id = docRef.id,
            title = title,
            content = content,
            imageUrl = imageUrl,
            author = User(
                id = currentUser.uid,
                email = currentEmail,
                displayName = currentDisplayName,
                avatarUrl = currentAvatarUrl // <--- Đây chính là Avatar chuẩn
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
                // [LƯU Ý] Nếu bạn đã cấu hình unsigned upload thì thêm .unsigned("tên_preset") vào đây
                // Ví dụ: .unsigned("nutricook_unsigned")
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

    /**
     * Custom mapper to convert DocumentSnapshot to Post, ignoring likeCount field
     * (likeCount is computed from likes.size, not stored in Firestore)
     */
    private fun DocumentSnapshot.toPostDomain(): Post? {
        if (!exists()) return null
        return try {
            // Parse author (nested object)
            val authorMap = get("author") as? Map<String, Any>
            val author = if (authorMap != null) {
                User(
                    id = authorMap["id"] as? String ?: "",
                    email = authorMap["email"] as? String ?: "",
                    displayName = authorMap["displayName"] as? String,
                    avatarUrl = authorMap["avatarUrl"] as? String
                )
            } else {
                User() // Fallback to empty user
            }

            Post(
                id = id,
                userId = getString("userId") ?: author.id, // For backward compatibility
                title = getString("title") ?: "",
                content = getString("content") ?: "",
                imageUrl = getString("imageUrl"),
                author = author,
                createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
                likes = (get("likes") as? List<String>) ?: emptyList(),
                saves = (get("saves") as? List<String>) ?: emptyList(),
                commentCount = getLong("commentCount")?.toInt() ?: 0
                // Note: likeCount is computed from likes.size, so we don't read it from Firestore
            )
        } catch (e: Exception) {
            Log.e("PostRepo", "Error mapping Post: ${e.message}")
            null
        }
    }
}