package com.example.nutricook.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nutricook.model.user.bestName
import com.example.nutricook.model.user.initial
import com.example.nutricook.viewmodel.profile.OtherProfileUiState
import com.example.nutricook.viewmodel.profile.OtherProfileViewModel

private val HeaderStart = Color(0xFFFFE0C6)
private val HeaderEnd   = Color(0xFFCCE7FF)

@Composable
fun OtherProfileScreen(
    onBack: () -> Unit,
    vm: OtherProfileViewModel = hiltViewModel(),
    // truyền vào uid hiện tại nếu có để ẩn nút Follow khi xem chính mình
    currentUserId: String? = null
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                ui.error != null -> ErrorBox(ui.error!!) { vm.refresh() }
                ui.profile != null -> OtherProfileContent(
                    ui = ui,
                    onBack = onBack,
                    onToggleFollow = { vm.toggleFollow() },
                    onLoadMore = { vm.loadMore() },
                    isMe = (currentUserId != null && ui.profile?.user?.id == currentUserId)
                )
            }
        }
    }
}

@Composable
private fun OtherProfileContent(
    ui: OtherProfileUiState,
    onBack: () -> Unit,
    onToggleFollow: () -> Unit,
    onLoadMore: () -> Unit,
    isMe: Boolean
) {
    val p = ui.profile!!
    val listState = rememberLazyListState()

    // phát hiện gần cuối list để load thêm — tránh LaunchedEffect theo index
    val reachedEnd by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(reachedEnd, ui.hasMore, ui.loadingMore) {
        if (reachedEnd && ui.hasMore && !ui.loadingMore) onLoadMore()
    }

    Box(Modifier.fillMaxSize()) {
        // header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Brush.horizontalGradient(listOf(HeaderStart, HeaderEnd)))
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // top bar
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color(0xFF374151))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1F2937))
                }
            }

            // avatar + name + follow
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // avatar (fallback chữ cái đầu nếu rỗng)
                    if (!p.user.avatarUrl.isNullOrBlank()) {
                        Surface(shape = MaterialTheme.shapes.large, color = Color(0xFFFFC166)) {
                            AsyncImage(
                                model = p.user.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.size(108.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        Surface(shape = MaterialTheme.shapes.large, color = Color(0xFFFFC166)) {
                            Box(Modifier.size(108.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = p.user.initial(),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(p.user.bestName(), fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1F2937))
                    Spacer(Modifier.height(8.dp))

                    // Button follow/unfollow — ẩn khi là chính mình
                    if (!isMe) {
                        Button(
                            onClick = onToggleFollow,
                            enabled = !ui.togglingFollow,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (ui.isFollowing) Color(0xFFFFE4E6) else Color(0xFFB8E6DC),
                                contentColor   = if (ui.isFollowing) Color(0xFFDC2626) else Color(0xFF0D7C74)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (ui.togglingFollow) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = if (ui.isFollowing) Icons.Outlined.PersonRemove else Icons.Outlined.PersonAdd,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(if (ui.isFollowing) "Unfollow" else "Follow")
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "${p.posts} Posts · ${p.followers} Followers · ${p.following} Following",
                        color = Color(0xFF6B7280), fontSize = 14.sp
                    )
                }
            }

            // posts (dùng key ổn định)
            itemsIndexed(
                items = ui.posts,
                key = { _, post -> post.id }
            ) { _, post ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    PostItem(post = post) // tái dùng component hiển thị post
                }
            }

            if (ui.loadingMore) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBox(msg: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(msg, color = Color(0xFFDC2626))
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRetry) { Text("Thử lại") }
    }
}
