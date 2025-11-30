package com.example.nutricook.data.profile

import android.net.Uri
import android.util.Log
import com.example.nutricook.model.common.Paged
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.profile.ActivityItem
import com.example.nutricook.model.profile.ActivityType
import com.example.nutricook.model.profile.Nutrition
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

        val reg = userDoc(uid).addSnapshotListener { snap, e ->
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
        awaitClose { reg.remove() }
    }

    // ===================== GET =====================
    override suspend fun getMyProfile(): Profile =
        getProfileByUid(requireUid())

    override suspend fun getProfileByUid(uid: String): Profile {
        if (uid.isEmpty()) throw Exception("User ID không hợp lệ")

        val doc = userDoc(uid)
        var snap = doc.get().await()

        if (!snap.exists()) {
            val u = auth.currentUser
            val email = u?.email ?: ""
            val displayName = u?.displayName ?: u?.email?.substringBefore("@") ?: ""

            val data = hashMapOf(
                "email" to email,
                "displayName" to displayName,
                "avatarUrl" to (u?.photoUrl?.toString()),
                "posts" to 0,
                "following" to 0,
                "followers" to 0,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            doc.set(data, SetOptions.merge()).await()
            snap = doc.get().await()
        }
        return snap.toProfileDomain() ?: throw Exception("Lỗi parse dữ liệu Profile")
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

        if (!email.isNullOrBlank() && email != current.email) {
            try {
                current.updateEmail(email).await()
            } catch (e: Exception) {
                if (e is FirebaseAuthRecentLoginRequiredException) {
                    throw IllegalStateException("Phiên đăng nhập đã cũ, vui lòng đăng nhập lại.")
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

    // ===================== UPDATE CALORIES TARGET =====================
    override suspend fun updateCaloriesTarget(caloriesTarget: Float) {
        val uid = requireUid()
        userDoc(uid).set(
            mapOf(
                "nutrition.caloriesTarget" to caloriesTarget,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        ).await()
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

    // ===================== POSTS =====================
    override suspend fun getUserPosts(uid: String, cursor: String?): Paged<Post> {
        val pageSize = DEFAULT_PAGE_SIZE
        var q = posts()
            .whereEqualTo("author.id", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize)

        tsFromCursor(cursor)?.let { q = q.startAfter(it) }
        val snaps = q.get().await().documents
        if (snaps.isEmpty()) return Paged(emptyList(), null)

        val author = fetchUser(uid)
        val items = snaps.mapNotNull { it.toPostDomain(author = author) }
        val next = nextCursorFrom(snaps.last())
        return Paged(items = items, nextCursor = next)
    }

    // ===================== SAVES =====================
    override suspend fun getUserSaves(uid: String, cursor: String?): Paged<Post> {
        val pageSize = DEFAULT_PAGE_SIZE
        var q = savesCol(uid)
            .orderBy("at", Query.Direction.DESCENDING)
            .limit(pageSize)

        tsFromCursor(cursor)?.let { q = q.startAfter(it) }
        val saveDocs = q.get().await().documents
        if (saveDocs.isEmpty()) return Paged(emptyList(), null)

        val postIds = saveDocs.mapNotNull { it.getString("postId") }
        if (postIds.isEmpty()) return Paged(emptyList(), null)

        val postsSnap = postIds.map { pid -> posts().document(pid).get() }.map { it.await() }

        val authorUids = postsSnap.mapNotNull { it.getString("author.id") }.distinct()
        val authorCache = mutableMapOf<String, User>()

        for (auid in authorUids) {
            val userSnap = userDoc(auid).get().await()
            authorCache[auid] = userSnap.toUserDomain(auid)
        }

        val items = postsSnap.mapNotNull { pSnap ->
            if (!pSnap.exists()) return@mapNotNull null
            val authorData = pSnap.get("author") as? Map<String, Any>
            val authorUid = authorData?.get("id") as? String ?: return@mapNotNull null

            val author = authorCache[authorUid] ?: return@mapNotNull null
            pSnap.toPostDomain(author = author)
        }

        val next = saveDocs.last().getTimestamp("at")?.toDate()?.time?.toString()
        return Paged(items = items, nextCursor = next)
    }

    // ===================== ACTIVITIES =====================
    override suspend fun getUserActivities(uid: String, cursor: String?): Paged<ActivityItem> {
        // Tạm thời trả về rỗng để tránh lỗi nếu chưa dùng tới
        return Paged(emptyList(), null)
    }

    // ===================== SEARCH =====================
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
    private fun tsFromCursor(cursor: String?): Timestamp? =
        cursor?.toLongOrNull()?.let { ms -> Timestamp(Date(ms)) }

    private fun nextCursorFrom(last: DocumentSnapshot): String? =
        last.getTimestamp("createdAt")?.toDate()?.time?.toString()

    private suspend fun fetchUser(uid: String): User {
        val snap = userDoc(uid).get().await()
        return snap.toUserDomain(uid)
    }

    private fun DocumentSnapshot.toUserDomain(uid: String): User {
        return User(
            id = uid,
            email = getString("email") ?: "",
            displayName = getString("displayName"),
            avatarUrl = getString("avatarUrl")
        )
    }

    // [QUAN TRỌNG] Hàm parse Profile AN TOÀN (Đã thêm try-catch)
    private fun DocumentSnapshot.toProfileDomain(): Profile? {
        if (!exists()) return null

        return try {
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

            var nutritionObj: Nutrition? = null
            val nutMap = get("nutrition") as? Map<String, Any>

            if (nutMap != null) {
                // Xử lý linh hoạt: có thể lồng trong 'targets' hoặc nằm phẳng ở ngoài
                val targets = nutMap["targets"] as? Map<String, Any> ?: nutMap

                val cals = (targets["caloriesTarget"] as? Number)?.toFloat() ?: 2000f
                val pro = (targets["proteinG"] as? Number)?.toFloat() ?: 150f
                val fat = (targets["fatG"] as? Number)?.toFloat() ?: 60f
                val carb = (targets["carbG"] as? Number)?.toFloat() ?: 200f

                nutritionObj = Nutrition(cals, pro, fat, carb)
            }

            val user = User(id = uid, email = email, displayName = displayName, avatarUrl = avatarUrl)
            Profile(user, posts, following, followers, 0, 0, bio, dayOfBirth, gender, nutritionObj)
        } catch (e: Exception) {
            Log.e("FirebaseProfileRepo", "Lỗi parse Profile: ${e.message}")
            null
        }
    }

    // [QUAN TRỌNG] Hàm parse Post AN TOÀN (Đã thêm try-catch)
    private fun DocumentSnapshot.toPostDomain(author: User): Post? {
        if (!exists()) return null

        return try {
            val content = getString("content") ?: ""
            val imageUrl = getString("imageUrl")
            val title = getString("title") ?: "" // Lấy title an toàn

            @Suppress("UNCHECKED_CAST")
            val likes = (get("likes") as? List<String>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val saves = (get("saves") as? List<String>) ?: emptyList()

            val createdAtMs = getLong("createdAt") ?: 0L
            val commentCount = getLong("commentCount")?.toInt() ?: 0

            Post(
                id = id,
                title = title,
                content = content,
                imageUrl = imageUrl,
                author = author,
                createdAt = createdAtMs,
                likes = likes,
                saves = saves,
                commentCount = commentCount
            )
        } catch (e: Exception) {
            Log.e("FirebaseProfileRepo", "Lỗi parse Post: ${e.message}")
            null
        }
    }

    private fun parseActivityType(s: String?): ActivityType = ActivityType.values().find { it.name == s?.uppercase() } ?: ActivityType.NEW_POST
    private fun hashToLong(s: String): Long = abs(s.hashCode().toLong())
}