package com.example.nutricook.viewmodel.newsfeed

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.newsfeed.PostRepository
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
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ListState<Post>())
    val state: StateFlow<ListState<Post>> = _state

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

    // --- 1. Load Feed ---
    fun loadFeed() {
        if (pagingInFlight) return
        _state.value = ListState(loading = true)
        viewModelScope.launch {
            runCatching { repo.getNewsFeed(cursor = null) }
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _state.value = ListState(loading = false, items = page.items, hasMore = page.nextCursor != null)
                }
                .onFailure { e ->
                    _state.value = ListState(loading = false, error = e.message ?: "Lỗi tải dữ liệu")
                }
        }
    }

    // --- 2. Load More ---
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
                    _state.value = _state.value.copy(
                        items = currentItems + page.items,
                        hasMore = page.nextCursor != null,
                        loadingMore = false
                    )
                }
                .onFailure { _state.value = _state.value.copy(loadingMore = false) }
            pagingInFlight = false
        }
    }

    // --- 3. Đăng bài (Có xử lý Upload ảnh) ---
    fun createPostWithImage(content: String, imageUri: Uri?) {
        if (content.isBlank() && imageUri == null) return

        // Set trạng thái loading để UI hiển thị vòng quay hoặc disable nút bấm
        _state.value = _state.value.copy(loading = true)

        viewModelScope.launch {
            var finalImageUrl: String? = null

            // Bước 1: Nếu có ảnh, upload lên Storage trước
            if (imageUri != null) {
                finalImageUrl = repo.uploadImageToStorage(imageUri)
                if (finalImageUrl == null) {
                    // Upload thất bại
                    _state.value = _state.value.copy(loading = false, error = "Không thể tải ảnh lên")
                    return@launch
                }
            }

            // Bước 2: Gọi Repository tạo bài viết với link ảnh (nếu có)
            runCatching {
                repo.createPost(content, finalImageUrl)
            }
                .onSuccess { newPost ->
                    val currentList = _state.value.items
                    // Thêm bài mới vào đầu danh sách ngay lập tức
                    _state.value = _state.value.copy(
                        items = listOf(newPost) + currentList,
                        loading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(loading = false, error = e.message)
                }
        }
    }

    // Giữ hàm cũ để tương thích nếu cần, trỏ về hàm mới
    fun createPost(content: String, imageUrl: String?) {
        // Hàm này ít dùng hơn vì giờ ta dùng Uri
        viewModelScope.launch {
            runCatching { repo.createPost(content, imageUrl) }
                .onSuccess { newPost ->
                    val currentList = _state.value.items
                    _state.value = _state.value.copy(items = listOf(newPost) + currentList)
                }
        }
    }

    // --- 4. Xóa bài ---
    fun deletePost(postId: String) {
        viewModelScope.launch {
            runCatching { repo.deletePost(postId) }
                .onSuccess {
                    val newList = _state.value.items.filter { it.id != postId }
                    _state.value = _state.value.copy(items = newList)
                }
        }
    }

    // --- 5. Toggle Like (Optimistic Update) ---
    fun toggleLike(post: Post) {
        val uid = auth.currentUser?.uid ?: return

        // [Optimistic Update] Cập nhật UI ngay lập tức cho mượt, không cần chờ Server
        val currentItems = _state.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == post.id }

        if (index != -1) {
            val updatedLikes = if (post.likes.contains(uid)) {
                post.likes - uid
            } else {
                post.likes + uid
            }
            // Tạo bản sao Post mới với danh sách like mới
            val updatedPost = post.copy(likes = updatedLikes)
            currentItems[index] = updatedPost

            // Cập nhật State
            _state.value = _state.value.copy(items = currentItems)
        }

        // Gọi xuống server (Chạy ngầm)
        viewModelScope.launch {
            repo.toggleLike(post.id, uid)
        }
    }

    // --- 6. Toggle Save (Optimistic Update) ---
    fun toggleSave(post: Post) {
        val uid = auth.currentUser?.uid ?: return

        val currentItems = _state.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.id == post.id }

        if (index != -1) {
            val updatedSaves = if (post.saves.contains(uid)) {
                post.saves - uid
            } else {
                post.saves + uid
            }
            val updatedPost = post.copy(saves = updatedSaves)
            currentItems[index] = updatedPost
            _state.value = _state.value.copy(items = currentItems)
        }

        viewModelScope.launch {
            repo.toggleSave(post.id, uid)
        }
    }
}