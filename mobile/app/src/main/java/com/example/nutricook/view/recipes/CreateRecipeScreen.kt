package com.example.nutricook.view.recipes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.net.Uri
import java.io.File
import com.example.nutricook.R
import com.example.nutricook.data.repository.CategoryFirestoreRepository
import com.example.nutricook.data.repository.FoodUploadRepository
import com.example.nutricook.viewmodel.CategoryUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class NguyenLieu(
    val ten: String,
    val soLuong: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    navController: NavController,
    categoryRepo: CategoryFirestoreRepository = CategoryFirestoreRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance()),
    uploadRepo: FoodUploadRepository = FoodUploadRepository()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State variables
    var recipeName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(3.0) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    
    // Load categories
    var categories by remember { mutableStateOf<List<CategoryUI>>(emptyList()) }
    var isLoadingCategories by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLoadingCategories = true
        runCatching {
            categories = categoryRepo.getCategories()
        }.onFailure { e ->
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "Lỗi tải danh mục: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        isLoadingCategories = false
    }
    
    // Image picker launcher - chỉ chọn ảnh (image only)
    // Sử dụng GetContent cho Android cũ, PickVisualMedia cho Android mới
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Convert URI to File for upload
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
                    inputStream?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    selectedImageFile = tempFile
                } catch (e: Exception) {
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        Toast.makeText(context, "Lỗi xử lý ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


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

        /** 🔹 Tên món ăn */
        item {
            Text(
                text = "Tên công thức *",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            OutlinedTextField(
                value = recipeName,
                onValueChange = { recipeName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập tên món ăn") }
            )
        }
        
        /** 🔹 Danh mục */
        item {
            Text(
                text = "Danh mục *",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            if (isLoadingCategories) {
                CircularProgressIndicator()
            } else {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        placeholder = { Text("Chọn danh mục") }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        /** 🔹 Calo */
        item {
            Text(
                text = "Calories *",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ví dụ: 52 kcal") }
            )
        }
        
        /** 🔹 Mô tả */
        item {
            Text(
                text = "Mô tả",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nhập mô tả món ăn...") },
                maxLines = 3
            )
        }
        
        /** 🔹 Rating */
        item {
            Text(
                text = "Đánh giá (sao)",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { index ->
                    IconButton(onClick = { rating = index.toDouble() }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index <= rating.toInt()) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text("${String.format("%.1f", rating)}/5", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        /** 🔹 Ảnh món ăn */
        item {
            Text(
                text = "Ảnh món ăn",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1C1C1E)
            )
            
            // Button chọn ảnh
            Button(
                onClick = {
                    // Chọn ảnh từ gallery - GetContent hỗ trợ tất cả phiên bản Android
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = "Chọn ảnh",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedImageUri == null) "Chọn ảnh từ bộ sưu tập" else "Chọn ảnh khác",
                    fontSize = 15.sp
                )
            }
        }
        
        /** 🔹 Preview ảnh */
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2F2F2))
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(selectedImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Ảnh món ăn đã chọn",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Chưa có ảnh", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }


        /** 🔹 Nút Upload */
        item {
            Button(
                onClick = {
                    if (recipeName.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (calories.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập calories", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedCategoryId == null) {
                        Toast.makeText(context, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isUploading = true
                    scope.launch {
                        try {
                            val result = uploadRepo.uploadFood(
                                name = recipeName,
                                calories = calories,
                                categoryId = selectedCategoryId!!,
                                description = description.ifBlank { null },
                                rating = rating,
                                imageFile = selectedImageFile,
                                userId = null
                            )
                            
                            result.onSuccess {
                                Toast.makeText(context, "Upload thành công!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }.onFailure { error ->
                                Toast.makeText(context, "Upload thất bại: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
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
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "Đăng món ăn", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Upload",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
