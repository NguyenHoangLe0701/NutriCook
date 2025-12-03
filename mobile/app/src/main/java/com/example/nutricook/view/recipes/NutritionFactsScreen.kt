package com.example.nutricook.view.recipes

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.example.nutricook.utils.NutritionData
import com.example.nutricook.utils.VitaminDetails
import com.example.nutricook.utils.NutritionCalculator
import com.example.nutricook.viewmodel.CreateRecipeViewModel
import com.example.nutricook.viewmodel.CategoriesViewModel
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.foundation.lazy.items
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionFactsScreen(
    navController: NavController,
    createRecipeViewModel: CreateRecipeViewModel,
    categoriesViewModel: CategoriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Lấy dữ liệu từ ViewModel
    val recipeState by createRecipeViewModel.state.collectAsState()
    // Sử dụng allFoodItems thay vì foodItems để có tất cả nguyên liệu từ mọi categories
    val allFoodItems by categoriesViewModel.allFoodItems.collectAsState()
    
    // Debug: Log toàn bộ recipeState
    LaunchedEffect(recipeState) {
        android.util.Log.d("NutritionFacts", "=== RecipeState Update ===")
        android.util.Log.d("NutritionFacts", "RecipeState.ingredients.size: ${recipeState.ingredients.size}")
        android.util.Log.d("NutritionFacts", "RecipeState.servings: ${recipeState.servings}")
        android.util.Log.d("NutritionFacts", "RecipeState.recipeName: ${recipeState.recipeName}")
        recipeState.ingredients.forEachIndexed { index, ing ->
            android.util.Log.d("NutritionFacts", "RecipeState.ingredients[$index]: name='${ing.name}', quantity='${ing.quantity}', foodItemId=${ing.foodItemId}, unit=${ing.unit}")
        }
    }
    
    // Lấy ingredients và servings từ state
    val ingredients = recipeState.ingredients.filter { 
        it.name.isNotBlank() && it.quantity.isNotBlank() && it.foodItemId != null 
    }
    // Cho phép thay đổi số phần ăn trong màn hình này
    var servingsInput by remember(recipeState.servings) { 
        mutableStateOf(recipeState.servings) 
    }
    val servings = servingsInput.toIntOrNull() ?: 1
    
    // Cập nhật servingsInput khi recipeState.servings thay đổi
    LaunchedEffect(recipeState.servings) {
        servingsInput = recipeState.servings
    }
    
    // Load tất cả foodItems nếu chưa có (luôn load để đảm bảo có dữ liệu)
    LaunchedEffect(Unit) {
        if (allFoodItems.isEmpty()) {
            android.util.Log.d("NutritionFacts", "Loading allFoodItems on first load...")
            categoriesViewModel.loadAllFoodItems()
        }
    }
    
    // Tạo map foodItems để tính toán - sử dụng allFoodItems
    val foodItemsMap = remember(allFoodItems) {
        val map = allFoodItems.associateBy { it.id }
        android.util.Log.d("NutritionFacts", "Created foodItemsMap with ${map.size} items")
        map
    }
    
    // Tự động tính lại dinh dưỡng từ ingredients và servings (khi servingsInput thay đổi)
    val nutritionData = remember(ingredients, foodItemsMap, servings, allFoodItems) {
        android.util.Log.d("NutritionFacts", "=== Recalculating nutrition ===")
        android.util.Log.d("NutritionFacts", "Calculating nutrition: ingredients=${ingredients.size}, foodItemsMap=${foodItemsMap.size}, servings=$servings (from servingsInput='$servingsInput')")
        
        if (ingredients.isEmpty()) {
            android.util.Log.w("NutritionFacts", "Cannot calculate: ingredients.isEmpty=true, foodItemsMap.isEmpty=${foodItemsMap.isEmpty()}")
            NutritionData()
        } else if (foodItemsMap.isEmpty()) {
            android.util.Log.w("NutritionFacts", "Cannot calculate: ingredients.isEmpty=false, foodItemsMap.isEmpty=true")
            NutritionData()
        } else {
            // Log detailed ingredient info only when we have ingredients to process
            ingredients.forEachIndexed { index, ingredient ->
                android.util.Log.d("NutritionFacts", "[$index] Ingredient: name='${ingredient.name}', quantity='${ingredient.quantity}', foodItemId=${ingredient.foodItemId}, unit=${ingredient.unit}")
                if (ingredient.foodItemId != null) {
                    val foodItem = foodItemsMap[ingredient.foodItemId]
                    if (foodItem != null) {
                        android.util.Log.d("NutritionFacts", "[$index] FoodItem found: name='${foodItem.name}', calories='${foodItem.calories}', fat=${foodItem.fat}, carbs=${foodItem.carbs}, protein=${foodItem.protein}")
                    } else {
                        android.util.Log.w("NutritionFacts", "[$index] FoodItem NOT found for ID: ${ingredient.foodItemId}")
                    }
                } else {
                    android.util.Log.w("NutritionFacts", "[$index] Ingredient has no foodItemId")
                }
            }
            
            val result = NutritionCalculator.calculateNutrition(ingredients, foodItemsMap, servings)
            android.util.Log.d("NutritionFacts", "Calculated nutrition: calories=${result.calories}, fat=${result.fat}, carbs=${result.carbs}, protein=${result.protein}")
            result
        }
    }
    
    // Lưu nutritionData vào ViewModel để đảm bảo đồng bộ
    LaunchedEffect(nutritionData) {
        createRecipeViewModel.setNutritionData(nutritionData)
    }
    
    // Animation cho circular progress
    val caloriesProgress = remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = caloriesProgress.value,
        animationSpec = tween(durationMillis = 1500),
        label = "calories_progress"
    )
    
    LaunchedEffect(nutritionData) {
        caloriesProgress.value = min(nutritionData.getCaloriesPercent() / 100f, 1f)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        /** 🔹 Header */
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF1C1C1E)
                    )
                }
                Text(
                    text = "Thông tin dinh dưỡng",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        /** 🔹 Servings Selector */
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8F9FA),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Số phần ăn",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Thông tin dinh dưỡng dưới đây được tính cho 1 phần ăn",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Nút giảm
                            IconButton(
                                onClick = {
                                    val current = servingsInput.toIntOrNull() ?: 1
                                    if (current > 1) {
                                        servingsInput = (current - 1).toString()
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFF00BFA5).copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Giảm",
                                    tint = Color(0xFF00BFA5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Hiển thị số phần
                            OutlinedTextField(
                                value = servingsInput,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        servingsInput = newValue
                                    }
                                },
                                modifier = Modifier.width(60.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00BFA5),
                                    unfocusedBorderColor = Color(0xFFE5E7EB)
                                )
                            )
                            
                            // Nút tăng
                            IconButton(
                                onClick = {
                                    val current = servingsInput.toIntOrNull() ?: 1
                                    servingsInput = (current + 1).toString()
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFF00BFA5).copy(alpha = 0.1f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tăng",
                                    tint = Color(0xFF00BFA5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        /** 🔹 Circular Progress - Calories */
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Custom circular progress using Canvas
                Canvas(
                    modifier = Modifier.size(200.dp)
                ) {
                    val strokeWidth = 16.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
                    
                    // Draw track (background circle)
                    drawCircle(
                        color = Color(0xFFE5E7EB),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Draw progress arc
                    val sweepAngle = 360f * animatedProgress
                    drawArc(
                        color = Color(0xFF00BFA5),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        ),
                        size = Size(radius * 2, radius * 2)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${nutritionData.calories.toInt()}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "Calories",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    if (servings > 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "(${servings} phần)",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
        
        /** 🔹 Three Cards: Fat, Carbs, Protein */
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fat Card
                NutritionCard(
                    title = "Chất béo",
                    value = "${String.format("%.2f", nutritionData.fat)}g",
                    percent = nutritionData.getFatPercent(),
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                
                // Carbs Card
                NutritionCard(
                    title = "Tinh bột",
                    value = "${String.format("%.2f", nutritionData.carbs)}g",
                    percent = nutritionData.getCarbsPercent(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                
                // Protein Card
                NutritionCard(
                    title = "Chất đạm",
                    value = "${String.format("%.2f", nutritionData.protein)}g",
                    percent = nutritionData.getProteinPercent(),
                    color = Color(0xFF00BFA5),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        /** 🔹 Nutrition Facts List */
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thông tin dinh dưỡng",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                    
                    // Nutrition items
                    NutritionItem(
                        label = "Tổng chất béo",
                        value = "${String.format("%.2f", nutritionData.fat)}g",
                        percent = nutritionData.getFatPercent()
                    )
                    NutritionItem(
                        label = "Cholesterol",
                        value = "${String.format("%.2f", nutritionData.cholesterol)}mg",
                        percent = nutritionData.getCholesterolPercent()
                    )
                    NutritionItem(
                        label = "Natri",
                        value = "${String.format("%.2f", nutritionData.sodium)}mg",
                        percent = nutritionData.getSodiumPercent()
                    )
                    NutritionItem(
                        label = "Tổng Carbohydrate",
                        value = "${String.format("%.2f", nutritionData.carbs)}g",
                        percent = nutritionData.getCarbsPercent()
                    )
                    NutritionItem(
                        label = "Protein",
                        value = "${String.format("%.2f", nutritionData.protein)}g",
                        percent = nutritionData.getProteinPercent()
                    )
                    // Vitamin với khả năng click để xem chi tiết
                    var showVitaminDetails by remember { mutableStateOf(false) }
                    
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showVitaminDetails = !showVitaminDetails }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "⚡",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Vitamin",
                                    fontSize = 15.sp,
                                    color = Color(0xFF1C1C1E)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (showVitaminDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showVitaminDetails) "Thu gọn" else "Mở rộng",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFF6B7280)
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${String.format("%.2f", nutritionData.vitamin)}%",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1C1C1E)
                                )
                                Surface(
                                    color = Color(0xFF00BFA5).copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${nutritionData.getVitaminPercent()}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF00BFA5),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        
                        // Hiển thị chi tiết vitamin khi mở rộng
                        if (showVitaminDetails) {
                            VitaminDetailsDialog(
                                vitaminDetails = nutritionData.vitaminDetails,
                                onDismiss = { showVitaminDetails = false }
                            )
                        }
                    }
                    
                    // Divider cho vitamin
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 12.dp),
                        color = Color(0xFFE5E7EB),
                        thickness = 1.dp
                    )
                }
            }
        }
        
        /** 🔹 Review Button */
        item {
            Button(
                onClick = {
                    Toast.makeText(context, "Xem lại công thức", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Đánh giá",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đánh giá",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun NutritionCard(
    title: String,
    value: String,
    percent: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon tròn với màu nền
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (title) {
                        "Chất béo" -> "%"
                        "Tinh bột" -> "0"
                        "Chất đạm" -> "•"
                        else -> "•"
                    },
                    fontSize = 24.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun NutritionItem(
    label: String,
    value: String,
    percent: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "⚡",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                color = Color(0xFF1C1C1E)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E)
            )
            Surface(
                color = Color(0xFF00BFA5).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$percent%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00BFA5),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
    
    // Divider
    HorizontalDivider(
        modifier = Modifier.padding(top = 12.dp),
        color = Color(0xFFE5E7EB),
        thickness = 1.dp
    )
}

