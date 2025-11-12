package com.example.nutricook.data.nutrition

import com.example.nutricook.model.nutrition.NutritionProfile
import kotlinx.coroutines.flow.Flow

interface INutritionProfileRepository {
    fun profileFlow(): Flow<NutritionProfile>
    suspend fun get(): NutritionProfile
    suspend fun save(profile: NutritionProfile)
}
