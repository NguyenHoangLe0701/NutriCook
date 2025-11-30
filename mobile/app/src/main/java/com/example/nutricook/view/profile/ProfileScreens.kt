package com.example.nutricook.view.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.model.newsfeed.Post
import com.example.nutricook.model.user.User
import com.example.nutricook.viewmodel.nutrition.NutritionViewModel
import com.example.nutricook.viewmodel.profile.ProfileViewModel
import com.example.nutricook.data.nutrition.GeminiNutritionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.EntryPointAccessors

// --- MÀU SẮC ---
private val TealPrimary = Color(0xFF10B981) // Đồng bộ màu Green với Newsfeed
private val TealLight = Color(0xFFECFDF5)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val DividerColor = Color(0xFFF3F4F6)
private val CardBg = Color(0xFFF9FAFB)

// Gradient Header
private val HeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFF0FDF4), Color(0xFFF9FAFB), Color.White)
)

// EntryPoint để inject GeminiNutritionService vào Composable
@EntryPoint
@InstallIn(ActivityComponent::class)
interface GeminiServiceEntryPoint {
    fun geminiService(): GeminiNutritionService
}

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {},
    onOpenRecent: () -> Unit = {},
    onEditAvatar: () -> Unit = {},
    onOpenPosts: () -> Unit = {},
    onOpenSaves: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel(),
    nutritionVm: NutritionViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()
    val nutritionState by nutritionVm.ui.collectAsState()
    val savedPosts by vm.savedPosts.collectAsState()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    // Inject GeminiNutritionService
    val context = LocalContext.current
    val geminiService = remember {
        val activity = context as? androidx.activity.ComponentActivity
            ?: null
        if (activity != null) {
            EntryPointAccessors.fromActivity(
                activity,
                GeminiServiceEntryPoint::class.java
            ).geminiService()
        } else {
            null
        }
    }

    // Logic: Khi chuyển sang tab "Đã lưu" (index 2), gọi loadSavedPosts
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 2) {
            vm.loadSavedPosts()
        }
    }

    Scaffold(
        bottomBar = bottomBar,
        containerColor = Color.White
    ) { padding ->
        if (ui.loading && ui.profile == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else if (ui.profile != null) {
            val p = ui.profile!!
            // Load nutrition data khi có user info
            LaunchedEffect(p.user.id) { nutritionVm.loadData() }

            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // ==========================================
                // 1. HEADER INFO
                // ==========================================
                item {
                    Box(modifier = Modifier.fillMaxWidth().background(HeaderGradient)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            // Top Bar
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onOpenSearch) {
                                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = TextDark)
                                }
                                Row {
                                    IconButton(onClick = onOpenRecent) {
                                        Icon(Icons.Outlined.History, contentDescription = "Recent", tint = TextDark)
                                    }
                                    IconButton(onClick = onOpenSettings) {
                                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = TextDark)
                                    }
                                }
                            }

                            // Avatar
                            val avatarUrl = p.user.avatarUrl
                            val displayName = p.user.bestName()
                            val initial = displayName.firstOrNull()?.uppercase() ?: "?"

                            Box(contentAlignment = Alignment.BottomEnd) {
                                if (avatarUrl.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier.size(110.dp).clip(CircleShape).background(TealLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = initial, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                    }
                                } else {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.size(110.dp).clip(CircleShape).background(Color.White),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Box(
                                    modifier = Modifier.offset(x = 6.dp, y = 6.dp).size(32.dp)
                                        .clip(CircleShape).background(TealPrimary)
                                        .border(2.dp, Color.White, CircleShape)
                                        .clickable { onEditAvatar() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text(text = displayName, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextDark))
                            Text(text = p.bio ?: "Food Blogger / Healthy Life 🌱", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))

                            Spacer(Modifier.height(24.dp))

                            // Stats
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileStatItem(count = p.posts.toString(), label = "Bài viết")
                                ProfileVerticalDivider()
                                ProfileStatItem(count = p.following.toString(), label = "Đang theo dõi")
                                ProfileVerticalDivider()
                                ProfileStatItem(count = p.followers.toString(), label = "Người theo dõi")
                            }
                        }
                    }
                }

                // ==========================================
                // 2. NUTRITION TRACKING CARD
                // ==========================================
                item {
                    val todayLog = nutritionState.todayLog
                    val todayCalories = todayLog?.calories ?: 0f
                    val caloriesTarget = p.nutrition?.caloriesTarget ?: 2000f
                    val historyData = if (nutritionState.history.isNotEmpty()) nutritionState.history.map { it.calories } else listOf(0f,0f,0f,0f,0f,0f,0f)

                    CaloriesTrackingCard(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        todayCalories = todayCalories,
                        caloriesTarget = caloriesTarget,
                        todayLog = todayLog,
                        weeklyData = historyData,
                        onAddClick = { showUpdateDialog = true },
                        onTargetChange = { newTarget ->
                            vm.updateCaloriesTarget(newTarget)
                        }
                    )
                }

                // ==========================================
                // 3. TABS
                // ==========================================
                item {
                    val tabs = listOf("Công thức", "Bài viết", "Đã lưu")
                    Column {
                        Spacer(Modifier.height(10.dp))
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
                            divider = { HorizontalDivider(color = DividerColor) }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 15.sp,
                                            color = if (selectedTabIndex == index) TextDark else TextGray
                                        )
                                    },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // ==========================================
                // 4. TAB CONTENT
                // ==========================================
                when (selectedTabIndex) {
                    0 -> {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                                Text("Bếp của bạn chưa đỏ lửa 🔥", color = TextGray)
                                TextButton(onClick = { }) { Text("Tạo công thức ngay", color = TealPrimary) }
                            }
                        }
                    }
                    1 -> {
                        item {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                                Text("Chia sẻ khoảnh khắc ăn uống 📸", color = TextGray)
                                TextButton(onClick = onOpenPosts) { Text("Xem tất cả bài viết", color = TealPrimary) }
                            }
                        }
                    }
                    2 -> {
                        // DANH SÁCH ĐÃ LƯU
                        if (savedPosts.isEmpty()) {
                            item {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                                    Text("Chưa có bài viết nào được lưu ❤️", color = TextGray)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Hãy lướt Newsfeed để lưu bài nhé!", fontSize = 13.sp, color = TealPrimary)
                                }
                            }
                        } else {
                            items(savedPosts, key = { it.id }) { post ->
                                SimpleSavedPostCard(post = post)
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            } // End LazyColumn

            // Dialog nhập liệu dinh dưỡng
            if (showUpdateDialog) {
                ProfessionalNutritionDialog(
                    initialCalories = nutritionState.todayLog?.calories ?: 0f,
                    initialProtein = nutritionState.todayLog?.protein ?: 0f,
                    initialFat = nutritionState.todayLog?.fat ?: 0f,
                    initialCarb = nutritionState.todayLog?.carb ?: 0f,
                    caloriesTarget = p.nutrition?.caloriesTarget ?: 2000f,
                    onDismiss = { showUpdateDialog = false },
                    onSave = { c, pr, f, cb ->
                        nutritionVm.updateTodayNutrition(c, pr, f, cb)
                        showUpdateDialog = false
                    },
                    geminiService = geminiService
                )
            }
        }
    }
}

