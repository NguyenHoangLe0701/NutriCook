package com.example.nutricook.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R
import androidx.compose.foundation.BorderStroke

data class Category(val name: String, val icon: Int)
data class NutritionItem(val name: String, val calories: String, val weight: String, val icon: Int)

@Composable
fun HomeScreen(navController: NavController) {
    val categories = listOf(
        Category("Rau củ", R.drawable.vegetable),
        Category("Trái cây", R.drawable.fruit),
        Category("Thịt", R.drawable.meat)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 🔹 Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(40.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_notification_status),
                    contentDescription = "Edit",
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = { navController.navigate("notifications") }, modifier = Modifier.size(40.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.ic_noti),
                    contentDescription = "Notifications",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // 🔹 Search Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Bạn muốn tìm kiếm gì?",
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Banner + Indicator
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("recipe_discovery") },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.banner_strawberry),
                    contentDescription = "Recipe Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == 0) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) Color(0xFF20B2AA) else Color(0xFFE0E0E0))
                    )
                    if (index < 2) Spacer(modifier = Modifier.width(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Categories Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Phân loại", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Xem tất cả",
                color = Color(0xFF20B2AA),
                modifier = Modifier.clickable { /* TODO */ }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Categories Row
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp)
                        .clickable { navController.navigate("categories") }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = category.icon),
                            contentDescription = category.name,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = category.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1B1B1B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Nutrition Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Giá trị dinh dưỡng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                "Xem tất cả",
                color = Color(0xFF20B2AA),
                modifier = Modifier.clickable {
                // Navigate đến màn hình NutritionDetailScreen
                navController.navigate("nutrition_detail")
             }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Nutrition Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFB2EBF2))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pineapple),
                        contentDescription = "Pineapple",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF8E1)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            "Dứa/Thơm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1B)
                        )
                        Text(
                            "48 kcal    100 g",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bookmark),
                            contentDescription = "Bookmark",
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { /* TODO */ }
                        )
                        Image(
                            painter = painterResource(id = R.drawable.minus_square),
                            contentDescription = "Remove",
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { /* TODO */ }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutritionStatCard(
                        value = "0.12g",
                        iconRes = R.drawable.protein,
                        modifier = Modifier.weight(1f)
                    )
                    NutritionStatCard(
                        value = "12.63g",
                        iconRes = R.drawable.finger_cricle,
                        modifier = Modifier.weight(1f)
                    )
                    NutritionStatCard(
                        value = "0.12g",
                        iconRes = R.drawable.fat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// 🔹 Component nhỏ cho từng ô dinh dưỡng
@Composable
fun NutritionStatCard(value: String, iconRes: Int, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FAFA)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(64.dp)
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "Nutrition Icon",
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 11.sp, color = Color(0xFF1B1B1B))
        }
    }
}
