package com.example.nutricook.view.categories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
    var selectedQuantity by remember { mutableStateOf("100 g") }
    var isBookmarked by remember { mutableStateOf(false) }
    var showVitaminDetails by remember { mutableStateOf(false) }
    
    // Load food item by ID
    LaunchedEffect(foodId) {
        try {
            foodItem = viewModel.getFoodById(foodId)
            isLoading = false
            // Set default quantity based on unit
            foodItem?.let {
                selectedQuantity = when (it.unit.lowercase()) {
                    "ml", "l" -> "100 ml"
                    "quả", "cái" -> "1 quả"
                    "cốc" -> "1 cốc"
                    "thìa canh" -> "1 thìa canh"
                    "thìa cà phê" -> "1 thìa cà phê"
                    "lát" -> "1 lát"
                    "tép" -> "1 tép"
                    else -> "100 g"
                }
            }
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    // Tính toán dinh dưỡng dựa trên quantity đã chọn
    val calculatedNutrition = remember(foodItem, selectedQuantity) {
        if (foodItem == null) return@remember null
        
        val baseNutrition = NutritionData(
            calories = parseCalories(foodItem!!.calories),
            fat = foodItem!!.fat,
            carbs = foodItem!!.carbs,
            protein = foodItem!!.protein,
            cholesterol = foodItem!!.cholesterol,
            sodium = foodItem!!.sodium,
            vitamin = foodItem!!.vitamin
        )
        
        // Parse quantity và tính multiplier dựa trên unit của foodItem
        val multiplier = when {
            selectedQuantity.contains("ml", ignoreCase = true) -> {
                val mlValue = selectedQuantity.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 100.0
                mlValue / 100.0
            }
            selectedQuantity.contains("l", ignoreCase = true) && !selectedQuantity.contains("ml", ignoreCase = true) -> {
                val lValue = selectedQuantity.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 1.0
                (lValue * 1000.0) / 100.0 // 1 l = 1000ml, tính trên 100ml
            }
            selectedQuantity.contains("kg", ignoreCase = true) -> {
                val kgValue = selectedQuantity.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 1.0
                (kgValue * 1000.0) / 100.0 // 1 kg = 1000g, tính trên 100g
            }
            selectedQuantity.contains("g", ignoreCase = true) -> {
                val gValue = selectedQuantity.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 100.0
                gValue / 100.0
            }
            selectedQuantity.contains("quả", ignoreCase = true) || selectedQuantity.contains("cái", ignoreCase = true) -> {
                val count = selectedQuantity.filter { it.isDigit() }.toIntOrNull() ?: 1
                count.toDouble() // Mỗi quả = 1x giá trị trên 100g
            }
            selectedQuantity.contains("cốc", ignoreCase = true) -> {
                val count = selectedQuantity.filter { it.isDigit() }.toIntOrNull() ?: 1
                count * 2.4 // 1 cốc ≈ 240ml ≈ 240g
            }
            selectedQuantity.contains("thìa canh", ignoreCase = true) -> {
                val count = selectedQuantity.filter { it.isDigit() }.toIntOrNull() ?: 1
                count * 0.15 // 1 thìa canh ≈ 15ml ≈ 15g
            }
            selectedQuantity.contains("thìa cà phê", ignoreCase = true) -> {
                val count = selectedQuantity.filter { it.isDigit() }.toIntOrNull() ?: 1
                count * 0.05 // 1 thìa cà phê ≈ 5ml ≈ 5g
            }
            selectedQuantity.contains("lát", ignoreCase = true) -> {
                val count = selectedQuantity.filter { it.isDigit() }.toIntOrNull() ?: 1
                count * 0.5 // Giả sử 1 lát ≈ 50g
            }
            selectedQuantity.contains("tép", ignoreCase = true) -> {
                val count = selectedQuantity.filter { it.isDigit() }.toIntOrNull() ?: 1
                count * 0.3 // Giả sử 1 tép ≈ 30g
            }
            else -> 1.0
        }
        
        NutritionData(
            calories = baseNutrition.calories * multiplier,
            fat = baseNutrition.fat * multiplier,
            carbs = baseNutrition.carbs * multiplier,
            protein = baseNutrition.protein * multiplier,
            cholesterol = baseNutrition.cholesterol * multiplier,
            sodium = baseNutrition.sodium * multiplier,
            vitamin = baseNutrition.vitamin * multiplier
        )
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (foodItem == null || calculatedNutrition == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Không tìm thấy nguyên liệu", color = Color.Gray)
        }
    } else {
        val nutritionData = calculatedNutrition
        
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .width(200.dp)
                            .height(200.dp),
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                        if (foodItem!!.imageUrl.isNotBlank() && foodItem!!.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(foodItem!!.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = foodItem!!.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                error = painterResource(id = R.drawable.cabbage),
                                placeholder = painterResource(id = R.drawable.cabbage),
                                onError = { 
                                    android.util.Log.e("FoodDetailScreen", "Error loading image: ${foodItem!!.imageUrl}")
                                }
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
            }
            
            /** 🔹 Quantity Selection */
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Tạo danh sách quantity options dựa trên unit của foodItem (xóa "1 oz" và "1 quả")
                    val quantityOptions = remember(foodItem?.unit) {
                        when (foodItem?.unit?.lowercase()) {
                            "ml", "l" -> listOf("100 ml", "250 ml", "500 ml", "1 l")
                            "quả", "cái" -> listOf("1 quả", "2 quả", "3 quả")
                            "cốc" -> listOf("1 cốc", "2 cốc", "3 cốc")
                            "thìa canh" -> listOf("1 thìa canh", "2 thìa canh", "3 thìa canh")
                            "thìa cà phê" -> listOf("1 thìa cà phê", "2 thìa cà phê", "3 thìa cà phê")
                            "lát" -> listOf("1 lát", "2 lát", "3 lát")
                            "tép" -> listOf("1 tép", "2 tép", "3 tép")
                            "kg" -> listOf("100 g", "250 g", "500 g", "1 kg")
                            else -> listOf("100 g", "250 g", "500 g", "1 kg") // Mặc định cho "g"
                        }
                    }
                    
                    // Đảm bảo selectedQuantity có trong danh sách
                    LaunchedEffect(quantityOptions, foodItem?.unit) {
                        if (quantityOptions.isNotEmpty()) {
                            // Set default quantity based on unit
                            val defaultQuantity = when (foodItem?.unit?.lowercase()) {
                                "ml", "l" -> "100 ml"
                                "quả", "cái" -> "1 quả"
                                "cốc" -> "1 cốc"
                                "thìa canh" -> "1 thìa canh"
                                "thìa cà phê" -> "1 thìa cà phê"
                                "lát" -> "1 lát"
                                "tép" -> "1 tép"
                                else -> "100 g"
                            }
                            if (selectedQuantity !in quantityOptions) {
                                selectedQuantity = if (defaultQuantity in quantityOptions) defaultQuantity else quantityOptions[0]
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        quantityOptions.forEachIndexed { index, option ->
                            QuantityButton(
                                text = option,
                                isSelected = selectedQuantity == option,
                                onClick = { selectedQuantity = option },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Quantity Visualizer (Bar chart) - đổi màu và chiều cao dựa trên calo
                    val baseCalories = parseCalories(foodItem?.calories ?: "0")
                    // Tính calo thực tế dựa trên quantity đã chọn
                    val actualCalories = calculatedNutrition?.calories ?: baseCalories
                    QuantityVisualizer(
                        selectedIndex = quantityOptions.indexOf(selectedQuantity).coerceIn(0, quantityOptions.size - 1),
                        calories = actualCalories,
                        totalOptions = quantityOptions.size
                    )
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
                        fontSize = 30.sp,
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
                        title = "Chất béo",
                        value = "${String.format("%.2f", nutritionData.fat).replace('.', ',')}g",
                        iconColor = Color(0xFF3B82F6),
                        iconSymbol = "%",
                        modifier = Modifier.weight(1f)
                    )
                    MacroCard(
                        title = "Tinh bột",
                        value = "${String.format("%.2f", nutritionData.carbs).replace('.', ',')}g",
                        iconColor = Color(0xFFFF9800),
                        iconSymbol = "0",
                        modifier = Modifier.weight(1f)
                    )
                    MacroCard(
                        title = "Chất đạm",
                        value = "${String.format("%.2f", nutritionData.protein).replace('.', ',')}g",
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
                            text = "Thông tin",
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
                            text = "Lợi ích của ${foodItem!!.name}:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                    
                    Text(
                        text = "Các vitamin và khoáng chất trong ${foodItem!!.name.lowercase()} có thể giúp rút ngắn thời gian nhiễm virus và vi khuẩn, đồng thời tăng cường sức khỏe xương. Ngoài ra, còn có bằng chứng cho thấy ${foodItem!!.name.lowercase()} có thể giúp ngăn ngừa ung thư và cải thiện chất lượng tinh trùng.",
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 20.sp
                    )
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
                                text = "Thông tin dinh dưỡng",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                        
                        NutritionFactItem(
                            label = "Tổng chất béo",
                            value = "${String.format("%.2f", nutritionData.fat).replace('.', ',')}g",
                            percent = nutritionData.getFatPercent()
                        )
                        NutritionFactItem(
                            label = "Cholesterol",
                            value = "${nutritionData.cholesterol.toInt()}mg",
                            percent = nutritionData.getCholesterolPercent(),
                            valueColor = Color(0xFF00BFA5)
                        )
                        NutritionFactItem(
                            label = "Natri",
                            value = "${nutritionData.sodium.toInt()}mg",
                            percent = nutritionData.getSodiumPercent(),
                            valueColor = Color(0xFF10B981)
                        )
                        NutritionFactItem(
                            label = "Tổng carbohydrate",
                            value = "${String.format("%.2f", nutritionData.carbs).replace('.', ',')}g",
                            percent = nutritionData.getCarbsPercent(),
                            valueColor = Color(0xFF10B981)
                        )
                        NutritionFactItem(
                            label = "Chất đạm",
                            value = "${String.format("%.2f", nutritionData.protein).replace('.', ',')}g",
                            percent = nutritionData.getProteinPercent(),
                            valueColor = Color(0xFF10B981)
                        )
                        NutritionFactItem(
                            label = "Vitamin",
                            value = "",
                            percent = nutritionData.getVitaminPercent(),
                            valueColor = Color(0xFF1C1C1E),
                            onClick = { showVitaminDetails = !showVitaminDetails }
                        )
                    }
                }
            }
            
            // Hiển thị chi tiết vitamin khi click
            item {
                AnimatedVisibility(
                    visible = showVitaminDetails && foodItem != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    foodItem?.let { currentFoodItem ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .background(
                                    Color(0xFF00BFA5).copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Chi tiết Vitamin (% Daily Value)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            val vitaminDetails = listOf(
                                "Vitamin A" to currentFoodItem.vitaminA,
                                "Vitamin B1 (Thiamin)" to currentFoodItem.vitaminB1,
                                "Vitamin B2 (Riboflavin)" to currentFoodItem.vitaminB2,
                                "Vitamin B3 (Niacin)" to currentFoodItem.vitaminB3,
                                "Vitamin B6" to currentFoodItem.vitaminB6,
                                "Vitamin B9 (Folate)" to currentFoodItem.vitaminB9,
                                "Vitamin B12" to currentFoodItem.vitaminB12,
                                "Vitamin C" to currentFoodItem.vitaminC,
                                "Vitamin D" to currentFoodItem.vitaminD,
                                "Vitamin E" to currentFoodItem.vitaminE,
                                "Vitamin K" to currentFoodItem.vitaminK
                            )
                            
                            vitaminDetails.forEach { (name, value) ->
                                if (value > 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 13.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                        Surface(
                                            color = Color(0xFF00BFA5).copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "${String.format("%.1f", value)}%",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF00BFA5),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            if (vitaminDetails.all { it.second == 0.0 }) {
                                Text(
                                    text = "Chưa có thông tin chi tiết về vitamin",
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
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
private fun QuantityVisualizer(selectedIndex: Int, calories: Double, totalOptions: Int) {
    val totalSegments = 10
    val maxHeight = 24.dp
    
    // Tính màu dựa trên mức độ calo (calo càng cao, màu càng nóng)
    // Calo thấp (< 50): xanh lá (0xFF10B981)
    // Calo trung bình (50-150): vàng cam (0xFFFF9800)
    // Calo cao (150-300): cam đỏ (0xFFFF5722)
    // Calo rất cao (> 300): đỏ (0xFFEF4444)
    val calorieColor = when {
        calories < 50 -> Color(0xFF10B981) // Xanh lá
        calories < 150 -> Color(0xFFFF9800) // Vàng cam
        calories < 300 -> Color(0xFFFF5722) // Cam đỏ
        else -> Color(0xFFEF4444) // Đỏ
    }
    
    // Tính chiều cao dựa trên calo (calo càng cao, thanh càng dài)
    // Normalize calo từ 0-500 thành 0-1, sau đó map thành chiều cao từ 8dp đến 24dp
    val normalizedCalories = (calories / 500.0).coerceIn(0.0, 1.0)
    val baseHeight = 8.dp + (normalizedCalories * 16.dp.value).dp
    
    // Tính phạm vi segments cho mỗi option
    // Chia 10 segments thành các phần bằng nhau cho mỗi option
    fun getOptionRange(optionIndex: Int): IntRange {
        val segmentsPerOption = totalSegments / totalOptions
        val remainder = totalSegments % totalOptions
        val start = optionIndex * segmentsPerOption + minOf(optionIndex, remainder)
        val end = start + segmentsPerOption + if (optionIndex < remainder) 1 else 0
        return start until end
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .width(280.dp)
                .height(maxHeight),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(totalSegments) { index ->
                // Xác định option nào chứa segment này
                val optionIndex = (0 until totalOptions).firstOrNull { index in getOptionRange(it) } ?: -1
                val isSelected = optionIndex == selectedIndex
                
                // Chiều cao của segment
                // Nếu được chọn: chiều cao dựa trên calo (calo càng cao, thanh càng dài)
                // Nếu không được chọn: chiều cao thấp, có variation để tạo pattern
                val height = if (isSelected) {
                    // Segment được chọn: chiều cao dựa trên calo, có variation nhỏ để tạo pattern
                    val variation = when {
                        index % 5 == 0 -> 2.dp // Cao nhất trong pattern
                        index % 3 == 0 -> 0.dp // Trung bình
                        else -> -1.dp // Thấp hơn một chút
                    }
                    (baseHeight + variation).coerceIn(8.dp, maxHeight)
                } else {
                    // Segment không được chọn: chiều cao thấp, có pattern
                    when {
                        index % 5 == 0 -> 6.dp
                        index % 3 == 0 -> 4.dp
                        else -> 3.dp
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(height)
                        .background(
                            color = if (isSelected) calorieColor else Color(0xFFE5E7EB),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
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
            // Icon tròn với màu nền đầy đủ (như NutritionFactsScreen)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconSymbol,
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
private fun NutritionFactItem(
    label: String,
    value: String,
    percent: Int,
    valueColor: Color = Color(0xFF1C1C1E),
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon lightning bolt (như NutritionFactsScreen)
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
            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = valueColor
                )
            }
            // Hiển thị phần trăm trong rounded rectangle màu xanh lá (như NutritionFactsScreen)
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

private fun parseCalories(caloriesStr: String): Double {
    val cleaned = caloriesStr.lowercase().trim()
    val numberPart = cleaned.filter { it.isDigit() || it == '.' || it == ',' }
        .replace(',', '.')
        .toDoubleOrNull() ?: 0.0
    return numberPart
}
