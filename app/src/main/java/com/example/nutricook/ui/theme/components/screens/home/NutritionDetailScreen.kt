package com.example.nutricook.ui.theme.components.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutricook.R
import com.example.nutricook.ui.navigation.BottomNavigationBar
import androidx.navigation.NavController
 
@Composable
fun NutritionDetailScreen(
    navController: NavController,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color(0xFF1B1B1B)
                )
            }
            Text(
                text = "Thông tin dinh dưỡng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vòng tròn Calories
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pizza), // ảnh vòng tròn bạn thêm
                contentDescription = "Calories"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("473", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Calories", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3 ô thành phần dinh dưỡng chính
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutritionBox("20g", "Chất béo", R.drawable.fat)
            NutritionBox("50g", "Tinh bột", R.drawable.finger_cricle)
            NutritionBox("24g", "Chất đạm", R.drawable.protein)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Phần chi tiết
        Text("✨ Thông tin dinh dưỡng", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        NutritionItem("Tổng chất béo", "20g", "25%", R.drawable.flash_circle_nutri)
        NutritionItem("Cholesterol", "100mg", "33%", R.drawable.flash_circle_nutri)
        NutritionItem("Natri", "1281mg", "56%", R.drawable.flash_circle_nutri)
        NutritionItem("Tổng Carbohydrate", "50g", "18%", R.drawable.flash_circle_nutri)
          // 🔹 Bottom Navigation
        BottomNavigationBar(navController = navController)
    }
    
}
 
@Composable
fun NutritionBox(value: String, label: String, iconRes: Int) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .background(Color(0xFFF6F6F6), shape = RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(32.dp)
        )
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun NutritionItem(name: String, value: String, percent: String, iconRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = name,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium)
            Text(value, color = Color(0xFF8C8C8C), fontSize = 12.sp)
        }
        Text(percent, color = Color(0xFF1B1B1B), fontWeight = FontWeight.Bold)
    }
}
