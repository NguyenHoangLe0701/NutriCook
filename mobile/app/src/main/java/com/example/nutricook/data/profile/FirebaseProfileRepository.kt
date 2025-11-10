package com.example.nutricook.data.profile

import android.net.Uri
import com.example.nutricook.model.common.Paged
import com.example.nutricook.model.profile.ActivityItem
import com.example.nutricook.model.profile.ActivityType
import com.example.nutricook.model.profile.Post
import com.example.nutricook.model.profile.Profile
import com.example.nutricook.model.user.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import kotlin.math.abs

class FirebaseProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    companion object {
        private const val USERS = "users"
        private const val POSTS = "posts"
        private const val SAVES = "saves"
        private const val ACTIVITIES = "activities"
        private const val DEFAULT_PAGE_SIZE = 20L
    }

    private fun requireUid(): String =
        auth.currentUser?.uid ?: error("Chưa đăng nhập")

    private fun users() = db.collection(USERS)
    private fun userDoc(uid: String) = users().document(uid)
    private fun posts() = db.collection(POSTS)
    private fun savesCol(uid: String) = userDoc(uid).collection(SAVES)
    private fun activitiesCol(uid: String) = userDoc(uid).collection(ACTIVITIES)

    // ===================== FLOW =====================
    override fun myProfileFlow(): Flow<Profile?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val reg = userDoc(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.toProfileDomain())
        }
        awaitClose { reg.remove() }
    }

    // ===================== GET =====================
    override suspend fun getMyProfile(): Profile =
        getProfileByUid(requireUid())

    override suspend fun getProfileByUid(uid: String): Profile {
        val doc = userDoc(uid)
        var snap = doc.get().await()

        // nếu chưa có -> tạo 1 bản default từ user đăng nhập
        if (!snap.exists()) {
            val u = auth.currentUser ?: error("Chưa đăng nhập")
            val data = mapOf(
                "email" to (u.email ?: ""),
                "displayName" to (u.displayName ?: u.email?.substringBefore("@")),
                "avatarUrl" to (u.photoUrl?.toString()),
                "posts" to 0,
                "following" to 0,
                "followers" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            doc.set(data, SetOptions.merge()).await()
            snap = doc.get().await()
        }
        return snap.toProfileDomain() ?: error("Không tạo được hồ sơ")
    }

    // ===================== UPDATE PROFILE =====================
    override suspend fun updateProfile(
        fullName: String?,
        email: String?,
        dayOfBirth: String?,
        gender: String?,
        bio: String?
    ) {
        val uid = requireUid()
        val current = auth.currentUser ?: error("Chưa đăng nhập")

        // Đổi email trong FirebaseAuth nếu thay đổi
        if (!email.isNullOrBlank() && email != current.email) {
            try {
                current.updateEmail(email).await()
            } catch (e: Exception) {
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    throw IllegalStateException("Phiên đăng nhập đã cũ, vui lòng đăng nhập lại trước khi đổi email.")
                } else {
                    throw e
                }
            }
        }

        val data = buildMap<String, Any> {
            fullName?.let { put("displayName", it) }
            email?.let { put("email", it) }
            dayOfBirth?.let { put("dayOfBirth", it) }
            gender?.let { put("gender", it) }
            bio?.let { put("bio", it) }
            put("updatedAt", FieldValue.serverTimestamp())
        }
        if (data.isNotEmpty()) {
            userDoc(uid).set(data, SetOptions.merge()).await()
        }
    }

    // ===================== UPDATE AVATAR =====================
    override suspend fun updateAvatar(localUri: String): String {
        val uid = requireUid()
        val ref = storage.reference.child("avatars/$uid.jpg")
        ref.putFile(Uri.parse(localUri)).await()
        val url = ref.downloadUrl.await().toString()

        userDoc(uid).set(
            mapOf(
                "avatarUrl" to url,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
        return url
    }

    // ===================== FOLLOW =====================
    override suspend fun setFollow(targetUid: String, follow: Boolean) {
        val me = requireUid()
        val meDoc = userDoc(me)
        val targetDoc = userDoc(targetUid)
        val followingRef = meDoc.collection("following").document(targetUid)
        val followerRef = targetDoc.collection("followers").document(me)

        db.runTransaction { t ->
            val already = t.get(followingRef).exists()

            if (follow && !already) {
                t.set(followingRef, mapOf("at" to FieldValue.serverTimestamp()))
                t.set(followerRef, mapOf("at" to FieldValue.serverTimestamp()))
                t.update(meDoc, "following", FieldValue.increment(1))
                t.update(targetDoc, "followers", FieldValue.increment(1))
            } else if (!follow && already) {
                t.delete(followingRef)
                t.delete(followerRef)
                t.update(meDoc, "following", FieldValue.increment(-1))
                t.update(targetDoc, "followers", FieldValue.increment(-1))
            }
            null
        }.await()
    }

    override suspend fun isFollowing(targetUid: String): Boolean {
        val me = requireUid()
        val snap = userDoc(me).collection("following").document(targetUid).get().await()
        return snap.exists()
    }

    // ===================== CHANGE PASSWORD =====================
    override suspend fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser ?: error("Chưa đăng nhập")
        val email = user.email ?: error("Tài khoản không có email")
        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    // ===================== POSTS (PAGINATED) =====================
    override suspend fun getUserPosts(uid: String, cursor: String?): Paged<Post> {
        val pageSize = DEFAULT_PAGE_SIZE
        var q = posts()
            .whereEqualTo("uid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize)

        tsFromCursor(cursor)?.let { q = q.startAfter(it) }

        val snaps = q.get().await().documents
        if (snaps.isEmpty()) return Paged(emptyList(), null)

        // lấy thông tin tác giả một lần
        val author = fetchUser(uid)

        val items = snaps.mapNotNull { it.toPostDomain(author = author) }
        val next = nextCursorFrom(snaps.last())
        return Paged(items = items, nextCursor = next)
    }

    // ===================== SAVES (PAGINATED) =====================
    override suspend fun getUserSaves(uid: String, cursor: String?): Paged<Post> {
        val pageSize = DEFAULT_PAGE_SIZE
        var q = savesCol(uid)
            .orderBy("at", Query.Direction.DESCENDING)
            .limit(pageSize)

        tsFromCursor(cursor)?.let { q = q.startAfter(it) }

        val saveDocs = q.get().await().documents
        if (saveDocs.isEmpty()) return Paged(emptyList(), null)

        // gom postId
        val postIds = saveDocs.mapNotNull { it.getString("postId") }
        // tải lần lượt (có thể tối ưu bằng batch/whereIn ≤ 10)
        val postsSnap = postIds.map { pid -> posts().document(pid).get().await() }

        // cache user theo uid để tránh gọi trùng
        val authorCache = HashMap<String, User>()
        fun getAuthor(authorUid: String): User = authorCache.getOrPut(authorUid) { runBlockingFetchUser(authorUid) }

        val items = postsSnap.mapNotNull { pSnap ->
            val authorUid = pSnap.getString("uid") ?: return@mapNotNull null
            val author = getAuthor(authorUid)
            pSnap.toPostDomain(author = author, forceSaved = true)
        }

        val next = saveDocs.last().getTimestamp("at")?.toDate()?.time?.toString()
        return Paged(items = items, nextCursor = next)
    }

    // ===================== ACTIVITIES (PAGINATED) =====================
    override suspend fun getUserActivities(uid: String, cursor: String?): Paged<ActivityItem> {
        val pageSize = DEFAULT_PAGE_SIZE
        var q = activitiesCol(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize)

        tsFromCursor(cursor)?.let { q = q.startAfter(it) }

        val docs = q.get().await().documents
        if (docs.isEmpty()) return Paged(emptyList(), null)

        // cache user theo uid
        val userCache = HashMap<String, User>()
        fun getUserCached(u: String): User = userCache.getOrPut(u) { runBlockingFetchUser(u) }

        val items = docs.mapNotNull { d ->
            val actorUid = d.getString("actorUid") ?: return@mapNotNull null
            val type = parseActivityType(d.getString("type"))
            val createdAtMs = d.getTimestamp("createdAt")?.toDate()?.time ?: 0L
            val targetPostIdStr = d.getString("postId")
            val targetPostIdLong = targetPostIdStr?.let { hashToLong(it) }

            ActivityItem(
                id = if (createdAtMs > 0L) createdAtMs else hashToLong(d.id),
                actor = getUserCached(actorUid),
                type = type,
                targetPostId = targetPostIdLong,
                createdAt = createdAtMs
            )
        }

        val next = docs.last().getTimestamp("createdAt")?.toDate()?.time?.toString()
        return Paged(items = items, nextCursor = next)
    }

    // ===================== HELPERS & MAPPERS =====================

    private fun tsFromCursor(cursor: String?): Timestamp? =
        cursor?.toLongOrNull()?.let { ms -> Timestamp(Date(ms)) }

    private fun nextCursorFrom(last: DocumentSnapshot): String? =
        last.getTimestamp("createdAt")?.toDate()?.time?.toString()

    private suspend fun fetchUser(uid: String): User {
        val snap = userDoc(uid).get().await()
        return snap.toUserDomain(uid)
    }

    // Khi cần dùng trong map sync (cache), tránh suspend trong lambda
    private fun runBlockingFetchUser(uid: String): User {
        // dùng Tasks.await thay vì runBlocking để không chặn main thread — nhưng ở đây repo đang gọi trong IO context
        val snap = userDoc(uid).get().result ?: throw IllegalStateException("Không tải được user $uid")
        return snap.toUserDomain(uid)
    }

    private fun DocumentSnapshot.toUserDomain(uid: String): User {
        val email = getString("email") ?: ""
        val displayName = getString("displayName")
        val avatarUrl = getString("avatarUrl")
        // Model User(id=String, email, displayName, avatarUrl)
        return User(
            id = uid,
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl
        )
    }

    private fun DocumentSnapshot.toProfileDomain(): Profile? {
        if (!exists()) return null

        val uid = id
        val email = getString("email") ?: ""
        val displayName = getString("displayName")
        val avatarUrl = getString("avatarUrl")
        val posts = getLong("posts")?.toInt() ?: 0
        val following = getLong("following")?.toInt() ?: 0
        val followers = getLong("followers")?.toInt() ?: 0
        val bio = getString("bio")
        val dayOfBirth = getString("dayOfBirth")
        val gender = getString("gender")

        val user = User(
            id = uid,
            email = email,
            displayName = displayName,
            avatarUrl = avatarUrl
        )

        return Profile(
            user = user,
            posts = posts,
            following = following,
            followers = followers,
            bio = bio,
            dayOfBirth = dayOfBirth,
            gender = gender
        )
    }

    private fun DocumentSnapshot.toPostDomain(author: User, forceSaved: Boolean = false): Post? {
        if (!exists()) return null
        val idLong = getLong("numericId") ?: hashToLong(id)
        val content = getString("content")
        @Suppress("UNCHECKED_CAST")
        val images = (get("images") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val createdAtMs = getTimestamp("createdAt")?.toDate()?.time ?: 0L
        val likeCount = getLong("likeCount")?.toInt() ?: 0
        val commentCount = getLong("commentCount")?.toInt() ?: 0

        return Post(
            id = idLong,
            author = author,
            content = content,
            images = images,
            createdAt = createdAtMs,
            likeCount = likeCount,
            commentCount = commentCount,
            isSaved = forceSaved
        )
    }

    private fun parseActivityType(s: String?): ActivityType =
        when (s?.uppercase()) {
            "FOLLOWED_YOU" -> ActivityType.FOLLOWED_YOU
            "LIKED_POST" -> ActivityType.LIKED_POST
            "COMMENTED_POST" -> ActivityType.COMMENTED_POST
            "NEW_POST" -> ActivityType.NEW_POST
            else -> ActivityType.NEW_POST
        }

    private fun hashToLong(s: String): Long = abs(s.hashCode().toLong())
}
