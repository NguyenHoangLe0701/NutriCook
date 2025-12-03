package com.example.nutricook.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.model.user.User
import com.example.nutricook.model.user.bestName
import com.example.nutricook.viewmodel.profile.ProfileViewModel

// Màu sắc đồng bộ với app
private val TealPrimary = Color(0xFF10B981)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    userId: String,
    initialTab: Int = 0, // 0: Followers, 1: Following
    targetName: String = "Danh sách", // [QUAN TRỌNG] Tên hiển thị trên Header
    onBack: () -> Unit,
    onUserClick: (String) -> Unit, // Navigate đến profile người đó
    vm: ProfileViewModel = hiltViewModel()
) {
    val followers by vm.followers.collectAsState()
    val following by vm.following.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(initialTab) }

    // Load data khi màn hình mở
    LaunchedEffect(userId) {
        vm.loadFollowLists(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = targetName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextDark
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Tabs: Followers | Following
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = TealPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = TealPrimary,
                        height = 3.dp
                    )
                },
                divider = {
                    HorizontalDivider(color = Color(0xFFF3F4F6))
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            "Followers (${followers.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = TealPrimary,
                    unselectedContentColor = TextGray
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Following (${following.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = TealPrimary,
                    unselectedContentColor = TextGray
                )
            }

            // List Content
            val listToShow = if (selectedTabIndex == 0) followers else following

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (listToShow.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedTabIndex == 0) "Chưa có người theo dõi" else "Chưa theo dõi ai",
                                color = TextGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(listToShow, key = { it.id }) { user ->
                        FollowUserItem(user = user, onClick = { onUserClick(user.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun FollowUserItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Logic
        val initial = user.bestName().firstOrNull()?.uppercase() ?: "?"

        if (user.avatarUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFECFDF5)), // TealLight background
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TealPrimary
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(Modifier.width(16.dp))

        // User Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.bestName(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 16.sp
                )
            )
            if (user.email.isNotBlank()) {
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextGray,
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}