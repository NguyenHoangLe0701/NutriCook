package com.example.nutricook.view.recipes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutricook.R
import com.example.nutricook.viewmodel.CreateRecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeStep3Screen(
    navController: NavController,
    createRecipeViewModel: CreateRecipeViewModel
) {
    val context = LocalContext.current
    
    // Lấy dữ liệu từ ViewModel
    val recipeState by createRecipeViewModel.state.collectAsState()
    
    // State variables - khôi phục từ ViewModel nếu có
    var description by remember { mutableStateOf(recipeState.description) }
    var notes by remember { mutableStateOf(recipeState.notes) }
    var tips by remember { mutableStateOf(recipeState.tips) }
    
    // Khôi phục dữ liệu từ ViewModel khi màn hình được tạo
    LaunchedEffect(Unit) {
        description = recipeState.description
        notes = recipeState.notes
        tips = recipeState.tips
    }
    
    // Lưu dữ liệu tự động vào ViewModel khi có thay đổi
    LaunchedEffect(description, notes, tips) {
        createRecipeViewModel.setStep3Data(description, notes, tips)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        /** 🔹 Header */
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF00BFA5).copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF00BFA5)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bước 3: Mô tả & Ghi chú",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Thêm mô tả chi tiết và các ghi chú",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Step indicator
                    Surface(
                        color = Color(0xFF00BFA5),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "3/4",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        /** 🔹 Mô tả chi tiết */
        item {
            Column {
                Text(
                    text = "Mô tả chi tiết",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập mô tả chi tiết về món ăn, hương vị, đặc điểm...") },
                    minLines = 4,
                    maxLines = 8
                )
            }
        }
        
        /** 🔹 Ghi chú */
        item {
            Column {
                Text(
                    text = "Ghi chú",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập các ghi chú quan trọng (ví dụ: bảo quản, lưu ý khi nấu...)") },
                    minLines = 3,
                    maxLines = 6
                )
            }
        }
        
        /** 🔹 Mẹo nấu ăn */
        item {
            Column {
                Text(
                    text = "Mẹo nấu ăn",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1C1E),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = tips,
                    onValueChange = { tips = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Chia sẻ các mẹo nấu ăn để món ăn ngon hơn...") },
                    minLines = 3,
                    maxLines = 6
                )
                Text(
                    text = "💡 Tất cả các trường trên đều tùy chọn, bạn có thể bỏ qua nếu không cần",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        
        /** 🔹 Nút Tiếp theo */
        item {
            Button(
                onClick = {
                    // Không cần validation vì tất cả đều tùy chọn
                    // Lưu dữ liệu vào ViewModel
                    createRecipeViewModel.setStep3Data(description, notes, tips)
                    
                    // Chuyển sang bước 4
                    navController.navigate("create_recipe_step4")
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
                Text(
                    text = "Tiếp theo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Tiếp theo",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

