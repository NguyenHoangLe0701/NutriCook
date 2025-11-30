package com.example.nutricook.view.newsfeed

import android.net.Uri
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nutricook.data.newsfeed.CommentRepository
import com.example.nutricook.model.hotnews.HotNewsArticle
import com.example.nutricook.model.newsfeed.Comment
import com.example.nutricook.model.newsfeed.FeedItem
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.viewmodel.newsfeed.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- MODERN COLOR PALETTE ---
val PrimaryGreen = Color(0xFF10B981)
val PrimaryGreenDark = Color(0xFF059669)
val AccentOrange = Color(0xFFF59E0B)
val BackgroundLight = Color(0xFFFAFAFA)
val CardBackground = Color(0xFFFFFFFF)
val HeartRed = Color(0xFFEF4444)
val TextDark = Color(0xFF111827)
val TextMuted = Color(0xFF6B7280)
val DividerColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsfeedScreen(
    navController: NavController? = null,
    viewModel: PostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf<String?>(null) } // postId hoặc articleId
    var commentPostType by remember { mutableStateOf("post") } // "post" hoặc "hotnews"
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Inject CommentRepository
    val context = androidx.compose.ui.platform.LocalContext.current
    val commentRepo = remember {
        val activity = context as? androidx.activity.ComponentActivity
        if (activity != null) {
            EntryPointAccessors.fromActivity(activity, CommentRepositoryEntryPoint::class.java)
                .commentRepository()
        } else {
            null
        }
    }

    // Reload khi quay lại từ CreateHotNewsScreen
    DisposableEffect(navController) {
        val listener = navController?.let { nc ->
            androidx.navigation.NavController.OnDestinationChangedListener { _, destination, _ ->
                if (destination.route == "newsfeed") {
                    nc.previousBackStackEntry?.savedStateHandle?.get<Boolean>("shouldReloadHotNews")?.let {
                        if (it) {
                            viewModel.loadFeed()
                            nc.previousBackStackEntry?.savedStateHandle?.remove<Boolean>("shouldReloadHotNews")
                        }
                    }
                }
            }
        }
        listener?.let { navController?.addOnDestinationChangedListener(it) }
        onDispose {
            listener?.let { navController?.removeOnDestinationChangedListener(it) }
        }
    }

    Scaffold(
        topBar = {
            // [ĐÃ SỬA] Truyền avatarUrl của user hiện tại vào TopBar
            ModernTopBar(currentUserAvatarUrl = currentUser?.photoUrl?.toString())
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Nút tạo Hot News (nằm trên)
                FloatingActionButton(
                    onClick = {
                        navController?.navigate("create_hot_news")
                    },
                    containerColor = AccentOrange,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .scale(0.9f)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        Icons.Default.Whatshot,
                        contentDescription = "Tạo Hot News",
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Nút tạo bài viết (nằm dưới)
                ModernFAB(onClick = { showCreateDialog = true })
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // 1. Hiển thị Loading khi danh sách rỗng và đang tải
            if (state.loading && state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            // 2. Hiển thị Empty View khi không có bài viết và không tải
            else if (!state.loading && state.items.isEmpty()) {
                if (state.error != null) {
                    ModernErrorView(state.error!!) { viewModel.loadFeed() }
                } else {
                    ModernEmptyView()
                }
            }
            // 3. Hiển thị Danh sách bài viết
            else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.items) { index, feedItem ->
                        // Logic Load More (Pagination)
                        if (index >= state.items.size - 2 && !state.loadingMore && state.hasMore) {
                            LaunchedEffect(Unit) { viewModel.loadMore() }
                        }

                        when (feedItem) {
                            is FeedItem.PostItem -> {
                                ModernPostCard(
                                    post = feedItem.post,
                                    currentUserId = currentUser?.uid ?: "",
                                    onLike = { viewModel.toggleLike(feedItem) },
                                    onSave = { viewModel.toggleSave(feedItem) },
                                    onDelete = { viewModel.deletePost(feedItem.post.id) },
                                    onComment = {
                                        showCommentDialog = feedItem.post.id
                                        commentPostType = "post"
                                    }
                                )
                            }
                            is FeedItem.HotNewsItem -> {
                                ModernHotNewsCard(
                                    article = feedItem.article,
                                    currentUserId = currentUser?.uid ?: "",
                                    onLike = { viewModel.toggleLike(feedItem) },
                                    onSave = { viewModel.toggleSave(feedItem) },
                                    onDelete = { viewModel.deletePost(feedItem.article.id) },
                                    onComment = {
                                        showCommentDialog = feedItem.article.id
                                        commentPostType = "hotnews"
                                    },
                                    onClick = {
                                        navController?.navigate("hot_news_detail/${feedItem.article.id}")
                                    }
                                )
                            }
                        }
                    }

                    // Loading indicator ở cuối danh sách khi load more
                    if (state.loadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = PrimaryGreen,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            ModernCreatePostDialog(
                onDismiss = { showCreateDialog = false },
                onSubmit = { title, content, uri ->
                    viewModel.createPostWithImage(title, content, uri)
                    showCreateDialog = false
                }
            )
        }

        // Comment Dialog
        showCommentDialog?.let { postId ->
            if (commentRepo != null) {
                CommentDialog(
                    postId = postId,
                    postType = commentPostType,
                    commentRepository = commentRepo,
                    currentUserId = currentUser?.uid ?: "",
                    onDismiss = {
                        showCommentDialog = null
                        // Reload feed để cập nhật commentCount
                        viewModel.loadFeed()
                    }
                )
            }
        }
    }
}

// EntryPoint để inject CommentRepository
@EntryPoint
@InstallIn(ActivityComponent::class)
interface CommentRepositoryEntryPoint {
    fun commentRepository(): CommentRepository
}

// --- MODERN TOP BAR ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTopBar(currentUserAvatarUrl: String? = null) { // [ĐÃ SỬA] Thêm tham số
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // [ĐÃ SỬA] Hiển thị Avatar User hoặc Icon mặc định
                if (!currentUserAvatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = currentUserAvatarUrl,
                        contentDescription = "My Avatar",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryGreen, PrimaryGreenDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "NutriCook",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextDark
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Search */ }) {
                Icon(Icons.Outlined.Search, null, tint = TextMuted)
            }
            IconButton(onClick = { /* Notifications */ }) {
                Box {
                    Icon(Icons.Outlined.Notifications, null, tint = TextMuted)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = 4.dp, y = (-4).dp)
                            .clip(CircleShape)
                            .background(HeartRed)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBackground),
        modifier = Modifier.shadow(elevation = 1.dp)
    )
}

