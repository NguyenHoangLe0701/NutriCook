package com.example.nutricook.data.profile

import android.net.Uri
import com.example.nutricook.model.profile.Profile
import com.example.nutricook.model.user.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseProfileRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    companion object {
        private const val USERS = "users"
    }

    private fun requireUid(): String =
        auth.currentUser?.uid ?: error("Chưa đăng nhập")

    private fun users() = db.collection(USERS)
    private fun userDoc(uid: String) = users().document(uid)

    // ===================== FLOW =====================
    override fun myProfileFlow(): Flow<Profile?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val reg = userDoc(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.toDomain())
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

        return snap.toDomain() ?: error("Không tạo được hồ sơ")
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

        // Map dữ liệu để lưu Firestore
        val data = buildMap<String, Any> {
            fullName?.let { put("displayName", it) }
            email?.let { put("email", it) }
            dayOfBirth?.let { put("dayOfBirth", it) }
            gender?.let { put("gender", it) }
            bio?.let { put("bio", it) }
            put("updatedAt", FieldValue.serverTimestamp())
        }

        // ❗ Dùng set(..., merge) để nếu doc chưa tồn tại thì sẽ tạo mới
        if (data.isNotEmpty()) {
            userDoc(uid).set(data, SetOptions.merge()).await()
        }
    }

    // ===================== UPDATE AVATAR =====================
    override suspend fun updateAvatar(localUri: String): String {
        val uid = requireUid()
        val ref = storage.reference.child("avatars/$uid.jpg")

        // upload
        ref.putFile(Uri.parse(localUri)).await()

        // lấy url
        val url = ref.downloadUrl.await().toString()

        // lưu vào firestore (merge để không lỗi nếu doc chưa có)
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

    // ===================== MAPPING =====================
    private fun DocumentSnapshot.toDomain(): Profile? {
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
}
