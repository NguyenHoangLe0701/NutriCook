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
import kotlinx.coroutines.delay

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
                        onAddClick = { showUpdateDialog = true }
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
                    }
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
    onAddClick: () -> Unit
) {
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
                    // Mục tiêu
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Mục tiêu", fontSize = 13.sp, color = TextGray)
                        Text("${caloriesTarget.toInt()} kcal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
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

@Composable
fun ImprovedChartCard(dataPoints: List<Float>, target: Float) {
    // Chart nằm trong nền xám nhẹ
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .padding(16.dp)
    ) {
        if (dataPoints.isEmpty() || dataPoints.all { it == 0f }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có dữ liệu", color = TextGray, fontSize = 12.sp)
            }
        } else {
            Column {
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

                    // Tô màu vùng bên dưới
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(TealPrimary.copy(alpha = 0.3f), TealPrimary.copy(alpha = 0.0f)),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Vẽ đường Line chính
                    drawPath(
                        path = path,
                        color = TealPrimary,
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )

                    // Vẽ điểm đầu và cuối
                    val firstY = height - (dataPoints.first() / maxVal) * height
                    val lastY = height - (dataPoints.last() / maxVal) * height
                    drawCircle(TealPrimary, 6f, Offset(0f, firstY))
                    drawCircle(TealPrimary, 6f, Offset(width, lastY))
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
    onSave: (Float, Float, Float, Float) -> Unit
) {
    var cal by remember { mutableStateOf(if(initialCalories > 0) initialCalories.toString() else "") }
    var pro by remember { mutableStateOf(if(initialProtein > 0) initialProtein.toString() else "") }
    var fat by remember { mutableStateOf(if(initialFat > 0) initialFat.toString() else "") }
    var carb by remember { mutableStateOf(if(initialCarb > 0) initialCarb.toString() else "") }

    val currentCalories = cal.toFloatOrNull() ?: 0f
    val remaining = caloriesTarget - currentCalories
    val progress = (currentCalories / caloriesTarget).coerceIn(0f, 1f)

    val foodCategories = remember {
        mapOf(
            "Phổ biến" to listOf(
                QuickFood("Cơm trắng", 130f, 3f, 0.3f, 28f),
                QuickFood("Bánh mì", 265f, 9f, 3.2f, 49f),
                QuickFood("Phở bò", 350f, 20f, 8f, 45f),
                QuickFood("Trứng ốp la", 155f, 13f, 11f, 1.1f),
                QuickFood("Bún chả", 320f, 18f, 12f, 35f),
                QuickFood("Bánh cuốn", 180f, 6f, 2f, 35f),
                QuickFood("Cháo gà", 150f, 12f, 4f, 18f),
                QuickFood("Xôi gấc", 240f, 4f, 1f, 52f)
            ),
            "Thịt & Cá" to listOf(
                QuickFood("Thịt gà", 165f, 31f, 3.6f, 0f),
                QuickFood("Thịt heo", 242f, 27f, 14f, 0f),
                QuickFood("Thịt bò", 250f, 26f, 17f, 0f),
                QuickFood("Cá hồi", 208f, 20f, 12f, 0f),
                QuickFood("Cá basa", 180f, 18f, 10f, 0f),
                QuickFood("Tôm", 99f, 24f, 0.3f, 0f),
                QuickFood("Thịt vịt", 337f, 19f, 28f, 0f),
                QuickFood("Thịt ngan", 200f, 22f, 11f, 0f)
            ),
            "Món nước" to listOf(
                QuickFood("Bún bò Huế", 380f, 22f, 10f, 45f),
                QuickFood("Hủ tiếu", 320f, 15f, 8f, 42f),
                QuickFood("Bánh canh", 280f, 12f, 6f, 38f),
                QuickFood("Mì Quảng", 350f, 18f, 9f, 40f),
                QuickFood("Bún riêu", 290f, 14f, 7f, 36f),
                QuickFood("Cháo lòng", 220f, 15f, 8f, 20f),
                QuickFood("Súp cua", 180f, 10f, 5f, 22f)
            ),
            "Món xào" to listOf(
                QuickFood("Cơm rang", 280f, 8f, 10f, 42f),
                QuickFood("Mì xào", 320f, 12f, 12f, 38f),
                QuickFood("Bún xào", 250f, 10f, 8f, 35f),
                QuickFood("Rau muống xào", 85f, 3f, 4f, 10f),
                QuickFood("Thịt bò xào", 220f, 20f, 12f, 8f),
                QuickFood("Gà xào sả ớt", 195f, 22f, 8f, 5f)
            ),
            "Món chiên" to listOf(
                QuickFood("Nem rán", 180f, 8f, 10f, 15f),
                QuickFood("Chả giò", 200f, 6f, 12f, 18f),
                QuickFood("Gà rán", 280f, 25f, 18f, 5f),
                QuickFood("Cá chiên", 220f, 20f, 14f, 2f),
                QuickFood("Tôm chiên", 195f, 18f, 10f, 8f),
                QuickFood("Đậu phụ chiên", 120f, 8f, 7f, 3f)
            ),
            "Món nướng" to listOf(
                QuickFood("Thịt nướng", 250f, 28f, 14f, 2f),
                QuickFood("Chả cá", 180f, 20f, 9f, 1f),
                QuickFood("Nem nướng", 220f, 15f, 12f, 8f),
                QuickFood("Gà nướng", 240f, 26f, 12f, 3f),
                QuickFood("Cá nướng", 200f, 22f, 10f, 1f)
            ),
            "Món canh" to listOf(
                QuickFood("Canh chua cá", 120f, 15f, 4f, 8f),
                QuickFood("Canh khổ qua", 85f, 8f, 3f, 6f),
                QuickFood("Canh rau củ", 60f, 2f, 1f, 12f),
                QuickFood("Canh bí đỏ", 75f, 2f, 0.5f, 18f),
                QuickFood("Canh măng", 95f, 5f, 2f, 10f)
            ),
            "Tráng miệng" to listOf(
                QuickFood("Chè đậu xanh", 180f, 5f, 2f, 38f),
                QuickFood("Chè thái", 220f, 2f, 8f, 42f),
                QuickFood("Bánh flan", 150f, 4f, 5f, 24f),
                QuickFood("Kem", 200f, 3f, 11f, 25f),
                QuickFood("Sữa chua", 100f, 5f, 3f, 15f)
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("Phổ biến") }
    var searchQuery by remember { mutableStateOf("") }
    
    val displayedFoods = remember(selectedCategory, searchQuery) {
        val categoryFoods = foodCategories[selectedCategory] ?: emptyList()
        if (searchQuery.isBlank()) {
            categoryFoods
        } else {
            categoryFoods.filter { 
                it.name.contains(searchQuery, ignoreCase = true) 
            }
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

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("🔍 Tìm kiếm món ăn...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Tìm kiếm", tint = TextGray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = TextGray, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    singleLine = true
                )

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
                                QuickFoodChip(food) {
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

data class QuickFood(val name: String, val calories: Float, val protein: Float, val fat: Float, val carb: Float)

@Composable
fun QuickFoodChip(food: QuickFood, onClick: () -> Unit) {
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
            // Food name
            Text(
                text = food.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 2,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            
            // Calories badge
            Surface(
                color = TealPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${food.calories.toInt()} kcal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TealPrimary,
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