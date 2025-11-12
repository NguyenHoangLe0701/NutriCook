// com/example/nutricook/di/NutritionModule.kt
package com.example.nutricook.di

import com.example.nutricook.data.nutrition.INutritionProfileRepository
import com.example.nutricook.data.nutrition.NutritionProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NutritionModule {
    @Binds @Singleton
    abstract fun bindNutritionProfileRepository(
        impl: NutritionProfileRepository
    ): INutritionProfileRepository
}
