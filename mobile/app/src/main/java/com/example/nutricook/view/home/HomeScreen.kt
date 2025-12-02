package com.example.nutricook.view.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nutricook.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.ImeAction
import com.example.nutricook.viewmodel.hotnews.HotNewsViewModel
import com.example.nutricook.viewmodel.search.SearchViewModel
import com.example.nutricook.model.search.SearchResult
import com.example.nutricook.model.search.SearchType
import com.example.nutricook.view.search.*
import kotlinx.coroutines.delay
import com.example.nutricook.viewmodel.CategoriesViewModel
import com.example.nutricook.viewmodel.CategoryUI
import com.example.nutricook.viewmodel.FoodItemUI
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlin.random.Random

data class Category(val name: String, val icon: Int)
data class NutritionItem(val name: String, val calories: String, val weight: String, val icon: Int)
data class RecipeSuggestion(val name: String, val image: Int)
data class Exercise(val name: String, val icon: Int, val duration: String = "", val caloriesBurned: Int = 0, val difficulty: String = "")

data class NutritionData(
    val value: String,
    val iconRes: Int,
    val label: String
)

data class FruitNutrition(
    val name: String,
    val kcal: String,
    val weight: String,
    val image: Int = R.drawable.pineapple, // Fallback
    val imageUrl: String? = null, // URL từ Firestore
    val nutrition: List<NutritionData>
)

