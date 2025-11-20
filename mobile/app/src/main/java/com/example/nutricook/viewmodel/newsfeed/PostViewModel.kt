package com.example.nutricook.viewmodel.newsfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutricook.data.newsfeed.PostRepository
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.viewmodel.common.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(
    private val repo: PostRepository
) : ViewModel() {

    // State quản lý danh sách bài viết (Loading, Data, Error)
    private val _state = MutableStateFlow(ListState<Post>())
    val state: StateFlow<ListState<Post>> = _state

    // Biến hỗ trợ phân trang
    private var nextCursor: String? = null
    private var pagingInFlight = false

    init {
        // Tự động tải Feed ngay khi ViewModel được khởi tạo
        loadFeed()
    }

    // --- 1. Tải danh sách bài viết (Làm mới) ---
    fun loadFeed() {
        if (pagingInFlight) return

        _state.value = ListState(loading = true)

        viewModelScope.launch {
            runCatching {
                repo.getNewsFeed(cursor = null)
            }
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    _state.value = ListState(
                        loading = false,
                        items = page.items,
                        hasMore = page.nextCursor != null
                    )
                }
                .onFailure { e ->
                    _state.value = ListState(loading = false, error = e.message ?: "Lỗi tải dữ liệu")
                }
        }
    }

    // --- 2. Tải thêm (Infinite Scroll) ---
    fun loadMore() {
        val cursor = nextCursor ?: return
        // Kiểm tra xem có đang load dở hoặc state đang báo loadMore không để tránh gọi trùng
        if (pagingInFlight || _state.value.loadingMore) return

        pagingInFlight = true
        _state.value = _state.value.copy(loadingMore = true)

        viewModelScope.launch {
            runCatching {
                repo.getNewsFeed(cursor)
            }
                .onSuccess { page ->
                    nextCursor = page.nextCursor
                    // Ghép list cũ + list mới
                    val currentItems = _state.value.items
                    _state.value = _state.value.copy(
                        items = currentItems + page.items,
                        hasMore = page.nextCursor != null,
                        loadingMore = false
                    )
                }
                .onFailure {
                    // Nếu lỗi khi load more, chỉ tắt loadingMore, giữ nguyên list cũ
                    _state.value = _state.value.copy(loadingMore = false)
                }
            pagingInFlight = false
        }
    }

    // --- 3. Đăng bài mới (Cập nhật thêm ảnh) ---
    fun createPost(content: String, imageUrl: String?) {
        // Kiểm tra: Nếu cả nội dung và ảnh đều trống thì không đăng
        if (content.isBlank() && imageUrl.isNullOrBlank()) return

        viewModelScope.launch {
            runCatching {
                // Gọi Repo (Lưu ý: Bạn cần cập nhật hàm createPost trong PostRepository để nhận thêm tham số imageUrl)
                repo.createPost(content, imageUrl)
            }
                .onSuccess { newPost ->
                    // UI Trick: Chèn ngay bài mới lên đầu danh sách
                    val currentList = _state.value.items
                    _state.value = _state.value.copy(
                        items = listOf(newPost) + currentList
                    )
                }
                .onFailure { e ->
                    e.printStackTrace()
                    // Có thể thêm logic báo lỗi (VD: set state error)
                }
        }
    }

    // --- 4. Xóa bài viết ---
    fun deletePost(postId: String) {
        viewModelScope.launch {
            runCatching {
                repo.deletePost(postId)
            }
                .onSuccess {
                    // UI Trick: Xóa ngay khỏi danh sách hiển thị bằng cách lọc ID
                    val currentList = _state.value.items
                    val newList = currentList.filter { it.id != postId }

                    _state.value = _state.value.copy(items = newList)
                }
                .onFailure { e ->
                    e.printStackTrace()
                }
        }
    }
}