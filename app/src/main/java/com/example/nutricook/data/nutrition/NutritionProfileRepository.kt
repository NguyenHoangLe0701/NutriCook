package com.example.nutricook.data.nutrition

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.example.nutricook.model.nutrition.NutritionProfile
import com.example.nutricook.model.nutrition.recalculate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

private const val FILE_NAME = "nutrition_profile.json"

@OptIn(ExperimentalSerializationApi::class)
object NutritionProfileSerializer : Serializer<NutritionProfile> {
    override val defaultValue: NutritionProfile = NutritionProfile().recalculate()

    override suspend fun readFrom(input: InputStream): NutritionProfile =
        runCatching {
            val text = input.readBytes().decodeToString()
            Json { ignoreUnknownKeys = true }.decodeFromString(
                NutritionProfile.serializer(),
                text
            )
        }.getOrElse { defaultValue }

    override suspend fun writeTo(t: NutritionProfile, output: OutputStream) {
        val text = Json { prettyPrint = false }.encodeToString(NutritionProfile.serializer(), t)
        output.write(text.encodeToByteArray())
    }
}

private val Context.nutriDataStore: DataStore<NutritionProfile> by dataStore(
    fileName = FILE_NAME,
    serializer = NutritionProfileSerializer
)

@Singleton
class NutritionProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : INutritionProfileRepository {

    override fun profileFlow() =
        context.nutriDataStore.data.map { it.recalculate() }

    override suspend fun get(): NutritionProfile =
        context.nutriDataStore.data.first().recalculate()

    override suspend fun save(profile: NutritionProfile) {
        context.nutriDataStore.updateData { profile.recalculate() }
    }
}
