package com.example.nutricook.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                                                calories = nutrition.calories.toInt().toString()
                                                protein = String.format("%.1f", nutrition.protein)
                                                fat = String.format("%.1f", nutrition.fat)
                                                carb = String.format("%.1f", nutrition.carb)
                                                showSuccess = true
                                                geminiError = null
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
            
            // Thông tin dinh dưỡng
            Text(
                "Thông tin dinh dưỡng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            
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
                    onValueChange = { calories = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0", fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    ),
                    singleLine = true
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
                        onValueChange = { protein = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        singleLine = true
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
                        onValueChange = { fat = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        singleLine = true
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
                        onValueChange = { carb = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealPrimary,
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Nút Lưu
            Button(
                onClick = {
                    val cal = calories.toFloatOrNull() ?: 0f
                    val prot = protein.toFloatOrNull() ?: 0f
                    val f = fat.toFloatOrNull() ?: 0f
                    val c = carb.toFloatOrNull() ?: 0f
                    
                    if (foodName.isNotBlank() && cal > 0) {
                        onSave(foodName, cal, prot, f, c)
                        navController.popBackStack()
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

