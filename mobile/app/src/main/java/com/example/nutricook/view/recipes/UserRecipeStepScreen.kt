package com.example.nutricook.view.recipes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRecipeStepScreen(
    navController: NavController,
    recipeId: String,
    stepIndex: Int
) {
    val context = LocalContext.current
    var recipeData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(recipeId, stepIndex) {
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("userRecipes")
                .document(recipeId)
                .get()
                .await()
            
            if (doc.exists()) {
                recipeData = doc.data
                android.util.Log.d("UserRecipeStep", "Recipe data loaded for recipeId: $recipeId, stepIndex: $stepIndex")
            } else {
                android.util.Log.w("UserRecipeStep", "Recipe document not found: $recipeId")
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRecipeStep", "Error loading recipe: ${e.message}", e)
            Toast.makeText(context, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Parse cookingSteps từ Firestore - xử lý nhiều kiểu dữ liệu
    val cookingStepsRaw = recipeData?.get("cookingSteps")
    android.util.Log.d("UserRecipeStep", "Raw cookingSteps type: ${cookingStepsRaw?.javaClass?.canonicalName}")
    android.util.Log.d("UserRecipeStep", "Raw cookingSteps: $cookingStepsRaw")
    
    val cookingSteps = when (cookingStepsRaw) {
        is List<*> -> {
            android.util.Log.d("UserRecipeStep", "Parsing as List, size: ${cookingStepsRaw.size}")
            cookingStepsRaw.mapIndexedNotNull { index, step ->
                when (step) {
                    is Map<*, *> -> {
                        val stepMap = step as? Map<String, Any>
                        android.util.Log.d("UserRecipeStep", "Step $index: $stepMap")
                        stepMap
                    }
                    else -> {
                        android.util.Log.w("UserRecipeStep", "Step $index is not a Map: ${step?.javaClass?.simpleName}")
                        null
                    }
                }
            }
        }
        is ArrayList<*> -> {
            android.util.Log.d("UserRecipeStep", "Parsing as ArrayList, size: ${cookingStepsRaw.size}")
            cookingStepsRaw.mapIndexedNotNull { index, step ->
                when (step) {
                    is Map<*, *> -> {
                        val stepMap = step as? Map<String, Any>
                        android.util.Log.d("UserRecipeStep", "Step $index: $stepMap")
                        stepMap
                    }
                    else -> {
                        android.util.Log.w("UserRecipeStep", "Step $index is not a Map: ${step?.javaClass?.simpleName}")
                        null
                    }
                }
            }
        }
        else -> {
            android.util.Log.w("UserRecipeStep", "cookingStepsRaw is not List or ArrayList: ${cookingStepsRaw?.javaClass?.canonicalName}")
            emptyList()
        }
    }
    
    // Debug logging
    android.util.Log.d("UserRecipeStep", "=== STEP INFO ===")
    android.util.Log.d("UserRecipeStep", "Recipe ID: $recipeId")
    android.util.Log.d("UserRecipeStep", "Step Index: $stepIndex")
    android.util.Log.d("UserRecipeStep", "Total Steps: ${cookingSteps.size}")
    android.util.Log.d("UserRecipeStep", "Cooking Steps: $cookingSteps")
    
    if (cookingSteps.isEmpty()) {
        android.util.Log.e("UserRecipeStep", "ERROR: No cooking steps found!")
        Toast.makeText(context, "Không tìm thấy bước nấu ăn", Toast.LENGTH_SHORT).show()
        navController.popBackStack()
        return
    }
    
    if (stepIndex < 0 || stepIndex >= cookingSteps.size) {
        android.util.Log.e("UserRecipeStep", "ERROR: Invalid stepIndex! stepIndex=$stepIndex, totalSteps=${cookingSteps.size}")
        Toast.makeText(context, "Bước ${stepIndex + 1} không tồn tại. Tổng số bước: ${cookingSteps.size}", Toast.LENGTH_SHORT).show()
        navController.popBackStack()
        return
    }
    
    val currentStep = cookingSteps[stepIndex]
    val stepDescription = currentStep["description"] as? String ?: ""
    val stepImageUrl = currentStep["imageUrl"] as? String
    val totalSteps = cookingSteps.size
    val isLastStep = stepIndex == totalSteps - 1
    
    // Debug logging
    android.util.Log.d("UserRecipeStep", "Current Step: ${stepIndex + 1} of $totalSteps")
    android.util.Log.d("UserRecipeStep", "Is Last Step: $isLastStep")
    android.util.Log.d("UserRecipeStep", "Step Description: $stepDescription")
    android.util.Log.d("UserRecipeStep", "Step Image URL: $stepImageUrl")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color(0xFF1C1C1E)
                        )
                    }
                },
                actions = {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser?.photoUrl != null) {
                        IconButton(onClick = { /* Profile */ }) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(currentUser.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(50)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF9FAFC))
        ) {
            // Content Column với padding bottom để tránh bị button che
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 100.dp), // Padding để tránh button che nội dung
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Step indicator
                Text(
                    text = "Bước ${String.format("%02d", stepIndex + 1)} của $totalSteps",
                    fontSize = 20.sp,
                    color = Color(0xFF2AB9A7),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Step title - có thể lấy từ step data nếu có, nếu không thì dùng mặc định
                val stepTitle = when (stepIndex) {
                    0 -> "Chuẩn bị nguyên liệu"
                    totalSteps - 1 -> "Hoàn thành"
                    else -> "Nấu ăn"
                }
                
                Text(
                    text = stepTitle,
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Step description
                if (stepDescription.isNotBlank()) {
                    Text(
                        text = stepDescription,
                        fontSize = 15.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Step image - với chiều cao linh hoạt hơn
                if (!stepImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(stepImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Step image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder image if no image URL
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có ảnh",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
            }
            
            // Button cố định ở dưới cùng - LUÔN HIỂN THỊ
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0xFFF9FAFC))
            ) {
                Button(
                    onClick = {
                        android.util.Log.d("UserRecipeStep", "Button clicked: stepIndex=$stepIndex, totalSteps=$totalSteps, isLastStep=$isLastStep")
                        if (isLastStep) {
                            // Chuyển sang màn hình thông tin dinh dưỡng
                            android.util.Log.d("UserRecipeStep", "Navigating to nutrition facts")
                            navController.navigate("user_recipe_nutrition_facts/$recipeId") {
                                // Xóa tất cả các màn hình step trước đó
                                popUpTo("user_recipe_step_${recipeId}_0") { inclusive = true }
                            }
                        } else {
                            // Chuyển sang bước tiếp theo
                            val nextStepIndex = stepIndex + 1
                            android.util.Log.d("UserRecipeStep", "Navigating to next step: $nextStepIndex")
                            navController.navigate("user_recipe_step_${recipeId}_${nextStepIndex}")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(vertical = 16.dp)
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLastStep) "Hoàn thành" else "Tiếp tục",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        if (!isLastStep) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                
                // Bottom spacing để đảm bảo nút không bị che bởi bottom navigation
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

