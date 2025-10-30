package com.example.nutricook.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nutricook.model.user.bestName
import com.example.nutricook.model.user.initial
import com.example.nutricook.viewmodel.profile.ProfileUiState
import com.example.nutricook.viewmodel.profile.ProfileViewModel

// ------------------------------------------------------
private val HeaderStart = Color(0xFFFFE0C6)
private val HeaderEnd = Color(0xFFCCE7FF)
private val ScreenBg = Color.White
private val CardBg = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {},
    onEditAvatar: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()

    Scaffold(
        containerColor = ScreenBg,
        topBar = {},
        bottomBar = bottomBar
    ) { padding ->
        when {
            ui.loading -> Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            ui.profile != null -> ProfileContent(
                modifier = Modifier.padding(padding),
                state = ui,
                onEditAvatar = onEditAvatar,
                onOpenSettings = onOpenSettings
            )

            else -> Box(
                Modifier
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
    onEditAvatar: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val p = state.profile!!
    val scroll = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // gradient trên cùng
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(HeaderStart, HeaderEnd)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            // thanh top
            TopBar(onOpenSettings = onOpenSettings)

            // avatar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AvatarBox(
                    avatarUrl = p.user.avatarUrl,
                    text = p.user.initial(),
                    onEdit = onEditAvatar
                )
            }

            Spacer(Modifier.height(8.dp))

            // tên
            Text(
                text = p.user.bestName(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF0F172A),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            // stats
            StatsCard(
                posts = p.posts,
                following = p.following,
                followers = p.followers,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(18.dp))

            // danh sách
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                ProfileMenuRow(
                    title = "Recent Activity",
                    onClick = { /* TODO */ }
                )
                ProfileMenuRow(
                    title = "Post (${p.posts})",
                    onClick = { /* TODO */ }
                )
                ProfileMenuRow(
                    title = "Save",
                    onClick = { /* TODO */ }
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "My Fatscret",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF0F172A),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            ChartPlaceholder(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TopBar(
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu",
                    tint = Color(0xFF1F2937)
                )
            }
        }

        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color(0xFF0F172A)
        )

        Surface(
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF1F2937)
                )
            }
        }
    }
}

@Composable
private fun AvatarBox(
    avatarUrl: String?,
    text: String,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFFFFC980)),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                // ✅ dùng AsyncImage đơn giản, không cần LocalContext, không cần ImageRequest
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .offset(x = 4.dp, y = 4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xFF29C2E5))
                .clickable { onEdit() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit avatar",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatCell(number = posts, label = "Post")
            Divider(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp),
                color = Color(0xFFE4E6EB)
            )
            StatCell(number = following, label = "Following")
            Divider(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp),
                color = Color(0xFFE4E6EB)
            )
            StatCell(number = followers, label = "Follower")
        }
    }
}

@Composable
private fun StatCell(
    number: Int,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF0F172A)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9AA3B5)
        )
    }
}

@Composable
private fun ProfileMenuRow(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD9E7FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF0F172A),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFFB8C0CC),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ChartPlaceholder(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .height(110.dp)
                .fillMaxWidth()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            // Text("Chưa có dữ liệu")
        }
    }
}
