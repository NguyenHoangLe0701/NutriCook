package com.example.nutricook.viewmodel.newsfeed

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.Page
import com.example.nutricook.data.hotnews.HotNewsRepository
import com.example.nutricook.data.newsfeed.PostRepository
import com.example.nutricook.model.hotnews.HotNewsArticle
import com.example.nutricook.model.newsfeed.FeedItem
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.viewmodel.common.ListState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repo: PostRepository,
    private val hotNewsRepo: HotNewsRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ListState<FeedItem>())
    val state: StateFlow<ListState<FeedItem>> = _state

    private var nextCursor: String? = null
    private var pagingInFlight = false

    init {
        monitorAuthAndLoad()
    }

    private fun monitorAuthAndLoad() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("DEBUG_FEED", "ViewModel: Đã có User -> LoadFeed.")
            loadFeed()
        } else {
            Log.d("DEBUG_FEED", "ViewModel: Chờ AuthStateListener...")
            _state.value = ListState(loading = true)
            var listener: FirebaseAuth.AuthStateListener? = null
            listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    loadFeed()
                    listener?.let { firebaseAuth.removeAuthStateListener(it) }
                }
            }
            auth.addAuthStateListener(listener)
        }
    }

    // --- 1. Load Feed (bao gồm cả Post và Hot News) ---
    fun loadFeed() {
        if (pagingInFlight) return
        _state.value = ListState(loading = true)
        viewModelScope.launch {
            try {
                // Load cả Post và Hot News
                val postsResult = runCatching { repo.getNewsFeed(cursor = null) }
                val hotNewsResult = runCatching { hotNewsRepo.getAllHotNews() }
                
                val posts = postsResult.getOrElse { Page(emptyList<Post>(), null) }
                val hotNews = hotNewsResult.getOrElse { emptyList<HotNewsArticle>() }
                
                // Chuyển đổi sang FeedItem và merge lại
                val feedItems = mutableListOf<FeedItem>()
                feedItems.addAll(posts.items.map { FeedItem.PostItem(it) })
                feedItems.addAll(hotNews.map { FeedItem.HotNewsItem(it) })
                
                // Sắp xếp theo thời gian tạo (mới nhất trước)
                feedItems.sortByDescending { it.createdAt }
                
                nextCursor = postsResult.getOrNull()?.nextCursor
                _state.value = ListState(
                    loading = false,
                    items = feedItems,
                    hasMore = nextCursor != null
                )
            } catch (e: Exception) {
                _state.value = ListState(loading = false, error = e.message ?: "Lỗi tải dữ liệu")
            }
        }
    }

    // --- 2. Load More (chỉ load thêm Post, Hot News đã load hết) ---
    fun loadMore() {
        val cursor = nextCursor ?: return
        if (pagingInFlight || _state.value.loadingMore) return
        pagingInFlight = true
        _state.value = _state.value.copy(loadingMore = true)

        viewModelScope.launch {
            runCatching { repo.getNewsFeed(cursor) }
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    val currentItems = _state.value.items
                    val newPostItems = page.items.map { FeedItem.PostItem(it) }
                    val allItems = (currentItems + newPostItems).sortedByDescending { it.createdAt }
                    _state.value = _state.value.copy(
                        items = allItems,
                        hasMore = page.nextCursor != null,
                        loadingMore = false
                    )
                }
                .onFailure { _state.value = _state.value.copy(loadingMore = false) }
            pagingInFlight = false
        }
    }

    // --- 3. Đăng bài (CẬP NHẬT QUAN TRỌNG: THÊM TITLE) ---
    fun createPostWithImage(title: String, content: String, imageUri: Uri?) {
        // Kiểm tra validation đơn giản
        if (content.isBlank() && imageUri == null) return

        _state.value = _state.value.copy(loading = true)

        viewModelScope.launch {
            var finalImageUrl: String? = null

            // Bước 1: Upload ảnh nếu có
            if (imageUri != null) {
                finalImageUrl = repo.uploadImageToStorage(imageUri)
                if (finalImageUrl == null) {
                    _state.value = _state.value.copy(loading = false, error = "Không thể tải ảnh lên")
                    return@launch
                }
            }

            // Bước 2: Gọi Repository tạo bài viết (truyền title vào)
            runCatching {
                repo.createPost(title, content, finalImageUrl)
            }
                .onSuccess { newPost ->
                    val currentList = _state.value.items
                    val newFeedItem = FeedItem.PostItem(newPost)
                    val allItems = (listOf(newFeedItem) + currentList).sortedByDescending { it.createdAt }
                    _state.value = _state.value.copy(
                        items = allItems,
                        loading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(loading = false, error = e.message)
                }
        }
    }

    // --- 4. Xóa bài (có thể là Post hoặc Hot News) ---
    fun deletePost(postId: String) {
        viewModelScope.launch {
            val item = _state.value.items.find { it.id == postId }
            when (item) {
                is FeedItem.PostItem -> {
                    runCatching { repo.deletePost(postId) }
                        .onSuccess {
                            val newList = _state.value.items.filter { it.id != postId }
                            _state.value = _state.value.copy(items = newList)
                        }
                }
                is FeedItem.HotNewsItem -> {
                    runCatching { hotNewsRepo.deleteHotNews(postId) }
                        .onSuccess { success ->
                            if (success) {
                                val newList = _state.value.items.filter { it.id != postId }
                                _state.value = _state.value.copy(items = newList)
                            }
                        }
                }
                null -> {}
            }
        }
    }

    // --- 5. Toggle Like (Optimistic Update) - Hỗ trợ cả Post và Hot News ---
    fun toggleLike(item: FeedItem) {
        val uid = auth.currentUser?.uid ?: return

        // [Optimistic Update] Cập nhật UI ngay lập tức
        val currentItems = _state.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == item.id }

        if (index != -1) {
            val updatedItem = when (item) {
                is FeedItem.PostItem -> {
                    val updatedLikes = if (item.post.likes.contains(uid)) {
                        item.post.likes - uid
                    } else {
                        item.post.likes + uid
                    }
                    FeedItem.PostItem(item.post.copy(likes = updatedLikes))
                }
                is FeedItem.HotNewsItem -> {
                    val updatedLikes = if (item.article.likes.contains(uid)) {
                        item.article.likes - uid
                    } else {
                        item.article.likes + uid
                    }
                    FeedItem.HotNewsItem(item.article.copy(likes = updatedLikes))
                }
            }
            currentItems[index] = updatedItem
            _state.value = _state.value.copy(items = currentItems)
        }

        // Gọi xuống server
        viewModelScope.launch {
            when (item) {
                is FeedItem.PostItem -> repo.toggleLike(item.post.id, uid)
                is FeedItem.HotNewsItem -> hotNewsRepo.toggleLike(item.article.id, uid)
            }
        }
    }

    // --- 6. Toggle Save (Optimistic Update) - Hỗ trợ cả Post và Hot News ---
    fun toggleSave(item: FeedItem) {
        val uid = auth.currentUser?.uid ?: return

        val currentItems = _state.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == item.id }

        if (index != -1) {
            val updatedItem = when (item) {
                is FeedItem.PostItem -> {
                    val updatedSaves = if (item.post.saves.contains(uid)) {
                        item.post.saves - uid
                    } else {
                        item.post.saves + uid
                    }
                    FeedItem.PostItem(item.post.copy(saves = updatedSaves))
                }
                is FeedItem.HotNewsItem -> {
                    val updatedSaves = if (item.article.saves.contains(uid)) {
                        item.article.saves - uid
                    } else {
                        item.article.saves + uid
                    }
                    FeedItem.HotNewsItem(item.article.copy(saves = updatedSaves))
                }
            }
            currentItems[index] = updatedItem
            _state.value = _state.value.copy(items = currentItems)
        }

        viewModelScope.launch {
            when (item) {
                is FeedItem.PostItem -> repo.toggleSave(item.post.id, uid)
                is FeedItem.HotNewsItem -> hotNewsRepo.toggleSave(item.article.id, uid)
            }
        }
    }
}