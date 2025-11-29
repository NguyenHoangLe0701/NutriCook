package com.example.nutricook.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
// [QUAN TRỌNG] Import đúng Model Post thống nhất để khớp với ViewModel
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.user.bestName
import com.example.nutricook.viewmodel.profile.PublicProfileViewModel

// --- MÀU SẮC CHUẨN FIGMA ---
private val TealPrimary = Color(0xFF2BB6AD)
private val TealLight = Color(0xFFE0F7F6)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF9CA3AF)
private val DividerColor = Color(0xFFF3F4F6)

// Gradient nhẹ màu cam phấn -> trắng
private val HeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFF0E8),
        Color(0xFFFFFBF9),
        Color.White
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    onBack: () -> Unit,
    onPostClick: (Post) -> Unit = {},
    viewModel: PublicProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            PublicProfileTopBar(
                onBack = onBack,
                title = "Profile"
            )
        },
        containerColor = Color.White
    ) { padding ->
        if (state.loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else if (state.profile != null) {
            val profile = state.profile!!

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // --- PHẦN HEADER ---
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(HeaderGradient)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        ) {
                            Spacer(Modifier.height(20.dp))

                            // --- LOGIC AVATAR ---
                            val avatarUrl = profile.user.avatarUrl
                            val initial = remember(profile.user) {
                                profile.user.bestName().firstOrNull()?.uppercase() ?: "?"
                            }

                            if (avatarUrl.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(TealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 40.sp,
                                            color = TealPrimary
                                        )
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Tên User
                            Text(
                                text = profile.user.bestName(),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    color = TealPrimary
                                )
                            )

                            // Bio
                            Text(
                                text = profile.bio ?: "Food Blogger / Photo Food",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextGray,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier.padding(top = 6.dp)
                            )

                            Spacer(Modifier.height(24.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatItem(count = profile.posts.toString(), label = "Post")

                                VerticalDivider(
                                    modifier = Modifier.height(30.dp),
                                    thickness = 1.dp,
                                    color = Color(0xFFE5E7EB)
                                )

                                StatItem(count = profile.following.toString(), label = "Following")

                                VerticalDivider(
                                    modifier = Modifier.height(30.dp),
                                    thickness = 1.dp,
                                    color = Color(0xFFE5E7EB)
                                )

                                StatItem(count = profile.followers.toString(), label = "Follower")
                            }

                            Spacer(Modifier.height(28.dp))

                            // Button Follow
                            Button(
                                onClick = { viewModel.toggleFollow() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TealPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(50.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 2.dp
                                )
                            ) {
                                Icon(
                                    imageVector = if (state.isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (state.isFollowing) "Following" else "Follow",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // --- TAB ROW ---
                item {
                    var selectedTabIndex by remember { mutableIntStateOf(0) }
                    val tabs = listOf("Personal Recipes", "Review")

                    Column {
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
                                HorizontalDivider(color = DividerColor)
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 16.sp,
                                            color = if (selectedTabIndex == index) TextDark else TextGray
                                        )
                                    },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    if (selectedTabIndex == 0) {
                        if (state.posts.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp), contentAlignment = Alignment.Center
                            ) {
                                Text("Chưa có công thức nào", color = TextGray)
                            }
                        }
                    } else {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp), contentAlignment = Alignment.Center
                        ) {
                            Text("Chưa có đánh giá nào", color = TextGray)
                        }
                    }
                }

                // --- DANH SÁCH BÀI VIẾT ---
                if (state.posts.isNotEmpty()) {
                    items(state.posts) { post ->
                        SimplePostCard(post = post, onClick = { onPostClick(post) })
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Không tìm thấy user", color = Color.Red)
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextDark
            )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileTopBar(onBack: () -> Unit, title: String) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = TextDark
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
        actions = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = TextDark
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun SimplePostCard(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(16.dp)) {
                Text(
                    text = if (post.title.isNotBlank()) post.title else "Món ngon mỗi ngày",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = TextDark,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                // Dùng thẳng post.content vì trong model mới nó non-null
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGray),
                    maxLines = 2
                )
            }
        }
    }
}