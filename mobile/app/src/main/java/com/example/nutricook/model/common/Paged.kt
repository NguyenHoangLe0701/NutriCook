package com.example.nutricook.model.common

data class Paged<T>(
    val items: List<T>,
    val nextCursor: String? = null
)