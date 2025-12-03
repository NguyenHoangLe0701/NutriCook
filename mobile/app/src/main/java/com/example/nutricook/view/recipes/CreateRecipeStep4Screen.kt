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
import androidx.compose.runtime.collectAsState
import com.example.nutricook.R
import com.example.nutricook.viewmodel.CreateRecipeViewModel
import com.example.nutricook.viewmodel.CategoriesViewModel
import com.example.nutricook.utils.NutritionCalculator
import com.example.nutricook.utils.NutritionData
import com.example.nutricook.data.repository.UserRecipeRepository
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.EntryPointAccessors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeStep4Screen(
    navController: NavController,
    createRecipeViewModel: CreateRecipeViewModel,
    categoriesViewModel: CategoriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val recipeRepository = remember {
        val activity = context as? androidx.activity.ComponentActivity
            ?: throw IllegalStateException("Context is not a ComponentActivity")
        EntryPointAccessors.fromActivity(
            activity,
            RecipeRepositoryEntryPoint::class.java
        ).recipeRepository()
    }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // Lấy dữ liệu từ ViewModel qua StateFlow
    val recipeState by createRecipeViewModel.state.collectAsState()
    val foodItems by categoriesViewModel.foodItems.collectAsState()
    val isEditMode by createRecipeViewModel.isEditMode.collectAsState()
    val editingRecipeId by createRecipeViewModel.editingRecipeId.collectAsState()
    
    // Lấy dữ liệu trực tiếp từ state để đảm bảo luôn cập nhật
    val recipeName = recipeState.recipeName
    val estimatedTime = recipeState.estimatedTime
    val servings = recipeState.servings
    val selectedImageUris = recipeState.selectedImageUris
    // Lọc nguyên liệu hợp lệ: phải có name, quantity và foodItemId
    val ingredients = recipeState.ingredients.filter { 
        it.name.isNotBlank() && 
        it.quantity.isNotBlank() && 
        it.foodItemId != null 
    }
    val cookingSteps = recipeState.cookingSteps.filter { it.description.isNotBlank() } // Lọc bước hợp lệ
    val description = recipeState.description
    val notes = recipeState.notes
    val tips = recipeState.tips
    
    // Debug: Log để kiểm tra dữ liệu
    LaunchedEffect(recipeState) {
        Log.d("Step4", "=== Recipe State Update ===")
        Log.d("Step4", "Recipe State: name='$recipeName', time='$estimatedTime', servings='$servings'")
        Log.d("Step4", "Total ingredients in state: ${recipeState.ingredients.size}")
        Log.d("Step4", "Valid ingredients (after filter): ${ingredients.size}")
        Log.d("Step4", "Steps: ${cookingSteps.size}, Images: ${selectedImageUris.size}")
        recipeState.ingredients.forEachIndexed { index, ing ->
            Log.d("Step4", "  Ingredient[$index]: name='${ing.name}', quantity='${ing.quantity}', foodItemId=${ing.foodItemId}, unit=${ing.unit}")
        }
        ingredients.forEachIndexed { index, ing ->
            Log.d("Step4", "  Valid[$index]: name='${ing.name}', quantity='${ing.quantity}', foodItemId=${ing.foodItemId}, unit=${ing.unit}")
        }
    }
    
    // Sử dụng allFoodItems thay vì foodItems để có tất cả nguyên liệu từ mọi categories
    val allFoodItems by categoriesViewModel.allFoodItems.collectAsState()
    
    // Load tất cả foodItems nếu chưa có
    LaunchedEffect(Unit) {
        if (allFoodItems.isEmpty()) {
            categoriesViewModel.loadAllFoodItems()
        }
    }
    
    // Tính toán dinh dưỡng
    val foodItemsMap = remember(allFoodItems) {
        allFoodItems.associateBy { it.id }
    }
    
    val nutritionData = remember(ingredients, foodItemsMap, servings, allFoodItems) {
        val servingsInt = servings.toIntOrNull() ?: 1
        Log.d("Step4", "=== Calculating Nutrition ===")
        Log.d("Step4", "Ingredients count: ${ingredients.size}")
        Log.d("Step4", "FoodItemsMap size: ${foodItemsMap.size}")
        Log.d("Step4", "AllFoodItems size: ${allFoodItems.size}")
        Log.d("Step4", "Servings: $servingsInt")
        val result = NutritionCalculator.calculateNutrition(ingredients, foodItemsMap, servingsInt)
        Log.d("Step4", "Calculated result: calories=${result.calories}, fat=${result.fat}, carbs=${result.carbs}, protein=${result.protein}")
        result
    }
    
    // Lưu nutritionData vào ViewModel
    LaunchedEffect(nutritionData) {
        createRecipeViewModel.setNutritionData(nutritionData)
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
                            text = "Bước 4: Xem lại & Hoàn thành",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Kiểm tra lại thông tin trước khi đăng",
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
                            text = "4/4",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
                        Text(
                            text = "• Tên món: ${if (recipeName.isNotBlank()) recipeName else "Chưa nhập"}",
                            fontSize = 14.sp,
                            color = if (recipeName.isNotBlank()) Color(0xFF1C1C1E) else Color.Gray
                        )
                        Text(
                            text = "• Thời gian: ${if (estimatedTime.isNotBlank()) "$estimatedTime phút" else "Chưa nhập"}",
                            fontSize = 14.sp,
                            color = if (estimatedTime.isNotBlank()) Color(0xFF1C1C1E) else Color.Gray
                        )
                        Text(
                            text = "• Số phần ăn: ${if (servings.isNotBlank()) servings else "Chưa nhập"}",
                            fontSize = 14.sp,
                            color = if (servings.isNotBlank()) Color(0xFF1C1C1E) else Color.Gray
                        )
                        Text(
                            text = "• Số ảnh: ${selectedImageUris.size}",
                            fontSize = 14.sp,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = "• Số nguyên liệu: ${ingredients.size}",
                            fontSize = 14.sp,
                            color = Color(0xFF1C1C1E)
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
                        Text(
                            text = "• Số bước nấu: ${cookingSteps.size}",
                            fontSize = 14.sp,
                            color = Color(0xFF1C1C1E)
                        )
                        if (cookingSteps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            cookingSteps.forEachIndexed { index, step ->
                                Text(
                                    text = "  ${index + 1}. ${if (step.description.isNotBlank()) step.description else "Chưa có mô tả"}",
                                    fontSize = 13.sp,
                                    color = if (step.description.isNotBlank()) Color(0xFF4B5563) else Color.Gray,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
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
                        Text(
                            text = "• Mô tả: ${if (description.isNotBlank()) description else "Chưa nhập"}",
                            fontSize = 14.sp,
                            color = if (description.isNotBlank()) Color(0xFF1C1C1E) else Color.Gray
                        )
                        Text(
                            text = "• Ghi chú: ${if (notes.isNotBlank()) notes else "Chưa nhập"}",
                            fontSize = 14.sp,
                            color = if (notes.isNotBlank()) Color(0xFF1C1C1E) else Color.Gray
                        )
                        Text(
                            text = "• Mẹo: ${if (tips.isNotBlank()) tips else "Chưa nhập"}",
                            fontSize = 14.sp,
                            color = if (tips.isNotBlank()) Color(0xFF1C1C1E) else Color.Gray
                        )
                    }
                }
            }
        }
        
        /** 🔹 Nút Xem thông tin dinh dưỡng */
        item {
            Button(
                onClick = {
                    // Chuyển sang màn hình thông tin dinh dưỡng với dữ liệu đã tính
                    navController.navigate("nutrition_facts")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp
                )
            ) {
                Text(
                    text = "Xem thông tin dinh dưỡng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        /** 🔹 Nút Đăng công thức */
        item {
            Button(
                onClick = {
                    // Sử dụng dữ liệu trực tiếp từ state
                    val currentState = createRecipeViewModel.state.value
                    val currentRecipeName = currentState.recipeName
                    val currentEstimatedTime = currentState.estimatedTime
                    val currentServings = currentState.servings
                    val currentImageUris = currentState.selectedImageUris
                    val currentIngredients = currentState.ingredients.filter { it.name.isNotBlank() }
                    val currentCookingSteps = currentState.cookingSteps.filter { it.description.isNotBlank() }
                    val currentDescription = currentState.description
                    val currentNotes = currentState.notes
                    val currentTips = currentState.tips
                    
                    Log.d("Step4", "Submit: name='$currentRecipeName', ingredients=${currentIngredients.size}, steps=${currentCookingSteps.size}")
                    
                    if (currentRecipeName.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    if (currentIngredients.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập ít nhất một nguyên liệu", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isSubmitting = true
                    scope.launch {
                        try {
                            // Tính lại nutritionData ngay trước khi lưu để đảm bảo dữ liệu mới nhất
                            val servingsInt = currentServings.toIntOrNull() ?: 1
                            val currentNutritionData = NutritionCalculator.calculateNutrition(
                                currentIngredients,
                                foodItemsMap,
                                servingsInt
                            )
                            
                            // Lưu nutritionData vào ViewModel để đồng bộ
                            createRecipeViewModel.setNutritionData(currentNutritionData)
                            
                            val result = if (isEditMode && editingRecipeId != null) {
                                // Update existing recipe
                                val recipeIdString = editingRecipeId as String
                                // Lấy ảnh cũ từ Firestore nếu không có ảnh mới
                                val existingRecipe = recipeRepository.getRecipeById(recipeIdString)
                                val existingImageUrls = existingRecipe?.get("imageUrls") as? List<*> 
                                    ?: emptyList<Any>()
                                val existingImageUrlsStr = existingImageUrls.mapNotNull { it as? String }
                                
                                recipeRepository.updateRecipe(
                                    recipeId = recipeIdString,
                                    recipeName = currentRecipeName,
                                    estimatedTime = currentEstimatedTime,
                                    servings = currentServings,
                                    imageUris = currentImageUris,
                                    ingredients = currentIngredients,
                                    cookingSteps = currentCookingSteps,
                                    description = currentDescription,
                                    notes = currentNotes,
                                    tips = currentTips,
                                    nutritionData = currentNutritionData,
                                    existingImageUrls = existingImageUrlsStr
                                ).map { recipeIdString }
                            } else {
                                // Create new recipe
                                recipeRepository.saveRecipe(
                                    recipeName = currentRecipeName,
                                    estimatedTime = currentEstimatedTime,
                                    servings = currentServings,
                                    imageUris = currentImageUris,
                                    ingredients = currentIngredients,
                                    cookingSteps = currentCookingSteps,
                                    description = currentDescription,
                                    notes = currentNotes,
                                    tips = currentTips,
                                    nutritionData = currentNutritionData
                                )
                            }
                            
                            result.onSuccess { recipeId ->
                                val message = if (isEditMode) {
                                    "Cập nhật công thức thành công!"
                                } else {
                                    if (currentImageUris.isNotEmpty()) {
                                        "Đăng công thức thành công!"
                                    } else {
                                        "Đăng công thức thành công! (Lưu ý: Ảnh chưa được upload - vui lòng kiểm tra cấu hình Firebase Storage)"
                                    }
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                createRecipeViewModel.clearAll()
                                
                                if (isEditMode) {
                                    // Quay lại màn hình chi tiết công thức
                                    navController.navigate("user_recipe_info/$recipeId") {
                                        popUpTo("create_recipe") { inclusive = true }
                                    }
                                } else {
                                    navController.navigate("recipes") {
                                        popUpTo("create_recipe") { inclusive = true }
                                    }
                                }
                            }.onFailure { error ->
                                val errorMessage = when {
                                    error.message?.contains("404") == true || error.message?.contains("Not Found") == true -> {
                                        "Lỗi: Firebase Storage chưa được cấu hình. Recipe đã được lưu nhưng ảnh chưa được upload. Vui lòng kiểm tra Firebase Console."
                                    }
                                    else -> "Lỗi: ${error.message}"
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp
                ),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = when {
                        isSubmitting && isEditMode -> "Đang cập nhật..."
                        isSubmitting -> "Đang đăng..."
                        isEditMode -> "Cập nhật công thức"
                        else -> "Đăng công thức"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@EntryPoint
@InstallIn(ActivityComponent::class)
interface RecipeRepositoryEntryPoint {
    fun recipeRepository(): UserRecipeRepository
}

