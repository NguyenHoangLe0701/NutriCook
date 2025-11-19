package com.example.nutricook.di

import com.example.nutricook.data.nutrition.FsMealRepository
import com.example.nutricook.data.nutrition.IMealRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MealModule {
    @Binds
    @Singleton
    abstract fun bindMealRepository(impl: FsMealRepository): IMealRepository
}
