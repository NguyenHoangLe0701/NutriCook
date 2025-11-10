package com.example.nutricook.view.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nutricook.model.user.bestName
import com.example.nutricook.model.user.initial
import com.example.nutricook.viewmodel.profile.PostViewModel
import com.example.nutricook.viewmodel.profile.ProfileUiState
import com.example.nutricook.viewmodel.profile.ProfileViewModel
import com.example.nutricook.viewmodel.profile.ProfileSearchViewModel   // <-- thêm

private val HeaderStart = Color(0xFFFFE0C6)
private val HeaderEnd = Color(0xFFCCE7FF)
private val ScreenBg = Color(0xFFFAFAFA)
private val CardBg = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {},
    onOpenRecent: () -> Unit = {},
    onOpenPosts: () -> Unit = {},
    onOpenSaves: () -> Unit = {},
    onEditAvatar: () -> Unit = {},
    // callback để mở hồ sơ người khác qua NavController: navController.navigate("user_profile/$uid")
    onOpenOtherProfile: (String) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel(),
    postVm: PostViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()

    Scaffold(
        containerColor = ScreenBg,
        topBar = {},
        bottomBar = bottomBar
    ) { padding ->
        when {
            ui.loading -> Box(
                Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            ui.profile != null -> {
                val me = ui.profile!!.user.id
                LaunchedEffect(me) { postVm.loadInitial(me) } // 1 trang ~ 3 bài (tùy backend)

                ProfileContent(
                    modifier = Modifier.padding(padding),
                    state = ui,
                    onEditAvatar = onEditAvatar,
                    onOpenSettings = onOpenSettings,
                    onOpenRecent = onOpenRecent,
                    onOpenPosts = onOpenPosts,
                    onOpenSaves = onOpenSaves,
                    onOpenOtherProfile = onOpenOtherProfile,
                    postVm = postVm
                )
            }

            else -> Box(
                Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text(ui.message ?: "Không có dữ liệu") }
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onEditAvatar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenPosts: () -> Unit,
    onOpenSaves: () -> Unit,
    onOpenOtherProfile: (String) -> Unit,
    postVm: PostViewModel
) {
    val p = state.profile!!
    val postsSt by postVm.state.collectAsState()
    var showSearch by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(ScreenBg)) {
        // header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.horizontalGradient(listOf(HeaderStart, HeaderEnd)))
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { TopBar(onOpenSettings = onOpenSettings, onOpenSearch = { showSearch = true }) }
            item { Spacer(Modifier.height(4.dp)) }

            // Avatar
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    AvatarBox(
                        avatarUrl = p.user.avatarUrl,
                        text = p.user.initial(),
                        onEdit = onEditAvatar
                    )
                }
            }

            // Name
            item {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = p.user.bestName(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF1F2937)
                    )
                }
            }

            // Stats
            item {
                Spacer(Modifier.height(16.dp))
                StatsCard(
                    posts = p.posts,
                    following = p.following,
                    followers = p.followers,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Menu
            item {
                Spacer(Modifier.height(16.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    ProfileMenuRow(
                        title = "Recent Activity",
                        iconBg = Color(0xFFD5D9E8),
                        icon = Icons.Outlined.History,
                        iconTint = Color(0xFF5B6B9A),
                        onClick = onOpenRecent
                    )
                    ProfileMenuRow(
                        title = "Post (${p.posts})",
                        iconBg = Color(0xFFBEF0E8),
                        icon = Icons.Outlined.Description,
                        iconTint = Color(0xFF1D9B87),
                        onClick = onOpenPosts
                    )
                    ProfileMenuRow(
                        title = "Save",
                        iconBg = Color(0xFFFFDDB8),
                        icon = Icons.Outlined.BookmarkBorder,
                        iconTint = Color(0xFFE07C00),
                        onClick = onOpenSaves
                    )
                }
            }

            // Chart title + chart
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "My Fatscret",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            item {
                Spacer(Modifier.height(12.dp))
                ChartCard(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth())
            }

            // Header My Posts
            item {
                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Posts",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color(0xFF1F2937)
                    )
                    TextButton(onClick = onOpenPosts) { Text("See all") }
                }
            }

            // Danh sách bài viết (Lazy, auto loadMore)
            when {
                postsSt.loading && postsSt.items.isEmpty() -> {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                postsSt.items.isEmpty() -> {
                    item {
                        Text(
                            "Chưa có bài viết",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                else -> {
                    itemsIndexed(postsSt.items) { index, post ->
                        Box(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            PostItem(post = post)
                        }

                        // Khi chạm cuối list -> loadMore nếu còn trang và chưa bận
                        val canLoadMore = postsSt.hasMore && !postsSt.loadingMore
                        if (index == postsSt.items.lastIndex && canLoadMore) {
                            LaunchedEffect(postsSt.items.size) {
                                postVm.loadMore()
                            }
                        }
                    }

                    // Hiển thị loading khi đang tải trang kế tiếp
                    if (postsSt.loadingMore && postsSt.items.isNotEmpty()) {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Search overlay
    if (showSearch) {
        SearchProfileDialog(
            onDismiss = { showSearch = false },
            onPick = { uid ->
                showSearch = false
                onOpenOtherProfile(uid)
            }
        )
    }
}

@Composable
private fun TopBar(onOpenSettings: () -> Unit, onOpenSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Đổi từ menu -> search
        Surface(shape = CircleShape, color = Color.White, shadowElevation = 2.dp) {
            IconButton(onClick = onOpenSearch) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = "Search profiles",
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            ),
            color = Color(0xFF1F2937)
        )

        Surface(shape = CircleShape, color = Color.White, shadowElevation = 2.dp) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchProfileDialog(
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    val vm: ProfileSearchViewModel = hiltViewModel()
    val ui by vm.ui.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF0EA5E9),
                modifier = Modifier.size(28.dp)
            )
        },
        title = { Text("Tìm người dùng", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column {
                OutlinedTextField(
                    value = ui.query,
                    onValueChange = { vm.updateQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập tên hoặc email…") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (ui.query.isNotBlank()) {
                            IconButton(onClick = { vm.updateQuery("") }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))

                when {
                    ui.loading -> {
                        Box(
                            Modifier.fillMaxWidth().height(80.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(strokeWidth = 2.dp) }
                    }
                    ui.error != null -> {
                        Text(ui.error ?: "Lỗi", color = Color(0xFFDC2626))
                    }
                    ui.results.isEmpty() -> {
                        Text(
                            text = if (ui.query.isBlank()) "Nhập từ khóa để tìm…" else "Không có kết quả",
                            color = Color(0xFF6B7280)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 0.dp, max = 300.dp)
                        ) {
                            items(ui.results) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPick(user.id) }
                                        .padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = CircleShape, color = Color(0xFFF1F5F9)) {
                                        Box(
                                            Modifier.size(40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                (user.displayName ?: user.email)
                                                    .firstOrNull()
                                                    ?.uppercase() ?: "U",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(user.displayName ?: user.email, fontWeight = FontWeight.SemiBold)
                                        Text(user.email, color = Color(0xFF6B7280), fontSize = 12.sp)
                                    }
                                    Icon(
                                        Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFFD1D5DB)
                                    )
                                }
                                Divider(color = Color(0xFFE5E7EB))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}

@Composable
private fun AvatarBox(avatarUrl: String?, text: String, onEdit: () -> Unit) {
    Box(Modifier.size(108.dp), contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = Modifier
                .size(108.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFFFFC166)),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Box(
            modifier = Modifier
                .offset(x = 6.dp, y = 6.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF06B6D4))
                .clickable { onEdit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = "Edit avatar",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun StatsCard(
    posts: Int,
    following: Int,
    followers: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatCell(number = posts, label = "Post")
            Box(Modifier.width(1.dp).height(36.dp).background(Color(0xFFE5E7EB)))
            StatCell(number = following, label = "Following")
            Box(Modifier.width(1.dp).height(36.dp).background(Color(0xFFE5E7EB)))
            StatCell(number = followers, label = "Follower")
        }
    }
}

@Composable
private fun StatCell(number: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 12.dp)) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color(0xFF1F2937)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = Color(0xFF9CA3AF)
        )
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    iconBg: Color,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                ),
                color = Color(0xFF1F2937),
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ChartCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(130.dp).padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val points = listOf(
                    0.0f to 0.55f,
                    0.12f to 0.5f,
                    0.24f to 0.52f,
                    0.36f to 0.48f,
                    0.48f to 0.45f,
                    0.6f to 0.42f,
                    0.72f to 0.38f,
                    0.84f to 0.4f,
                    1.0f to 0.38f
                )

                val linePath = Path()
                val fillPath = Path()

                points.forEachIndexed { index, (x, y) ->
                    val xPos = x * width
                    val yPos = (1 - y) * height
                    if (index == 0) {
                        linePath.moveTo(xPos, yPos)
                        fillPath.moveTo(xPos, height)
                        fillPath.lineTo(xPos, yPos)
                    } else {
                        linePath.lineTo(xPos, yPos)
                        fillPath.lineTo(xPos, yPos)
                    }
                }

                fillPath.lineTo(width, height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF06B6D4).copy(alpha = 0.25f),
                            Color(0xFF06B6D4).copy(alpha = 0.05f)
                        )
                    )
                )

                drawPath(path = linePath, color = Color(0xFF06B6D4), style = Stroke(width = 2.5.dp.toPx()))

                points.forEach { (x, y) ->
                    val xPos = x * width
                    val yPos = (1 - y) * height
                    drawCircle(
                        color = Color(0xFF06B6D4),
                        radius = 3.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(xPos, yPos)
                    )
                }
            }
        }
    }
}
