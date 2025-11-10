package com.example.nutricook.view.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

data class NotificationItem(
    val id: Int,
    val imageRes: Int,
    val title: String,
    val subtitle: String,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {
    val notifications = listOf(
        NotificationItem(1, R.drawable.sample_food_1, "Mdodocook đã tải lên công thức mới", "Good New Orleans Creole Gumbo", "vài giây trước"),
        NotificationItem(2, R.drawable.sample_food_2, "tcn5 đã tải lên công thức mới", "Noodle Casserole with Sour Cream and Cheese", "1 phút trước"),
        NotificationItem(3, R.drawable.sample_food_3, "Ý nghĩa của chữ 'Burn' trên nồi Instant Pot", "Mẹo nấu gà", "1 giờ trước"),
        NotificationItem(4, R.drawable.sample_food_4, "Không có khuôn muffin?", "Mẹo TikTok này sẽ cứu bạn!", "20 phút trước"),
        NotificationItem(5, R.drawable.sample_food_5, "Ý nghĩa của chữ 'Burn' trên nồi Instant Pot", "Chicken Tips", "1 giờ trước"),
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 12.dp, bottom = 4.dp)
            ) {
                // 🔹 Hàng 1: Nút quay lại
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.Black
                    )
                }

                // 🔹 Hàng 2: Tiêu đề + nút Xem tất cả
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Thông báo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    TextButton(onClick = { /* TODO: Xử lý xem tất cả */ }) {
                        Text("Xem tất cả", color = Color(0xFF00BFA5), fontSize = 15.sp)
                    }
                }
                Divider(color = Color(0xFFE0E0E0))
            }
        },
        containerColor = Color(0xFFF9F9F9)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notifications) { item ->
                NotificationRow(item)
            }
        }
    }
}

@Composable
fun NotificationRow(item: NotificationItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .padding(end = 12.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.subtitle, color = Color.Gray, fontSize = 14.sp)
            Text(item.time, color = Color(0xFF9E9E9E), fontSize = 12.sp)
        }
    }
}