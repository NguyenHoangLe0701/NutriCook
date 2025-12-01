package com.example.nutricook.view.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.data.nutrition.GeminiNutritionService
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import android.util.Log

private val TealPrimary = Color(0xFF2BB6AD)
private val TealLight = Color(0xFFE0F7F6)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFoodCalculatorScreen(
    navController: NavController,
    onSave: (String, Float, Float, Float, Float) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Inject GeminiNutritionService (sử dụng EntryPoint từ ProfileScreens.kt)
    val geminiService = remember {
        val activity = context as? androidx.activity.ComponentActivity
        if (activity != null) {
            EntryPointAccessors.fromActivity(
                activity,
                com.example.nutricook.view.profile.GeminiServiceEntryPoint::class.java
            ).geminiService()
        } else {
            null
        }
    }
    
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    
    var isLoadingGemini by remember { mutableStateOf(false) }
    var geminiError by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Tính calories món ăn",
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
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TealLight)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TealPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Nhập tên món ăn",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Hệ thống sẽ tự động tính calories và dinh dưỡng cho bạn",
                        fontSize = 14.sp,
                        color = TextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // Tên món ăn
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Tên món ăn *",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Ví dụ: Cá ngừ 200gr, 1 quả táo, 100g bơ...",
                            fontSize = 14.sp
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            Icons.Outlined.Restaurant, 
                            contentDescription = null, 
                            tint = TealPrimary
                        ) 
                    },
                    trailingIcon = {
                        if (foodName.isNotEmpty() && !isLoadingGemini && geminiService != null && geminiService.isApiKeyConfigured()) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoadingGemini = true
                                        geminiError = null
                                        
                                        try {
                                            val nutrition = geminiService.calculateNutrition(foodName.trim())
                                            
                                            if (nutrition != null && nutrition.calories > 0) {
                                                // Chỉ cập nhật nếu người dùng chưa nhập thủ công hoặc đang trống
                                                if (calories.isBlank() || calories == "0") {
                                                    calories = nutrition.calories.toInt().toString()
                                                    protein = String.format("%.1f", nutrition.protein)
                                                    fat = String.format("%.1f", nutrition.fat)
                                                    carb = String.format("%.1f", nutrition.carb)
                                                    showSuccess = true
                                                    geminiError = null
                                                    Log.d("CustomFoodCalc", "Auto-calculated: Calories=${nutrition.calories}, Protein=${nutrition.protein}, Fat=${nutrition.fat}, Carb=${nutrition.carb}")
                                                } else {
                                                    // Nếu đã có giá trị thủ công, chỉ cập nhật nếu người dùng muốn
                                                    Log.d("CustomFoodCalc", "User already entered manual values, keeping them")
                                                    geminiError = "Bạn đã nhập giá trị thủ công. Giữ nguyên hoặc xóa để dùng giá trị tự động."
                                                }
                                            } else {
                                                geminiError = "Không thể tính calories tự động. Vui lòng nhập thủ công."
                                            }
                                        } catch (e: Exception) {
                                            geminiError = "Lỗi khi tính calories: ${e.message}"
                                        } finally {
                                            isLoadingGemini = false
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.AutoAwesome,
                                    contentDescription = "Tự động tính",
                                    tint = TealPrimary
                                )
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Đang tính calories...",
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }
                
                if (showSuccess) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Đã tính calories tự động!",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                if (geminiError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            geminiError!!,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Thông tin dinh dưỡng với nút Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Thông tin dinh dưỡng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                // Nút Reset - Đẹp hơn
                if (foodName.isNotBlank() || calories.isNotBlank() || protein.isNotBlank() || fat.isNotBlank() || carb.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            foodName = ""
                            calories = ""
                            protein = ""
                            fat = ""
                            carb = ""
                            geminiError = null
                            showSuccess = false
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
            
            // Calories
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Calories (kcal) *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { newValue ->
                        // Chỉ cho phép số dương (0-9 và dấu chấm cho decimal)
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        // Không cho phép nhiều dấu chấm
                        val dotCount = filtered.count { it == '.' }
                        if (dotCount <= 1) {
                            calories = filtered
                            geminiError = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0", fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    singleLine = true,
                    isError = calories.isNotBlank() && (calories.toFloatOrNull() == null || calories.toFloatOrNull()!! < 0)
                )
            }
            
            // Protein, Fat, Carb
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Protein (g)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { newValue ->
                            // Chỉ cho phép số dương
                            val filtered = newValue.filter { it.isDigit() || it == '.' }
                            val dotCount = filtered.count { it == '.' }
                            if (dotCount <= 1) {
                                protein = filtered
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        singleLine = true,
                        isError = protein.isNotBlank() && (protein.toFloatOrNull() == null || protein.toFloatOrNull()!! < 0)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Fat (g)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { newValue ->
                            // Chỉ cho phép số dương
                            val filtered = newValue.filter { it.isDigit() || it == '.' }
                            val dotCount = filtered.count { it == '.' }
                            if (dotCount <= 1) {
                                fat = filtered
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        singleLine = true,
                        isError = fat.isNotBlank() && (fat.toFloatOrNull() == null || fat.toFloatOrNull()!! < 0)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Carb (g)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    )
                    OutlinedTextField(
                        value = carb,
                        onValueChange = { newValue ->
                            // Chỉ cho phép số dương
                            val filtered = newValue.filter { it.isDigit() || it == '.' }
                            val dotCount = filtered.count { it == '.' }
                            if (dotCount <= 1) {
                                carb = filtered
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        singleLine = true,
                        isError = carb.isNotBlank() && (carb.toFloatOrNull() == null || carb.toFloatOrNull()!! < 0)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Nút Lưu
            Button(
                onClick = {
                    // Parse và validate giá trị
                    val cal = calories.toFloatOrNull() ?: 0f
                    val prot = protein.toFloatOrNull() ?: 0f
                    val f = fat.toFloatOrNull() ?: 0f
                    val c = carb.toFloatOrNull() ?: 0f
                    
                    // Validation: Đảm bảo calories hợp lệ
                    if (cal < 0 || cal > 10000) {
                        geminiError = "Calories phải trong khoảng 0-10000 kcal"
                        return@Button
                    }
                    
                    if (foodName.isNotBlank() && cal > 0) {
                        android.util.Log.d("CustomFoodCalc", "Saving: Name=$foodName, Calories=$cal, Protein=$prot, Fat=$f, Carb=$c")
                        onSave(foodName, cal, prot, f, c)
                        navController.popBackStack()
                    } else {
                        geminiError = "Vui lòng nhập tên món ăn và calories > 0"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary
                ),
                enabled = foodName.isNotBlank() && calories.toFloatOrNull() != null && calories.toFloatOrNull()!! > 0
            ) {
                Text(
                    "Lưu món ăn",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Lưu ý
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "💡 Lưu ý",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Nhập tên món ăn kèm số lượng (ví dụ: Cá ngừ 200gr, 1 quả táo)\n" +
                        "• Bấm icon ✨ để tự động tính calories\n" +
                        "• Có thể chỉnh sửa thông tin dinh dưỡng sau khi tính tự động",
                        fontSize = 13.sp,
                        color = Color(0xFFE65100),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
