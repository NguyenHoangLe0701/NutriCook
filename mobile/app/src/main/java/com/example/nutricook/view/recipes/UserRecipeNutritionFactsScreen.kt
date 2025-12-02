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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.nutricook.utils.NutritionData
import com.example.nutricook.utils.VitaminDetails
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import kotlinx.coroutines.tasks.await
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRecipeNutritionFactsScreen(
    navController: NavController,
    recipeId: String
) {
    val context = LocalContext.current
    var recipeData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(recipeId) {
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("userRecipes")
                .document(recipeId)
                .get()
                .await()
            
            if (doc.exists()) {
                recipeData = doc.data
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRecipeNutrition", "Error loading recipe: ${e.message}", e)
            Toast.makeText(context, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    
    // Parse nutrition data from Firestore
    val nutritionData = remember(recipeData) {
        if (recipeData != null) {
            val nutrition = recipeData!!["nutritionData"] as? Map<String, Any> ?: emptyMap()
            val vitaminDetailsRaw = nutrition["vitaminDetails"] as? Map<String, Any> ?: emptyMap()
            
            NutritionData(
                calories = (nutrition["calories"] as? Number)?.toDouble() ?: 0.0,
                fat = (nutrition["fat"] as? Number)?.toDouble() ?: 0.0,
                carbs = (nutrition["carbs"] as? Number)?.toDouble() ?: 0.0,
                protein = (nutrition["protein"] as? Number)?.toDouble() ?: 0.0,
                cholesterol = (nutrition["cholesterol"] as? Number)?.toDouble() ?: 0.0,
                sodium = (nutrition["sodium"] as? Number)?.toDouble() ?: 0.0,
                vitamin = (nutrition["vitamin"] as? Number)?.toDouble() ?: 0.0,
                vitaminDetails = VitaminDetails(
                    vitaminA = (vitaminDetailsRaw["vitaminA"] as? Number)?.toDouble() ?: 0.0,
                    vitaminB1 = (vitaminDetailsRaw["vitaminB1"] as? Number)?.toDouble() ?: 0.0,
                    vitaminB2 = (vitaminDetailsRaw["vitaminB2"] as? Number)?.toDouble() ?: 0.0,
                    vitaminB3 = (vitaminDetailsRaw["vitaminB3"] as? Number)?.toDouble() ?: 0.0,
                    vitaminB6 = (vitaminDetailsRaw["vitaminB6"] as? Number)?.toDouble() ?: 0.0,
                    vitaminB9 = (vitaminDetailsRaw["vitaminB9"] as? Number)?.toDouble() ?: 0.0,
                    vitaminB12 = (vitaminDetailsRaw["vitaminB12"] as? Number)?.toDouble() ?: 0.0,
                    vitaminC = (vitaminDetailsRaw["vitaminC"] as? Number)?.toDouble() ?: 0.0,
                    vitaminD = (vitaminDetailsRaw["vitaminD"] as? Number)?.toDouble() ?: 0.0,
                    vitaminE = (vitaminDetailsRaw["vitaminE"] as? Number)?.toDouble() ?: 0.0,
                    vitaminK = (vitaminDetailsRaw["vitaminK"] as? Number)?.toDouble() ?: 0.0
                )
            )
        } else {
            NutritionData()
        }
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
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    value = "${nutritionData.fat.toInt()}g",
                    percent = nutritionData.getFatPercent(),
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                
                // Carbs Card
                NutritionCard(
                    title = "Tinh bột",
                    value = "${nutritionData.carbs.toInt()}g",
                    percent = nutritionData.getCarbsPercent(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                
                // Protein Card
                NutritionCard(
                    title = "Chất đạm",
                    value = "${nutritionData.protein.toInt()}g",
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
                        value = "${nutritionData.fat.toInt()}g",
                        percent = nutritionData.getFatPercent()
                    )
                    NutritionItem(
                        label = "Cholesterol",
                        value = "${nutritionData.cholesterol.toInt()}mg",
                        percent = nutritionData.getCholesterolPercent()
                    )
                    NutritionItem(
                        label = "Natri",
                        value = "${nutritionData.sodium.toInt()}mg",
                        percent = nutritionData.getSodiumPercent()
                    )
                    NutritionItem(
                        label = "Tổng Carbohydrate",
                        value = "${nutritionData.carbs.toInt()}g",
                        percent = nutritionData.getCarbsPercent()
                    )
                    NutritionItem(
                        label = "Protein",
                        value = "${nutritionData.protein.toInt()}g",
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
                                    text = "${nutritionData.vitamin.toInt()}%",
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
                    navController.navigate("review_screen/$recipeId")
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