// =====================================================
// HELPER COMPOSABLES & EXTENSIONS
// =====================================================

fun User.bestName(): String {
    return if (!displayName.isNullOrBlank()) displayName else email.substringBefore("@")
}

@Composable
fun SimpleSavedPostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Ảnh Thumbnail
            val thumb = post.imageUrl
            if (!thumb.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumb)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            } else {
                Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF3F4F6)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = TextGray)
                }
                Spacer(Modifier.width(12.dp))
            }

            // Nội dung - ĐÃ CẬP NHẬT ĐỂ HIỂN THỊ TITLE
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.author.bestName(),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextGray, fontSize = 12.sp)
                )
                Spacer(Modifier.height(4.dp))

                // [FIX] Hiển thị Title nếu có
                if (post.title.isNotBlank()) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextDark
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = post.content.ifBlank { "..." },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (post.title.isNotBlank()) TextDark.copy(alpha = 0.8f) else TextDark
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextDark, fontSize = 22.sp))
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextGray, fontSize = 13.sp))
    }
}

@Composable
fun ProfileVerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFE5E7EB)))
}

@Composable
fun CaloriesTrackingCard(
    modifier: Modifier = Modifier,
    todayCalories: Float,
    caloriesTarget: Float,
    todayLog: com.example.nutricook.model.nutrition.DailyLog?,
    weeklyData: List<Float>,
    onAddClick: () -> Unit,
    onTargetChange: ((Float) -> Unit)? = null
) {
    var showTargetDialog by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Theo dõi Calories",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextDark, fontSize = 20.sp)
                    )
                    Text(
                        text = "Hôm nay",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextGray, fontSize = 14.sp)
                    )
                }
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Thêm", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = (todayCalories / caloriesTarget).coerceIn(0f, 1f)
                    val remaining = caloriesTarget - todayCalories

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)

                        drawArc(
                            color = Color(0xFFE5E7EB),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                        drawArc(
                            color = if(progress >= 1f) Color(0xFFEF4444) else TealPrimary,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${todayCalories.toInt()}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextDark, fontSize = 30.sp)
                        )
                        Text(
                            text = "kcal",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextGray, fontSize = 12.sp)
                        )
                        if (remaining > 0) {
                            Text(
                                text = "Còn ${remaining.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TealPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = "Vượt ${(-remaining).toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mục tiêu với nút chỉnh sửa
                    Row(
                        Modifier.fillMaxWidth(), 
                        Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mục tiêu", fontSize = 13.sp, color = TextGray)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("${caloriesTarget.toInt()} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            if (onTargetChange != null) {
                                IconButton(
                                    onClick = { showTargetDialog = true },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = "Chỉnh sửa mục tiêu",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (todayLog != null) {
                        MacroStatItem(
                            label = "Protein",
                            value = "${todayLog.protein.toInt()}",
                            unit = "g",
                            color = Color(0xFF3B82F6),
                            progress = (todayLog.protein / (caloriesTarget * 0.3f / 4f)).coerceIn(0f, 1f)
                        )
                        MacroStatItem(
                            label = "Carb",
                            value = "${todayLog.carb.toInt()}",
                            unit = "g",
                            color = Color(0xFF10B981),
                            progress = (todayLog.carb / (caloriesTarget * 0.45f / 4f)).coerceIn(0f, 1f)
                        )
                        MacroStatItem(
                            label = "Fat",
                            value = "${todayLog.fat.toInt()}",
                            unit = "g",
                            color = Color(0xFFF59E0B),
                            progress = (todayLog.fat / (caloriesTarget * 0.25f / 9f)).coerceIn(0f, 1f)
                        )
                    } else {
                        MacroStatItem("Protein", "0", "g", Color(0xFF3B82F6), 0f)
                        MacroStatItem("Carb", "0", "g", Color(0xFF10B981), 0f)
                        MacroStatItem("Fat", "0", "g", Color(0xFFF59E0B), 0f)
                    }
                }
            }

            HorizontalDivider(color = DividerColor, thickness = 1.dp)

            Column {
                Text(
                    text = "7 ngày qua",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ImprovedChartCard(dataPoints = weeklyData, target = caloriesTarget)
            }
        }
    }
    
    // Dialog chỉnh sửa mục tiêu
    if (showTargetDialog && onTargetChange != null) {
        CaloriesTargetDialog(
            currentTarget = caloriesTarget,
            onDismiss = { showTargetDialog = false },
            onSave = { newTarget ->
                onTargetChange(newTarget)
                showTargetDialog = false
            }
        )
    }
}

@Composable
fun MacroStatItem(
    label: String,
    value: String,
    unit: String,
    color: Color,
    progress: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = label, fontSize = 13.sp, color = TextGray)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = " $unit", fontSize = 11.sp, color = TextGray)
            }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

