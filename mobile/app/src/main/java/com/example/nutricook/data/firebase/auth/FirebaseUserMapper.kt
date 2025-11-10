package com.example.nutricook.data.firebase.auth

import com.example.nutricook.model.user.User
import com.google.firebase.auth.FirebaseUser

internal fun FirebaseUser.toDomain(): User = User(
    id = uid,
    email = email.orEmpty(),
    displayName = displayName,
    avatarUrl = photoUrl?.toString()
)
