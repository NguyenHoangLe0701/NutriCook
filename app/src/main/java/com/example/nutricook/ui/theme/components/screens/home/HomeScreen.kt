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

data class Category(val name: String, val icon: Int)
data class NutritionItem(val name: String, val calories: String, val weight: String, val icon: Int)

@Composable
fun HomeScreen(navController: NavController) {
    val categories = listOf(
        Category("Rau củ", R.drawable.salad),
        Category("Trái cây", R.drawable.pizza),
        Category("Thịt", R.drawable.pizza)
    )
    
    val nutritionItems = listOf(
        NutritionItem("Dứa/Thơm", "48 kcal", "100 g", R.drawable.pizza)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* TODO: Profile */ },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF20B2AA), CircleShape)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
            
            IconButton(
                onClick = { /* TODO: Notifications */ },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF20B2AA), CircleShape)
            ) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
            }
        }

        // Search Bar
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
                IconButton(onClick = { /* TODO: Filter */ }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

      // Featured Recipe Card (Full width)
Box(
    modifier = Modifier
        .fillMaxWidth()
        .clickable { navController.navigate("recipe_discovery") }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp), // bỏ bo góc nếu muốn full hết
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner_strawberry),
            contentDescription = "Recipe Banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )
    }
}
Spacer(modifier = Modifier.height(24.dp))

        // Categories Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Phân loại",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Xem tất cả",
                color = Color(0xFF20B2AA),
                modifier = Modifier.clickable { /* TODO: See all categories */ }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Dots
        Row(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 0) 10.dp else 6.dp)
                        .background(
                            if (index == 0) Color(0xFF20B2AA) else Color.Gray,
                            CircleShape
                        )
                        .padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Row
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.clickable { navController.navigate("categories") }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = category.icon),
                            contentDescription = category.name,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = category.name,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nutrition Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Giá trị dinh dưỡng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Xem tất cả",
                color = Color(0xFF20B2AA),
                modifier = Modifier.clickable { /* TODO: See all nutrition */ }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nutrition Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF20B2AA))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.pizza),
                        contentDescription = "Pineapple",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dứa/Thơm",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "48 kcal • 100 g",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        IconButton(onClick = { /* TODO: Bookmark */ }) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                        }
                        IconButton(onClick = { /* TODO: Remove */ }) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Percent, contentDescription = "Protein", tint = Color.Blue)
                            Text("0.12g", fontSize = 10.sp)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Circle, contentDescription = "Carbs", tint = Color(0xFFFF8C00))
                            Text("12.63g", fontSize = 10.sp)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Circle, contentDescription = "Fat", tint = Color(0xFF20B2AA))
                            Text("0.12g", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
