package com.example.nutricook.view.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.nutricook.R
import com.example.nutricook.viewmodel.QueryViewModel
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import com.google.firebase.firestore.FirebaseFirestore

// 🧱 Data models
data class RecipeCategory(
    val category: String,
    val color: Color,
    val title: String,
    val imageRes: Int,
    val userCount: Int,
    val additionalUsers: Int
)

data class TodayRecipe(
    val name: String,
    val description: String,
    val rating: Double,
    val imageRes: Int,
    val imageUrl: String? = null, // URL ảnh từ server
    val reviews: Int,
    val userName: String? = null, // Tên người upload
    val createdAt: String? = null, // Ngày upload
    val recipeId: String? = null // ID của recipe trong Firestore (cho user recipes)
)

@Composable
fun RecipeDiscoveryScreen(navController: NavController, queryVM: QueryViewModel = hiltViewModel()) {
    // Load from Firestore
    val firebaseCategories by queryVM.categories
    val firebaseRecipes by queryVM.recipes
    val isLoading by queryVM.isLoading
    val error by queryVM.error

    // Fetch on first load
    LaunchedEffect(Unit) {
        queryVM.loadCategories()
        queryVM.loadRecipes()
    }

    // Convert Map to typed data, fallback to SampleData if empty
    val categories = if (firebaseCategories.isNotEmpty()) {
        firebaseCategories.mapNotNull { map ->
            try {
                RecipeCategory(
                    category = map["category"] as? String ?: "",
                    color = Color(0xFF3AC7BF),
                    title = map["title"] as? String ?: "",
                    imageRes = R.drawable.pho,
                    userCount = (map["userCount"] as? Number)?.toInt() ?: 0,
                    additionalUsers = (map["additionalUsers"] as? Number)?.toInt() ?: 0
                )
            } catch (_: Exception) {
                null
            }
        }
    } else {
        com.example.nutricook.data.SampleData.categories
    }

    // Lấy user recipes từ Firestore (userRecipes collection)
    val userRecipes = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("userRecipes")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            
            // Check if coroutine is still active before updating state
            if (currentCoroutineContext().isActive) {
                userRecipes.value = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.apply {
                        put("docId", doc.id)
                    }
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Re-throw cancellation exceptions to properly cancel the coroutine
            throw e
        } catch (e: Exception) {
            // Only log non-cancellation errors
            if (currentCoroutineContext().isActive) {
                android.util.Log.e("RecipeDiscovery", "Error loading user recipes: ${e.message}", e)
            }
        }
    }
    
    // Lấy món ăn từ Firestore (foodItems collection) - fallback
    val foodItems = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("foodItems")
                .whereEqualTo("available", true)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            
            // Check if coroutine is still active before updating state
            if (currentCoroutineContext().isActive) {
                foodItems.value = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.apply {
                        put("docId", doc.id)
                    }
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Re-throw cancellation exceptions to properly cancel the coroutine
            throw e
        } catch (_: Exception) {
            // Ignore other errors loading food items
        }
    }
    
    val todayRecipes = if (userRecipes.value.isNotEmpty()) {
        // Ưu tiên hiển thị user recipes (recipes do người dùng tạo)
        userRecipes.value.mapIndexedNotNull { index, map ->
            try {
                val imageUrls = map["imageUrls"] as? List<*> ?: emptyList<Any>()
                val firstImageUrl = imageUrls.firstOrNull() as? String
                val docId = map["docId"] as? String
                
                TodayRecipe(
                    name = map["recipeName"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                    imageRes = R.drawable.beefandcabbage,
                    imageUrl = firstImageUrl,
                    reviews = (map["reviewCount"] as? Number)?.toInt() ?: 0,
                    userName = map["userEmail"] as? String,
                    createdAt = (map["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.toString(),
                    recipeId = docId
                )
            } catch (e: Exception) {
                android.util.Log.e("RecipeDiscovery", "Error parsing user recipe: ${e.message}", e)
                null
            }
        }
    } else if (foodItems.value.isNotEmpty()) {
        // Fallback: hiển thị món ăn từ foodItems
        foodItems.value.mapNotNull { map ->
            try {
                val imageUrl = map["imageUrl"] as? String
                val baseUrl = "http://192.168.88.164:8080"
                val fullImageUrl = if (!imageUrl.isNullOrBlank()) {
                    if (imageUrl.startsWith("http")) imageUrl else "$baseUrl$imageUrl"
                } else null
                
                TodayRecipe(
                    name = map["name"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                    imageRes = R.drawable.beefandcabbage,
                    imageUrl = fullImageUrl,
                    reviews = (map["reviews"] as? Number)?.toInt() ?: 0,
                    userName = map["userName"] as? String,
                    createdAt = (map["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.toString()
                )
            } catch (_: Exception) {
                null
            }
        }
    } else if (firebaseRecipes.isNotEmpty()) {
        // Fallback: dùng dữ liệu cũ nếu không có món ăn mới
        firebaseRecipes.mapNotNull { map ->
            try {
                TodayRecipe(
                    name = map["name"] as? String ?: "",
                    description = map["description"] as? String ?: "",
                    rating = (map["rating"] as? Number)?.toDouble() ?: 0.0,
                    imageRes = R.drawable.beefandcabbage,
                    reviews = (map["reviews"] as? Number)?.toInt() ?: 0
                )
            } catch (_: Exception) {
                null
            }
        }
    } else {
        emptyList()
    }

    val listState = rememberLazyListState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $error", color = Color.Red)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 16.dp)
        ) {
            // 🧭 Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Bạn muốn nấu món gì ?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { 
    navController.navigate("ingredient_browser") 
}) {
    Icon(
        Icons.Default.GridView,
        contentDescription = "Ingredient Browser",
        tint = Color(0xFF00BFA5) // tuỳ chọn: cho màu nhất quán
    )
}
            }
        }

        // 🍱 Category Cards
        items(categories) { recipe ->
            RecipeCategoryCard(recipe) {
                // ✅ Khi bấm vào, chuyển sang RecipeDetailScreen
                navController.navigate("recipe_detail/${recipe.title}/${recipe.imageRes}")
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Công thức hôm nay",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Xem tất cả",
                    fontSize = 14.sp,
                    color = Color(0xFF79D7D2),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { }
                )
            }
        }

        // 🍳 Today Recipes
        items(todayRecipes) { recipe ->
            TodayRecipeItem(recipe) {
                // ✅ Khi bấm vào, mở UserRecipeInfoScreen nếu có recipeId, nếu không thì mở RecipeInfoScreen cũ
                if (recipe.recipeId != null) {
                    navController.navigate("user_recipe_info/${recipe.recipeId}")
                } else {
                    navController.navigate("recipe_info/${recipe.name}/${recipe.imageRes}")
                }
            }
        }
        }
    }
}

