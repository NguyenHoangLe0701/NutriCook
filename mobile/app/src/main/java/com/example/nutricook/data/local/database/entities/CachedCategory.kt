package com.example.nutricook.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity để cache category data từ Firestore
 */
@Entity(tableName = "cached_categories")
data class CachedCategory(
    @PrimaryKey
    val id: Long,
    val name: String,
    val icon: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

