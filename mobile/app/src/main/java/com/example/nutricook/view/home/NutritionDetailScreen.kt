package com.example.nutricook.view.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

@Composable
fun NutritionDetailScreen(
    navController: NavController
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
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color(0xFF1B1B1B)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Thông tin dinh dưỡng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B)
            )
        }

        Spacer(modifier = Modifier.size(16.dp))

        // Vòng tròn Calories (minh hoạ)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.pizza),
                contentDescription = "Calories"
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("473", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Calories", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.size(24.dp))

        // 3 ô thành phần chính
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NutritionBox("20g", "Chất béo", R.drawable.fat)
            NutritionBox("50g", "Tinh bột", R.drawable.finger_cricle)
            NutritionBox("24g", "Chất đạm", R.drawable.protein)
        }

        Spacer(modifier = Modifier.size(24.dp))

        // Chi tiết
        Text("✨ Thông tin dinh dưỡng", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.size(8.dp))
        NutritionRow("Tổng chất béo", "20g", "25%", R.drawable.flash_circle_nutri)
        NutritionRow("Cholesterol", "100mg", "33%", R.drawable.flash_circle_nutri)
        NutritionRow("Natri", "1281mg", "56%", R.drawable.flash_circle_nutri)
        NutritionRow("Tổng Carbohydrate", "50g", "18%", R.drawable.flash_circle_nutri)

        // ❌ KHÔNG đặt BottomNavigationBar ở đây.
        // Bottom bar đã được AppNav bọc bằng Scaffold ở các màn tab.
    }
}

@Composable
private fun NutritionBox(value: String, label: String, iconRes: Int) {
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
private fun NutritionRow(name: String, value: String, percent: String, iconRes: Int) {
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
