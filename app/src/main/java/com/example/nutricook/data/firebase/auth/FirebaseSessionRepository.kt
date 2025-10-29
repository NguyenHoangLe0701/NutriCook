package com.example.nutricook.data.firebase.auth

import com.example.nutricook.model.user.IUser
import com.example.nutricook.model.repository.auth.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSessionRepository @Inject constructor(
    private val ds: FirebaseAuthDataSource
) : SessionRepository {

    override val currentUser: Flow<IUser?> =
        ds.authState.map { it?.toDomain() }

    override suspend fun signOut() {
        ds.signOut()
    }
}
