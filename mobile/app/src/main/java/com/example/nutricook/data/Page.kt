package com.example.nutricook.data

// Class dùng chung để bọc dữ liệu phân trang
data class Page<T>(
    val items: List<T>,
    val nextCursor: String?
)