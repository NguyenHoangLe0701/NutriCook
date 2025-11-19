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
import com.example.nutricook.viewmodel.profile.ProfileSearchViewModel
import com.example.nutricook.model.nutrition.DailyTotals
import kotlin.math.max
import kotlin.math.min

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
    onOpenOtherProfile: (String) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel(),
    postVm: PostViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()
    val tgt by vm.targets.collectAsState()          // mục tiêu “My Fat”

    // ---- DỮ LIỆU THẬT ----
    val today by vm.todayTotals.collectAsState()    // đã ăn hôm nay
    val last7 by vm.last7Days.collectAsState()      // 7 ngày gần nhất

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
                LaunchedEffect(me) { postVm.loadInitial(me) }

                ProfileContent(
                    modifier = Modifier.padding(padding),
                    state = ui,
                    caloriesTarget = tgt.calories,
                    proteinTarget = tgt.proteinG,
                    fatTarget = tgt.fatG,
                    carbTarget = tgt.carbG,
                    // thực tế hôm nay
                    caloriesIn = today.caloriesIn,
                    proteinIn = today.proteinG,
                    fatIn = today.fatG,
                    carbIn = today.carbG,
                    // chart 7 ngày
                    last7Days = last7,
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
    caloriesTarget: Int,
    proteinTarget: Double,
    fatTarget: Double,
    carbTarget: Double,
    caloriesIn: Int,
    proteinIn: Double,
    fatIn: Double,
    carbIn: Double,
    last7Days: List<DailyTotals>,
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

            // --- My Fat card (mục tiêu hôm nay + đã ăn + còn lại) ---
            item {
                Spacer(Modifier.height(18.dp))
                MyFatCard(
                    caloriesTarget = caloriesTarget,
                    proteinTarget = proteinTarget,
                    fatTarget = fatTarget,
                    carbTarget = carbTarget,
                    caloriesIn = caloriesIn,
                    proteinIn = proteinIn,
                    fatIn = fatIn,
                    carbIn = carbIn,
                    onAdjust = onOpenSettings,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Chart title + chart
            item {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "My Fatscret (7 ngày gần nhất)",
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
                ChartCard(
                    days = last7Days,
                    kcalTarget = caloriesTarget,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                )
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

                        val canLoadMore = postsSt.hasMore && !postsSt.loadingMore
                        if (index == postsSt.items.lastIndex && canLoadMore) {
                            LaunchedEffect(postsSt.items.size) { postVm.loadMore() }
                        }
                    }

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

/** Hàng menu đơn giản */
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

/** Card mục tiêu – đã ăn – còn lại (hôm nay) */
@Composable
private fun MyFatCard(
    caloriesTarget: Int,
    proteinTarget: Double,
    fatTarget: Double,
    carbTarget: Double,
    caloriesIn: Int,
    proteinIn: Double,
    fatIn: Double,
    carbIn: Double,
    onAdjust: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainKcal = (caloriesTarget - caloriesIn).coerceAtLeast(0)
    val remainPro  = (proteinTarget - proteinIn).coerceAtLeast(0.0)
    val remainFat  = (fatTarget - fatIn).coerceAtLeast(0.0)
    val remainCarb = (carbTarget - carbIn).coerceAtLeast(0.0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "My Fat (hôm nay)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onAdjust) { Text("Điều chỉnh") }
            }
            Spacer(Modifier.height(8.dp))

            // Hàng mục tiêu
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroBox(title = "KCAL", value = "$caloriesTarget")
                MacroBox(title = "PRO",  value = "${"%.0f".format(proteinTarget)}g")
                MacroBox(title = "FAT",  value = "${"%.0f".format(fatTarget)}g")
                MacroBox(title = "CARB", value = "${"%.0f".format(carbTarget)}g")
            }
            Spacer(Modifier.height(8.dp))
            // Hàng đã ăn
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroBox(title = "ĐÃ ĂN", value = "$caloriesIn")
                MacroBox(title = "PRO",  value = "${"%.0f".format(proteinIn)}g")
                MacroBox(title = "FAT",  value = "${"%.0f".format(fatIn)}g")
                MacroBox(title = "CARB", value = "${"%.0f".format(carbIn)}g")
            }
            Spacer(Modifier.height(8.dp))
            // Hàng còn lại
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroBox(title = "CÒN", value = "$remainKcal")
                MacroBox(title = "PRO",  value = "${"%.0f".format(remainPro)}g")
                MacroBox(title = "FAT",  value = "${"%.0f".format(remainFat)}g")
                MacroBox(title = "CARB", value = "${"%.0f".format(remainCarb)}g")
            }
        }
    }
}

@Composable
private fun MacroBox(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF6F6F6))
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(title, color = Color.Gray, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

/**
 * Biểu đồ 7 ngày gần nhất:
 * - Vẽ đường theo caloriesIn của mỗi ngày (chuẩn hoá theo max(target, max(caloriesIn)) để dễ nhìn).
 * - Nếu rỗng -> vẽ placeholder nhẹ.
 */
@Composable
private fun ChartCard(
    days: List<DailyTotals>,
    kcalTarget: Int,
    modifier: Modifier = Modifier
) {
    val values = if (days.isEmpty()) {
        listOf(0.55f, 0.50f, 0.52f, 0.48f, 0.45f, 0.42f, 0.40f).map { (it * (kcalTarget.coerceAtLeast(1))).toFloat() }
    } else {
        days.map { it.caloriesIn.toFloat() }
    }
    val maxValue = max(1f, max(values.maxOrNull() ?: 1f, kcalTarget.toFloat()))

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                if (values.isEmpty()) return@Canvas

                val pts = values.mapIndexed { i, v ->
                    val x = if (values.size == 1) 0f else i.toFloat() / (values.lastIndex.toFloat())
                    val y = (v / maxValue).coerceIn(0f, 1f) // 0..1
                    x to y
                }

                val line = Path()
                val fill = Path()
                pts.forEachIndexed { idx, (x, y) ->
                    val xp = x * w
                    val yp = (1f - y) * h
                    if (idx == 0) {
                        line.moveTo(xp, yp)
                        fill.moveTo(xp, h)
                        fill.lineTo(xp, yp)
                    } else {
                        line.lineTo(xp, yp)
                        fill.lineTo(xp, yp)
                    }
                }
                fill.lineTo(w, h); fill.close()

                drawPath(
                    path = fill,
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF06B6D4).copy(alpha = 0.25f), Color(0xFF06B6D4).copy(alpha = 0.05f))
                    )
                )
                drawPath(path = line, color = Color(0xFF06B6D4), style = Stroke(width = 2.5.dp.toPx()))

                // chấm
                pts.forEach { (x, y) ->
                    val xp = x * w; val yp = (1f - y) * h
                    drawCircle(Color(0xFF06B6D4), radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(xp, yp))
                }
            }
        }

        // Nhãn trục X đơn giản (tùy chọn)
        if (days.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(days.first().date.takeLast(5), fontSize = 11.sp, color = Color.Gray)
                Text(days.last().date.takeLast(5), fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
