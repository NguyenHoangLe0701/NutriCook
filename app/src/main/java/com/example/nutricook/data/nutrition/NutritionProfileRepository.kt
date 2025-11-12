// file: com/example/nutricook/data/nutrition/NutritionProfileRepository.kt
package com.example.nutricook.data.nutrition

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.nutricook.model.nutrition.NutritionProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val FILE_NAME = "nutrition_profile.json"

@OptIn(ExperimentalSerializationApi::class)
object NutritionProfileSerializer : Serializer<NutritionProfile> {
    override val defaultValue: NutritionProfile = NutritionProfile()

    override suspend fun readFrom(input: InputStream): NutritionProfile =
        runCatching {
            val text = input.readBytes().decodeToString()
            Json { ignoreUnknownKeys = true }.decodeFromString(
                NutritionProfile.serializer(),
                text
            )
        }.getOrElse { defaultValue }

    override suspend fun writeTo(t: NutritionProfile, output: OutputStream) {
        val text = Json.encodeToString(NutritionProfile.serializer(), t)
        output.write(text.encodeToByteArray())
    }
}

private val Context.nutriDataStore: DataStore<NutritionProfile> by dataStore(
    fileName = FILE_NAME,
    serializer = NutritionProfileSerializer
)

@Singleton
class NutritionProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : INutritionProfileRepository {

    private fun uid(): String = auth.currentUser?.uid ?: error("Chưa đăng nhập")
    private fun userDoc() = db.collection("users").document(uid())

    /** Trả RAW từ DataStore (không recalc). */
    override fun profileFlow(): Flow<NutritionProfile> =
        context.nutriDataStore.data

    /** Lấy RAW (không recalc). */
    override suspend fun get(): NutritionProfile =
        context.nutriDataStore.data.first()

    /** Lưu RAW (không recalc) + sync Firestore dạng JSON. */
    override suspend fun save(profile: NutritionProfile) {
        // 1) Local
        context.nutriDataStore.updateData { profile }
        // 2) Remote
        val json = Json.encodeToString(NutritionProfile.serializer(), profile)
        userDoc().set(mapOf("nutrition" to json), SetOptions.merge()).await()
    }

    /** Helper: ưu tiên Firebase nếu có, rồi cache xuống DataStore. */
    suspend fun getFromFirebaseOrLocal(): NutritionProfile {
        val snap = userDoc().get().await()
        val json = snap.getString("nutrition")
        return if (json != null) {
            val parsed = Json.decodeFromString<NutritionProfile>(json)
            context.nutriDataStore.updateData { parsed }
            parsed
        } else {
            get()
        }
    }

    /** Chỉ cập nhật Firestore (không đụng DataStore) khi cần. */
    suspend fun updateNutritionOnly(profile: NutritionProfile) {
        val json = Json.encodeToString(NutritionProfile.serializer(), profile)
        userDoc().set(mapOf("nutrition" to json), SetOptions.merge()).await()
    }
}
