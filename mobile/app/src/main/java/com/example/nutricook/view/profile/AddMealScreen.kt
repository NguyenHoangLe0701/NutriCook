package com.example.nutricook.view.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.data.nutrition.GeminiNutritionService
import com.example.nutricook.utils.DecimalInputHelper
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val TealPrimary = Color(0xFF2BB6AD)
private val TealLight = Color(0xFFE0F7F6)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    navController: NavController,
    initialCalories: Float = 0f,
    initialProtein: Float = 0f,
    initialFat: Float = 0f,
    initialCarb: Float = 0f,
    caloriesTarget: Float = 2000f,
    onSave: (Float, Float, Float, Float) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Inject GeminiNutritionService
    val geminiService = remember {
        val activity = context as? androidx.activity.ComponentActivity
        if (activity != null) {
            EntryPointAccessors.fromActivity(
                activity,
                GeminiServiceEntryPoint::class.java
            ).geminiService()
        } else {
            null
        }
    }
    
    // KHÔNG khởi tạo với initial values - chỉ nhập phần tăng thêm
    var cal by remember { mutableStateOf("") }
    var pro by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }

    // Tính tổng để hiển thị (initial + nhập thêm)
    val totalCalories = (initialCalories + (cal.toFloatOrNull() ?: 0f))
    val remaining = caloriesTarget - totalCalories
    val progress = (totalCalories / caloriesTarget).coerceIn(0f, 1f)

    // Dữ liệu món ăn (copy từ ProfessionalNutritionDialog)
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
                QuickFood("Xôi mặn thập cẩm", 550f, 25f, 20f, 65f),
                QuickFood("Xôi gà xé", 480f, 22f, 15f, 62f)
            ),
            "🍜 Phở, Bún & Mì" to listOf(
                QuickFood("Phở bò tái", 430f, 22f, 12f, 60f),
                QuickFood("Phở bò chín", 410f, 20f, 10f, 60f),
                QuickFood("Phở đặc biệt (xe lửa)", 600f, 35f, 20f, 70f),
                QuickFood("Phở gà (thịt trắng)", 400f, 25f, 12f, 55f),
                QuickFood("Bún bò Huế (giò heo)", 550f, 28f, 25f, 55f),
                QuickFood("Bún riêu cua", 420f, 18f, 15f, 55f),
                QuickFood("Bún đậu mắm tôm (1 mẹt)", 650f, 40f, 35f, 60f),
                QuickFood("Bún thịt nướng", 450f, 18f, 15f, 60f),
                QuickFood("Hủ tiếu Nam Vang", 400f, 18f, 12f, 58f),
                QuickFood("Mì quảng tôm thịt", 480f, 22f, 18f, 55f)
            ),
            "🥖 Bánh Mì & Sáng" to listOf(
                QuickFood("Bánh mì thịt đầy đủ", 450f, 18f, 20f, 50f),
                QuickFood("Bánh mì ốp la (2 trứng)", 400f, 14f, 18f, 45f),
                QuickFood("Bánh mì chả lụa", 350f, 12f, 10f, 45f),
                QuickFood("Bánh mì heo quay", 480f, 18f, 25f, 45f),
                QuickFood("Bánh bao thịt trứng", 320f, 10f, 12f, 40f),
                QuickFood("Bánh cuốn nóng (1 dĩa)", 350f, 10f, 12f, 50f),
                QuickFood("Bánh xèo (1 cái)", 350f, 10f, 20f, 30f),
                QuickFood("Khoai lang luộc (1 củ)", 120f, 2f, 0.5f, 28f)
            ),
            "🥩 Thịt & Protein" to listOf(
                QuickFood("Ức gà luộc (100g)", 165f, 31f, 3.6f, 0f),
                QuickFood("Đùi gà chiên (1 cái)", 300f, 18f, 20f, 5f),
                QuickFood("Thịt heo nạc luộc (100g)", 145f, 25f, 4f, 0f),
                QuickFood("Thịt kho tàu (1 phần)", 350f, 15f, 25f, 5f),
                QuickFood("Chả lụa (100g)", 230f, 15f, 18f, 2f),
                QuickFood("Thịt bò thăn (100g)", 250f, 26f, 15f, 0f),
                QuickFood("Trứng gà luộc (1 quả)", 78f, 6f, 5f, 0.5f),
                QuickFood("Trứng chiên (2 trứng)", 250f, 14f, 20f, 2f),
                QuickFood("Đậu hũ trắng (1 bìa)", 76f, 8f, 4f, 2f)
            ),
            "🐟 Hải Sản" to listOf(
                QuickFood("Cá hồi áp chảo (100g)", 208f, 20f, 13f, 0f),
                QuickFood("Cá thu chiên (1 khúc)", 250f, 19f, 18f, 2f),
                QuickFood("Tôm hấp (100g)", 99f, 24f, 0.5f, 0.2f),
                QuickFood("Tôm rang thịt", 300f, 25f, 20f, 5f),
                QuickFood("Mực hấp gừng", 100f, 16f, 1f, 3f),
                QuickFood("Mực xào chua ngọt", 200f, 18f, 8f, 12f)
            ),
            "🥗 Rau Củ & Canh" to listOf(
                QuickFood("Rau muống luộc", 40f, 3f, 0.5f, 6f),
                QuickFood("Rau muống xào tỏi", 120f, 3f, 10f, 6f),
                QuickFood("Bông cải xanh luộc", 34f, 2.8f, 0.4f, 7f),
                QuickFood("Canh rau ngót thịt bằm", 120f, 8f, 5f, 5f),
                QuickFood("Canh chua cá", 150f, 12f, 5f, 10f),
                QuickFood("Salad trộn dầu giấm", 80f, 1f, 7f, 5f)
            ),
            "🍎 Trái cây" to listOf(
                QuickFood("Chuối (1 quả)", 105f, 1.3f, 0.4f, 27f),
                QuickFood("Táo (1 quả)", 95f, 0.5f, 0.3f, 25f),
                QuickFood("Cam (1 quả)", 62f, 1.2f, 0.2f, 15f),
                QuickFood("Dưa hấu (1 miếng)", 46f, 0.9f, 0.2f, 11f),
                QuickFood("Xoài chín (1 quả)", 200f, 2.8f, 1.2f, 50f),
                QuickFood("Bơ (1/2 quả)", 160f, 2f, 15f, 9f)
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("⭐ Phổ biến nhất") }
    var searchQuery by remember { mutableStateOf("") }
    var geminiResult by remember { mutableStateOf<QuickFood?>(null) }
    var isLoadingGemini by remember { mutableStateOf(false) }
    var geminiError by remember { mutableStateOf<String?>(null) }
    
    // Debounce search và tự động gọi Gemini nếu không tìm thấy
    LaunchedEffect(searchQuery, geminiService) {
        geminiResult = null
        geminiError = null
        
        if (searchQuery.isBlank()) {
            return@LaunchedEffect
        }
        
        delay(1000)
        
        val allFoods = foodCategories.values.flatten()
        val exactMatch = allFoods.any { 
            it.name.equals(searchQuery.trim(), ignoreCase = true) 
        }
        
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
                    geminiError = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                geminiError = null
            } finally {
                isLoadingGemini = false
            }
        } else if (!exactMatch && searchQuery.length >= 3 && (geminiService == null || !geminiService.isApiKeyConfigured())) {
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
        
        if (geminiResult != null && !filtered.any { it.name.equals(geminiResult!!.name, ignoreCase = true) }) {
            listOf(geminiResult!!) + filtered
        } else {
            filtered
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Thêm bữa ăn",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Calorie Progress
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TealLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Đã nạp: ${totalCalories.toInt()}",
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            "Mục tiêu: ${caloriesTarget.toInt()}",
                            color = TextGray,
                            fontSize = 16.sp
                        )
                    }
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if(progress > 1f) Color.Red else TealPrimary,
                        trackColor = Color(0xFFE5E7EB)
                    )
                }
            }

            // Search bar
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

            // Quick Suggestions
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
                
                // Food grid
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
                                // Cộng vào giá trị hiện tại (chỉ phần nhập thêm, không bao gồm initial)
                                val currentCal = cal.toFloatOrNull() ?: 0f
                                val currentPro = pro.toFloatOrNull() ?: 0f
                                val currentFat = fat.toFloatOrNull() ?: 0f
                                val currentCarb = carb.toFloatOrNull() ?: 0f
                                
                                cal = (currentCal + food.calories).toString()
                                pro = (currentPro + food.protein).toString()
                                fat = (currentFat + food.fat).toString()
                                carb = (currentCarb + food.carb).toString()
                            }
                        }
                    }
                }
            }

            Divider()

            // Manual Input với nút Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Hoặc nhập thủ công",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextDark
                )
                // Nút Reset - Đẹp hơn
                if (cal.isNotBlank() || pro.isNotBlank() || fat.isNotBlank() || carb.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            cal = ""
                            pro = ""
                            fat = ""
                            carb = ""
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Reset",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Reset",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = cal,
                onValueChange = { newValue ->
                    // Normalize input: hỗ trợ cả dấu phẩy và dấu chấm, tự động thêm "0" nếu cần
                    cal = DecimalInputHelper.normalizeDecimalInput(newValue)
                },
                label = { Text("Calories (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                isError = !DecimalInputHelper.isValid(cal)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MacroInputField(label = "Protein", value = pro, onValueChange = { pro = it }, color = Color(0xFF3B82F6), modifier = Modifier.weight(1f))
                MacroInputField(label = "Fat", value = fat, onValueChange = { fat = it }, color = Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                MacroInputField(label = "Carb", value = carb, onValueChange = { carb = it }, color = Color(0xFF10B981), modifier = Modifier.weight(1f))
            }

            // Nút tính calories tự động
            OutlinedButton(
                onClick = {
                    navController.navigate("custom_food_calculator")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TealPrimary
                )
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tính calories tự động", fontWeight = FontWeight.Medium)
            }

            // Nút Lưu
            Button(
                onClick = {
                    // Parse với hỗ trợ cả dấu phẩy và dấu chấm
                    val calValue = DecimalInputHelper.parseToFloat(cal) ?: 0f
                    val proValue = DecimalInputHelper.parseToFloat(pro) ?: 0f
                    val fatValue = DecimalInputHelper.parseToFloat(fat) ?: 0f
                    val carbValue = DecimalInputHelper.parseToFloat(carb) ?: 0f
                    
                    // Chỉ lưu phần tăng thêm, không lưu tổng (vì updateTodayNutrition sẽ cộng dồn)
                    onSave(
                        calValue,
                        proValue,
                        fatValue,
                        carbValue
                    )
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Lưu nhật ký", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
