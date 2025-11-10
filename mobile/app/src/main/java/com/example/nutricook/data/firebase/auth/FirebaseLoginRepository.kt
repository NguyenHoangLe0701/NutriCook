package com.example.nutricook.data.firebase.auth

import com.example.nutricook.model.user.IUser
import com.example.nutricook.model.repository.auth.LoginRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseLoginRepository @Inject constructor(
    private val ds: FirebaseAuthDataSource
) : LoginRepository {
    override suspend fun signIn(email: String, password: String): Result<IUser> =
        runCatching { ds.signIn(email, password).toDomain() }
}
