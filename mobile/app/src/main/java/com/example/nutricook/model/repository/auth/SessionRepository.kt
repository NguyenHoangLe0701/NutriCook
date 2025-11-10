package com.example.nutricook.model.repository.auth

import com.example.nutricook.model.user.IUser
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val currentUser: Flow<IUser?>
    suspend fun signOut()
}
