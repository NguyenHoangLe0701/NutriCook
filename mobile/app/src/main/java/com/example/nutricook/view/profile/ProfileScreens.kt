package com.example.nutricook.view.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
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
                    todayLog = nutritionState.todayLog,
                    onEditAvatar = onEditAvatar,
                    onOpenSettings = onOpenSettings,
                    onOpenRecent = onOpenRecent,
                    onOpenPosts = onOpenPosts,
                    onOpenSaves = onOpenSaves,
                    onOpenSearch = onOpenSearch,
                    onOpenUpdateDialog = { showUpdateDialog = true }
                )

                if (showUpdateDialog) {
                    val caloriesTarget = ui.profile?.nutrition?.caloriesTarget ?: 2000f
                    ProfessionalNutritionDialog(
                        initialCalories = nutritionState.todayLog?.calories ?: 0f,
                        initialProtein = nutritionState.todayLog?.protein ?: 0f,
                        initialFat = nutritionState.todayLog?.fat ?: 0f,
                        initialCarb = nutritionState.todayLog?.carb ?: 0f,
                        caloriesTarget = caloriesTarget,
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
    todayLog: com.example.nutricook.model.nutrition.DailyLog?,
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

            // Calories Tracking Section - Redesigned
            val todayCalories = realChartData.lastOrNull() ?: 0f
            val caloriesTarget = p.nutrition?.caloriesTarget ?: 2000f
            
            CaloriesTrackingCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                todayCalories = todayCalories,
                caloriesTarget = caloriesTarget,
                todayLog = todayLog,
                weeklyData = realChartData,
                onAddClick = onOpenUpdateDialog
            )

            Spacer(Modifier.height(100.dp))
        }
    }
}

