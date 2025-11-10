package com.example.nutricook.di

import com.example.nutricook.data.firebase.auth.FirebaseAuthDataSource
import com.example.nutricook.data.firebase.auth.FirebaseLoginRepository
import com.example.nutricook.data.firebase.auth.FirebaseRegisterRepository
import com.example.nutricook.data.firebase.auth.FirebaseSessionRepository
import com.example.nutricook.model.repository.auth.LoginRepository
import com.example.nutricook.model.repository.auth.RegisterRepository
import com.example.nutricook.model.repository.auth.SessionRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthProvideModule {

    @Provides @Singleton
    fun provideAuthDataSource(auth: FirebaseAuth): FirebaseAuthDataSource =
        FirebaseAuthDataSource(auth)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindModule {

    @Binds @Singleton
    abstract fun bindLoginRepository(impl: FirebaseLoginRepository): LoginRepository

    @Binds @Singleton
    abstract fun bindRegisterRepository(impl: FirebaseRegisterRepository): RegisterRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: FirebaseSessionRepository): SessionRepository
}
