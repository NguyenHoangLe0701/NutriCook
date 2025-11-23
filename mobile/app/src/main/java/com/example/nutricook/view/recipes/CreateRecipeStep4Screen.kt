package com.example.nutricook.view.recipes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeStep4Screen(
    navController: NavController
) {
    val context = LocalContext.current
    var isSubmitting by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        /** 🔹 Header */
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bước 4: Xem lại & Hoàn thành",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kiểm tra lại thông tin trước khi đăng",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                // Step indicator
                Surface(
                    color = Color(0xFF00BFA5).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "4/4",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00BFA5)
                    )
                }
            }
        }
        
        /** 🔹 Thông báo hoàn thành */
        item {
            Surface(
                color = Color(0xFF00BFA5).copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00BFA5),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sẵn sàng đăng công thức!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00BFA5)
                        )
                        Text(
                            text = "Hãy xem lại thông tin bên dưới",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        /** 🔹 Tóm tắt thông tin */
        item {
            Column {
                Text(
                    text = "Tóm tắt thông tin",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // Thông tin cơ bản
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📋 Thông tin cơ bản",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                        // TODO: Hiển thị thông tin từ bước 1
                        Text(
                            text = "• Tên món: [Từ bước 1]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "• Thời gian: [Từ bước 1]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "• Số phần ăn: [Từ bước 1]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "• Số ảnh: [Từ bước 1]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "• Số nguyên liệu: [Từ bước 1]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Hướng dẫn nấu ăn
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "👨‍🍳 Hướng dẫn nấu ăn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                        // TODO: Hiển thị số bước nấu ăn từ bước 2
                        Text(
                            text = "• Số bước nấu: [Từ bước 2]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Mô tả & Ghi chú
                Surface(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📝 Mô tả & Ghi chú",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                        // TODO: Hiển thị thông tin từ bước 3
                        Text(
                            text = "• Mô tả: [Từ bước 3]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "• Ghi chú: [Từ bước 3]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "• Mẹo: [Từ bước 3]",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        /** 🔹 Nút Hoàn thành */
        item {
            Button(
                onClick = {
                    isSubmitting = true
                    // TODO: Upload công thức lên server
                    // Simulate upload
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isSubmitting = false
                        Toast.makeText(context, "Đăng công thức thành công!", Toast.LENGTH_SHORT).show()
                        // Navigate back to home or recipe list
                        navController.navigate("recipes") {
                            popUpTo("create_recipe") { inclusive = true }
                        }
                    }, 2000)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5),
                    contentColor = Color.White
                ),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Đang đăng...", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Hoàn thành",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hoàn thành & Đăng",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

