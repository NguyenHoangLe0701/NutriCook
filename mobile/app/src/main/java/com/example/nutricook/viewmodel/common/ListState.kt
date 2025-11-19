package com.example.nutricook.viewmodel.common

data class ListState<T>(
    val loading: Boolean = false,
    val items: List<T> = emptyList(),
    val error: String? = null,

    // cho phân trang
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false
)
