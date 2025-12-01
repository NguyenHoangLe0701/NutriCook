package com.example.nutricook.view.recipes

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.R
import com.example.nutricook.viewmodel.CreateRecipeViewModel

data class CookingStep(
    val description: String = "",
    val imageUri: Uri? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeStep2Screen(
    navController: NavController,
    createRecipeViewModel: CreateRecipeViewModel
) {
    val context = LocalContext.current
    
    // State variables
    var cookingSteps by remember { mutableStateOf<List<CookingStep>>(listOf(CookingStep())) }
    
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
                            text = "Bước 2: Hướng dẫn nấu ăn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Nhập các bước nấu ăn chi tiết",
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
                            text = "2/4",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        /** 🔹 Danh sách các bước nấu ăn */
        itemsIndexed(cookingSteps) { index, step ->
            CookingStepItem(
                step = step,
                index = index,
                onStepUpdate = { updatedStep ->
                    cookingSteps = cookingSteps.mapIndexed { i, s ->
                        if (i == index) updatedStep else s
                    }
                },
                onDelete = {
                    if (cookingSteps.size > 1) {
                        cookingSteps = cookingSteps.filterIndexed { i, _ -> i != index }
                    }
                },
                canDelete = cookingSteps.size > 1
            )
        }
        
        /** 🔹 Nút thêm bước mới */
        item {
            OutlinedButton(
                onClick = {
                    cookingSteps = cookingSteps + CookingStep()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF00BFA5)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm bước",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm bước nấu ăn", fontSize = 15.sp)
            }
        }
        
        /** 🔹 Nút Tiếp theo */
        item {
            Button(
                onClick = {
                    // Validation
                    val validSteps = cookingSteps.filter { it.description.isNotBlank() }
                    if (validSteps.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập ít nhất một bước nấu ăn", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // Lưu dữ liệu vào ViewModel
                    createRecipeViewModel.setStep2Data(validSteps)
                    
                    // Chuyển sang bước 3
                    navController.navigate("create_recipe_step3")
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

@Composable
private fun CookingStepItem(
    step: CookingStep,
    index: Int,
    onStepUpdate: (CookingStep) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean = true
) {
    val context = LocalContext.current
    
    // Image picker launcher cho từng bước
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onStepUpdate(step.copy(imageUri = it))
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Header của bước
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bước ${index + 1}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00BFA5)
            )
            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa bước",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // TextField mô tả bước
        OutlinedTextField(
            value = step.description,
            onValueChange = { newDescription ->
                onStepUpdate(step.copy(description = newDescription))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Nhập mô tả bước nấu ăn...") },
            minLines = 3,
            maxLines = 6
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Ảnh của bước (nếu có)
        if (step.imageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(step.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Ảnh bước ${index + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Nút xóa ảnh
                IconButton(
                    onClick = {
                        onStepUpdate(step.copy(imageUri = null))
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(bottomStart = 12.dp)
                        )
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Xóa ảnh",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // Button thêm ảnh
            OutlinedButton(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF00BFA5)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = "Thêm ảnh",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm ảnh minh họa (tùy chọn)", fontSize = 14.sp)
            }
        }
    }
}

