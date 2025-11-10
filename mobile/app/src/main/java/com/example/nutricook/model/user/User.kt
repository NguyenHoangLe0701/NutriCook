package com.example.nutricook.model.user

data class User(
    override val id: String,
    override val email: String,
    override val displayName: String? = null,
    override val avatarUrl: String? = null
) : IUser