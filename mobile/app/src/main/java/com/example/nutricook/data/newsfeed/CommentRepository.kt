package com.example.nutricook.data.newsfeed

import com.example.nutricook.model.newsfeed.Comment
import com.example.nutricook.model.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    /**
     * Lấy danh sách bình luận cho một Post hoặc Hot News
     */
    suspend fun getComments(postId: String, postType: String = "post"): List<Comment> {
        return try {
            val collection = if (postType == "hotnews") "hotNewsComments" else "postComments"
            // Bỏ orderBy để không cần Firestore index, sẽ sort trong code
            val snapshot = firestore.collection(collection)
                .whereEqualTo("postId", postId)
                .get()
                .await()
            
            val comments = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    // Parse author
                    val authorObj = data["author"] as? Map<*, *>
                    val author = if (authorObj != null) {
                        User(
                            id = authorObj["id"] as? String ?: "",
                            email = authorObj["email"] as? String ?: "",
                            displayName = authorObj["displayName"] as? String ?: "",
                            avatarUrl = authorObj["avatarUrl"] as? String
                        )
                    } else {
                        User()
                    }
                    
                    Comment(
                        id = doc.id,
                        postId = data["postId"] as? String ?: "",
                        postType = data["postType"] as? String ?: "post",
                        author = author,
                        content = data["content"] as? String ?: "",
                        createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("CommentRepo", "Error parsing comment: ${e.message}")
                    null
                }
            }
            
            // Sort theo createdAt ascending trong code
            comments.sortedBy { it.createdAt }
        } catch (e: Exception) {
            android.util.Log.e("CommentRepo", "Error fetching comments: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Thêm bình luận mới
     */
    suspend fun addComment(postId: String, postType: String, content: String): Comment? {
        return try {
            val currentUser = auth.currentUser ?: return null
            
            val collection = if (postType == "hotnews") "hotNewsComments" else "postComments"
            val docRef = firestore.collection(collection).document()
            
            val author = User(
                id = currentUser.uid,
                email = currentUser.email ?: "",
                displayName = currentUser.displayName ?: "Anonymous",
                avatarUrl = currentUser.photoUrl?.toString()
            )
            
            val comment = Comment(
                id = docRef.id,
                postId = postId,
                postType = postType,
                author = author,
                content = content,
                createdAt = System.currentTimeMillis()
            )
            
            docRef.set(comment).await()
            
            // Tăng commentCount trong Post hoặc Hot News
            val targetCollection = if (postType == "hotnews") "hotNews" else "posts"
            firestore.collection(targetCollection).document(postId)
                .update("commentCount", FieldValue.increment(1))
                .await()
            
            comment
        } catch (e: Exception) {
            android.util.Log.e("CommentRepo", "Error adding comment: ${e.message}")
            null
        }
    }
    
    /**
     * Xóa bình luận (chỉ người đăng mới có thể xóa)
     */
    suspend fun deleteComment(commentId: String, postId: String, postType: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            
            val collection = if (postType == "hotnews") "hotNewsComments" else "postComments"
            val docRef = firestore.collection(collection).document(commentId)
            val doc = docRef.get().await()
            
            if (!doc.exists()) return false
            
            // Kiểm tra xem người dùng hiện tại có phải là tác giả không
            val authorObj = doc.get("author") as? Map<*, *>
            val authorId = authorObj?.get("id") as? String
            
            if (authorId != currentUser.uid) {
                android.util.Log.w("CommentRepo", "User ${currentUser.uid} is not the author of comment $commentId")
                return false
            }
            
            docRef.delete().await()
            
            // Giảm commentCount trong Post hoặc Hot News
            val targetCollection = if (postType == "hotnews") "hotNews" else "posts"
            firestore.collection(targetCollection).document(postId)
                .update("commentCount", FieldValue.increment(-1))
                .await()
            
            true
        } catch (e: Exception) {
            android.util.Log.e("CommentRepo", "Error deleting comment: ${e.message}")
            false
        }
    }
}

