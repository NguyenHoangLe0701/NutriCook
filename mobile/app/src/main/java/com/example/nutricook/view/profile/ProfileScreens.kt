package com.example.nutricook.view.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.model.user.bestName
import com.example.nutricook.viewmodel.nutrition.NutritionViewModel
// Đã xóa import PostViewModel vì không cần thiết ở màn hình này
import com.example.nutricook.viewmodel.profile.ProfileUiState
import com.example.nutricook.viewmodel.profile.ProfileViewModel

// --- MÀU SẮC ---
private val PeachGradientStart = Color(0xFFFFF0E3)
private val PeachGradientEnd = Color(0xFFFFFFFF)
private val TealPrimary = Color(0xFF2BB6AD)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF9CA3AF)
private val ScreenBg = Color(0xFFFAFAFA)

@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit = {},
    onOpenRecent: () -> Unit = {},
    onOpenPosts: () -> Unit = {},
    onOpenSaves: () -> Unit = {},
    onEditAvatar: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    // Inject ViewModel
    vm: ProfileViewModel = hiltViewModel(),
    // [ĐÃ XÓA] postVm: PostViewModel vì màn hình này không hiển thị list bài viết
    nutritionVm: NutritionViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()
    val nutritionState by nutritionVm.ui.collectAsState()

    // State quản lý Dialog nhập liệu
    var showUpdateDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = bottomBar,
        containerColor = ScreenBg,
        topBar = {}
    ) { padding ->
        when {
            ui.loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = TealPrimary) }

            ui.profile != null -> {
                val me = ui.profile!!.user.id
                LaunchedEffect(me) {
                    // [ĐÃ XÓA] postVm.loadInitial(me)
                    nutritionVm.loadData()
                }

                ProfileContent(
                    modifier = Modifier.padding(padding),
                    state = ui,
                    realChartData = nutritionState.history.map { it.calories },
                    onEditAvatar = onEditAvatar,
                    onOpenSettings = onOpenSettings,
                    onOpenRecent = onOpenRecent,
                    onOpenPosts = onOpenPosts,
                    onOpenSaves = onOpenSaves,
                    onOpenSearch = onOpenSearch,
                    onOpenUpdateDialog = { showUpdateDialog = true }
                )

                if (showUpdateDialog) {
                    UpdateNutritionDialog(
                        initialCalories = nutritionState.todayLog?.calories ?: 0f,
                        initialProtein = nutritionState.todayLog?.protein ?: 0f,
                        initialFat = nutritionState.todayLog?.fat ?: 0f,
                        initialCarb = nutritionState.todayLog?.carb ?: 0f,
                        onDismiss = { showUpdateDialog = false },
                        onSave = { c, p, f, cb ->
                            nutritionVm.updateTodayNutrition(c, p, f, cb)
                            showUpdateDialog = false
                        }
                    )
                }
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
    realChartData: List<Float>,
    onEditAvatar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenPosts: () -> Unit,
    onOpenSaves: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenUpdateDialog: () -> Unit
) {
    val p = state.profile!!
    val scroll = rememberScrollState()

    Box(modifier = modifier.fillMaxSize().background(ScreenBg)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(Brush.verticalGradient(listOf(PeachGradientStart, PeachGradientEnd)))
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyProfileTopBar(
                onEditProfile = { /* TODO */ },
                onSettings = onOpenSettings,
                onSearch = onOpenSearch
            )

            Spacer(Modifier.height(8.dp))

            AvatarSection(p.user.avatarUrl, p.user.bestName(), onEditAvatar)

            Spacer(Modifier.height(24.dp))

            StatsRow(p.posts, p.following, p.followers)

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuCardItem("Recent Activity", null, Icons.Outlined.Image, Color(0xFF5B6B9A), Color(0xFFD5D9E8), onOpenRecent)
                MenuCardItem("Post", p.posts, Icons.Outlined.Description, Color(0xFF1D9B87), Color(0xFFBEF0E8), onOpenPosts)
                MenuCardItem("Save", null, Icons.Outlined.Bookmark, Color(0xFFE07C00), Color(0xFFFFDDB8), onOpenSaves)
            }

            Spacer(Modifier.height(30.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Fatscret (Calories)",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    )
                    IconButton(onClick = onOpenUpdateDialog) {
                        Icon(Icons.Default.Edit, contentDescription = "Update", tint = TealPrimary)
                    }
                }

                Spacer(Modifier.height(8.dp))

                val chartData = if (realChartData.isEmpty()) listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f) else realChartData
                ChartCard(dataPoints = chartData)

                if (p.nutrition != null) {
                    val n = p.nutrition!!
                    Text(
                        text = "Target: ${n.caloriesTarget.toInt()} Kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun UpdateNutritionDialog(
    initialCalories: Float,
    initialProtein: Float,
    initialFat: Float,
    initialCarb: Float,
    onDismiss: () -> Unit,
    onSave: (Float, Float, Float, Float) -> Unit
) {
    var cal by remember { mutableStateOf(if(initialCalories > 0) initialCalories.toString() else "") }
    var pro by remember { mutableStateOf(if(initialProtein > 0) initialProtein.toString() else "") }
    var fat by remember { mutableStateOf(if(initialFat > 0) initialFat.toString() else "") }
    var carb by remember { mutableStateOf(if(initialCarb > 0) initialCarb.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hôm nay bạn ăn gì?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cal,
                    onValueChange = { cal = it },
                    label = { Text("Calories (Kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pro,
                        onValueChange = { pro = it },
                        label = { Text("Pro (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = carb,
                        onValueChange = { carb = it },
                        label = { Text("Carb (g)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        cal.toFloatOrNull() ?: 0f,
                        pro.toFloatOrNull() ?: 0f,
                        fat.toFloatOrNull() ?: 0f,
                        carb.toFloatOrNull() ?: 0f
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = TextGray) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ChartCard(dataPoints: List<Float>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).padding(16.dp)) {
            if (dataPoints.isEmpty() || dataPoints.all { it == 0f }) {
                Text("Chưa có dữ liệu", modifier = Modifier.align(Alignment.Center), color = TextGray)
            } else {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val maxVal = dataPoints.maxOrNull() ?: 100f
                    val minVal = (dataPoints.minOrNull() ?: 0f) * 0.8f
                    val range = if (maxVal - minVal <= 1f) 100f else maxVal - minVal

                    val points = dataPoints.mapIndexed { index, value ->
                        val x = index * (width / (dataPoints.size - 1).coerceAtLeast(1))
                        val normalizedY = (value - minVal) / range
                        val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
                        Offset(x, y)
                    }

                    val path = Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val conX1 = (p0.x + p1.x) / 2f
                                val conY1 = p0.y
                                val conX2 = (p0.x + p1.x) / 2f
                                val conY2 = p1.y
                                cubicTo(conX1, conY1, conX2, conY2, p1.x, p1.y)
                            }
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(TealPrimary.copy(alpha = 0.2f), TealPrimary.copy(alpha = 0.0f)),
                            startY = 0f, endY = height
                        )
                    )
                    drawPath(path = path, color = TealPrimary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    points.forEach {
                        drawCircle(Color.White, 5.dp.toPx(), it)
                        drawCircle(TealPrimary, 3.dp.toPx(), it)
                    }
                }
            }
        }
    }
}

@Composable
fun MyProfileTopBar(
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onEditProfile,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White)
        ) {
            Icon(imageVector = Icons.Outlined.EditNote, contentDescription = "Edit Profile", tint = TealPrimary)
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "Profile",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextDark
            )
        )

        Spacer(Modifier.weight(1f))

        Row {
            IconButton(
                onClick = onSearch,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White)
            ) {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search", tint = Color(0xFF566275))
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onSettings,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White)
            ) {
                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings", tint = Color(0xFF566275))
            }
        }
    }
}

@Composable
fun AvatarSection(avatarUrl: String?, name: String, onEditAvatar: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomEnd) {
            val shape = RoundedCornerShape(32.dp)
            val size = 110.dp

            if (avatarUrl.isNullOrBlank()) {
                val initial = name.firstOrNull()?.uppercase() ?: "?"
                Box(
                    modifier = Modifier.size(size).clip(shape).background(Color(0xFFFFC107)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = initial, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(size).clip(shape).background(Color.White),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = 6.dp, y = 6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(TealPrimary)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onEditAvatar() },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(text = name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)))
    }
}

@Composable
fun StatsRow(posts: Int, following: Int, followers: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(count = posts, label = "Post")
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
        Text(text = count.toString(), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = TextDark))
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(color = TextGray, fontSize = 13.sp))
    }
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFF3F4F6)))
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = TextDark))
                if (count != null) {
                    Text(text = " ($count)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark))
                }
            }
            Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp))
        }
    }
}