// --- DIALOG CHỈNH SỬA MỤC TIÊU CALORIES ---
@Composable
fun CaloriesTargetDialog(
    currentTarget: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    var targetValue by remember { mutableStateOf(currentTarget.toInt().toString()) }
    var selectedPreset by remember { mutableStateOf<String?>(null) }
    
    val presets = mapOf(
        "Nữ giới" to 2000f,
        "Nam giới" to 2350f,
        "Tập gym (Nam)" to 3000f,
        "Tập gym (Nữ)" to 2500f,
        "Giảm cân" to 1500f,
        "Tăng cân" to 2800f
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Điều chỉnh mục tiêu",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextGray)
                    }
                }
                
                Text(
                    "Chọn mục tiêu phù hợp với bạn",
                    fontSize = 14.sp,
                    color = TextGray
                )
                
                // Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Gợi ý nhanh", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets.keys.toList()) { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = {
                                    selectedPreset = preset
                                    targetValue = presets[preset]!!.toInt().toString()
                                },
                                label = { 
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(preset, fontSize = 12.sp)
                                        Text("${presets[preset]!!.toInt()} kcal", fontSize = 10.sp)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF3F4F6),
                                    labelColor = TextDark
                                )
                            )
                        }
                    }
                }
                
                // Input tùy chỉnh
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { 
                        targetValue = it.filter { char -> char.isDigit() }
                        selectedPreset = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mục tiêu calories (kcal)") },
                    leadingIcon = { Icon(Icons.Outlined.Favorite, contentDescription = null, tint = TealPrimary) },
                    suffix = { Text("kcal", color = TextGray, fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                
                Button(
                    onClick = {
                        val newTarget = targetValue.toFloatOrNull() ?: currentTarget
                        if (newTarget in 1000f..5000f) {
                            onSave(newTarget)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lưu mục tiêu", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ImprovedChartCard(dataPoints: List<Float>, target: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color.White
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        if (dataPoints.isEmpty() || dataPoints.all { it == 0f }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.BarChart, contentDescription = null, tint = TextGray, modifier = Modifier.size(32.dp))
                    Text("Chưa có dữ liệu", color = TextGray, fontSize = 13.sp)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Labels cho trục Y
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val maxVal = maxOf(dataPoints.maxOrNull() ?: target, target * 1.2f)
                    Text("${maxVal.toInt()}", fontSize = 10.sp, color = TextGray)
                    Text("${target.toInt()}", fontSize = 10.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                    Text("0", fontSize = 10.sp, color = TextGray)
                }
                
                Spacer(Modifier.height(4.dp))
                
                Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val width = size.width
                    val height = size.height
                    val maxVal = maxOf(dataPoints.maxOrNull() ?: target, target * 1.2f)

                    val targetY = height - (target / maxVal) * height
                    drawLine(
                        color = Color(0xFF6366F1).copy(alpha = 0.5f),
                        start = Offset(0f, targetY),
                        end = Offset(width, targetY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    val path = Path()
                    val fillPath = Path()

                    val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

                    dataPoints.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = height - (value / maxVal) * height

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevY = height - (dataPoints[index - 1] / maxVal) * height
                            val conX1 = (prevX + x) / 2f
                            val conY1 = prevY
                            val conX2 = (prevX + x) / 2f
                            val conY2 = y

                            path.cubicTo(conX1, conY1, conX2, conY2, x, y)
                            fillPath.cubicTo(conX1, conY1, conX2, conY2, x, y)
                        }

                        if (index == dataPoints.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // Tô màu vùng bên dưới với gradient đẹp hơn
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                TealPrimary.copy(alpha = 0.4f),
                                TealPrimary.copy(alpha = 0.15f),
                                TealPrimary.copy(alpha = 0.0f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Vẽ đường Line chính với độ dày hơn
                    drawPath(
                        path = path,
                        color = TealPrimary,
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                    )

                    // Vẽ tất cả các điểm dữ liệu
                    dataPoints.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = height - (value / maxVal) * height
                        val pointColor = if (value >= target) Color(0xFFEF4444) else TealPrimary
                        drawCircle(
                            color = pointColor,
                            radius = 5f,
                            center = Offset(x, y)
                        )
                        // Vòng tròn ngoài
                        drawCircle(
                            color = pointColor.copy(alpha = 0.3f),
                            radius = 8f,
                            center = Offset(x, y),
                            style = Stroke(width = 2f)
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Labels cho trục X (ngày)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dataPoints.forEachIndexed { index, _ ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = "T${index + 1}",
                                fontSize = 9.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- DIALOG INPUT & DATA ---

@Composable
fun ProfessionalNutritionDialog(
    initialCalories: Float,
    initialProtein: Float,
    initialFat: Float,
    initialCarb: Float,
    caloriesTarget: Float,
    onDismiss: () -> Unit,
    onSave: (Float, Float, Float, Float) -> Unit,
    geminiService: GeminiNutritionService? = null
) {
    var cal by remember { mutableStateOf(if(initialCalories > 0) initialCalories.toString() else "") }
    var pro by remember { mutableStateOf(if(initialProtein > 0) initialProtein.toString() else "") }
    var fat by remember { mutableStateOf(if(initialFat > 0) initialFat.toString() else "") }
    var carb by remember { mutableStateOf(if(initialCarb > 0) initialCarb.toString() else "") }

    val currentCalories = cal.toFloatOrNull() ?: 0f
    val remaining = caloriesTarget - currentCalories
    val progress = (currentCalories / caloriesTarget).coerceIn(0f, 1f)

    // Dữ liệu món ăn
    val foodCategories = remember {
        mapOf(
            "⭐ Phổ biến nhất" to listOf(
                QuickFood("Cơm trắng (1 chén vừa)", 130f, 2.7f, 0.3f, 28.2f),
                QuickFood("Phở bò tái (1 tô)", 430f, 22f, 12f, 60f),
                QuickFood("Bánh mì thịt đầy đủ", 450f, 18f, 20f, 50f),
                QuickFood("Cơm tấm sườn bì chả", 627f, 32f, 28f, 65f),
                QuickFood("Trứng ốp la (1 quả)", 90f, 6.3f, 7f, 0.6f),
                QuickFood("Gỏi cuốn tôm thịt (1 cái)", 65f, 4f, 1f, 10f),
                QuickFood("Cà phê sữa đá (1 ly)", 180f, 2f, 5f, 30f),
                QuickFood("Chuối (1 quả)", 105f, 1.3f, 0.4f, 27f)
            ),
            "🍚 Cơm & Xôi" to listOf(
                QuickFood("Cơm trắng (100g)", 130f, 2.7f, 0.3f, 28f),
                QuickFood("Cơm gạo lứt (100g)", 110f, 2.6f, 0.9f, 23f),
                QuickFood("Cơm tấm sườn nướng", 520f, 25f, 20f, 60f),
                QuickFood("Cơm tấm bì chả", 590f, 28f, 25f, 62f),
                QuickFood("Cơm gà xối mỡ", 650f, 30f, 35f, 55f),
                QuickFood("Cơm gà Hải Nam", 550f, 28f, 22f, 60f),
                QuickFood("Cơm rang dưa bò", 580f, 22f, 25f, 65f),
                QuickFood("Cơm rang thập cẩm", 560f, 18f, 22f, 70f),
                QuickFood("Cơm rang hải sản", 540f, 20f, 20f, 68f),
                QuickFood("Cơm cháy kho quẹt", 450f, 12f, 15f, 68f),
                QuickFood("Cơm niêu (1 thố nhỏ)", 200f, 4f, 0.5f, 45f),
                QuickFood("Cơm lam (1 ống)", 150f, 3f, 0.5f, 35f),
                QuickFood("Xôi mặn thập cẩm", 550f, 25f, 20f, 65f),
                QuickFood("Xôi gà xé", 480f, 22f, 15f, 62f),
                QuickFood("Xôi gấc", 350f, 5f, 8f, 60f),
                QuickFood("Xôi đậu xanh", 320f, 8f, 6f, 58f),
                QuickFood("Xôi bắp (ngô)", 300f, 5f, 8f, 55f),
                QuickFood("Xôi khúc", 400f, 12f, 15f, 50f),
                QuickFood("Xôi vò", 350f, 6f, 10f, 55f),
                QuickFood("Cháo lòng", 350f, 25f, 15f, 30f),
                QuickFood("Cháo gà", 280f, 20f, 8f, 30f),
                QuickFood("Cháo sườn", 320f, 15f, 10f, 40f),
                QuickFood("Cháo trắng hột vịt muối", 220f, 10f, 8f, 35f),
                QuickFood("Cháo ếch Singapore", 450f, 25f, 12f, 55f)
            ),
            "🍜 Phở, Bún & Mì" to listOf(
                QuickFood("Phở bò tái", 430f, 22f, 12f, 60f),
                QuickFood("Phở bò chín", 410f, 20f, 10f, 60f),
                QuickFood("Phở bò nạm", 450f, 21f, 15f, 60f),
                QuickFood("Phở đặc biệt (xe lửa)", 600f, 35f, 20f, 70f),
                QuickFood("Phở gà (thịt trắng)", 400f, 25f, 12f, 55f),
                QuickFood("Phở gà (đùi, da)", 450f, 22f, 18f, 55f),
                QuickFood("Phở cuốn (3 cái)", 350f, 15f, 10f, 45f),
                QuickFood("Phở xào bò", 650f, 25f, 30f, 65f),
                QuickFood("Bún bò Huế (giò heo)", 550f, 28f, 25f, 55f),
                QuickFood("Bún bò Huế (nạm)", 480f, 25f, 18f, 55f),
                QuickFood("Bún riêu cua", 420f, 18f, 15f, 55f),
                QuickFood("Bún ốc", 350f, 15f, 8f, 50f),
                QuickFood("Bún đậu mắm tôm (1 mẹt)", 650f, 40f, 35f, 60f),
                QuickFood("Bún thịt nướng", 450f, 18f, 15f, 60f),
                QuickFood("Bún mắm miền Tây", 520f, 25f, 20f, 58f),
                QuickFood("Bún thang", 380f, 20f, 10f, 50f),
                QuickFood("Bún chả cá Nha Trang", 400f, 20f, 10f, 55f),
                QuickFood("Hủ tiếu Nam Vang", 400f, 18f, 12f, 58f),
                QuickFood("Hủ tiếu gõ (bình dân)", 300f, 10f, 8f, 50f),
                QuickFood("Hủ tiếu bò kho", 500f, 25f, 20f, 55f),
                QuickFood("Mì quảng tôm thịt", 480f, 22f, 18f, 55f),
                QuickFood("Mì quảng gà", 500f, 25f, 20f, 55f),
                QuickFood("Cao lầu Hội An", 450f, 20f, 15f, 60f),
                QuickFood("Bánh canh cua", 420f, 18f, 12f, 60f),
                QuickFood("Bánh canh ghẹ", 400f, 20f, 10f, 58f),
                QuickFood("Bánh canh chả cá", 380f, 15f, 10f, 58f),
                QuickFood("Mì xào bò rau cải", 580f, 25f, 28f, 60f),
                QuickFood("Mì xào giòn hải sản", 620f, 20f, 35f, 65f),
                QuickFood("Mì Ý sốt bò bằm", 550f, 22f, 18f, 70f),
                QuickFood("Miến gà", 350f, 25f, 8f, 45f),
                QuickFood("Miến lươn", 380f, 20f, 10f, 45f),
                QuickFood("Miến trộn", 400f, 15f, 15f, 50f),
                QuickFood("Nui xào bò", 500f, 22f, 20f, 58f)
            ),
            "🥖 Bánh Mì & Sáng" to listOf(
                QuickFood("Bánh mì thịt đầy đủ", 450f, 18f, 20f, 50f),
                QuickFood("Bánh mì ốp la (2 trứng)", 400f, 14f, 18f, 45f),
                QuickFood("Bánh mì chả lụa", 350f, 12f, 10f, 45f),
                QuickFood("Bánh mì heo quay", 480f, 18f, 25f, 45f),
                QuickFood("Bánh mì xíu mại", 420f, 15f, 18f, 48f),
                QuickFood("Bánh mì chảo", 550f, 25f, 30f, 40f),
                QuickFood("Bánh mì que (Pate)", 200f, 5f, 8f, 25f),
                QuickFood("Bánh bao thịt trứng", 320f, 10f, 12f, 40f),
                QuickFood("Bánh bao xá xíu", 300f, 8f, 10f, 42f),
                QuickFood("Bánh bao chay", 180f, 4f, 2f, 35f),
                QuickFood("Bánh cuốn nóng (1 dĩa)", 350f, 10f, 12f, 50f),
                QuickFood("Bánh cuốn trứng", 400f, 16f, 15f, 50f),
                QuickFood("Bánh ướt chả lụa", 320f, 10f, 10f, 48f),
                QuickFood("Bánh giò", 300f, 12f, 15f, 30f),
                QuickFood("Bánh chưng (1 góc 1/8)", 350f, 15f, 15f, 40f),
                QuickFood("Bánh tét (1 khoanh)", 300f, 10f, 12f, 38f),
                QuickFood("Bánh bèo (1 chén)", 50f, 2f, 1f, 10f),
                QuickFood("Bánh nậm (1 cái)", 60f, 3f, 2f, 8f),
                QuickFood("Bánh bột lọc (1 dĩa nhỏ)", 300f, 8f, 10f, 45f),
                QuickFood("Bánh xèo (1 cái)", 350f, 10f, 20f, 30f),
                QuickFood("Bánh khọt (1 dĩa 10 cái)", 400f, 12f, 22f, 35f),
                QuickFood("Khoai lang luộc (1 củ)", 120f, 2f, 0.5f, 28f),
                QuickFood("Bắp luộc (1 trái)", 150f, 4f, 2f, 30f),
                QuickFood("Ngũ cốc (1 chén)", 150f, 5f, 2f, 30f)
            ),
            "🥩 Thịt & Protein" to listOf(
                QuickFood("Ức gà luộc (100g)", 165f, 31f, 3.6f, 0f),
                QuickFood("Ức gà nướng (100g)", 180f, 30f, 5f, 0f),
                QuickFood("Đùi gà chiên (1 cái)", 300f, 18f, 20f, 5f),
                QuickFood("Cánh gà chiên nước mắm", 450f, 25f, 30f, 10f),
                QuickFood("Gà kho gừng (100g)", 200f, 22f, 10f, 5f),
                QuickFood("Gà rang muối (100g)", 250f, 20f, 15f, 5f),
                QuickFood("Thịt heo ba chỉ luộc (100g)", 518f, 9f, 53f, 0f),
                QuickFood("Thịt heo nạc luộc (100g)", 145f, 25f, 4f, 0f),
                QuickFood("Thịt kho tàu (1 phần)", 350f, 15f, 25f, 5f),
                QuickFood("Sườn xào chua ngọt", 350f, 15f, 20f, 15f),
                QuickFood("Sườn cốt lết nướng", 250f, 22f, 15f, 5f),
                QuickFood("Chả lụa (100g)", 230f, 15f, 18f, 2f),
                QuickFood("Lạp xưởng (1 cây)", 180f, 8f, 15f, 5f),
                QuickFood("Nem rán (1 cái)", 120f, 5f, 8f, 10f),
                QuickFood("Thịt bò thăn (100g)", 250f, 26f, 15f, 0f),
                QuickFood("Bò bít tết (150g)", 350f, 38f, 20f, 0f),
                QuickFood("Bò lúc lắc (100g)", 300f, 25f, 20f, 10f),
                QuickFood("Bò kho (1 chén)", 250f, 20f, 15f, 10f),
                QuickFood("Thịt bò xào hành tây", 250f, 22f, 15f, 8f),
                QuickFood("Trứng gà luộc (1 quả)", 78f, 6f, 5f, 0.5f),
                QuickFood("Trứng chiên (2 trứng)", 250f, 14f, 20f, 2f),
                QuickFood("Trứng cút (5 quả)", 75f, 6f, 5f, 0.5f),
                QuickFood("Lòng trắng trứng (1 cái)", 17f, 3.6f, 0f, 0.2f),
                QuickFood("Đậu hũ trắng (1 bìa)", 76f, 8f, 4f, 2f),
                QuickFood("Đậu hũ chiên (1 bìa)", 150f, 10f, 10f, 5f),
                QuickFood("Đậu hũ nhồi thịt", 200f, 15f, 12f, 8f)
            ),
            "🐟 Hải Sản" to listOf(
                QuickFood("Cá hồi áp chảo (100g)", 208f, 20f, 13f, 0f),
                QuickFood("Cá hồi sống (Sashimi)", 200f, 20f, 12f, 0f),
                QuickFood("Cá thu chiên (1 khúc)", 250f, 19f, 18f, 2f),
                QuickFood("Cá lóc kho tộ (1 khúc)", 180f, 18f, 8f, 5f),
                QuickFood("Cá diêu hồng hấp", 150f, 20f, 5f, 2f),
                QuickFood("Cá basa kho tộ", 220f, 15f, 15f, 5f),
                QuickFood("Canh chua cá lóc", 150f, 12f, 5f, 10f),
                QuickFood("Tôm hấp (100g)", 99f, 24f, 0.5f, 0.2f),
                QuickFood("Tôm rang thịt", 300f, 25f, 20f, 5f),
                QuickFood("Tôm lăn bột chiên", 350f, 15f, 25f, 20f),
                QuickFood("Mực hấp gừng", 100f, 16f, 1f, 3f),
                QuickFood("Mực xào chua ngọt", 200f, 18f, 8f, 12f),
                QuickFood("Mực nướng sa tế", 150f, 20f, 5f, 5f),
                QuickFood("Bạch tuộc nướng", 160f, 20f, 5f, 5f),
                QuickFood("Nghêu hấp sả (1 tô)", 100f, 15f, 2f, 5f),
                QuickFood("Hàu nướng mỡ hành (1 con)", 80f, 5f, 5f, 3f),
                QuickFood("Ốc hương rang muối", 200f, 15f, 10f, 5f),
                QuickFood("Cua biển hấp (1 con)", 250f, 30f, 2f, 0f)
            ),
            "🥗 Rau Củ & Canh" to listOf(
                QuickFood("Rau muống luộc", 40f, 3f, 0.5f, 6f),
                QuickFood("Rau muống xào tỏi", 120f, 3f, 10f, 6f),
                QuickFood("Cải thìa xào dầu hào", 90f, 2f, 7f, 5f),
                QuickFood("Bông cải xanh luộc", 34f, 2.8f, 0.4f, 7f),
                QuickFood("Su su luộc", 30f, 1f, 0f, 6f),
                QuickFood("Đậu que xào thịt bò", 200f, 15f, 10f, 10f),
                QuickFood("Khổ qua xào trứng", 150f, 8f, 10f, 8f),
                QuickFood("Canh rau ngót thịt bằm", 120f, 8f, 5f, 5f),
                QuickFood("Canh bí đỏ thịt bằm", 150f, 8f, 6f, 15f),
                QuickFood("Canh chua cá", 150f, 12f, 5f, 10f),
                QuickFood("Canh khổ qua nhồi thịt", 180f, 10f, 8f, 8f),
                QuickFood("Canh khoai mỡ", 200f, 5f, 8f, 25f),
                QuickFood("Salad trộn dầu giấm", 80f, 1f, 7f, 5f),
                QuickFood("Salad cá ngừ", 250f, 20f, 15f, 5f),
                QuickFood("Salad ức gà", 200f, 25f, 8f, 5f),
                QuickFood("Nộm đu đủ (Gỏi)", 150f, 5f, 5f, 20f),
                QuickFood("Gỏi ngó sen tôm thịt", 320f, 18f, 12f, 20f),
                QuickFood("Dưa leo (1 quả)", 16f, 0.7f, 0.1f, 4f),
                QuickFood("Cà chua (1 quả)", 22f, 1f, 0.2f, 5f)
            ),
            "🍎 Trái cây" to listOf(
                QuickFood("Chuối (1 quả)", 105f, 1.3f, 0.4f, 27f),
                QuickFood("Táo (1 quả)", 95f, 0.5f, 0.3f, 25f),
                QuickFood("Cam (1 quả)", 62f, 1.2f, 0.2f, 15f),
                QuickFood("Quýt (1 quả)", 40f, 0.8f, 0.1f, 10f),
                QuickFood("Bưởi (1 múi)", 40f, 0.8f, 0.1f, 10f),
                QuickFood("Dưa hấu (1 miếng)", 46f, 0.9f, 0.2f, 11f),
                QuickFood("Dứa (Thơm) - 100g", 50f, 0.5f, 0.1f, 13f),
                QuickFood("Xoài chín (1 quả)", 200f, 2.8f, 1.2f, 50f),
                QuickFood("Xoài xanh (100g)", 60f, 0.8f, 0.4f, 15f),
                QuickFood("Thanh long (1 quả)", 200f, 2f, 0.5f, 45f),
                QuickFood("Bơ (1/2 quả)", 160f, 2f, 15f, 9f),
                QuickFood("Nho (100g)", 69f, 0.7f, 0.2f, 18f),
                QuickFood("Dâu tây (100g)", 32f, 0.7f, 0.3f, 7.7f),
                QuickFood("Sầu riêng (1 múi lớn)", 350f, 4f, 13f, 65f),
                QuickFood("Mít (100g)", 95f, 1.7f, 0.6f, 23f),
                QuickFood("Vải (10 quả)", 66f, 0.8f, 0.4f, 16f),
                QuickFood("Nhãn (10 quả)", 60f, 1f, 0.1f, 15f),
                QuickFood("Ổi (1 quả)", 60f, 2.5f, 0.9f, 14f),
                QuickFood("Đu đủ (100g)", 43f, 0.5f, 0.3f, 11f),
                QuickFood("Măng cụt (100g)", 73f, 0.4f, 0.6f, 18f)
            ),
            "🍧 Tráng miệng & Ăn vặt" to listOf(
                QuickFood("Chè thái", 400f, 5f, 15f, 60f),
                QuickFood("Chè đậu xanh", 300f, 8f, 2f, 60f),
                QuickFood("Chè trôi nước (1 chén)", 350f, 4f, 8f, 65f),
                QuickFood("Chè bưởi", 320f, 2f, 10f, 60f),
                QuickFood("Sữa chua (1 hộp)", 100f, 5f, 3f, 15f),
                QuickFood("Sữa chua nếp cẩm", 200f, 6f, 4f, 35f),
                QuickFood("Bánh flan (1 cái)", 120f, 4f, 5f, 15f),
                QuickFood("Kem tươi (1 cây)", 200f, 3f, 10f, 25f),
                QuickFood("Tào phớ (Tàu hũ)", 150f, 8f, 2f, 25f),
                QuickFood("Bánh tráng trộn", 350f, 8f, 15f, 45f),
                QuickFood("Bánh tráng nướng", 300f, 8f, 12f, 40f),
                QuickFood("Cá viên chiên (1 xiên)", 120f, 8f, 8f, 5f),
                QuickFood("Xúc xích nướng (1 cây)", 150f, 6f, 12f, 2f),
                QuickFood("Nem chua rán (1 cái)", 80f, 5f, 6f, 2f),
                QuickFood("Khoai lang lắc", 300f, 2f, 10f, 50f),
                QuickFood("Bắp xào tép", 350f, 8f, 15f, 45f),
                QuickFood("Hột vịt lộn (1 quả)", 182f, 13.6f, 12.4f, 4f),
                QuickFood("Cút lộn xào me (1 dĩa)", 300f, 15f, 18f, 10f),
                QuickFood("Snack khoai tây (1 gói)", 160f, 2f, 10f, 15f),
                QuickFood("Hạt điều (50g)", 280f, 9f, 22f, 15f),
                QuickFood("Hạt hướng dương (50g)", 290f, 10f, 25f, 10f)
            ),
            "🥤 Đồ uống" to listOf(
                QuickFood("Cà phê đen đá", 10f, 0.5f, 0f, 2f),
                QuickFood("Cà phê sữa đá", 180f, 4f, 8f, 25f),
                QuickFood("Bạc xỉu", 250f, 5f, 10f, 35f),
                QuickFood("Trà sữa trân châu (Size M)", 450f, 2f, 15f, 80f),
                QuickFood("Trà đào cam sả", 120f, 0f, 0f, 30f),
                QuickFood("Nước cam vắt", 120f, 2f, 0.5f, 28f),
                QuickFood("Nước ép dưa hấu", 80f, 1f, 0f, 20f),
                QuickFood("Nước dừa tươi", 60f, 1f, 0.5f, 15f),
                QuickFood("Sinh tố bơ", 350f, 4f, 20f, 40f),
                QuickFood("Coca Cola (1 lon)", 140f, 0f, 0f, 39f),
                QuickFood("Bia (1 lon)", 150f, 1f, 0f, 12f),
                QuickFood("Sữa tươi không đường (200ml)", 120f, 6f, 6f, 10f),
                QuickFood("Sữa tươi có đường (200ml)", 150f, 6f, 6f, 18f),
                QuickFood("Sữa hạt (Hạnh nhân)", 60f, 2f, 5f, 2f),
                QuickFood("Sữa đậu nành", 100f, 7f, 4f, 8f)
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("⭐ Phổ biến nhất") }
    var searchQuery by remember { mutableStateOf("") }
    var geminiResult by remember { mutableStateOf<QuickFood?>(null) }
    var isLoadingGemini by remember { mutableStateOf(false) }
    var geminiError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // Debounce search và tự động gọi Gemini nếu không tìm thấy
    LaunchedEffect(searchQuery, geminiService) {
        geminiResult = null
        geminiError = null
        
        if (searchQuery.isBlank()) {
            return@LaunchedEffect
        }
        
        // Đợi 1000ms sau khi ngừng gõ để tránh gọi API quá nhiều
        delay(1000)
        
        // Kiểm tra xem có kết quả trong danh sách không
        val allFoods = foodCategories.values.flatten()
        val found = allFoods.any { 
            it.name.contains(searchQuery, ignoreCase = true) 
        }
        
        // Nếu không tìm thấy và có Gemini service, tự động gọi API
        if (!found && geminiService != null && geminiService.isApiKeyConfigured() && searchQuery.length >= 3) {
            isLoadingGemini = true
            geminiError = null
            
            try {
                val nutrition = geminiService.calculateNutrition(searchQuery)
                
                if (nutrition != null) {
                    geminiResult = QuickFood(
                        name = searchQuery,
                        calories = nutrition.calories,
                        protein = nutrition.protein,
                        fat = nutrition.fat,
                        carb = nutrition.carb
                    )
                    geminiError = null
                } else {
                    geminiError = null // Không hiển thị lỗi, chỉ đơn giản là không có kết quả
                }
            } catch (e: Exception) {
                geminiError = null // Không hiển thị lỗi để tránh rối mắt
            } finally {
                isLoadingGemini = false
            }
        } else if (!found && searchQuery.length >= 3 && (geminiService == null || !geminiService.isApiKeyConfigured())) {
            // Nếu không có API key, không làm gì cả
            isLoadingGemini = false
        }
    }
    
    val displayedFoods = remember(selectedCategory, searchQuery, geminiResult) {
        val allFoods = foodCategories[selectedCategory] ?: emptyList()
        val filtered = if (searchQuery.isBlank()) {
            allFoods
        } else {
            allFoods.filter { 
                it.name.contains(searchQuery, ignoreCase = true) 
            }
        }
        
        // Thêm kết quả từ Gemini nếu có và không trùng
        if (geminiResult != null && !filtered.any { it.name.equals(geminiResult!!.name, ignoreCase = true) }) {
            listOf(geminiResult!!) + filtered
        } else {
            filtered
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thêm bữa ăn", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextDark))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextGray) }
                }

                Column {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Đã nạp: ${currentCalories.toInt()}", fontWeight = FontWeight.Bold, color = TealPrimary)
                        Text("Mục tiêu: ${caloriesTarget.toInt()}", color = TextGray)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = if(progress > 1f) Color.Red else TealPrimary,
                        trackColor = Color(0xFFE5E7EB)
                    )
                }

                // Search bar - tự động tính calories khi không tìm thấy
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            if (geminiService?.isApiKeyConfigured() == true) 
                                "Tìm kiếm hoặc nhập món ăn (ví dụ: nửa con cá ngừ, 1 trái dâu)..."
                            else 
                                "Tìm kiếm món ăn...", 
                            fontSize = 14.sp
                        ) 
                    },
                    leadingIcon = { 
                        if (isLoadingGemini) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TealPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Search, contentDescription = "Tìm kiếm", tint = TextGray)
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { 
                                searchQuery = ""
                                geminiResult = null
                                geminiError = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = TextGray, modifier = Modifier.size(18.dp))
                            }
                        } else if (geminiResult != null) {
                            Icon(
                                Icons.Outlined.AutoAwesome, 
                                contentDescription = "Kết quả từ AI", 
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    singleLine = true
                )
                
                // Hiển thị thông báo khi đang tính calories từ AI
                if (isLoadingGemini && searchQuery.length >= 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = TealPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "✨ Đang tính calories cho \"$searchQuery\"...",
                            fontSize = 12.sp,
                            color = TextGray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
                
                // Hiển thị thông báo khi có kết quả từ AI
                if (geminiResult != null && !isLoadingGemini) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Đã tìm thấy \"${geminiResult!!.name}\" với ${geminiResult!!.calories.toInt()} kcal",
                                fontSize = 12.sp,
                                color = Color(0xFF6366F1),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gợi ý nhanh", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        if (displayedFoods.isNotEmpty()) {
                            Text(
                                "${displayedFoods.size} món",
                                fontSize = 12.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Categories
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        items(foodCategories.keys.toList()) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { 
                                    selectedCategory = cat
                                    searchQuery = ""
                                },
                                label = { Text(cat, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF3F4F6),
                                    labelColor = TextDark
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategory == cat,
                                    selectedBorderColor = TealPrimary,
                                    borderColor = Color(0xFFE5E7EB),
                                    selectedBorderWidth = 1.5.dp,
                                    borderWidth = 1.dp
                                )
                            )
                        }
                    }
                    
                    // Food grid - scrollable
                    if (displayedFoods.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = TextGray, modifier = Modifier.size(40.dp))
                                Text("Không tìm thấy món ăn", color = TextGray, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            items(displayedFoods) { food ->
                                val isFromGemini = food == geminiResult
                                QuickFoodChip(food, isFromGemini = isFromGemini) {
                                    cal = (currentCalories + food.calories).toString()
                                    pro = ((pro.toFloatOrNull() ?: 0f) + food.protein).toString()
                                    fat = ((fat.toFloatOrNull() ?: 0f) + food.fat).toString()
                                    carb = ((carb.toFloatOrNull() ?: 0f) + food.carb).toString()
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = cal, onValueChange = { cal = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroInputField(label = "Protein", value = pro, onValueChange = { pro = it }, color = Color(0xFF3B82F6), modifier = Modifier.weight(1f))
                    MacroInputField(label = "Fat", value = fat, onValueChange = { fat = it }, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                    MacroInputField(label = "Carb", value = carb, onValueChange = { carb = it }, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = {
                        onSave(cal.toFloatOrNull()?:0f, pro.toFloatOrNull()?:0f, fat.toFloatOrNull()?:0f, carb.toFloatOrNull()?:0f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lưu nhật ký", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Dialog nhập món ăn tùy chỉnh
@Composable
fun CustomFoodInputDialog(
    onDismiss: () -> Unit,
    onAdd: (QuickFood) -> Unit,
    geminiService: GeminiNutritionService? = null
) {
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    var isLoadingGemini by remember { mutableStateOf(false) }
    var geminiError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Thêm món ăn tùy chỉnh",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = TextDark)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextGray)
                    }
                }
                
                // Tên món ăn
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Tên món ăn (ví dụ: 1 quả táo, 100g bơ)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = TealPrimary) },
                    trailingIcon = {
                        if (foodName.isNotEmpty() && !isLoadingGemini && geminiService != null && geminiService.isApiKeyConfigured()) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoadingGemini = true
                                        geminiError = null
                                        
                                        val nutrition = geminiService.calculateNutrition(foodName)
                                        
                                        if (nutrition != null) {
                                            calories = nutrition.calories.toInt().toString()
                                            protein = String.format("%.1f", nutrition.protein)
                                            fat = String.format("%.1f", nutrition.fat)
                                            carb = String.format("%.1f", nutrition.carb)
                                        } else {
                                            geminiError = "Không thể tính calories tự động. Vui lòng nhập thủ công."
                                        }
                                        
                                        isLoadingGemini = false
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = "Tự động tính", tint = TealPrimary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                
                if (isLoadingGemini) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = TealPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Đang tính calories...", fontSize = 12.sp, color = TextGray)
                    }
                }
                
                if (geminiError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = geminiError ?: "",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF856404)
                        )
                    }
                }
                
                // Thông tin dinh dưỡng
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Outlined.LocalFireDepartment, contentDescription = null, tint = Color(0xFFEF4444)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroInputField(
                        label = "Protein (g)",
                        value = protein,
                        onValueChange = { protein = it },
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.weight(1f)
                    )
                    MacroInputField(
                        label = "Fat (g)",
                        value = fat,
                        onValueChange = { fat = it },
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )
                    MacroInputField(
                        label = "Carb (g)",
                        value = carb,
                        onValueChange = { carb = it },
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Gợi ý nhanh cho một số món phổ biến
                Text("💡 Gợi ý nhanh:", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val suggestions = listOf(
                        QuickFood("Táo (1 quả)", 95f, 0.5f, 0.3f, 25f),
                        QuickFood("Bơ (1 quả)", 320f, 4f, 29f, 17f),
                        QuickFood("Chuối (1 quả)", 105f, 1.3f, 0.4f, 27f),
                        QuickFood("Cam (1 quả)", 62f, 1.2f, 0.2f, 15f)
                    )
                    items(suggestions) { suggestion ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                foodName = suggestion.name
                                calories = suggestion.calories.toInt().toString()
                                protein = String.format("%.1f", suggestion.protein)
                                fat = String.format("%.1f", suggestion.fat)
                                carb = String.format("%.1f", suggestion.carb)
                            },
                            label = { Text(suggestion.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF3F4F6),
                                labelColor = TextDark
                            )
                        )
                    }
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = {
                            val calValue = calories.toFloatOrNull() ?: 0f
                            val proValue = protein.toFloatOrNull() ?: 0f
                            val fatValue = fat.toFloatOrNull() ?: 0f
                            val carbValue = carb.toFloatOrNull() ?: 0f
                            
                            if (foodName.isNotBlank() && calValue > 0) {
                                onAdd(QuickFood(foodName, calValue, proValue, fatValue, carbValue))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = foodName.isNotBlank() && calories.toFloatOrNull() ?: 0f > 0
                    ) {
                        Text("Thêm", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class QuickFood(val name: String, val calories: Float, val protein: Float, val fat: Float, val carb: Float)

@Composable
fun QuickFoodChip(food: QuickFood, isFromGemini: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, TealPrimary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Food name với badge AI nếu từ Gemini
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = food.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isFromGemini) {
                    Surface(
                        color = Color(0xFF6366F1).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = "AI",
                                modifier = Modifier.size(10.dp),
                                tint = Color(0xFF6366F1)
                            )
                            Text(
                                text = "AI",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6366F1)
                            )
                        }
                    }
                }
            }
            
            // Calories badge
            Surface(
                color = if (isFromGemini) Color(0xFF6366F1).copy(alpha = 0.1f) else TealPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${food.calories.toInt()} kcal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isFromGemini) Color(0xFF6366F1) else TealPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            // Macro info (small)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "P:${food.protein.toInt()}",
                    fontSize = 9.sp,
                    color = Color(0xFF3B82F6),
                    fontWeight = FontWeight.Medium
                )
                Text("•", fontSize = 9.sp, color = TextGray)
                Text(
                    text = "F:${food.fat.toInt()}",
                    fontSize = 9.sp,
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Medium
                )
                Text("•", fontSize = 9.sp, color = TextGray)
                Text(
                    text = "C:${food.carb.toInt()}",
                    fontSize = 9.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun MacroInputField(label: String, value: String, onValueChange: (String) -> Unit, color: Color, modifier: Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedLabelColor = color)
    )
}