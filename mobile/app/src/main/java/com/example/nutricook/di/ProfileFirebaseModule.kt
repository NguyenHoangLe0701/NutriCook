package com.example.nutricook.di

import com.example.nutricook.data.profile.FirebaseProfileRepository
import com.example.nutricook.data.profile.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseCoreModule {
    @Provides @Singleton fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides @Singleton fun provideDb(): FirebaseFirestore = FirebaseFirestore.getInstance()
    @Provides @Singleton fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileBindModule {
    @Binds @Singleton
    abstract fun bindProfileRepository(impl: FirebaseProfileRepository): ProfileRepository
}
