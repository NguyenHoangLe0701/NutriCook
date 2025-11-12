package com.example.nutricook.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * CHỈ một FirebaseCoreModule duy nhất.
 * Cấp phát Auth / Firestore / Storage dùng chung toàn app.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseCoreModule {

    @Provides @Singleton
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}
