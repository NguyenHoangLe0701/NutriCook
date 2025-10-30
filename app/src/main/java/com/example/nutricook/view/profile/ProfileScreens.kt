package com.example.nutricook.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.model.user.bestName
import com.example.nutricook.model.user.initial
import com.example.nutricook.viewmodel.profile.ProfileUiState
import com.example.nutricook.viewmodel.profile.ProfileViewModel

private val Teal = Color(0xFF20B2AA)
private val Bg   = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {},
    onEditAvatar: () -> Unit = {},
    onLogout: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},      // 👈 để NavGraph truyền bottom nav xuống
    vm: ProfileViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()

    Scaffold(
        containerColor = Bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hồ sơ") },
                navigationIcon = {
                    Surface(shape = CircleShape, color = Color.White.copy(.7f)) {
                        IconButton(onClick = { /* nếu có drawer thì mở ở đây */ }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                        }
                    }
                },
                actions = {
                    // settings
                    Surface(shape = CircleShape, color = Color.White.copy(.7f)) {
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Cài đặt")
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    // optional
                    Surface(shape = CircleShape, color = Color.White.copy(.7f)) {
                        IconButton(onClick = { /* TODO: action khác */ }) {
                            Icon(Icons.Outlined.Add, contentDescription = "Thêm")
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    // logout
                    Surface(shape = CircleShape, color = Color.White.copy(.7f)) {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Outlined.Logout, contentDescription = "Đăng xuất")
                        }
                    }
                }
            )
        },
        bottomBar = bottomBar
    ) { padding ->
        when {
            ui.loading -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            ui.profile != null -> ProfileContent(
                modifier = Modifier.padding(padding),
                state = ui,
                onEditAvatar = onEditAvatar
            )

            else -> Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(ui.message ?: "Không có dữ liệu")
            }
        }
    }
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    state: ProfileUiState,
    onEditAvatar: () -> Unit
) {
    val p = state.profile!!

    Box(modifier = modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFF2FFFD), Color(0xFFFFF1FB))
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(36.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                AvatarWithEditBadge(text = p.user.initial(), onEdit = onEditAvatar)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = p.user.bestName(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(14.dp))
            StatsRowCard(
                posts = p.posts,
                following = p.following,
                followers = p.followers,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                ChevronItem(
                    title = "Hoạt động gần đây (10)",
                    icon = Icons.Outlined.Inbox,
                    iconTint = Color(0xFF6B7A99),
                    onClick = { /* TODO */ }
                )
                ChevronItem(
                    title = "Bài đăng (${p.posts})",
                    icon = Icons.Outlined.Description,
                    iconTint = Color(0xFF4C9AFF),
                    onClick = { /* TODO */ }
                )
                ChevronItem(
                    title = "Đã lưu (10)",
                    icon = Icons.Outlined.BookmarkBorder,
                    iconTint = Color(0xFFFF8A65),
                    onClick = { /* TODO */ }
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "Fatscore của tôi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ChartPlaceholderCard(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AvatarWithEditBadge(text: String, onEdit: () -> Unit) {
    Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.BottomEnd) {
        Surface(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Teal.copy(alpha = .16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Teal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Surface(
            onClick = onEdit,
            shape = CircleShape,
            color = Teal,
            shadowElevation = 2.dp,
            modifier = Modifier
                .offset(x = 6.dp, y = 6.dp)
                .size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "Sửa ảnh đại diện",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StatsRowCard(
    posts: Int,
    following: Int,
    followers: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatMini(number = posts, label = "Bài đăng")
            Divider(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp),
                color = Bg
            )
            StatMini(number = following, label = "Đang theo dõi")
            Divider(
                modifier = Modifier
                    .height(28.dp)
                    .width(1.dp),
                color = Bg
            )
            StatMini(number = followers, label = "Người theo dõi")
        }
    }
}

@Composable
private fun StatMini(number: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$number", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChevronItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = "Mở",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChartPlaceholderCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Biểu đồ sẽ đặt ở đây")
        }
    }
}
