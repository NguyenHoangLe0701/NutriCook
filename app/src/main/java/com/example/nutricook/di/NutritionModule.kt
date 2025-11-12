package com.example.nutricook.di

import android.content.Context
import com.example.nutricook.data.nutrition.INutritionProfileRepository
import com.example.nutricook.data.nutrition.NutritionProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NutritionModule {

    @Provides
    @Singleton
    fun provideNutritionRepo(
        @ApplicationContext ctx: Context
    ): INutritionProfileRepository = NutritionProfileRepository(ctx)
}
