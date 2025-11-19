package com.example.nutricook.model.user

interface IUser {
    val id: String
    val email: String
    val displayName: String?
    val avatarUrl: String?
}