package com.example.nutricook.view.profile

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.model.user.bestName
import com.example.nutricook.viewmodel.nutrition.NutritionViewModel
import com.example.nutricook.viewmodel.profile.ProfileUiState
import com.example.nutricook.viewmodel.profile.ProfileViewModel
import com.example.nutricook.data.nutrition.GeminiNutritionService
import kotlinx.coroutines.delay
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.EntryPointAccessors

// --- MÀU SẮC ---
private val TealPrimary = Color(0xFF2BB6AD)
private val TealLight = Color(0xFFE0F7F6)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF9CA3AF)
private val DividerColor = Color(0xFFF3F4F6)
private val CardBg = Color(0xFFF9FAFB) // Màu nền cho biểu đồ tuần

// Gradient Header
private val HeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFF0E8), Color(0xFFFFFBF9), Color.White)
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
    var showUpdateDialog by remember { mutableStateOf(false) }
    
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

    Scaffold(
        bottomBar = bottomBar,
        containerColor = Color.White
    ) { padding ->
        if (ui.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TealPrimary)
            }
        } else if (ui.profile != null) {
            val p = ui.profile!!
            LaunchedEffect(p.user.id) { nutritionVm.loadData() }

            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                // ==========================================
                // 1. HEADER (Style Public Profile)
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
                                horizontalArrangement = Arrangement.SpaceBetween, // [UPDATED] Căn đều 2 bên
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Nút Search bên trái
                                IconButton(onClick = onOpenSearch) {
                                    Icon(Icons.Outlined.Search, contentDescription = "Search", tint = TextDark)
                                }

                                // Cụm nút bên phải
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
                            val initial = p.user.bestName().firstOrNull()?.uppercase() ?: "?"
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
                                        model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
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
                            Text(text = p.user.bestName(), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp, color = TextDark))
                            Text(text = "Food Blogger / Healthy Life 🌱", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))

                            Spacer(Modifier.height(24.dp))

                            // Stats
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileStatItem(count = p.posts.toString(), label = "Post")
                                ProfileVerticalDivider()
                                ProfileStatItem(count = p.following.toString(), label = "Following")
                                ProfileVerticalDivider()
                                ProfileStatItem(count = p.followers.toString(), label = "Follower")
                            }
                        }
                    }
                }

                // ==========================================
                // 2. THẺ CALORIES CHI TIẾT (RESTORED FULL VERSION)
                // ==========================================
                item {
                    val todayCalories = nutritionState.history.map { it.calories }.lastOrNull() ?: 0f
                    val caloriesTarget = p.nutrition?.caloriesTarget ?: 2000f
                    val historyData = nutritionState.history.map { it.calories }
                    val todayLog = nutritionState.todayLog

                    // Gọi lại Component Calories đầy đủ
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
                    var selectedTabIndex by remember { mutableIntStateOf(0) }
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

                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp).height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when(selectedTabIndex) {
                                0 -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Bếp của bạn chưa đỏ lửa 🔥", color = TextGray)
                                        TextButton(onClick = { /* Navigate to create recipe */ }) {
                                            Text("Tạo công thức ngay", color = TealPrimary)
                                        }
                                    }
                                }
                                1 -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Chia sẻ khoảnh khắc ăn uống 📸", color = TextGray)
                                        TextButton(onClick = onOpenPosts) {
                                            Text("Xem tất cả bài viết", color = TealPrimary)
                                        }
                                    }
                                }
                                2 -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Chưa lưu món ngon nào ❤️", color = TextGray)
                                        TextButton(onClick = onOpenSaves) {
                                            Text("Xem kho lưu trữ", color = TealPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showUpdateDialog) {
                ProfessionalNutritionDialog(
                    initialCalories = nutritionState.todayLog?.calories ?: 0f,
                    initialProtein = nutritionState.todayLog?.protein ?: 0f,
                    initialFat = nutritionState.todayLog?.fat ?: 0f,
                    initialCarb = nutritionState.todayLog?.carb ?: 0f,
                    caloriesTarget = p.nutrition?.caloriesTarget ?: 2000f,
                    onDismiss = { showUpdateDialog = false },
                    onSave = { c, p, f, cb ->
                        nutritionVm.updateTodayNutrition(c, p, f, cb)
                        showUpdateDialog = false
                    },
                    geminiService = geminiService
                )
            }
        }
    }
}

// =====================================================
// HELPER COMPOSABLES
// =====================================================

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

