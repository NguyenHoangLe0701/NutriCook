package com.example.nutricook.view.recipes

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.R
import com.example.nutricook.data.repository.CategoryFirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRecipeInfoScreen(
    navController: NavController,
    recipeId: String
) {
    val context = LocalContext.current
    var recipeData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(recipeId) {
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("userRecipes")
                .document(recipeId)
                .get()
                .await()
            
            if (doc.exists()) {
                recipeData = doc.data
            } else {
                error = "Không tìm thấy công thức"
            }
        } catch (e: Exception) {
            error = "Lỗi: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Scaffold(
        containerColor = Color(0xFFF9FAFC),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Favorite action */ }) {
                        Icon(
                            Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Lỗi không xác định",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }
            recipeData != null -> {
                val recipe = recipeData!!
                val recipeName = recipe["recipeName"] as? String ?: "Unknown"
                val userEmail = recipe["userEmail"] as? String ?: "Unknown"
                val estimatedTime = recipe["estimatedTime"] as? String ?: "0"
                val servings = recipe["servings"] as? String ?: "1"
                val imageUrls = recipe["imageUrls"] as? List<*> ?: emptyList<Any>()
                val firstImageUrl = imageUrls.firstOrNull() as? String
                val ingredients = recipe["ingredients"] as? List<Map<String, Any>> ?: emptyList()
                val cookingSteps = recipe["cookingSteps"] as? List<Map<String, Any>> ?: emptyList()
                val rating = (recipe["rating"] as? Number)?.toDouble() ?: 0.0
                val reviewCount = (recipe["reviewCount"] as? Number)?.toInt() ?: 0
                val description = recipe["description"] as? String ?: ""
                val notes = recipe["notes"] as? String ?: ""
                val tips = recipe["tips"] as? String ?: ""
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color.White),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recipeName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF222222),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Text(
                            text = "By ${userEmail.split("@").firstOrNull() ?: "Unknown"}",
                            fontSize = 15.sp,
                            color = Color(0xFF12B3AD),
                            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$estimatedTime", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Thời gian", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$servings phần", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Khẩu phần", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (index < rating.toInt()) Color(0xFFFFB300) else Color(0xFFE0E0E0)
                                        )
                                    }
                                }
                                Text(
                                    text = if (reviewCount > 0) "${String.format("%.1f", rating)}/5 ($reviewCount đánh giá)" else "Chưa có đánh giá",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Hiển thị ảnh đầu tiên
                        if (!firstImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(firstImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = recipeName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                error = painterResource(id = R.drawable.beefandcabbage),
                                placeholder = painterResource(id = R.drawable.beefandcabbage)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.beefandcabbage),
                                contentDescription = recipeName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Nguyên liệu (${ingredients.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    items(ingredients) { ingredient ->
                        val name = ingredient["name"] as? String ?: ""
                        val quantity = ingredient["quantity"] as? String ?: ""
                        val unit = ingredient["unit"] as? String ?: ""
                        val foodItemId = (ingredient["foodItemId"] as? Number)?.toLong()
                        UserIngredientItem(
                            name = name,
                            quantity = "$quantity $unit",
                            foodItemId = foodItemId
                        )
                    }

                    // Hiển thị Mô tả, Ghi chú, Mẹo nấu ăn
                    if (description.isNotBlank() || notes.isNotBlank() || tips.isNotBlank()) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Mô tả
                            if (description.isNotBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                ) {
                                    Text(
                                        text = "📝 Mô tả",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B1B1B),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = description,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4B5563),
                                        lineHeight = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            // Ghi chú
                            if (notes.isNotBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                ) {
                                    Text(
                                        text = "📌 Ghi chú",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B1B1B),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = notes,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4B5563),
                                        lineHeight = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            // Mẹo nấu ăn
                            if (tips.isNotBlank()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                ) {
                                    Text(
                                        text = "💡 Mẹo nấu ăn",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B1B1B),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = tips,
                                        fontSize = 14.sp,
                                        color = Color(0xFF4B5563),
                                        lineHeight = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                // Navigate to first step
                                if (cookingSteps.isNotEmpty()) {
                                    navController.navigate("user_recipe_step_${recipeId}_0")
                                } else {
                                    Toast.makeText(context, "Công thức chưa có bước nấu ăn", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Bắt đầu nấu",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_next_arrow),
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun UserIngredientItem(
    name: String,
    quantity: String,
    foodItemId: Long? = null
) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }
    
    // Lấy hình ảnh từ foodItem nếu có foodItemId
    LaunchedEffect(foodItemId) {
        if (foodItemId != null) {
            isLoadingImage = true
            try {
                val repository = CategoryFirestoreRepository(
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                )
                val foodItem = repository.getFoodById(foodItemId)
                imageUrl = foodItem?.imageUrl
            } catch (e: Exception) {
                android.util.Log.e("UserIngredientItem", "Error loading food item image: ${e.message}", e)
            } finally {
                isLoadingImage = false
            }
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hiển thị hình ảnh từ foodItem nếu có, nếu không thì hiển thị chữ cái đầu
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                error = painterResource(id = R.drawable.beefandcabbage),
                placeholder = painterResource(id = R.drawable.beefandcabbage)
            )
        } else {
            // Placeholder icon - hiển thị chữ cái đầu nếu không có ảnh
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = name.take(1).uppercase(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = quantity,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

