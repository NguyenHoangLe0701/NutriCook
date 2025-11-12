package com.example.nutricook.view.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.R
import com.example.nutricook.viewmodel.QueryViewModel

data class CookingTip(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val category: String
)

data class CalorieInfo(
    val foodName: String,
    val calories: Int,
    val imageRes: Int,
    val benefits: List<String>
)

@Composable
fun RecipeGuidanceScreen(navController: NavController, queryVM: QueryViewModel = hiltViewModel()) {
    // Load from Firestore
    val firebaseCookingTips by queryVM.cookingTips
    val firebaseCalorieInfo by queryVM.calorieInfo
    val isLoading by queryVM.isLoading

    LaunchedEffect(Unit) {
        queryVM.loadCookingTips()
        queryVM.loadCalorieInfo()
    }

    // Convert Map to typed data, fallback to SampleData
    val cookingTips: List<CookingTip> = if (firebaseCookingTips.isNotEmpty()) {
        firebaseCookingTips.mapNotNull { map ->
            try {
                CookingTip(
                    title = map["title"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    icon = Icons.Default.Restaurant,
                    category = map["category"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    } else {
        com.example.nutricook.data.SampleData.cookingTips.mapNotNull { map ->
            try {
                CookingTip(
                    title = map["title"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    icon = Icons.Default.Restaurant,
                    category = map["category"] as? String ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    val calorieInfo: List<CalorieInfo> = if (firebaseCalorieInfo.isNotEmpty()) {
        firebaseCalorieInfo.mapNotNull { map ->
            try {
                @Suppress("UNCHECKED_CAST")
                CalorieInfo(
                    foodName = map["foodName"] as? String ?: "",
                    calories = (map["calories"] as? Number)?.toInt() ?: 0,
                    imageRes = R.drawable.pineapple,
                    benefits = (map["benefits"] as? List<String>) ?: emptyList()
                )
            } catch (e: Exception) {
                null
            }
        }
    } else {
        com.example.nutricook.data.SampleData.calorieInfo.mapNotNull { map ->
            try {
                @Suppress("UNCHECKED_CAST")
                CalorieInfo(
                    foodName = map["foodName"] as? String ?: "",
                    calories = (map["calories"] as? Number)?.toInt() ?: 0,
                    imageRes = R.drawable.pineapple,
                    benefits = (map["benefits"] as? List<String>) ?: emptyList()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Công Thức & Hướng dẫn",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

        item {
            // Welcome Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = "Cooking",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF20B2AA)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hướng dẫn nấu ăn thông minh",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tính calo, mẹo nấu ăn và đề xuất bài tập",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Calorie Calculator Section
            Text(
                text = "Tính calo thực phẩm",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(calorieInfo) { food ->
            CalorieInfoCard(
                food = food,
                onClick = { navController.navigate("food_detail/${food.foodName}") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Cooking Tips Section
            Text(
                text = "Mẹo nấu ăn",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(cookingTips) { tip ->
            CookingTipCard(tip = tip)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            // Exercise Suggestions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { navController.navigate("exercise_suggestions") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = "Exercise",
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFFF8C00)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Đề xuất bài tập",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Tính toán bài tập cần thiết để đốt cháy calo",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(
                        Icons.Default.ArrowForwardIos,
                        contentDescription = "Navigate",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
        }
    }
}

@Composable
fun CalorieInfoCard(
    food: CalorieInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = food.imageRes),
                contentDescription = food.foodName,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = food.foodName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${food.calories} kcal / 100g",
                    fontSize = 14.sp,
                    color = Color(0xFF20B2AA),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Lợi ích: ${food.benefits.take(2).joinToString(", ")}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = "View Details",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CookingTipCard(tip: CookingTip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tip.icon,
                contentDescription = tip.title,
                tint = Color(0xFF20B2AA),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tip.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = tip.description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = tip.category,
                    fontSize = 12.sp,
                    color = Color(0xFF20B2AA),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
