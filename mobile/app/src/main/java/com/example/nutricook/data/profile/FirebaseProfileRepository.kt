package com.example.nutricook.data.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.nutricook.BuildConfig
import com.example.nutricook.model.common.Paged
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.profile.ActivityItem
import com.example.nutricook.model.profile.Nutrition
import com.example.nutricook.model.profile.Profile
import com.example.nutricook.model.user.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ProfileRepository {

    companion object {
        private const val USERS = "users"
        private const val POSTS = "posts"
        private const val SAVES = "saves"
        private const val DEFAULT_PAGE_SIZE = 20L
    }

    // --- HELPER ---
    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw Exception("Người dùng chưa đăng nhập")

    private fun users() = db.collection(USERS)
    private fun userDoc(uid: String) = users().document(uid)
    private fun posts() = db.collection(POSTS)
    private fun savesCol(uid: String) = userDoc(uid).collection(SAVES)

    // ===================== 1. LOCAL DATA & STATE =====================

    override fun myProfileFlow(): Flow<Profile?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = userDoc(uid).addSnapshotListener { snap, e ->
            if (e != null) {
                Log.e("ProfileRepo", "Lỗi lắng nghe profile: ${e.message}")
                return@addSnapshotListener
            }
            if (snap != null && snap.exists()) {
                trySend(snap.toProfileDomain())
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getMyProfile(): Profile =
        getProfileByUid(requireUid())

    // ===================== 2. PUBLIC INFO =====================

    override suspend fun getProfileByUid(uid: String): Profile {
        if (uid.isEmpty()) throw Exception("User ID không hợp lệ")

        val docRef = userDoc(uid)
        var snap = docRef.get().await()

        if (!snap.exists()) {
            val u = auth.currentUser
            if (u != null && u.uid == uid) {
                val email = u.email ?: ""
                val displayName = u.displayName ?: u.email?.substringBefore("@") ?: "User"

                val initialData = hashMapOf(
                    "email" to email,
                    "displayName" to displayName,
                    "avatarUrl" to (u.photoUrl?.toString()),
                    "posts" to 0,
                    "following" to 0,
                    "followers" to 0,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(initialData, SetOptions.merge()).await()
                snap = docRef.get().await()
            } else {
                throw Exception("Không tìm thấy hồ sơ người dùng")
            }
        }
        return snap.toProfileDomain() ?: throw Exception("Lỗi dữ liệu Profile")
    }

    // ===================== 3. UPDATES =====================

    override suspend fun updateProfile(
        fullName: String?,
        dayOfBirth: String?,
        gender: String?,
        bio: String?
    ): Profile {
        val uid = requireUid()

        val updates = mutableMapOf<String, Any>()
        fullName?.let { updates["displayName"] = it }
        dayOfBirth?.let { updates["dayOfBirth"] = it }
        gender?.let { updates["gender"] = it }
        bio?.let { updates["bio"] = it }
        updates["updatedAt"] = FieldValue.serverTimestamp()

        if (updates.isNotEmpty()) {
            userDoc(uid).update(updates).await()
        }
        return getMyProfile()
    }

    /**
     * [FIX 1] Sử dụng Cloudinary Upload
     */
    override suspend fun updateAvatar(localUri: String): String {
        val uid = requireUid()

        setupCloudinaryIfNeeded()

        val downloadUrl = suspendCancellableCoroutine<String> { continuation ->
            val requestId = MediaManager.get().upload(Uri.parse(localUri))
                .option("folder", "nutricook/avatars")
                .option("public_id", uid)
                .option("overwrite", true)
                .option("resource_type", "image")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val url = resultData["secure_url"] as? String
                            ?: resultData["url"] as? String
                            ?: ""
                        if (continuation.isActive) continuation.resume(url)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(Exception("Lỗi Cloudinary: ${error.description}"))
                        }
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(Exception("Lỗi Reschedule: ${error.description}"))
                        }
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                MediaManager.get().cancelRequest(requestId)
            }
        }

        userDoc(uid).update(
            mapOf(
                "avatarUrl" to downloadUrl,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        return downloadUrl
    }

    private fun setupCloudinaryIfNeeded() {
        try {
            MediaManager.get()
        } catch (e: Exception) {
            val config = HashMap<String, String>()
            config["cloud_name"] = BuildConfig.CLOUDINARY_CLOUD_NAME
            config["api_key"] = BuildConfig.CLOUDINARY_API_KEY
            config["api_secret"] = BuildConfig.CLOUDINARY_API_SECRET
            config["secure"] = "true"
            MediaManager.init(context, config)
        }
    }

    override suspend fun updateCaloriesTarget(caloriesTarget: Float): Profile {
        val uid = requireUid()
        val updates = mapOf(
            "nutrition.caloriesTarget" to caloriesTarget,
            "nutrition.updatedAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        userDoc(uid).update(updates).await()
        return getMyProfile()
    }

    // ===================== 4. ACCOUNT SECURITY =====================

    override suspend fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser ?: throw Exception("Chưa đăng nhập")
        val email = user.email ?: throw Exception("Tài khoản không có email")

        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    // ===================== 5. SOCIAL ACTIONS =====================

    override suspend fun setFollow(targetUid: String, follow: Boolean) {
        val myUid = requireUid()
        if (myUid == targetUid) return

        val myDoc = userDoc(myUid)
        val targetDoc = userDoc(targetUid)

        val followingRef = myDoc.collection("following").document(targetUid)
        val followerRef = targetDoc.collection("followers").document(myUid)

        db.runTransaction { transaction ->
            val isFollowing = transaction.get(followingRef).exists()

            if (follow && !isFollowing) {
                transaction.set(followingRef, mapOf("at" to FieldValue.serverTimestamp()))
                transaction.set(followerRef, mapOf("at" to FieldValue.serverTimestamp()))
                transaction.update(myDoc, "following", FieldValue.increment(1))
                transaction.update(targetDoc, "followers", FieldValue.increment(1))
            } else if (!follow && isFollowing) {
                transaction.delete(followingRef)
                transaction.delete(followerRef)
                transaction.update(myDoc, "following", FieldValue.increment(-1))
                transaction.update(targetDoc, "followers", FieldValue.increment(-1))
            }
        }.await()
    }

    override suspend fun isFollowing(targetUid: String): Boolean {
        val myUid = requireUid()
        val snap = userDoc(myUid).collection("following").document(targetUid).get().await()
        return snap.exists()
    }

    // ===================== 6. LIST DATA =====================

    override suspend fun getUserPosts(uid: String, cursor: String?): Paged<Post> {
        var query = posts()
            .whereEqualTo("author.id", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(DEFAULT_PAGE_SIZE)

        tsFromCursor(cursor)?.let { query = query.startAfter(it) }

        val snaps = query.get().await().documents
        if (snaps.isEmpty()) return Paged(emptyList(), null)

        val author = fetchUser(uid)
        val items = snaps.mapNotNull { it.toPostDomain(author) }
        val nextKey = nextCursorFrom(snaps.last())
        return Paged(items, nextKey)
    }

    /**
     * [FIX 2] Hàm lấy bài đã lưu (Saved Posts) an toàn hơn.
     * Tự động handle trường hợp tác giả bài viết bị null hoặc lỗi data.
     */
    override suspend fun getSavedPosts(uid: String, cursor: String?): Paged<Post> {
        var query = savesCol(uid)
            .orderBy("at", Query.Direction.DESCENDING)
            .limit(DEFAULT_PAGE_SIZE)

        tsFromCursor(cursor)?.let { query = query.startAfter(it) }

        val saveDocs = query.get().await().documents
        if (saveDocs.isEmpty()) return Paged(emptyList(), null)

        val postIds = saveDocs.mapNotNull { it.getString("postId") }
        if (postIds.isEmpty()) return Paged(emptyList(), null)

        // Lấy chi tiết bài viết
        val postsSnapshots = postIds.map { pid -> posts().document(pid).get() }.map { it.await() }

        // Lấy danh sách tác giả
        val authorIds = postsSnapshots.mapNotNull { it.getString("author.id") }.distinct()
        val authorsMap = mutableMapOf<String, User>()

        if (authorIds.isNotEmpty()) {
            try {
                for (auid in authorIds) {
                    runCatching { authorsMap[auid] = fetchUser(auid) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Map data an toàn
        val items = postsSnapshots.mapNotNull { snap ->
            if (!snap.exists()) return@mapNotNull null

            val authorId = snap.getString("author.id")
            // Fallback nếu không tìm thấy user
            val author = if (authorId != null) {
                authorsMap[authorId] ?: User(id = authorId, displayName = "Unknown User")
            } else {
                User(id = "unknown", displayName = "Unknown")
            }

            snap.toPostDomain(author)
        }

        val nextKey = saveDocs.last().getTimestamp("at")?.toDate()?.time?.toString()
        return Paged(items, nextKey)
    }

    override suspend fun getUserActivities(uid: String, cursor: String?): Paged<ActivityItem> {
        return Paged(emptyList(), null)
    }

    // ===================== 7. SEARCH =====================

    override suspend fun searchProfiles(query: String): List<Profile> {
        if (query.isBlank()) return emptyList()
        val snapshot = db.collection(USERS)
            .orderBy("displayName")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(20)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toProfileDomain() }
    }

    // ===================== MAPPERS =====================

    private suspend fun fetchUser(uid: String): User {
        val snap = userDoc(uid).get().await()
        return snap.toUserDomain()
    }

    private fun DocumentSnapshot.toUserDomain(): User {
        return User(
            id = id,
            email = getString("email") ?: "",
            displayName = getString("displayName"),
            avatarUrl = getString("avatarUrl")
        )
    }

    private fun DocumentSnapshot.toProfileDomain(): Profile? {
        if (!exists()) return null
        return try {
            val user = this.toUserDomain()
            var nutritionObj: Nutrition? = null
            val nutMap = get("nutrition") as? Map<String, Any>
            if (nutMap != null) {
                val cals = (nutMap["caloriesTarget"] as? Number)?.toFloat() ?: 0f
                val pro = (nutMap["proteinG"] as? Number)?.toFloat() ?: 0f
                val fat = (nutMap["fatG"] as? Number)?.toFloat() ?: 0f
                val carb = (nutMap["carbG"] as? Number)?.toFloat() ?: 0f
                nutritionObj = Nutrition(cals, pro, fat, carb)
            }

            Profile(
                user = user,
                posts = getLong("posts")?.toInt() ?: 0,
                following = getLong("following")?.toInt() ?: 0,
                followers = getLong("followers")?.toInt() ?: 0,
                bio = getString("bio"),
                dayOfBirth = getString("dayOfBirth"),
                gender = getString("gender"),
                nutrition = nutritionObj
            )
        } catch (e: Exception) {
            Log.e("ProfileRepo", "Error mapping: ${e.message}")
            null
        }
    }

    private fun DocumentSnapshot.toPostDomain(author: User): Post? {
        if (!exists()) return null
        return try {
            Post(
                id = id,
                title = getString("title") ?: "",
                content = getString("content") ?: "",
                imageUrl = getString("imageUrl"),
                author = author,
                createdAt = getLong("createdAt") ?: 0L,
                likes = (get("likes") as? List<String>) ?: emptyList(),
                saves = (get("saves") as? List<String>) ?: emptyList(),
                commentCount = getLong("commentCount")?.toInt() ?: 0
            )
        } catch (e: Exception) { null }
    }

    private fun tsFromCursor(cursor: String?): Timestamp? =
        cursor?.toLongOrNull()?.let { Timestamp(Date(it)) }

    private fun nextCursorFrom(snap: DocumentSnapshot): String? =
        snap.getTimestamp("createdAt")?.toDate()?.time?.toString()
}