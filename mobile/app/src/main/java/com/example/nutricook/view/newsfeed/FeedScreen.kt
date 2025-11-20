package com.example.nutricook.view.newsfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.viewmodel.newsfeed.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    vm: PostViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Bảng tin NutriCook",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF0F2F5)) // Màu nền xám nhẹ giống Facebook
        ) {
            if (state.loading && state.items.isEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Thanh đăng bài nhanh (Giống "Bạn đang nghĩ gì?")
                    item {
                        CreatePostBar(onClick = { showCreateDialog = true })
                    }

                    // 2. Danh sách bài viết
                    itemsIndexed(
                        items = state.items,
                        key = { _, post -> post.id }
                    ) { index, post ->
                        // Logic load more (Infinite Scroll)
                        if (index >= state.items.size - 2 && state.hasMore && !state.loadingMore) {
                            LaunchedEffect(Unit) { vm.loadMore() }
                        }

                        PostItem(
                            post = post,
                            currentUserId = currentUserId,
                            onDelete = { postId -> vm.deletePost(postId) }
                        )
                    }

                    // 3. Loading khi cuộn xuống đáy
                    if (state.loadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            // Hiển thị lỗi nếu có
            state.error?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
                ) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    // Dialog tạo bài viết mới
    if (showCreateDialog) {
        CreatePostDialog(
            onDismiss = { showCreateDialog = false },
            onSubmit = { content, imageUrl ->
                vm.createPost(content, imageUrl) // Gọi hàm mới có 2 tham số
                showCreateDialog = false
            }
        )
    }
}

// Component: Thanh đăng bài nhanh ở đầu trang
@Composable
fun CreatePostBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.padding(4.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f).height(36.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Bạn đang nghĩ gì?", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Outlined.Image, contentDescription = "Photo", tint = Color(0xFF4CAF50))
        }
    }
}

// Component: Một bài đăng (Post)
@Composable
fun PostItem(
    post: Post,
    currentUserId: String,
    onDelete: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.extraSmall, // Bo góc rất ít hoặc vuông để giống Newsfeed
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp) // Khoảng cách giữa các bài
    ) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
            // 1. Header: Avatar + Tên + Ngày + Nút Xóa
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.padding(4.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author.email.substringBefore("@"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = formatTimestamp(post.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Nút xóa (chỉ hiện nếu là chủ bài viết)
                if (post.author.id == currentUserId) {
                    IconButton(onClick = { onDelete(post.id) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Nội dung Text
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            // 3. Ảnh bài viết (Nếu có)
            if (!post.imageUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
            }

            // 4. Footer: Like, Comment (UI Dummy)
            Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = {}) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Thích", color = Color.Gray)
                }
                TextButton(onClick = {}) {
                    Icon(Icons.Default.Comment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Bình luận", color = Color.Gray)
                }
            }
        }
    }
}

// Dialog tạo bài viết (Hỗ trợ nhập Text và Link ảnh)
@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onSubmit: (String, String?) -> Unit) {
    var text by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var showUrlInput by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tạo bài viết", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Input Text
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Bạn đang nghĩ gì thế?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // Preview ảnh
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }

                // Input Link Ảnh (Hiện khi bấm nút ảnh)
                if (showUrlInput) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Dán đường link ảnh vào đây") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toolbar Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { showUrlInput = !showUrlInput }) {
                        Icon(Icons.Outlined.Image, contentDescription = "Add Image", tint = Color(0xFF4CAF50))
                    }

                    Button(
                        onClick = { onSubmit(text, imageUrl.ifBlank { null }) },
                        enabled = text.isNotBlank() || imageUrl.isNotBlank()
                    ) {
                        Text("Đăng")
                    }
                }
            }
        }
    }
}

// Hàm format thời gian
fun formatTimestamp(timestamp: Long): String {
    return if (timestamp > 0) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } else {
        "Vừa xong"
    }
}