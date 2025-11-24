package com.example.nutricook.view.newsfeed

import android.net.Uri
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.viewmodel.newsfeed.PostViewModel
import com.google.firebase.auth.FirebaseAuth

// --- MÀU SẮC CHỦ ĐẠO ---
val NutriGreen = Color(0xFF4CAF50)
val NutriBackground = Color(0xFFF5F7F6)
val RedHeart = Color(0xFFE91E63)
val TextPrimary = Color(0xFF1E1E1E)
val TextSecondary = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsfeedScreen(
    viewModel: PostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Lấy User hiện tại để check xem đã like/save chưa
    val currentUser = FirebaseAuth.getInstance().currentUser

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NutriCook Feed", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NutriGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Đăng bài")
            }
        },
        containerColor = NutriBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (state.items.isEmpty() && !state.loading) {
                // Màn hình trống
                EmptyView()
            } else {
                // Danh sách bài viết
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(state.items) { index, post ->
                        // Tự động load thêm khi cuộn xuống cuối
                        if (index >= state.items.size - 2 && !state.loadingMore) {
                            LaunchedEffect(Unit) { viewModel.loadMore() }
                        }

                        PostCard(
                            post = post,
                            currentUserId = currentUser?.uid ?: "",
                            onLike = { viewModel.toggleLike(post) },
                            onSave = { viewModel.toggleSave(post) },
                            onDelete = { viewModel.deletePost(post.id) },
                            onComment = { /* TODO: Mở màn hình comment sau */ }
                        )
                    }

                    if (state.loadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = NutriGreen, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            // Loading ban đầu
            if (state.loading && state.items.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = NutriGreen)
            }

            // Hiển thị lỗi nếu có
            if (state.error != null && state.items.isEmpty()) {
                ErrorView(state.error!!) { viewModel.loadFeed() }
            }
        }

        // Dialog đăng bài (Phiên bản nâng cấp chọn ảnh)
        if (showCreateDialog) {
            CreatePostAdvancedDialog(
                onDismiss = { showCreateDialog = false },
                onSubmit = { content, uri ->
                    viewModel.createPostWithImage(content, uri)
                    showCreateDialog = false
                }
            )
        }
    }
}

// --- ITEM BÀI VIẾT (CARD) ---
@Composable
fun PostCard(
    post: Post,
    currentUserId: String,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onDelete: (String) -> Unit,
    onComment: () -> Unit
) {
    // Kiểm tra trạng thái Like/Save
    val isLiked = post.likes.contains(currentUserId)
    val isSaved = post.saves.contains(currentUserId)
    val isAuthor = post.author.id == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(NutriGreen.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.email.take(1).uppercase(),
                        fontWeight = FontWeight.Bold, color = NutriGreen
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.author.email.substringBefore("@"), fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(
                        DateUtils.getRelativeTimeSpanString(post.createdAt).toString(),
                        fontSize = 12.sp, color = TextSecondary
                    )
                }

                // Nếu là tác giả thì hiện nút xóa (Tạm thời dùng Icon Close nhỏ gọn)
                if (isAuthor) {
                    IconButton(onClick = { onDelete(post.id) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa", tint = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Nội dung Text
            if (post.content.isNotBlank()) {
                Text(post.content, fontSize = 15.sp, lineHeight = 22.sp, color = TextPrimary)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Ảnh (Coil AsyncImage)
            if (!post.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 350.dp) // Chiều cao linh hoạt
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = Color.LightGray.copy(0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // 4. Action Buttons (Like, Comment, Save)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- Nút Like ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike() }.padding(4.dp)
                ) {
                    // Animation màu sắc
                    val iconColor by animateColorAsState(if (isLiked) RedHeart else TextSecondary, label = "LikeColor")
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = iconColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.likes.size}", color = iconColor)
                }

                // --- Nút Comment ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onComment() }.padding(4.dp)
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment", tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${post.commentCount}", color = TextSecondary)
                }

                // Nút Share (Trang trí)
                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = TextSecondary)

                // --- Nút Save ---
                IconButton(onClick = onSave) {
                    val saveColor by animateColorAsState(if (isSaved) NutriGreen else TextSecondary, label = "SaveColor")
                    Icon(
                        imageVector = if (isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = saveColor
                    )
                }
            }
        }
    }
}

// --- DIALOG ĐĂNG BÀI NÂNG CẤP (Image Picker) ---
@Composable
fun CreatePostAdvancedDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Uri?) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Khởi tạo Launcher để mở thư viện ảnh
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Chia sẻ món ngon", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NutriGreen)
                Spacer(modifier = Modifier.height(16.dp))

                // Input Text
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Hôm nay bạn nấu gì?...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NutriGreen,
                        cursorColor = NutriGreen
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Khu vực hiển thị ảnh đã chọn
                if (selectedImageUri != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Nút Xóa ảnh (Góc trên phải)
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                .background(Color.Black.copy(0.5f), CircleShape).size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    // Nút bấm chọn ảnh
                    OutlinedButton(
                        onClick = {
                            // Mở thư viện ảnh (Chỉ chọn ảnh)
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NutriGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = NutriGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thêm ảnh từ thư viện", color = NutriGreen)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSubmit(content, selectedImageUri) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NutriGreen),
                    shape = RoundedCornerShape(12.dp),
                    // Chỉ cho bấm khi có nội dung hoặc có ảnh
                    enabled = content.isNotBlank() || selectedImageUri != null
                ) {
                    Text("Đăng ngay", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- CÁC HELPER VIEW ---
@Composable
fun EmptyView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Chưa có bài viết nào", color = TextSecondary)
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Có lỗi xảy ra!", color = Color.Red, fontWeight = FontWeight.Bold)
        Text(error, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = NutriGreen)) {
            Text("Thử lại")
        }
    }
}