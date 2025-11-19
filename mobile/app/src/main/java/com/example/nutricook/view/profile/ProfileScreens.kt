package com.example.nutricook.view.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.model.profile.Post
import com.example.nutricook.model.user.bestName
import com.example.nutricook.viewmodel.profile.PostViewModel
import com.example.nutricook.viewmodel.profile.ProfileUiState
import com.example.nutricook.viewmodel.profile.ProfileViewModel

// --- MÀU SẮC & STYLE ---
private val PeachGradientStart = Color(0xFFFFF0E3) // Cam phấn nhạt
private val PeachGradientEnd = Color(0xFFFFFFFF)   // Trắng
private val TealPrimary = Color(0xFF2BB6AD)        // Xanh chủ đạo
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF9CA3AF)
private val CardBg = Color.White
private val ScreenBg = Color(0xFFFAFAFA)

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {},
    onOpenRecent: () -> Unit = {},
    onOpenPosts: () -> Unit = {},
    onOpenSaves: () -> Unit = {},
    onEditAvatar: () -> Unit = {}, // Callback mở thư viện ảnh
    onOpenSearch: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel(),
    postVm: PostViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()

    Scaffold(
        bottomBar = bottomBar,
        containerColor = ScreenBg,
        // TopBar chúng ta tự vẽ bên trong content để đè lên Gradient
        topBar = {}
    ) { padding ->
        when {
            ui.loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = TealPrimary) }

            ui.profile != null -> {
                val me = ui.profile!!.user.id
                LaunchedEffect(me) { postVm.loadInitial(me) }

                ProfileContent(
                    modifier = Modifier.padding(padding),
                    state = ui,
                    onEditAvatar = onEditAvatar,
                    onOpenSettings = onOpenSettings,
                    onOpenRecent = onOpenRecent,
                    onOpenPosts = onOpenPosts,
                    onOpenSaves = onOpenSaves,
                    onOpenSearch = onOpenSearch
                )
            }

            else -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(ui.message ?: "Không tải được dữ liệu") }
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
    onOpenSearch: () -> Unit
) {
    val p = state.profile!!
    val scroll = rememberScrollState()

    // Gradient Background cho phần Header
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // Lớp Gradient nền phía trên
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp) // Phủ hết phần header info
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PeachGradientStart, PeachGradientEnd)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Bar (Custom)
            MyProfileTopBar(
                onEditProfile = { /* TODO: Navigate to Edit Text Info */ },
                onSettings = onOpenSettings
            )

            Spacer(Modifier.height(8.dp))

            // 2. Avatar + Name Section
            AvatarSection(
                avatarUrl = p.user.avatarUrl,
                name = p.user.bestName(),
                onEditAvatar = onEditAvatar
            )

            Spacer(Modifier.height(24.dp))

            // 3. Stats Row (Post / Following / Follower)
            StatsRow(
                posts = p.posts,
                following = p.following,
                followers = p.followers
            )

            Spacer(Modifier.height(24.dp))

            // 4. Menu List (Recent Activity, Post, Save)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lưu ý: Dữ liệu Activity/Save chưa có count trong Profile model,
                // tạm thời hiển thị số mặc định hoặc ẩn số đi nếu muốn chính xác.
                // Ở đây mình để (10) giả lập theo Figma hoặc bạn có thể bỏ đi.

                MenuCardItem(
                    title = "Recent Activity",
                    icon = Icons.Outlined.Image, // Icon dạng ảnh/gallery
                    iconColor = Color(0xFF5B6B9A),
                    iconBg = Color(0xFFD5D9E8),
                    // count = p.activitiesCount, // Chưa có field này
                    onClick = onOpenRecent
                )

                MenuCardItem(
                    title = "Post",
                    count = p.posts, // Dữ liệu thật
                    icon = Icons.Outlined.Description, // Icon file/post
                    iconColor = Color(0xFF1D9B87),
                    iconBg = Color(0xFFBEF0E8),
                    onClick = onOpenPosts
                )

                MenuCardItem(
                    title = "Save",
                    // count = p.savesCount, // Chưa có field này
                    icon = Icons.Outlined.Bookmark,
                    iconColor = Color(0xFFE07C00),
                    iconBg = Color(0xFFFFDDB8),
                    onClick = onOpenSaves
                )
            }

            Spacer(Modifier.height(30.dp))

            // 5. Chart Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "My Fatscret", // Theo Figma
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )
                Spacer(Modifier.height(12.dp))
                ChartCard()
            }

            Spacer(Modifier.height(100.dp)) // Padding bottom cho BottomBar
        }
    }
}

