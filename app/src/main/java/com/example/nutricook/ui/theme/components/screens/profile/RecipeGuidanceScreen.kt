package com.example.nutricook.ui.screens.profile

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
import com.example.nutricook.R

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
fun RecipeGuidanceScreen(navController: NavController) {
    val cookingTips = listOf(
        CookingTip(
            title = "Cách nấu cơm ngon",
            description = "Tỷ lệ nước và gạo 1:1.5, để lửa nhỏ 15 phút",
            icon = Icons.Default.Restaurant,
            category = "Cơm"
        ),
        CookingTip(
            title = "Cách luộc trứng hoàn hảo",
            description = "Nước sôi, thả trứng, đun 6-7 phút cho lòng đào",
            icon = Icons.Default.Egg,
            category = "Trứng"
        ),
        CookingTip(
            title = "Cách ướp thịt mềm",
            description = "Dùng nước dứa hoặc giấm táo ướp 30 phút",
            icon = Icons.Default.LocalDining,
            category = "Thịt"
        ),
        CookingTip(
            title = "Cách làm nước dùng trong",
            description = "Đun sôi, vớt bọt, thêm hành tây và gừng",
            icon = Icons.Default.SoupKitchen,
            category = "Nước dùng"
        )
    )

    val calorieInfo = listOf(
        CalorieInfo(
            foodName = "Thơm (Dứa)",
            calories = 48,
            imageRes = R.drawable.pineapple,
            benefits = listOf(
                "Giàu vitamin C",
                "Hỗ trợ tiêu hóa",
                "Chống viêm",
                "Tăng cường miễn dịch"
            )
        ),
        CalorieInfo(
            foodName = "Chuối",
            calories = 89,
            imageRes = R.drawable.banana,
            benefits = listOf(
                "Giàu kali",
                "Cung cấp năng lượng",
                "Tốt cho tim mạch",
                "Hỗ trợ cơ bắp"
            )
        ),
        CalorieInfo(
            foodName = "Cà rốt",
            calories = 52,
            imageRes = R.drawable.carrot,
            benefits = listOf(
                "Giàu beta-carotene",
                "Tốt cho mắt",
                "Chống oxy hóa",
                "Hỗ trợ da khỏe"
            )
        )
    )

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
