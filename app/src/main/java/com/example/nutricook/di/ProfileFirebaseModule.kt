package com.example.nutricook.di

import com.example.nutricook.data.profile.FirebaseProfileRepository
import com.example.nutricook.data.profile.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds @Singleton
    abstract fun bindProfileRepository(impl: FirebaseProfileRepository): ProfileRepository
}