@Composable
fun ExpandableFruitCard(fruit: FruitNutrition) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFBDECEC))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (fruit.imageUrl != null && fruit.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = fruit.imageUrl,
                        contentDescription = fruit.name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF8E1)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = fruit.image),
                        placeholder = painterResource(id = fruit.image)
                    )
                } else {
                    Image(
                        painter = painterResource(id = fruit.image),
                        contentDescription = fruit.name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF8E1)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        fruit.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B)
                    )
                    Text(
                        "${fruit.kcal}    ${fruit.weight}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ✅ Nút mở rộng / thu gọn
                Image(
                    painter = painterResource(
                        id = if (isExpanded) R.drawable.minus_square else R.drawable.add_square
                    ),
                    contentDescription = if (isExpanded) "Thu gọn" else "Mở rộng",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { isExpanded = !isExpanded }
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fruit.nutrition.forEach { nut ->
                            NutritionStatCard(
                                value = nut.value,
                                label = nut.label,
                                iconRes = nut.iconRes,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    hotNewsViewModel: HotNewsViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    categoriesViewModel: CategoriesViewModel = hiltViewModel()
) {
    var isActive by remember { mutableStateOf(true) }
    var showSearchResults by remember { mutableStateOf(false) }
    
    // Load hot news from Firestore
    val hotNewsState by hotNewsViewModel.uiState.collectAsState()
    val searchState by searchViewModel.uiState.collectAsState()
    
    // Load categories from CategoriesViewModel
    val categoriesState by categoriesViewModel.categories.collectAsState()
    val foodItemsState by categoriesViewModel.foodItems.collectAsState()
    
    // Load recent food items (nguyên liệu mới)
    val recentFoodItems = remember { mutableStateOf<List<FoodItemUI>>(emptyList()) }
    
    // Load user recipes
    val userRecipes = remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        hotNewsViewModel.loadHotNews()
        
        // Load recent food items from all categories
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("foodItems")
                .orderBy("id", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            
            if (currentCoroutineContext().isActive) {
                recentFoodItems.value = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        
                        // Parse calories - có thể là string hoặc number
                        val caloriesValue = when {
                            data["calories"] is String -> {
                                val calStr = data["calories"] as String
                                // Nếu đã có "kcal" thì giữ nguyên, nếu không thì thêm "kcal"
                                if (calStr.contains("kcal", ignoreCase = true)) {
                                    calStr
                                } else {
                                    "$calStr kcal"
                                }
                            }
                            data["calories"] is Number -> {
                                "${(data["calories"] as Number).toDouble().toInt()} kcal"
                            }
                            else -> "0 kcal"
                        }
                        
                        FoodItemUI(
                            id = (data["id"] as? Number)?.toLong() ?: 0L,
                            name = data["name"] as? String ?: "",
                            calories = caloriesValue,
                            imageUrl = data["imageUrl"] as? String ?: "",
                            unit = data["unit"] as? String ?: "g",
                            fat = (data["fat"] as? Number)?.toDouble() ?: 0.0,
                            carbs = (data["carbs"] as? Number)?.toDouble() ?: 0.0,
                            protein = (data["protein"] as? Number)?.toDouble() ?: 0.0,
                            cholesterol = (data["cholesterol"] as? Number)?.toDouble() ?: 0.0,
                            sodium = (data["sodium"] as? Number)?.toDouble() ?: 0.0,
                            vitamin = (data["vitamin"] as? Number)?.toDouble() ?: 0.0
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Error parsing food item: ${e.message}", e)
                        null
                    }
                }
            }
        } catch (e: Exception) {
            if (e !is kotlinx.coroutines.CancellationException && currentCoroutineContext().isActive) {
                android.util.Log.e("HomeScreen", "Error loading recent food items: ${e.message}", e)
            }
        }
        
        // Load user recipes
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("userRecipes")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
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
            if (e !is kotlinx.coroutines.CancellationException && currentCoroutineContext().isActive) {
                android.util.Log.e("HomeScreen", "Error loading user recipes: ${e.message}", e)
            }
        }
    }
    
    // Show search results when query is not blank
    LaunchedEffect(searchState.query) {
        showSearchResults = searchState.query.isNotBlank()
    }
    
    // Get first 3 articles for home screen
    val displayedNews = remember(hotNewsState.articles) {
        hotNewsState.articles.take(3)
    }
    
    // Get random 4 recipes from user recipes
    val randomRecipes = remember(userRecipes.value) {
        if (userRecipes.value.isNotEmpty()) {
            userRecipes.value.shuffled().take(4)
        } else {
            emptyList()
        }
    }
    val recipeSuggestions = listOf(
        RecipeSuggestion("Gà chiên nước mắm", R.drawable.recipe_chicken),
        RecipeSuggestion("Cá hấp bia", R.drawable.recipe_fish)
    )
    // Lấy exercises từ ExerciseSuggestionsScreen (đồng bộ) - Tên tiếng Việt
    val exercises = remember {
        listOf(
            Exercise("Chạy bộ", R.drawable.run, "20 phút", 200, "Cao"),
            Exercise("Quần vợt", R.drawable.tenis, "20 phút", 200, "Cao"),
            Exercise("Bóng chày", R.drawable.baseball, "20 phút", 200, "Trung bình"),
            Exercise("Đạp xe", R.drawable.cycling, "15 phút", 100, "Trung bình")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 🔹 Thanh ứng dụng trên cùng
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Logo giữa — luôn nằm chính giữa tuyệt đối
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo ứng dụng",
                    modifier = Modifier
                        .width(130.dp) // 👈 giảm từ 150 xuống 120 cho cân
                        .height(60.dp)
                        .padding(vertical = 4.dp),
                    contentScale = ContentScale.Fit
                )

                // Hai icon trái phải
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigate("create_recipe") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_notification_status),
                            contentDescription = "Chỉnh sửa",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { navController.navigate("notifications") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_noti),
                            contentDescription = "Thông báo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }


        // 🔹 Thanh tìm kiếm
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Tìm kiếm",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    TextField(
                        value = searchState.query,
                        onValueChange = searchViewModel::onQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Tìm kiếm nguyên liệu...",
                                color = Color.Gray
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { /* Search is handled by onValueChange with debounce */ }
                        )
                    )
                    
                    if (searchState.query.isNotBlank()) {
                        IconButton(onClick = { searchViewModel.clearSearch() }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Xóa",
                                tint = Color.Gray
                            )
                        }
                    }
                    
                    IconButton(onClick = { searchViewModel.toggleFilters() }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            tint = if (searchState.showFilters) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
        
        // 🔹 Filter Chips (when filters are shown)
        if (searchState.showFilters) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Loại tìm kiếm",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = searchState.selectedTypes.contains(SearchType.RECIPES),
                            onClick = { searchViewModel.toggleSearchType(SearchType.RECIPES) },
                            label = { Text("Công thức") },
                            leadingIcon = if (searchState.selectedTypes.contains(SearchType.RECIPES)) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                        
                        FilterChip(
                            selected = searchState.selectedTypes.contains(SearchType.FOODS),
                            onClick = { searchViewModel.toggleSearchType(SearchType.FOODS) },
                            label = { Text("Thực phẩm") },
                            leadingIcon = if (searchState.selectedTypes.contains(SearchType.FOODS)) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                        
                        FilterChip(
                            selected = searchState.selectedTypes.contains(SearchType.NEWS),
                            onClick = { searchViewModel.toggleSearchType(SearchType.NEWS) },
                            label = { Text("Tin tức") },
                            leadingIcon = if (searchState.selectedTypes.contains(SearchType.NEWS)) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }
        }
        
        // 🔹 Search Results (when query is not blank)
        if (showSearchResults && searchState.query.isNotBlank()) {
            if (searchState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (searchState.error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = searchState.error ?: "Lỗi không xác định",
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                val allResultsEmpty = searchState.results.values.all { it.isEmpty() }
                
                if (allResultsEmpty) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Không tìm thấy kết quả",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    // Recipes
                    if (searchState.results[SearchType.RECIPES]?.isNotEmpty() == true) {
                        item {
                            Text(
                                text = "Công thức nấu ăn",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(searchState.results[SearchType.RECIPES]!!) { result ->
                            if (result is SearchResult.RecipeResult) {
                                RecipeResultItem(
                                    result,
                                    onClick = { navController.navigate("user_recipe_info/${result.id}") }
                                )
                            }
                        }
                    }
                    
                    // Foods
                    if (searchState.results[SearchType.FOODS]?.isNotEmpty() == true) {
                        item {
                            Text(
                                text = "Thực phẩm",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(searchState.results[SearchType.FOODS]!!) { result ->
                            if (result is SearchResult.FoodResult) {
                                FoodResultItem(
                                    result,
                                    onClick = { navController.navigate("add_meal") }
                                )
                            }
                        }
                    }
                    
                    // News
                    if (searchState.results[SearchType.NEWS]?.isNotEmpty() == true) {
                        item {
                            Text(
                                text = "Tin tức",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(searchState.results[SearchType.NEWS]!!) { result ->
                            if (result is SearchResult.NewsResult) {
                                NewsResultItem(
                                    result,
                                    onClick = { navController.navigate("article_detail") }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 🔹 Nội dung bình thường (khi không search)
        if (!showSearchResults || searchState.query.isBlank()) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pagerState = rememberPagerState(pageCount = { 3 })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) { page ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { navController.navigate("recipe_discovery") },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 🔹 Background màu
                            Image(
                                painter = when (page) {
                                    0 -> painterResource(id = R.drawable.bg_banner_blue)
                                    1 -> painterResource(id = R.drawable.bg_banner_orange)
                                    2 -> painterResource(id = R.drawable.bg_banner_pink)
                                    else -> painterResource(id = R.drawable.bg_banner_blue)
                                },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // 🔹 Foreground xóa phông full size
                            Image(
                                painter = when (page) {
                                    0 -> painterResource(id = R.drawable.food_salmon_nobg)
                                    1 -> painterResource(id = R.drawable.food_chicken_nobg)
                                    2 -> painterResource(id = R.drawable.food_strawberry_nobg)
                                    else -> painterResource(id = R.drawable.food_salmon_nobg)
                                },
                                contentDescription = "Food Image",
                                contentScale = ContentScale.Crop, // full size
                                modifier = Modifier.fillMaxSize()
                            )

                            // 🔹 Text + Button
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 20.dp)
                            ) {
                                Text(
                                    text = when (page) {
                                        0 -> "10 Top-Rated Salmon Recipes Inspired"
                                        1 -> "Easy Chicken Meals for Busy Days"
                                        2 -> "Fresh Strawberry Desserts to Try"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )

                                Text(
                                    text = when (page) {
                                        0 -> "by the Mediterranean style..."
                                        1 -> "Simple, healthy, and flavorful..."
                                        2 -> "Sweet and refreshing ideas..."
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.DarkGray
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Button(
                                    onClick = { navController.navigate("recipe_discovery") },
                                    modifier = Modifier.padding(top = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF333333)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Read more", color = Color.White)
                                }
                            }
                        }
                    }
                }

                // 🔁 Tự động chuyển banner
                LaunchedEffect(Unit) {
                    while (isActive) {
                        delay(3000)
                        pagerState.animateScrollToPage((pagerState.currentPage + 1) % 3)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 Dot indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == pagerState.currentPage) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == pagerState.currentPage)
                                        Color(0xFF20B2AA)
                                    else
                                        Color(0xFFE0E0E0)
                                )
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }

        // 🔹 Nguyên liệu (từ categories)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Nguyên liệu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Xem tất cả",
                    color = Color(0xFF20B2AA),
                    modifier = Modifier.clickable { navController.navigate("categories") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categoriesState) { category ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .width(120.dp)
                            .height(50.dp)
                            .clickable { 
                                categoriesViewModel.selectCategory(category.id)
                                navController.navigate("categories")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = category.icon,
                                fontSize = 24.sp
                            )
                            Text(
                                text = category.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1B1B1B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 🔹 Giá trị dinh dưỡng (từ nguyên liệu mới)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Giá trị dinh dưỡng", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Xem tất cả",
                    color = Color(0xFF20B2AA),
                    modifier = Modifier.clickable {
                        navController.navigate("categories")
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hiển thị các nguyên liệu mới được thêm
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                recentFoodItems.value.take(3).forEach { foodItem ->
                    // Parse calories để lấy số (bỏ "kcal" nếu có)
                    val caloriesNumber = foodItem.calories
                        .replace("kcal", "", ignoreCase = true)
                        .replace(" ", "")
                        .toDoubleOrNull() ?: 0.0
                    
                    ExpandableFruitCard(
                        fruit = FruitNutrition(
                            name = foodItem.name,
                            kcal = foodItem.calories, // Giữ nguyên format từ database (có thể là "100 kcal" hoặc "100")
                            weight = "100 ${foodItem.unit}", // Sử dụng unit từ database
                            image = R.drawable.pineapple, // Fallback image
                            imageUrl = foodItem.imageUrl, // URL từ Firestore
                            nutrition = listOf(
                                NutritionData("${String.format("%.2f", foodItem.fat)}g", R.drawable.fat, "Fat"),
                                NutritionData("${String.format("%.2f", foodItem.carbs)}g", R.drawable.finger_cricle, "Carbs"),
                                NutritionData("${String.format("%.2f", foodItem.protein)}g", R.drawable.protein, "Protein")
                            )
                        )
                    )
                }
            }
        }

        // 🔹 Gợi ý món ăn (từ user recipes, random 4 món)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gợi ý món ăn", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Xem tất cả",
                    color = Color(0xFF20B2AA),
                    modifier = Modifier.clickable { navController.navigate("recipe_discovery") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (randomRecipes.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(randomRecipes) { recipe ->
                        val recipeName = recipe["recipeName"] as? String ?: "Món ăn"
                        val imageUrl = recipe["imageUrls"] as? List<String> ?: emptyList()
                        val firstImage = imageUrl.firstOrNull() ?: ""
                        val estimatedTime = recipe["estimatedTime"] as? String ?: "15 phút"
                        val servings = recipe["servings"] as? String ?: "2"
                        val rating = (recipe["rating"] as? Number)?.toDouble() ?: 0.0
                        val docId = recipe["docId"] as? String ?: ""
                        
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier
                                .width(180.dp)
                                .clickable { 
                                    if (docId.isNotEmpty()) {
                                        navController.navigate("user_recipe_info/$docId")
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (firstImage.isNotEmpty()) {
                                    AsyncImage(
                                        model = firstImage,
                                        contentDescription = recipeName,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        error = painterResource(id = R.drawable.pizza),
                                        placeholder = painterResource(id = R.drawable.pizza)
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.pizza),
                                        contentDescription = recipeName,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = recipeName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1B1B1B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Sao",
                                            tint = if (index < rating.toInt()) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        text = if (estimatedTime.contains("phút") || estimatedTime.contains("giờ")) estimatedTime else "$estimatedTime phút",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text("$servings khẩu phần", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔹 Tin mới (Hot News) - Đổi thành tiếng Việt
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tin tức nổi bật", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Xem tất cả",
                    color = Color(0xFF20B2AA),
                    modifier = Modifier.clickable { navController.navigate("all_hot_news") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hotNewsState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (displayedNews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có bài đăng nào",
                        color = Color.Gray
                    )
                }
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    displayedNews.forEach { article ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    navController.navigate("hot_news_detail/${article.id}")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = article.thumbnailUrl ?: "",
                                    contentDescription = article.title,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.news_fastfood)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = article.getTranslatedCategory(),
                                        color = Color(0xFF20B2AA),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = article.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1B1B1B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AsyncImage(
                                            model = article.author.avatarUrl ?: "",
                                            contentDescription = "Tác giả",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop,
                                            error = painterResource(id = R.drawable.news_fastfood)
                                        )
                                        Text(
                                            text = article.author.displayName ?: article.author.email ?: "Anonymous",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = article.getFormattedDate(),
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔹 Activity and Exercise - Đổi thành tiếng Việt
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Hoạt động và tập luyện \uD83C\uDFCB", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Xem tất cả",
                    color = Color(0xFF20B2AA),
                    modifier = Modifier.clickable {
                        navController.navigate("exercise_suggestions")
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(exercises.take(3)) { exercise ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .width(120.dp)
                            .height(140.dp)
                            .clickable { 
                                // Navigate to exercise detail screen với thông tin đầy đủ
                                if (exercise.duration.isNotEmpty() && exercise.caloriesBurned > 0) {
                                    navController.navigate(
                                        "exercise_detail/${exercise.name}/${exercise.icon}/${exercise.duration}/${exercise.caloriesBurned}/${exercise.difficulty}"
                                    )
                                } else {
                                    navController.navigate("exercise_suggestions")
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Icon với nền trắng, icon màu teal (ngược lại)
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(2.dp, Color(0xFF20B2AA), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = exercise.icon),
                                    contentDescription = exercise.name,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(Color(0xFF20B2AA))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = exercise.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1B1B1B),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
        } // Close if (!showSearchResults || searchState.query.isBlank())
    }
}

// 🔹 Component nhỏ cho từng ô dinh dưỡng
@Composable
fun NutritionStatCard(
    value: String,
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEFEF)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(90.dp)
            .width(100.dp)
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "Biểu tượng dinh dưỡng",
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B1B1B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF6B6B6B)
            )
        }
    }
}
