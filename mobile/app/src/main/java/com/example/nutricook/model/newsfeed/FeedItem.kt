package com.example.nutricook.model.newsfeed

import com.example.nutricook.model.hotnews.HotNewsArticle

/**
 * Sealed class để đại diện cho các item trong feed (có thể là Post hoặc HotNewsArticle)
 */
sealed class FeedItem {
    abstract val id: String
    abstract val createdAt: Long
    
    data class PostItem(val post: Post) : FeedItem() {
        override val id: String get() = post.id
        override val createdAt: Long get() = post.createdAt
    }
    
    data class HotNewsItem(val article: HotNewsArticle) : FeedItem() {
        override val id: String get() = article.id
        override val createdAt: Long get() = article.createdAt
    }
}