// --- MODERN FAB ---
@Composable
fun ModernFAB(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh)
    )

    FloatingActionButton(
        onClick = {
            pressed = true
            onClick()
        },
        containerColor = PrimaryGreen,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .scale(scale)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
    ) {
        Icon(Icons.Default.Edit, contentDescription = "Tạo bài viết", modifier = Modifier.size(24.dp))
    }
}

// --- MODERN POST CARD ---
@Composable
fun ModernPostCard(
    post: Post,
    currentUserId: String,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onDelete: (String) -> Unit,
    onComment: () -> Unit
) {
    val isLiked = post.likes.contains(currentUserId)
    val isSaved = post.saves.contains(currentUserId)
    val isAuthor = post.author.id == currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            PostHeader(
                email = post.author.email,
                avatarUrl = post.author.avatarUrl, // [ĐÃ SỬA] Truyền avatarUrl
                timestamp = post.createdAt,
                isAuthor = isAuthor,
                onDelete = { onDelete(post.id) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (post.title.isNotBlank()) {
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextDark,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content
            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = if (post.title.isNotBlank()) TextDark.copy(alpha = 0.8f) else TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Image
            if (!post.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Stats
            PostStats(likesCount = post.likes.size, commentsCount = post.commentCount)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            PostActions(
                isLiked = isLiked,
                isSaved = isSaved,
                onLike = onLike,
                onComment = onComment,
                onSave = onSave
            )
        }
    }
}

// --- POST HEADER ---
@Composable
fun PostHeader(
    email: String,
    avatarUrl: String?, // [ĐÃ SỬA] Thêm tham số avatarUrl
    timestamp: Long,
    isAuthor: Boolean,
    onDelete: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // [ĐÃ SỬA] Logic hiển thị Avatar
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback: Chữ cái đầu
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryGreen.copy(0.2f), AccentOrange.copy(0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = email.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PrimaryGreen
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                email.substringBefore("@"),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = TextDark
            )
            val timeString = try {
                DateUtils.getRelativeTimeSpanString(timestamp).toString()
            } catch (e: Exception) {
                "Vừa xong"
            }
            Text(
                timeString,
                fontSize = 13.sp,
                color = TextMuted
            )
        }

        if (isAuthor) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Xóa",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            IconButton(onClick = { /* More options */ }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Outlined.MoreVert, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// --- POST STATS ---
@Composable
fun PostStats(likesCount: Int, commentsCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (likesCount > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(HeartRed.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Favorite,
                        null,
                        tint = HeartRed,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "$likesCount lượt thích",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        if (commentsCount > 0) {
            Text(
                "$commentsCount bình luận",
                fontSize = 13.sp,
                color = TextMuted
            )
        }
    }
}

// --- POST ACTIONS ---
@Composable
fun PostActions(
    isLiked: Boolean,
    isSaved: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionButton(
            icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            text = "Thích",
            isActive = isLiked,
            activeColor = HeartRed,
            onClick = onLike
        )

        ActionButton(
            icon = Icons.Outlined.ChatBubbleOutline,
            text = "Bình luận",
            isActive = false,
            activeColor = PrimaryGreen,
            onClick = onComment
        )

        ActionButton(
            icon = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            text = "Lưu",
            isActive = isSaved,
            activeColor = AccentOrange,
            onClick = onSave
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = if (isActive) activeColor else TextMuted,
        animationSpec = tween(300)
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .scale(scale)
    ) {
        Icon(
            icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
    }
}

// --- MODERN CREATE POST DIALOG ---
@Composable
fun ModernCreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tạo bài viết",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Tiêu đề món ăn...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = PrimaryGreen
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Chia sẻ công thức của bạn...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = DividerColor,
                        cursorColor = PrimaryGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .background(Color.Black.copy(0.6f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryGreen
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryGreen.copy(0.3f))
                ) {
                    Icon(Icons.Outlined.Image, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm ảnh", fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSubmit(title, content, selectedImageUri) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = PrimaryGreen.copy(0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = (title.isNotBlank() && content.isNotBlank()) || selectedImageUri != null
                ) {
                    Text(
                        "Đăng bài",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// --- MODERN HOT NEWS CARD ---
@Composable
fun ModernHotNewsCard(
    article: HotNewsArticle,
    currentUserId: String,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onDelete: (String) -> Unit,
    onComment: () -> Unit,
    onClick: () -> Unit
) {
    val isLiked = article.likes.contains(currentUserId)
    val isSaved = article.saves.contains(currentUserId)
    val isAuthor = article.author.id == currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            PostHeader(
                email = article.author.email,
                avatarUrl = article.author.avatarUrl, // [ĐÃ SỬA] Truyền avatarUrl
                timestamp = article.createdAt,
                isAuthor = isAuthor,
                onDelete = { onDelete(article.id) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Badge
            Surface(
                color = AccentOrange.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = article.category,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentOrange,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = article.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextDark,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Content
            if (article.content.isNotBlank()) {
                Text(
                    text = article.content,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = TextDark.copy(alpha = 0.8f),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Thumbnail
            if (!article.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = article.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Stats
            PostStats(likesCount = article.likes.size, commentsCount = article.commentCount)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Actions
            PostActions(
                isLiked = isLiked,
                isSaved = isSaved,
                onLike = onLike,
                onComment = onComment,
                onSave = onSave
            )
        }
    }
}

// --- COMMENT DIALOG ---
@Composable
fun CommentDialog(
    postId: String,
    postType: String,
    commentRepository: CommentRepository,
    currentUserId: String,
    onDismiss: () -> Unit
) {
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Load comments
    LaunchedEffect(postId, postType) {
        isLoading = true
        try {
            comments = commentRepository.getComments(postId, postType)
        } catch (e: Exception) {
            android.util.Log.e("CommentDialog", "Error loading comments: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Bình luận",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextMuted)
                    }
                }

                Divider(color = DividerColor, thickness = 1.dp)

                // Comments List
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                } else if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = TextMuted
                            )
                            Text(
                                "Chưa có bình luận nào",
                                fontSize = 16.sp,
                                color = TextMuted
                            )
                            Text(
                                "Hãy là người đầu tiên bình luận!",
                                fontSize = 14.sp,
                                color = TextMuted.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                currentUserId = currentUserId,
                                onDelete = {
                                    coroutineScope.launch {
                                        commentRepository.deleteComment(comment.id, postId, postType)
                                        comments = commentRepository.getComments(postId, postType)
                                    }
                                }
                            )
                        }
                    }
                }

                Divider(color = DividerColor, thickness = 1.dp)

                // Input Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Viết bình luận...", color = TextMuted) },
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = DividerColor
                        ),
                        maxLines = 3,
                        enabled = !isSubmitting
                    )
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank() && !isSubmitting) {
                                isSubmitting = true
                                coroutineScope.launch {
                                    try {
                                        val newComment = commentRepository.addComment(postId, postType, commentText.trim())
                                        if (newComment != null) {
                                            commentText = ""
                                            // Thêm delay nhỏ để Firestore kịp cập nhật
                                            kotlinx.coroutines.delay(300)
                                            comments = commentRepository.getComments(postId, postType)
                                        } else {
                                            android.util.Log.e("CommentDialog", "Failed to add comment: newComment is null")
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("CommentDialog", "Error adding comment: ${e.message}")
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        },
                        enabled = commentText.isNotBlank() && !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Gửi",
                                tint = if (commentText.isNotBlank()) PrimaryGreen else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- COMMENT ITEM ---
@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String,
    onDelete: () -> Unit
) {
    val isAuthor = comment.author.id == currentUserId

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // [ĐÃ SỬA] Avatar
        val avatarUrl = comment.author.avatarUrl
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryGreen.copy(0.2f), AccentOrange.copy(0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (comment.author.email ?: "?").take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PrimaryGreen
                )
            }
        }

        // Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val authorName = when {
                    !comment.author.displayName.isNullOrBlank() -> comment.author.displayName ?: ""
                    !comment.author.email.isBlank() -> comment.author.email.substringBefore("@")
                    else -> "Anonymous"
                }
                Text(
                    text = authorName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextDark
                )
                if (isAuthor) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Xóa",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = TextDark,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = try {
                    android.text.format.DateUtils.getRelativeTimeSpanString(comment.createdAt).toString()
                } catch (e: Exception) {
                    "Vừa xong"
                },
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

// --- MODERN EMPTY VIEW ---
@Composable
fun ModernEmptyView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(PrimaryGreen.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.RamenDining,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = PrimaryGreen
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Chưa có bài viết nào",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Hãy là người đầu tiên chia sẻ món ngon!",
            fontSize = 14.sp,
            color = TextMuted
        )
    }
}

// --- MODERN ERROR VIEW ---
@Composable
fun ModernErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = HeartRed.copy(0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Đã có lỗi xảy ra",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextDark
        )
        Text(
            error,
            color = TextMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp).padding(horizontal = 24.dp)
        ) {
            Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thử lại", fontWeight = FontWeight.SemiBold)
        }
    }
}