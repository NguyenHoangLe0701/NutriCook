package com.example.nutricook.data.hotnews

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.nutricook.model.hotnews.HotNewsArticle
import com.example.nutricook.model.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class HotNewsRepository @Inject constructor(
    private val mediaManager: MediaManager,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    
    /**
     * Lấy danh sách tất cả Hot News articles từ Firestore
     */
    suspend fun getAllHotNews(): List<HotNewsArticle> {
        return try {
            val snapshot = firestore.collection("hotNews")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
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
                    
                    HotNewsArticle(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        content = data["content"] as? String ?: "",
                        category = data["category"] as? String ?: "",
                        author = author,
                        thumbnailUrl = data["thumbnailUrl"] as? String,
                        link = data["link"] as? String,
                        createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    android.util.Log.e("HotNewsRepo", "Error parsing document ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HotNewsRepo", "Error fetching hot news: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Lấy danh sách Hot News với filter theo category
     */
    suspend fun getHotNewsByCategory(category: String?): List<HotNewsArticle> {
        val allNews = getAllHotNews()
        return if (category.isNullOrBlank()) {
            allNews
        } else {
            allNews.filter { it.category.equals(category, ignoreCase = true) }
        }
    }
    
    /**
     * Tạo bài đăng Hot News mới
     */
    suspend fun createHotNewsArticle(
        title: String,
        content: String,
        category: String,
        thumbnailUri: Uri?,
        link: String?
    ): Result<HotNewsArticle> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("Bạn chưa đăng nhập"))
            
            // Upload thumbnail nếu có
            val thumbnailUrl = thumbnailUri?.let { uploadImage(it) }
            
            val docRef = firestore.collection("hotNews").document()
            
            val author = User(
                id = currentUser.uid,
                email = currentUser.email ?: "",
                displayName = currentUser.displayName ?: "Anonymous",
                avatarUrl = currentUser.photoUrl?.toString()
            )
            
            val article = HotNewsArticle(
                id = docRef.id,
                title = title,
                content = content,
                category = category,
                author = author,
                thumbnailUrl = thumbnailUrl,
                link = link,
                createdAt = System.currentTimeMillis()
            )
            
            // Lưu vào Firestore
            docRef.set(article).await()
            
            Result.success(article)
        } catch (e: Exception) {
            android.util.Log.e("HotNewsRepo", "Error creating hot news: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Upload image to Cloudinary
     */
    private suspend fun uploadImage(uri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    android.util.Log.e("HotNewsRepo", "User not logged in")
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }
                
                // Generate unique public_id for Cloudinary
                val timestamp = System.currentTimeMillis()
                val random = (0..9999).random()
                val publicId = "hotnews/${currentUser.uid}/${timestamp}_${random}"
                
                android.util.Log.d("HotNewsRepo", "Uploading image to Cloudinary: $publicId")
                
                // Upload to Cloudinary
                val requestId = mediaManager.upload(uri)
                    .option("public_id", publicId)
                    .option("folder", "nutricook/hotnews")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            android.util.Log.d("HotNewsRepo", "Upload started: $requestId")
                        }
                        
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            android.util.Log.d("HotNewsRepo", "Upload progress: $progress%")
                        }
                        
                        override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                            val secureUrl = resultData["secure_url"] as? String
                            val url = resultData["url"] as? String
                            val imageUrl = secureUrl ?: url
                            
                            if (imageUrl != null) {
                                android.util.Log.d("HotNewsRepo", "Image uploaded successfully: $imageUrl")
                                continuation.resume(imageUrl)
                            } else {
                                android.util.Log.e("HotNewsRepo", "Upload succeeded but no URL returned")
                                continuation.resume(null)
                            }
                        }
                        
                        override fun onError(requestId: String, error: ErrorInfo) {
                            android.util.Log.e("HotNewsRepo", "Upload error: ${error.description}")
                            continuation.resume(null)
                        }
                        
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            android.util.Log.w("HotNewsRepo", "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()
                
                android.util.Log.d("HotNewsRepo", "Upload request dispatched: $requestId")
            } catch (e: Exception) {
                android.util.Log.e("HotNewsRepo", "Error uploading image: ${e.message}")
                continuation.resume(null)
            }
        }
    }
}