// Professional Nutrition Dialog với tính năng thông minh
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
    
    // Quick food suggestions - Món ăn Việt Nam phổ biến
    val foodCategories = remember {
        mapOf(
            "Phổ biến" to listOf(
                QuickFood("Cơm trắng", 130f, 3f, 0.3f, 28f),
                QuickFood("Bánh mì", 265f, 9f, 3.2f, 49f),
                QuickFood("Phở bò", 350f, 20f, 8f, 45f),
                QuickFood("Bún chả", 380f, 25f, 12f, 42f),
                QuickFood("Cơm tấm", 450f, 22f, 15f, 55f),
                QuickFood("Bánh cuốn", 220f, 8f, 5f, 38f)
            ),
            "Thịt & Cá" to listOf(
                QuickFood("Thịt gà", 165f, 31f, 3.6f, 0f),
                QuickFood("Thịt heo", 242f, 27f, 14f, 0f),
                QuickFood("Thịt bò", 250f, 26f, 17f, 0f),
                QuickFood("Cá hồi", 208f, 20f, 12f, 0f),
                QuickFood("Cá basa", 180f, 18f, 10f, 0f),
                QuickFood("Tôm", 99f, 24f, 0.3f, 0f),
                QuickFood("Trứng", 155f, 13f, 11f, 1.1f)
            ),
            "Rau & Củ" to listOf(
                QuickFood("Rau muống", 25f, 2.5f, 0.2f, 4f),
                QuickFood("Rau cải", 20f, 2f, 0.2f, 3f),
                QuickFood("Cà chua", 18f, 0.9f, 0.2f, 3.9f),
                QuickFood("Dưa chuột", 16f, 0.7f, 0.1f, 3.6f),
                QuickFood("Cà rốt", 41f, 0.9f, 0.2f, 10f),
                QuickFood("Khoai tây", 77f, 2f, 0.1f, 17f)
            ),
            "Trái cây" to listOf(
                QuickFood("Chuối", 89f, 1.1f, 0.3f, 23f),
                QuickFood("Táo", 52f, 0.3f, 0.2f, 14f),
                QuickFood("Cam", 47f, 0.9f, 0.1f, 12f),
                QuickFood("Xoài", 60f, 0.8f, 0.4f, 15f),
                QuickFood("Dưa hấu", 30f, 0.6f, 0.2f, 8f),
                QuickFood("Ổi", 68f, 2.6f, 0.9f, 14f)
            ),
            "Món canh" to listOf(
                QuickFood("Canh chua", 85f, 8f, 3f, 8f),
                QuickFood("Canh khổ qua", 45f, 4f, 1.5f, 5f),
                QuickFood("Canh rau củ", 35f, 2f, 1f, 6f),
                QuickFood("Súp cua", 120f, 12f, 4f, 10f)
            ),
            "Đồ uống" to listOf(
                QuickFood("Nước lọc", 0f, 0f, 0f, 0f),
                QuickFood("Nước dừa", 19f, 0.7f, 0.2f, 3.7f),
                QuickFood("Nước cam", 45f, 0.7f, 0.2f, 10f),
                QuickFood("Sữa tươi", 61f, 3.2f, 3.3f, 4.8f),
                QuickFood("Cà phê đen", 2f, 0.1f, 0f, 0f),
                QuickFood("Trà đá", 0f, 0f, 0f, 0f)
            ),
            "Đồ ngọt" to listOf(
                QuickFood("Chè đậu xanh", 150f, 4f, 2f, 30f),
                QuickFood("Chè thái", 180f, 2f, 3f, 40f),
                QuickFood("Bánh flan", 120f, 3f, 4f, 18f),
                QuickFood("Kem", 207f, 3.5f, 11f, 24f)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Thêm bữa ăn",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                        Text(
                            text = "Theo dõi dinh dưỡng hôm nay",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextGray,
                                fontSize = 13.sp
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = TextGray)
                    }
                }
                
                // Lộ trình Calories
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF0F9FF),
                                    Color(0xFFE0F2FE)
                                )
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lộ trình hôm nay",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            )
                            Text(
                                text = "${currentCalories.toInt()}/${caloriesTarget.toInt()} kcal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = when {
                                progress >= 1f -> Color(0xFFEF4444)
                                progress >= 0.8f -> Color(0xFFFF9800)
                                else -> TealPrimary
                            },
                            trackColor = Color(0xFFE5E7EB)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (remaining > 0) {
                                Text(
                                    text = "Còn ${remaining.toInt()} kcal",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            } else {
                                Text(
                                    text = "Vượt ${(-remaining).toInt()} kcal",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextGray,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                
                // Quick Suggestions với Categories và Search
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Thêm nhanh",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        )
                        Text(
                            text = "${displayedFoods.size} món",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        )
                    }
                    
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tìm món ăn...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = "Tìm kiếm", tint = TextGray)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa", tint = TextGray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                    
                    // Category Tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(foodCategories.keys.toList()) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { 
                                    Text(
                                        category, 
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = TealPrimary,
                                    containerColor = Color(0xFFF3F4F6),
                                    labelColor = TextDark
                                ),
                                border = if (selectedCategory == category) {
                                    FilterChipDefaults.filterChipBorder(
                                        borderColor = TealPrimary,
                                        borderWidth = 1.5.dp,
                                        selected = true,
                                        enabled = true
                                    )
                                } else null
                            )
                        }
                    }
                    
                    // Food Items Grid
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(displayedFoods) { food ->
                            QuickFoodChip(
                                food = food,
                                onClick = {
                                    val newCal = (currentCalories + food.calories).toString()
                                    val newPro = ((pro.toFloatOrNull() ?: 0f) + food.protein).toString()
                                    val newFat = ((fat.toFloatOrNull() ?: 0f) + food.fat).toString()
                                    val newCarb = ((carb.toFloatOrNull() ?: 0f) + food.carb).toString()
                                    cal = newCal
                                    pro = newPro
                                    fat = newFat
                                    carb = newCarb
                                }
                            )
                        }
                    }
                }
                
                // Input Fields
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Calories Input
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Calories (Kcal)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                            )
                            TextField(
                                value = cal,
                                onValueChange = { cal = it },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    
                    // Macros Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroInputField(
                            label = "Protein",
                            value = pro,
                            onValueChange = { pro = it },
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f)
                        )
                        MacroInputField(
                            label = "Fat",
                            value = fat,
                            onValueChange = { fat = it },
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        MacroInputField(
                            label = "Carb",
                            value = carb,
                            onValueChange = { carb = it },
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray)
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onSave(
                                cal.toFloatOrNull() ?: 0f,
                                pro.toFloatOrNull() ?: 0f,
                                fat.toFloatOrNull() ?: 0f,
                                carb.toFloatOrNull() ?: 0f
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lưu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Quick Food Data Class
data class QuickFood(
    val name: String,
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carb: Float
)

@Composable
fun QuickFoodChip(
    food: QuickFood,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .width(110.dp)
            .clickable { 
                onClick()
                isPressed = true
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) TealPrimary.copy(alpha = 0.1f) else Color(0xFFF0FDF4)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 4.dp else 2.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isPressed) 1.5.dp else 1.dp, 
            color = if (isPressed) TealPrimary else Color(0xFFD1FAE5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Food Icon/Emoji placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        TealPrimary.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        food.name.contains("Cơm") -> "🍚"
                        food.name.contains("Phở") || food.name.contains("Bún") -> "🍜"
                        food.name.contains("Bánh") -> "🥖"
                        food.name.contains("Thịt") -> "🍖"
                        food.name.contains("Cá") -> "🐟"
                        food.name.contains("Tôm") -> "🦐"
                        food.name.contains("Trứng") -> "🥚"
                        food.name.contains("Rau") -> "🥬"
                        food.name.contains("Chuối") -> "🍌"
                        food.name.contains("Táo") -> "🍎"
                        food.name.contains("Cam") -> "🍊"
                        food.name.contains("Xoài") -> "🥭"
                        food.name.contains("Canh") -> "🍲"
                        food.name.contains("Nước") -> "💧"
                        food.name.contains("Cà phê") -> "☕"
                        food.name.contains("Chè") -> "🍧"
                        food.name.contains("Kem") -> "🍦"
                        else -> "🍽️"
                    },
                    fontSize = 20.sp
                )
            }
            
            Text(
                text = food.name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 13.sp
                ),
                maxLines = 2,
                minLines = 1,
                lineHeight = 16.sp
            )
            
            Text(
                text = "${food.calories.toInt()} kcal",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TealPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            // Macros info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (food.protein > 0) {
                    Text(
                        text = "P:${food.protein.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF3B82F6),
                            fontSize = 9.sp
                        )
                    )
                }
                if (food.carb > 0) {
                    Text(
                        text = "C:${food.carb.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF10B981),
                            fontSize = 9.sp
                        )
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}

