package com.example.nutricook.ui.screens.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeUploadSuccessScreen(
    navController: NavController,
    imageRes: Int = R.drawable.img_upload 
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 10.dp) 
    ) {
        /** 🔹 Nút quay lại đặt sát trên cùng */
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp)
                .clip(CircleShape)
                .background(Color(0xFFF4F6FA))
                .size(46.dp) // 👉 to hơn nhẹ
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color(0xFF1E1E1E),
                modifier = Modifier.size(26.dp) 
            )
        }

        /** 🔹 Nội dung chính căn giữa */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            /** Ảnh món ăn */
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Ảnh món ăn đã đăng",
                modifier = Modifier
                    .size(300.dp) 
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(40.dp))

            /** Text thông báo */
            Text(
                text = "Công thức của bạn đã đăng thành công!",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            /** Nút về trang chính */
            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier
                    .fillMaxWidth(0.6f) 
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF20C7B5),
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Về trang chủ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.icon_arrow_right),
                        contentDescription = "Đi tiếp",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
