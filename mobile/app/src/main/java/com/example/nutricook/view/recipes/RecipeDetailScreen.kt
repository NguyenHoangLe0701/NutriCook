package com.example.nutricook.view.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

data class Exercise(
    val name: String,
    val duration: String,
    val caloriesBurned: Int,
    val imageRes: Int,
    val difficulty: String
)

data class NutritionItem(
    val name: String,
    val calories: Int,
    val protein: String,
    val fat: String,
    val carbs: String,
    val imageRes: Int
)

data class NotificationItem(
    val title: String,
    val subtitle: String,
    val time: String,
    val imageRes: Int
)

// Data class cho người đã xem
data class MethodGroupViewer(
    val userId: String,
    val userName: String,
    val avatarUrl: String?,
    val viewedAt: com.google.firebase.Timestamp
)

@Composable
fun RecipeDetailScreen(navController: NavController, recipeTitle: String, imageRes: Int) {
    RecipeDetailScreenContent(navController, recipeTitle, null, emptyList(), imageRes)
}

@Composable
fun RecipeDetailScreen(navController: NavController, methodName: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    
    // Load method group data
    val userRecipes = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load và lưu view history
    val viewers = remember { mutableStateOf<List<MethodGroupViewer>>(emptyList()) }
    
    LaunchedEffect(methodName) {
        // Lưu view của user hiện tại
        val currentUser = auth.currentUser
        if (currentUser != null) {
            try {
                val viewerData = hashMapOf(
                    "userId" to currentUser.uid,
                    "userName" to (currentUser.displayName ?: currentUser.email?.split("@")?.firstOrNull() ?: "User"),
                    "avatarUrl" to (currentUser.photoUrl?.toString() ?: ""),
                    "viewedAt" to com.google.firebase.Timestamp.now()
                )
                
                // Lưu vào collection methodGroupViews/{methodName}/viewers/{userId}
                firestore.collection("methodGroupViews")
                    .document(methodName)
                    .collection("viewers")
                    .document(currentUser.uid)
                    .set(viewerData)
                    .await()
            } catch (e: Exception) {
                android.util.Log.e("RecipeDetail", "Error saving view: ${e.message}", e)
            }
        }
        
        // Load danh sách viewers
        try {
            val viewersSnapshot = firestore.collection("methodGroupViews")
                .document(methodName)
                .collection("viewers")
                .orderBy("viewedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            if (currentCoroutineContext().isActive) {
                viewers.value = viewersSnapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        MethodGroupViewer(
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "User",
                            avatarUrl = data["avatarUrl"] as? String,
                            viewedAt = data["viewedAt"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RecipeDetail", "Error loading viewers: ${e.message}", e)
        }
    }
    
    LaunchedEffect(methodName) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("userRecipes")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            if (currentCoroutineContext().isActive) {
                userRecipes.value = snapshot.documents.mapNotNull { doc ->
                    doc.data?.toMutableMap()?.apply {
                        put("docId", doc.id)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("RecipeDetail", "Error loading recipes: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }
    
    // Filter recipes by method
    val methodRecipes = remember(userRecipes.value, methodName) {
        userRecipes.value.mapNotNull { map ->
            try {
                val ingredients = map["ingredients"] as? List<Map<String, Any>> ?: emptyList()
                val hasMethod = ingredients.any { ingredient ->
                    val cookingMethod = ingredient["cookingMethod"] as? String ?: ""
                    cookingMethod == methodName
                }
                
                if (hasMethod) {
                    val imageUrls = map["imageUrls"] as? List<*> ?: emptyList<Any>()
                    val firstImageUrl = imageUrls.firstOrNull() as? String
                    val docId = map["docId"] as? String ?: ""
                    val userEmail = map["userEmail"] as? String ?: ""
                    val createdAt = map["createdAt"] as? com.google.firebase.Timestamp
                    val createdAtStr = createdAt?.toDate()?.let { date ->
                        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(date)
                    }
                    val rating = (map["rating"] as? Number)?.toDouble() ?: 0.0
                    val reviews = (map["reviews"] as? Number)?.toInt() ?: 0
                    
                    MethodGroupRecipe(
                        recipeId = docId,
                        name = map["recipeName"] as? String ?: "",
                        description = map["description"] as? String ?: "",
                        estimatedTime = map["estimatedTime"] as? String ?: "0",
                        servings = map["servings"] as? String ?: "1",
                        imageUrl = firstImageUrl,
                        author = userEmail.split("@").firstOrNull() ?: "Unknown",
                        imageRes = R.drawable.beefandcabbage,
                        userEmail = userEmail,
                        createdAt = createdAtStr,
                        rating = rating,
                        reviews = reviews
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    // Get method group info với viewers thực tế
    val methodGroupInfo = remember(viewers.value, methodRecipes, methodName) {
        when (methodName) {
            "Xào" -> RecipeMethodGroup(
                methodName = "Xào",
                displayName = "Công thức Xào",
                category = "Xào",
                title ="Công thức Xào thơm ngon",
                description = "Khám phá các món xào thơm ngon, đậm đà. Phương pháp xào giữ được hương vị tự nhiên của nguyên liệu, tạo nên những món ăn hấp dẫn và bổ dưỡng.",
                color = Color(0xFFE8F5E9),
                icon = "⚡",
                imageRes = R.drawable.xao,
                userCount = viewers.value.size.coerceAtMost(3),
                additionalUsers = (viewers.value.size - 3).coerceAtLeast(0),
                recipes = methodRecipes
            )
            "Chiên" -> RecipeMethodGroup(
                methodName = "Chiên",
                displayName = "Công thức Chiên",
                category = "Chiên",
                title = "Các món chiên giòn rụm",
                description = "Đang cập nhật",
                color = Color(0xFFFFE5CC),
                icon = "⚡",
                imageRes = R.drawable.chien,
                userCount = viewers.value.size.coerceAtMost(3),
                additionalUsers = (viewers.value.size - 3).coerceAtLeast(0),
                recipes = emptyList(),
                isUpdating = true
            )
            "Hấp" -> RecipeMethodGroup(
                methodName = "Hấp",
                displayName = "Công thức Hấp",
                category = "Hấp",
                title = "Các món hấp thanh đạm",
                description = "Đang cập nhật",
                color = Color(0xFFE3F2FD),
                icon = "⚡",
                imageRes = R.drawable.hap,
                userCount = viewers.value.size.coerceAtMost(3),
                additionalUsers = (viewers.value.size - 3).coerceAtLeast(0),
                recipes = emptyList(),
                isUpdating = true
            )
            "Nướng" -> RecipeMethodGroup(
                methodName = "Nướng",
                displayName = "Công thức Nướng",
                category = "Nướng",
                title = "Các món nướng thơm lừng",
                description = "Đang cập nhật",
                color = Color(0xFFFFEBEE),
                icon = "⚡",
                imageRes = R.drawable.nuong,
                userCount = viewers.value.size.coerceAtMost(3),
                additionalUsers = (viewers.value.size - 3).coerceAtLeast(0),
                recipes = emptyList(),
                isUpdating = true
            )
            else -> null
        }
    }
    
    if (methodGroupInfo != null) {
        RecipeDetailScreenContent(
            navController = navController,
            recipeTitle = methodGroupInfo.displayName,
            methodGroup = methodGroupInfo,
            viewers = viewers.value,
            imageRes = R.drawable.beefandcabbage
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy phương pháp nấu: $methodName")
        }
    }
}

@Composable
private fun RecipeDetailScreenContent(
    navController: NavController,
    recipeTitle: String,
    methodGroup: RecipeMethodGroup?,
    viewers: List<MethodGroupViewer> = emptyList(),
    imageRes: Int
) {
    val scrollState = rememberScrollState()
    var isFavorite by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(Color.White)
                .padding(top = 80.dp)
        ) {
            // 🔹 Header Recipe Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(methodGroup?.color ?: Color(0xFFFFEBD2))
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(bottom = 20.dp)
            ) {
                // Layout với Row để text và hình ảnh không overlap
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Phần text bên trái - có weight để không bị che
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp)
                    ) {
                        // Category với icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡",
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = methodGroup?.category ?: "Salmon Recipes",
                                color = when(methodGroup?.methodName) {
                                    "Xào" -> Color(0xFF1B8A5A)
                                    "Chiên" -> Color(0xFFFF7A00)
                                    "Hấp" -> Color(0xFF2196F3)
                                    "Nướng" -> Color(0xFFE91E63)
                                    else -> Color(0xFFFF7A00)
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = methodGroup?.title ?: "7 Sheet Pan Salmon Recipes for Busy Weeknights",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Mô tả ngắn bên dưới title
                        if (methodGroup?.description != null && methodGroup.description != "Đang cập nhật" && methodGroup.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = methodGroup.description,
                                color = Color.Gray,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        // Hiển thị avatars của người đã xem (dưới mô tả)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        MethodGroupViewersRow(
                            viewers = viewers.take(3),
                            additionalCount = (viewers.size - 3).coerceAtLeast(0)
                        )
                    }

                    // Hình ảnh bên phải (hình tròn như hình)
                    Image(
                        painter = painterResource(id = methodGroup?.imageRes ?: imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Description Section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Description",
                    color = Color(0xFF2A2D34),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = methodGroup?.description ?: "There's nothing like a one-pan meal to make hectic weeknights (or Sunday meal prep) so much simpler. These healthy salmon and vegetable dinners come together like a dream — just throw everything onto a sheet pan, season, and bake. From simple salmon bakes with roasted asparagus to deceptively easy, restaurant-worthy recipes that'll impress everyone at your table, you'll find a convenient new favorite in this collection of our best sheet pan salmon recipes.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Recipes Section
            Text(
                text = "Recipes",
                color = Color(0xFF2A2D34),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val recipes = if (methodGroup != null && methodGroup.recipes.isNotEmpty()) {
                methodGroup.recipes.map { recipe ->
                    RecipeItem(
                        title = recipe.name,
                        time = recipe.estimatedTime,
                        servings = recipe.servings,
                        author = recipe.author,
                        image = recipe.imageRes,
                        imageUrl = recipe.imageUrl,
                        recipeId = recipe.recipeId,
                        userEmail = recipe.userEmail,
                        createdAt = recipe.createdAt,
                        rating = recipe.rating,
                        reviews = recipe.reviews
                    )
                }
            } else if (methodGroup == null) {
                // Fallback data for old route
                listOf(
                    RecipeItem("Everything Salmon Sheet Pan Dinner", "30 minutes", "4 Servings", "Nicolemmom", R.drawable.sample_food_1, null, null),
                    RecipeItem("Sheet Pan Lemon Garlic Salmon", "25 minutes", "4 Servings", "Fioa", R.drawable.sample_food_2, null, null),
                    RecipeItem("Best Salmon Bake", "35 minutes", "4 Servings", "MAGGIE120", R.drawable.sample_food_3, null, null),
                    RecipeItem("Simple Seafood Sheet Pan Meal", "40 minutes", "8 Servings", "Juliana Hale", R.drawable.sample_food_4, null, null)
                )
            } else {
                emptyList()
            }

            if (methodGroup?.isUpdating == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đang cập nhật...",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else if (recipes.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    recipes.forEach { recipe ->
                        RecipeCardItem(recipe, navController)
                    }
                }
            }
        }

        // 🔹 Nút Back và Tim (overlay trên cùng)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 36.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Back
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Nút Tim (Favorite)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.85f), CircleShape)
            ) {
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFF4F4F) else Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

data class RecipeItem(
    val title: String,
    val time: String,
    val servings: String,
    val author: String,
    val image: Int,
    val imageUrl: String? = null,
    val recipeId: String? = null,
    val userEmail: String? = null,
    val createdAt: String? = null,
    val rating: Double = 0.0,
    val reviews: Int = 0
)

@Composable
fun RecipeCardItem(recipe: RecipeItem, navController: NavController? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = navController != null && recipe.recipeId != null) {
                navController?.navigate("user_recipe_info/${recipe.recipeId}")
            },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (recipe.imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = recipe.image),
                    placeholder = painterResource(id = recipe.image)
                )
            } else {
                Image(
                    painter = painterResource(id = recipe.image),
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Người đăng
                if (!recipe.userEmail.isNullOrBlank()) {
                    Text(
                        text = "Người đăng: ${recipe.userEmail}",
                        fontSize = 13.sp,
                        color = Color(0xFF00BFA5),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Đăng ngày
                if (!recipe.createdAt.isNullOrBlank()) {
                    Text(
                        text = "Đăng ngày: ${recipe.createdAt}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
                
                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < recipe.rating.toInt()) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (index < recipe.rating.toInt()) Color(0xFFFFD700) else Color(0xFFE5E7EB)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%.1f", recipe.rating)}/5 (${recipe.reviews} đánh giá)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Composable để hiển thị avatars của người đã xem
@Composable
fun MethodGroupViewersRow(
    viewers: List<MethodGroupViewer>,
    additionalCount: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        viewers.forEachIndexed { index, viewer ->
            // Avatar với border overlap (như hình)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.White, CircleShape)
                    .then(
                        if (index > 0) {
                            Modifier.offset(x = (-12 * index).dp)
                        } else {
                            Modifier
                        }
                    )
                    .zIndex((viewers.size - index).toFloat())
            ) {
                if (!viewer.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(viewer.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = viewer.userName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.avatar_sample),
                        placeholder = painterResource(id = R.drawable.avatar_sample)
                    )
                } else {
                    // Hiển thị chữ cái đầu nếu không có avatar (màu xám nhạt như hình)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewer.userName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Spacing sau avatars
        if (viewers.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // Hiển thị số người còn lại
        if (additionalCount > 0) {
            Text(
                text = "+$additionalCount Khác",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