// --- CALORIES TRACKING CARD (PHIÊN BẢN ĐẦY ĐỦ - KHÔI PHỤC) ---
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
            // 1. Header
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

            // 2. Circular Progress & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // A. Vòng tròn Calories
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

                        // Track (Nền xám)
                        drawArc(
                            color = Color(0xFFE5E7EB),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )
                        // Progress (Màu Teal)
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

                // B. Danh sách Macro (Protein/Carb/Fat)
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

                    // Macros
                    if (todayLog != null) {
                        MacroStatItem(
                            label = "Protein",
                            value = "${todayLog.protein.toInt()}",
                            unit = "g",
                            color = Color(0xFF3B82F6), // Blue
                            progress = (todayLog.protein / (caloriesTarget * 0.3f / 4f)).coerceIn(0f, 1f)
                        )
                        MacroStatItem(
                            label = "Carb",
                            value = "${todayLog.carb.toInt()}",
                            unit = "g",
                            color = Color(0xFF10B981), // Green
                            progress = (todayLog.carb / (caloriesTarget * 0.45f / 4f)).coerceIn(0f, 1f)
                        )
                        MacroStatItem(
                            label = "Fat",
                            value = "${todayLog.fat.toInt()}",
                            unit = "g",
                            color = Color(0xFFF59E0B), // Orange
                            progress = (todayLog.fat / (caloriesTarget * 0.25f / 9f)).coerceIn(0f, 1f)
                        )
                    } else {
                        // Placeholder nếu chưa có data
                        MacroStatItem("Protein", "0", "g", Color(0xFF3B82F6), 0f)
                        MacroStatItem("Carb", "0", "g", Color(0xFF10B981), 0f)
                        MacroStatItem("Fat", "0", "g", Color(0xFFF59E0B), 0f)
                    }
                }
            }

            Divider(color = DividerColor, thickness = 1.dp)

            // 3. Biểu đồ tuần
            Column {
                Text(
                    text = "7 ngày qua",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // Chart nằm trong Card luôn
                ImprovedChartCard(dataPoints = if (weeklyData.isEmpty()) listOf(0f,0f,0f,0f,0f,0f,0f) else weeklyData, target = caloriesTarget)
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
    // Chart nằm trong nền xám nhẹ với padding tốt hơn
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

                    // Vẽ đường Target
                    val targetY = height - (target / maxVal) * height
                    drawLine(
                        color = Color(0xFF6366F1).copy(alpha = 0.5f),
                        start = Offset(0f, targetY),
                        end = Offset(width, targetY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Vẽ biểu đồ vùng (Area Chart)
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
                            // Bezier curve cho mượt
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

// --- DIALOG INPUT (GIỮ NGUYÊN) ---
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

    // Dữ liệu calories chính xác cho món ăn Việt Nam (tính cho 1 phần ăn tiêu chuẩn)
    val foodCategories = remember {
        mapOf(
            "Phổ biến" to listOf(
                QuickFood("Cơm trắng (1 bát)", 130f, 2.7f, 0.3f, 28f), // 100g cơm
                QuickFood("Bánh mì (1 ổ)", 265f, 8.5f, 3.2f, 49f), // 1 ổ bánh mì ~80g
                QuickFood("Phở bò (1 tô)", 456f, 24f, 12f, 58f), // 1 tô phở đầy đủ
                QuickFood("Trứng ốp la (2 quả)", 180f, 12f, 14f, 1.2f), // 2 quả trứng
                QuickFood("Bún chả (1 suất)", 398f, 22f, 15f, 42f), // 1 suất đầy đủ
                QuickFood("Bánh cuốn (1 đĩa)", 220f, 8f, 3f, 40f), // 1 đĩa ~200g
                QuickFood("Cháo gà (1 tô)", 185f, 15f, 5f, 22f), // 1 tô cháo
                QuickFood("Xôi gấc (1 phần)", 280f, 5f, 2f, 60f) // 1 phần xôi
            ),
            "Thịt & Cá" to listOf(
                QuickFood("Thịt gà (100g)", 165f, 31f, 3.6f, 0f), // 100g thịt gà luộc
                QuickFood("Thịt heo (100g)", 242f, 27f, 14f, 0f), // 100g thịt heo
                QuickFood("Thịt bò (100g)", 250f, 26f, 17f, 0f), // 100g thịt bò
                QuickFood("Cá hồi (100g)", 208f, 20f, 12f, 0f), // 100g cá hồi
                QuickFood("Cá basa (100g)", 180f, 18f, 10f, 0f), // 100g cá basa
                QuickFood("Tôm (100g)", 99f, 24f, 0.3f, 0f), // 100g tôm
                QuickFood("Thịt vịt (100g)", 337f, 19f, 28f, 0f), // 100g thịt vịt
                QuickFood("Thịt ngan (100g)", 200f, 22f, 11f, 0f) // 100g thịt ngan
            ),
            "Món nước" to listOf(
                QuickFood("Bún bò Huế (1 tô)", 480f, 28f, 15f, 52f), // 1 tô đầy đủ
                QuickFood("Hủ tiếu (1 tô)", 380f, 18f, 10f, 48f), // 1 tô hủ tiếu
                QuickFood("Bánh canh (1 tô)", 320f, 15f, 8f, 42f), // 1 tô bánh canh
                QuickFood("Mì Quảng (1 tô)", 420f, 22f, 12f, 45f), // 1 tô mì Quảng
                QuickFood("Bún riêu (1 tô)", 350f, 18f, 10f, 40f), // 1 tô bún riêu
                QuickFood("Cháo lòng (1 tô)", 280f, 18f, 12f, 25f), // 1 tô cháo lòng
                QuickFood("Súp cua (1 tô)", 220f, 12f, 8f, 28f) // 1 tô súp cua
            ),
            "Món xào" to listOf(
                QuickFood("Cơm rang (1 đĩa)", 350f, 12f, 12f, 50f), // 1 đĩa cơm rang
                QuickFood("Mì xào (1 đĩa)", 420f, 15f, 15f, 48f), // 1 đĩa mì xào
                QuickFood("Bún xào (1 đĩa)", 320f, 12f, 10f, 42f), // 1 đĩa bún xào
                QuickFood("Rau muống xào (1 đĩa)", 95f, 4f, 5f, 12f), // 1 đĩa rau muống
                QuickFood("Thịt bò xào (1 đĩa)", 280f, 25f, 15f, 10f), // 1 đĩa thịt bò xào
                QuickFood("Gà xào sả ớt (1 đĩa)", 240f, 28f, 10f, 8f) // 1 đĩa gà xào
            ),
            "Món chiên" to listOf(
                QuickFood("Nem rán (2 cái)", 220f, 10f, 12f, 18f), // 2 cái nem
                QuickFood("Chả giò (2 cái)", 240f, 8f, 14f, 20f), // 2 cái chả giò
                QuickFood("Gà rán (1 phần)", 350f, 30f, 22f, 8f), // 1 phần gà rán
                QuickFood("Cá chiên (1 con)", 280f, 25f, 18f, 3f), // 1 con cá chiên
                QuickFood("Tôm chiên (5 con)", 240f, 22f, 12f, 10f), // 5 con tôm
                QuickFood("Đậu phụ chiên (2 miếng)", 150f, 10f, 9f, 5f) // 2 miếng đậu phụ
            ),
            "Món nướng" to listOf(
                QuickFood("Thịt nướng (100g)", 280f, 32f, 16f, 3f), // 100g thịt nướng
                QuickFood("Chả cá (2 miếng)", 220f, 24f, 11f, 2f), // 2 miếng chả cá
                QuickFood("Nem nướng (3 xiên)", 280f, 18f, 15f, 10f), // 3 xiên nem nướng
                QuickFood("Gà nướng (1 phần)", 320f, 30f, 15f, 5f), // 1 phần gà nướng
                QuickFood("Cá nướng (1 con)", 240f, 26f, 12f, 2f) // 1 con cá nướng
            ),
            "Món canh" to listOf(
                QuickFood("Canh chua cá (1 tô)", 150f, 18f, 5f, 10f), // 1 tô canh chua
                QuickFood("Canh khổ qua (1 tô)", 110f, 10f, 4f, 8f), // 1 tô canh khổ qua
                QuickFood("Canh rau củ (1 tô)", 75f, 3f, 2f, 15f), // 1 tô canh rau củ
                QuickFood("Canh bí đỏ (1 tô)", 90f, 3f, 1f, 22f), // 1 tô canh bí đỏ
                QuickFood("Canh măng (1 tô)", 120f, 6f, 3f, 12f) // 1 tô canh măng
            ),
            "Tráng miệng" to listOf(
                QuickFood("Chè đậu xanh (1 ly)", 220f, 6f, 3f, 45f), // 1 ly chè
                QuickFood("Chè thái (1 ly)", 280f, 3f, 10f, 50f), // 1 ly chè thái
                QuickFood("Bánh flan (1 phần)", 180f, 5f, 6f, 28f), // 1 phần bánh flan
                QuickFood("Kem (1 ly)", 250f, 4f, 14f, 30f), // 1 ly kem
                QuickFood("Sữa chua (1 hộp)", 120f, 6f, 4f, 18f) // 1 hộp sữa chua
            ),
            "Trái cây" to listOf(
                QuickFood("Táo (1 quả)", 95f, 0.5f, 0.3f, 25f), // 1 quả táo trung bình
                QuickFood("Chuối (1 quả)", 105f, 1.3f, 0.4f, 27f), // 1 quả chuối
                QuickFood("Cam (1 quả)", 62f, 1.2f, 0.2f, 15f), // 1 quả cam
                QuickFood("Bơ (1 quả)", 320f, 4f, 29f, 17f), // 1 quả bơ
                QuickFood("Xoài (1 quả)", 202f, 1.4f, 0.4f, 50f), // 1 quả xoài
                QuickFood("Dưa hấu (100g)", 30f, 0.6f, 0.2f, 8f), // 100g dưa hấu
                QuickFood("Dứa (100g)", 50f, 0.5f, 0.1f, 13f), // 100g dứa
                QuickFood("Nho (100g)", 69f, 0.7f, 0.2f, 18f), // 100g nho
                QuickFood("Dâu tây (100g)", 32f, 0.7f, 0.3f, 8f), // 100g dâu tây
                QuickFood("Ổi (1 quả)", 112f, 2.6f, 0.4f, 24f), // 1 quả ổi
                QuickFood("Thanh long (1 quả)", 60f, 1.2f, 0.4f, 13f), // 1 quả thanh long
                QuickFood("Mít (100g)", 95f, 1.5f, 0.3f, 24f) // 100g mít
            ),
            "Rau củ" to listOf(
                QuickFood("Cà rốt (100g)", 41f, 0.9f, 0.2f, 10f), // 100g cà rốt
                QuickFood("Cà chua (1 quả)", 22f, 1.1f, 0.2f, 5f), // 1 quả cà chua
                QuickFood("Dưa chuột (1 quả)", 16f, 0.7f, 0.1f, 4f), // 1 quả dưa chuột
                QuickFood("Bắp cải (100g)", 25f, 1.3f, 0.1f, 6f), // 100g bắp cải
                QuickFood("Rau muống (100g)", 23f, 2.6f, 0.2f, 3f), // 100g rau muống
                QuickFood("Rau cải (100g)", 27f, 2.9f, 0.4f, 4f), // 100g rau cải
                QuickFood("Khoai tây (100g)", 77f, 2f, 0.1f, 17f), // 100g khoai tây
                QuickFood("Khoai lang (100g)", 86f, 1.6f, 0.1f, 20f), // 100g khoai lang
                QuickFood("Bí đỏ (100g)", 26f, 1f, 0.1f, 7f), // 100g bí đỏ
                QuickFood("Đậu bắp (100g)", 33f, 2f, 0.2f, 7f), // 100g đậu bắp
                QuickFood("Mướp (100g)", 20f, 1f, 0.2f, 4f), // 100g mướp
                QuickFood("Đậu que (100g)", 31f, 1.8f, 0.2f, 7f) // 100g đậu que
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("Phổ biến") }
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
        
        // Kiểm tra xem có kết quả chính xác (exact match) trong danh sách không
        val allFoods = foodCategories.values.flatten()
        val exactMatch = allFoods.any { 
            it.name.equals(searchQuery.trim(), ignoreCase = true) 
        }
        
        // Nếu không có exact match và có Gemini service, tự động gọi API
        if (!exactMatch && geminiService != null && geminiService.isApiKeyConfigured() && searchQuery.length >= 3) {
            isLoadingGemini = true
            geminiError = null
            
            try {
                val nutrition = geminiService.calculateNutrition(searchQuery.trim())
                
                if (nutrition != null) {
                    geminiResult = QuickFood(
                        name = searchQuery.trim(),
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
                e.printStackTrace() // Log lỗi để debug
                geminiError = null // Không hiển thị lỗi để tránh rối mắt
            } finally {
                isLoadingGemini = false
            }
        } else if (!exactMatch && searchQuery.length >= 3 && (geminiService == null || !geminiService.isApiKeyConfigured())) {
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
                                "Tìm kiếm hoặc nhập món ăn..."
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