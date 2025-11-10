package com.example.nutricook.di

interface AuthProvider {
    fun currentUserId(): Long
}