@Composable
fun RecipeCategoryCard(recipe: RecipeCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = recipe.color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.category,
                    fontSize = 13.sp,
                    color = Color(0xFF1B8A5A),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = recipe.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(recipe.userCount) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(-8.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+${recipe.additionalUsers} Khác",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageRes)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun TodayRecipeItem(recipe: TodayRecipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hiển thị ảnh từ URL nếu có, nếu không thì dùng imageRes
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl ?: recipe.imageRes)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = recipe.imageRes),
                placeholder = painterResource(id = recipe.imageRes)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recipe.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (recipe.description.isNotBlank()) {
                    Text(
                        text = recipe.description,
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Hiển thị tên người upload nếu có
                if (!recipe.userName.isNullOrBlank()) {
                    Text(
                        text = "Người đăng: ${recipe.userName}",
                        fontSize = 11.sp,
                        color = Color(0xFF00BFA5),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Hiển thị ngày upload nếu có
                val formattedDate = remember(recipe.createdAt) {
                    if (!recipe.createdAt.isNullOrBlank()) {
                        try {
                            val date = java.text.SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.US).parse(recipe.createdAt)
                            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date ?: java.util.Date())
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        null
                    }
                }
                
                if (formattedDate != null) {
                    Text(
                        text = "Đăng ngày: $formattedDate",
                        fontSize = 10.sp,
                        color = Color(0xFF999999)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(5) { index ->
                        val starColor =
                            if (index < recipe.rating.toInt()) Color(0xFFFFB300)
                            else Color(0xFFE0E0E0)
                        Icon(
                            painter = painterResource(id = R.drawable.star14),
                            contentDescription = null,
                            tint = starColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${String.format(java.util.Locale.getDefault(), "%.1f", recipe.rating)}/5 (${recipe.reviews} đánh giá)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