@Composable
fun MacroInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark,
                        fontSize = 11.sp
                    )
                )
            }
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
            )
        }
    }
}

// Professional Calories Tracking Card
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Theo dõi Calories",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 22.sp
                        )
                    )
                    Text(
                        text = "Hôm nay",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextGray,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Thêm", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Circular Progress với Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = (todayCalories / caloriesTarget).coerceIn(0f, 1f)
                    val sweepAngle = progress * 360f
                    val remaining = caloriesTarget - todayCalories
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 16.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val center = Offset(size.width / 2, size.height / 2)
                        val topLeft = Offset(center.x - radius, center.y - radius)
                        val sizeArc = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        
                        // Background circle
                        drawArc(
                            color = Color(0xFFE5E7EB),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = sizeArc
                        )
                        
                        // Progress circle
                        drawArc(
                            color = when {
                                progress >= 1f -> Color(0xFFEF4444) // Red when exceeded
                                progress >= 0.8f -> Color(0xFFFF9800) // Orange when close
                                else -> TealPrimary
                            },
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = sizeArc
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${todayCalories.toInt()}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                fontSize = 32.sp
                            )
                        )
                        Text(
                            text = "kcal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextGray,
                                fontSize = 12.sp
                            )
                        )
                        if (remaining > 0) {
                            Text(
                                text = "Còn ${remaining.toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = "+${(-remaining).toInt()}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Stats Column
                Column(
                    modifier = Modifier.weight(1f).padding(start = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MacroStatItem(
                        label = "Mục tiêu",
                        value = "${caloriesTarget.toInt()}",
                        unit = "kcal",
                        color = Color(0xFF6366F1)
                    )
                    
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
                    }
                }
            }

            Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)

            // Weekly Chart
            Column {
                Text(
                    text = "7 ngày qua",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val chartData = if (weeklyData.isEmpty()) listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f) else weeklyData
                ImprovedChartCard(dataPoints = chartData, target = caloriesTarget)
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
    progress: Float? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextGray,
                    fontSize = 13.sp
                )
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = " $unit",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextGray,
                        fontSize = 11.sp
                    )
                )
            }
        }
        if (progress != null) {
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun ImprovedChartCard(dataPoints: List<Float>, target: Float, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).padding(16.dp)) {
            if (dataPoints.isEmpty() || dataPoints.all { it == 0f }) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.BarChart,
                        contentDescription = null,
                        tint = TextGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Chưa có dữ liệu",
                        color = TextGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Column {
                    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    val width = size.width
                    val height = size.height
                    val maxVal = maxOf(dataPoints.maxOrNull() ?: target, target * 1.2f)
                    val minVal = 0f

                    // Draw target line
                    val targetY = height - ((target - minVal) / (maxVal - minVal)) * height * 0.85f - height * 0.075f
                    val dashPath = Path().apply {
                        moveTo(0f, targetY)
                        lineTo(width, targetY)
                    }
                    drawPath(
                        path = dashPath,
                        color = Color(0xFF6366F1).copy(alpha = 0.3f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                        )
                    )

                    val points = dataPoints.mapIndexed { index, value ->
                        val x = 24.dp.toPx() + index * ((width - 48.dp.toPx()) / (dataPoints.size - 1).coerceAtLeast(1))
                        val normalizedY = (value - minVal) / (maxVal - minVal)
                        val y = height - (normalizedY * height * 0.85f) - height * 0.075f
                        Offset(x, y)
                    }

                    // Draw filled area
                    val fillPath = Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points.first().x, height - 16.dp.toPx())
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, height - 16.dp.toPx())
                            close()
                        }
                    }
                    
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                TealPrimary.copy(alpha = 0.3f),
                                TealPrimary.copy(alpha = 0.05f)
                            ),
                            startY = points.minOfOrNull { it.y } ?: 0f,
                            endY = height
                        )
                    )

                    // Draw line
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
                    
                    drawPath(
                        path = path,
                        color = TealPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw points
                    points.forEach { point ->
                        drawCircle(Color.White, 6.dp.toPx(), point)
                        drawCircle(TealPrimary, 4.dp.toPx(), point)
                    }

                    // Day labels sẽ được vẽ bằng Text composable bên ngoài Canvas
                    }
                    
                    // Day labels
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
                        days.forEachIndexed { index, day ->
                            if (index < dataPoints.size) {
                                Text(
                                    text = day,
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
                                )
                            }
                        }
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