// ================= COMPONENTS =================

@Composable
fun MyProfileTopBar(
    onEditProfile: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Nút Edit bên trái (Icon cây bút/note) - Giống Figma
        IconButton(
            onClick = onEditProfile,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White)
        ) {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = "Edit Profile",
                tint = TealPrimary
            )
        }

        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextDark
            )
        )

        // Nút Settings bên phải (Icon bánh răng)
        IconButton(
            onClick = onSettings,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White) // Nền trắng mờ hoặc rõ
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF566275) // Màu xám đậm
            )
        }
    }
}

@Composable
fun AvatarSection(
    avatarUrl: String?,
    name: String,
    onEditAvatar: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Avatar Shape: Rounded Rectangle (Squircle) giống Figma
            val shape = RoundedCornerShape(32.dp)
            val size = 110.dp

            if (avatarUrl.isNullOrBlank()) {
                // Avatar mặc định: Chữ cái đầu
                val initial = name.firstOrNull()?.uppercase() ?: "?"
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(shape)
                        .background(Color(0xFFFFC107)), // Màu vàng cam giống ảnh
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(size)
                        .clip(shape)
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )
            }

            // Nút Edit Avatar nhỏ màu xanh (Teal)
            Box(
                modifier = Modifier
                    .offset(x = 6.dp, y = 6.dp) // Đẩy ra góc một chút
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(TealPrimary)
                    .border(2.dp, Color.White, CircleShape) // Viền trắng xung quanh nút
                    .clickable { onEditAvatar() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Change Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937) // Màu xanh đen đậm
            )
        )
    }
}

@Composable
fun StatsRow(posts: Int, following: Int, followers: Int) {
    // Card trắng bo góc chứa stats
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Phẳng hoặc bóng nhẹ tùy ý
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(count = posts, label = "Post")

            // Divider mờ
            VerticalDivider()

            StatItem(count = following, label = "Following")

            VerticalDivider()

            StatItem(count = followers, label = "Follower")
        }
    }
}

@Composable
fun StatItem(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextDark
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextGray,
                fontSize = 13.sp
            )
        )
    }
}

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(Color(0xFFF3F4F6)) // Màu xám rất nhạt
    )
}

@Composable
fun MenuCardItem(
    title: String,
    count: Int? = null,
    icon: ImageVector,
    iconColor: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg), // Nền icon màu nhạt
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor, // Màu icon đậm
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Title + Count
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                )
                if (count != null) {
                    Text(
                        text = " ($count)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )
                }
            }

            // Arrow Right
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ChartCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Vẽ chart giả lập giống hình (Đường cong teal)
                val width = size.width
                val height = size.height

                val path = Path().apply {
                    moveTo(0f, height * 0.8f)
                    cubicTo(
                        width * 0.2f, height * 0.2f,
                        width * 0.5f, height * 0.9f,
                        width * 0.8f, height * 0.4f
                    )
                    lineTo(width, height * 0.6f)
                }

                // Fill Gradient bên dưới đường line
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TealPrimary.copy(alpha = 0.2f),
                            TealPrimary.copy(alpha = 0.0f)
                        )
                    )
                )

                // Stroke Line
                drawPath(
                    path = path,
                    color = TealPrimary,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Các điểm chấm tròn
                drawCircle(TealPrimary, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(width * 0.25f, height * 0.45f))
                drawCircle(TealPrimary, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(width * 0.65f, height * 0.6f))
            }
        }
    }
}