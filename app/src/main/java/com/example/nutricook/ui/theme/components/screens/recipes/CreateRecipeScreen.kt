package com.example.nutricook.ui.screens.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R
import androidx.compose.ui.draw.clip

data class NguyenLieu(
    val ten: String,
    val soLuong: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(navController: NavController) {

    val danhSachNguyenLieu = listOf(
        NguyenLieu("Bột bánh kem trắng", "16.5 ounces"),
        NguyenLieu("Trứng", "2 quả lớn"),
        NguyenLieu("Đường bột", "½ cốc")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        /** 🔹 Thanh tiêu đề */
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "Tạo công thức của bạn",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cùng sáng tạo món ăn riêng của bạn nhé!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        /** 🔹 Tiêu đề món ăn */
        item {
            Text(
                text = "Tên công thức",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            OutlinedTextField(
                value = "Bánh quy Clever Cake Mix",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { /* chỉnh sửa tên */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_upload),
                            contentDescription = "Chỉnh sửa",
                            modifier = Modifier.size(28.dp),
                            tint = Color(0xFF00BFA5)
                        )
                    }
                }
            )
        }

        /** 🔹 Ảnh món ăn */
        item {
            Image(
                painter = painterResource(id = R.drawable.cake),
                contentDescription = "Ảnh món ăn",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2F2F2))
            )
        }

        /** 🔹 Số khẩu phần ăn */
        item {
            Text(
                text = "Khẩu phần ăn",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = "4",
                onValueChange = {},
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = "Người ăn",
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF00BFA5)
                    )
                },
                modifier = Modifier.width(140.dp)
            )
        }

        /** 🔹 Thời gian nấu */
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = "10 phút",
                    onValueChange = {},
                    label = { Text("Chuẩn bị") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.time),
                            contentDescription = "Chuẩn bị",
                            modifier = Modifier.size(28.dp),
                            tint = Color(0xFF00BFA5)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = "40 phút",
                    onValueChange = {},
                    label = { Text("Nấu ăn") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.time),
                            contentDescription = "Nấu ăn",
                            modifier = Modifier.size(28.dp),
                            tint = Color(0xFF00BFA5)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        /** 🔹 Danh sách nguyên liệu */
         item {
            Text(
                text = "Nguyên liệu",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(danhSachNguyenLieu) { nl ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_upload),
                        contentDescription = "Tải ảnh nguyên liệu",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFB0B0B0)
                    )
                    Text(
                        text = nl.ten,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }
                Text(
                    text = nl.soLuong,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        /** 🔹 Thêm nguyên liệu mới */
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(onClick = { /* thêm nguyên liệu */ }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm nguyên liệu",
                        modifier = Modifier.size(26.dp),
                        tint = Color(0xFF00BFA5)
                    )
                    Text(
                        text = "Thêm nguyên liệu",
                        color = Color(0xFF00BFA5),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        /** 🔹 Nút tiếp theo */
        item {
            Button(
                onClick = { 
    navController.navigate("recipe_direction") 
},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Tiếp theo", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Tiếp",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
