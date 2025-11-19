package com.example.nutricook.data.firebase.auth

import com.example.nutricook.model.user.IUser
import com.example.nutricook.model.repository.auth.RegisterRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRegisterRepository @Inject constructor(
    private val ds: FirebaseAuthDataSource
) : RegisterRepository {
    override suspend fun signUp(email: String, password: String): Result<IUser> =
        runCatching { ds.signUp(email, password).toDomain() }
}
