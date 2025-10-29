package com.example.nutricook.ui.screens.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

data class BuocNauAn(
    val moTa: String,
    val coAnh: Boolean = false,
    val anhRes: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDirectionsScreen(navController: NavController) {

    val danhSachBuoc = listOf(
        BuocNauAn("Đặt khay nướng ở giữa lò và làm nóng ở 190°C (375°F)."),
        BuocNauAn("Khuấy đều bột bánh, trứng và bơ trong tô lớn cho đến khi tạo thành hỗn hợp mềm.", true, R.drawable.mix_egg),
        BuocNauAn("Nặn bột thành từng viên tròn 2.5cm, lăn qua đường bột. Đặt các viên cách nhau khoảng 5cm trên khay nướng.", true, R.drawable.mix_egg),
        BuocNauAn("Nướng trong lò đã làm nóng sẵn, mỗi lần 1 khay, đến khi rìa bánh cứng và mặt bánh hơi nứt (khoảng 9–11 phút). Để nguội 3 phút trên khay, sau đó chuyển ra giá để nguội hoàn toàn.", true, R.drawable.mix_egg)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        /** 🔹 Thanh tiêu đề */
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Hướng dẫn nấu ăn",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        /** 🔹 Danh sách bước */
        itemsIndexed(danhSachBuoc) { index, buoc ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Bước ${String.format("%02d", index + 1)}",
                    color = Color(0xFF304FFE),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF5F7FB),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = buoc.moTa,
                            fontSize = 15.sp,
                            color = Color(0xFF6A6A6A),
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            painter = painterResource(id = R.drawable.icon_upload),
                            contentDescription = "Thêm ảnh minh họa",
                            modifier = Modifier
                                .size(28.dp) 
                                .padding(start = 10.dp),
                            tint = Color(0xFFB0B0B0)
                        )
                    }
                }

                if (buoc.coAnh && buoc.anhRes != null) {
                    Image(
                        painter = painterResource(id = buoc.anhRes),
                        contentDescription = "Ảnh bước ${index + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        /** 🔹 Nút Add Step */
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { /* TODO: thêm bước mới */ },
                color = Color(0xFFF8F8F8)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Thêm bước",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Thêm bước mới",
                        fontSize = 15.sp,
                        color = Color(0xFF303030),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
    Spacer(modifier = Modifier.height(12.dp))

  Button(
        onClick = { navController.navigate("upload_success") },
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp) 
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF20C7B5),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 3.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Đăng công thức",
                fontSize = 19.sp, 
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                painter = painterResource(id = R.drawable.icon_arrow_right),
                contentDescription = "Gửi",
                modifier = Modifier.size(22.dp), 
                tint = Color.White
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
}
    }
}
