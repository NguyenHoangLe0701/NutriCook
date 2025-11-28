package com.example.nutricook.view.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.R
import com.example.nutricook.utils.NutritionData
import com.example.nutricook.viewmodel.CategoriesViewModel
import com.example.nutricook.viewmodel.FoodItemUI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailScreen(
    navController: NavController,
    foodId: Long,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var foodItem by remember { mutableStateOf<FoodItemUI?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedQuantity by remember { mutableStateOf("100g") }
    var isBookmarked by remember { mutableStateOf(false) }
    
    // Load food item by ID
    LaunchedEffect(foodId) {
        try {
            foodItem = viewModel.getFoodById(foodId)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (foodItem == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Không tìm thấy nguyên liệu", color = Color.Gray)
        }
    } else {
        val nutritionData = NutritionData(
            calories = parseCalories(foodItem!!.calories),
            fat = foodItem!!.fat,
            carbs = foodItem!!.carbs,
            protein = foodItem!!.protein,
            cholesterol = foodItem!!.cholesterol,
            sodium = foodItem!!.sodium,
            vitamin = foodItem!!.vitamin
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            /** 🔹 Header */
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF1C1C1E)
                            )
                        }
                        Text(
                            text = foodItem!!.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { isBookmarked = !isBookmarked }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                modifier = Modifier.size(24.dp),
                                tint = if (isBookmarked) Color(0xFF00BFA5) else Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
            
            /** 🔹 Food Image */
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (foodItem!!.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(foodItem!!.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = foodItem!!.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                error = painterResource(id = R.drawable.cabbage),
                                placeholder = painterResource(id = R.drawable.cabbage)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.cabbage),
                                contentDescription = foodItem!!.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
            
            /** 🔹 Quantity Selection */
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuantityButton(
                            text = "1 oz",
                            isSelected = selectedQuantity == "1 oz",
                            onClick = { selectedQuantity = "1 oz" },
                            modifier = Modifier.weight(1f)
                        )
                        QuantityButton(
                            text = "100 g",
                            isSelected = selectedQuantity == "100g",
                            onClick = { selectedQuantity = "100g" },
                            modifier = Modifier.weight(1f)
                        )
                        QuantityButton(
                            text = "1 fruit",
                            isSelected = selectedQuantity == "1 fruit",
                            onClick = { selectedQuantity = "1 fruit" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Quantity Visualizer (Bar chart)
                    QuantityVisualizer(selectedIndex = when(selectedQuantity) {
                        "1 oz" -> 0
                        "100g" -> 1
                        "1 fruit" -> 2
                        else -> 1
                    })
                }
            }
            
            /** 🔹 Calorie Count */
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${nutritionData.calories.toInt()} kcal",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                }
            }
            
            /** 🔹 Three Cards: Fat, Carbs, Protein */
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroCard(
                        title = "Fat",
                        value = "${String.format("%.2f", nutritionData.fat)}g",
                        iconColor = Color(0xFF3B82F6),
                        iconSymbol = "%",
                        modifier = Modifier.weight(1f)
                    )
                    MacroCard(
                        title = "Carbs",
                        value = "${String.format("%.2f", nutritionData.carbs)}g",
                        iconColor = Color(0xFFFF9800),
                        iconSymbol = "0",
                        modifier = Modifier.weight(1f)
                    )
                    MacroCard(
                        title = "Protein",
                        value = "${String.format("%.2f", nutritionData.protein)}g",
                        iconColor = Color(0xFF00BFA5),
                        iconSymbol = "•",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            /** 🔹 Information Section */
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🌿",
                            fontSize = 18.sp
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "☀️",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${foodItem!!.name} Health Benefits:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                    
                    Text(
                        text = "Các vitamin và khoáng chất trong ${foodItem!!.name.lowercase()} có thể giúp rút ngắn thời gian nhiễm virus và vi khuẩn, đồng thời tăng cường sức khỏe xương. Ngoài ra, còn có bằng chứng cho thấy ${foodItem!!.name.lowercase()} có thể giúp ngăn ngừa ung thư và cải thiện chất lượng tinh trùng.",
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    OutlinedButton(
                        onClick = { /* TODO: View all benefits */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF00BFA5)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00BFA5))
                    ) {
                        Text(
                            text = "Xem tất cả",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(180f)
                        )
                    }
                }
            }
            
            /** 🔹 Nutrition Facts Section */
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "✨",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nutrition Facts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                        
                        NutritionFactItem(
                            label = "Total Fat",
                            value = "${String.format("%.2f", nutritionData.fat)}g",
                            percent = nutritionData.getFatPercent()
                        )
                        NutritionFactItem(
                            label = "Cholesterol",
                            value = "${nutritionData.cholesterol.toInt()}mg",
                            percent = nutritionData.getCholesterolPercent(),
                            valueColor = Color(0xFF00BFA5)
                        )
                        NutritionFactItem(
                            label = "Sodium",
                            value = "${nutritionData.sodium.toInt()}mg",
                            percent = nutritionData.getSodiumPercent(),
                            valueColor = Color(0xFF10B981)
                        )
                        NutritionFactItem(
                            label = "Total Carbohydrate",
                            value = "${String.format("%.2f", nutritionData.carbs)}g",
                            percent = nutritionData.getCarbsPercent(),
                            valueColor = Color(0xFF10B981)
                        )
                        NutritionFactItem(
                            label = "Protein",
                            value = "${String.format("%.2f", nutritionData.protein)}g",
                            percent = nutritionData.getProteinPercent(),
                            valueColor = Color(0xFF10B981)
                        )
                        
                        // Vitamin section (expandable)
                        ExpandableVitaminSection(
                            vitaminPercent = nutritionData.getVitaminPercent()
                        )
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun QuantityButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF00BFA5) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color(0xFF9CA3AF)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun QuantityVisualizer(selectedIndex: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(10) { index ->
            val height = when {
                index % 5 == 0 -> 20.dp
                index % 3 == 0 -> 14.dp
                else -> 10.dp
            }
            val isSelected = when(selectedIndex) {
                0 -> index == 2
                1 -> index == 5
                2 -> index == 8
                else -> false
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .background(
                        color = if (isSelected) Color(0xFF00BFA5) else Color(0xFFE5E7EB),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
private fun MacroCard(
    title: String,
    value: String,
    iconColor: Color,
    iconSymbol: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconSymbol,
                    fontSize = 20.sp,
                    color = iconColor,
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
            Text(
                text = title,
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun NutritionFactItem(
    label: String,
    value: String,
    percent: Int,
    valueColor: Color = Color(0xFF1C1C1E)
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
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00BFA5).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
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
                color = valueColor
            )
            Text(
                text = "$percent%",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
        }
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(top = 12.dp),
        color = Color(0xFFE5E7EB),
        thickness = 1.dp
    )
}

@Composable
private fun ExpandableVitaminSection(vitaminPercent: Int) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF9800).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Vitamin",
                    fontSize = 15.sp,
                    color = Color(0xFF1C1C1E)
                )
            }
            Text(
                text = "$vitaminPercent%",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
        }
        
        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp)
            ) {
                VitaminSubItem("Vitamin D", "-", null, Color(0xFFEF4444))
                VitaminSubItem("Calcium", "13mg", 1, Color(0xFF1C1C1E))
                VitaminSubItem("Iron", "0.28mg", 2, Color(0xFF1C1C1E))
                VitaminSubItem("Potassium", "115mg", 2, Color(0xFF1C1C1E))
                VitaminSubItem("Vitamin A", "3mcg", 0, Color(0xFF3B82F6))
                VitaminSubItem("Vitamin C", "36.2mg", 40, Color(0xFFFFD700))
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(top = 12.dp),
            color = Color(0xFFE5E7EB),
            thickness = 1.dp
        )
    }
}

@Composable
private fun VitaminSubItem(
    label: String,
    value: String,
    percent: Int?,
    labelColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = labelColor,
                    fontWeight = FontWeight.Medium
                )
                if (value != "-") {
                    Text(
                        text = value,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
        if (percent != null) {
            Text(
                text = "$percent%",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
        }
    }
}

private fun parseCalories(caloriesStr: String): Double {
    val cleaned = caloriesStr.lowercase().trim()
    val numberPart = cleaned.filter { it.isDigit() || it == '.' || it == ',' }
        .replace(',', '.')
        .toDoubleOrNull() ?: 0.0
    return numberPart
}
