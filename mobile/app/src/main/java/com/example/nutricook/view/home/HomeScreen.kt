package com.example.nutricook.view.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.FilterList

data class Category(val name: String, val icon: Int)
data class NutritionItem(val name: String, val calories: String, val weight: String, val icon: Int)
data class RecipeSuggestion(val name: String, val image: Int)
data class NewsFeed(val title: String, val image: Int, val category: String, val author: String, val date: String)
data class Exercise(val name: String, val icon: Int)

@Composable
fun HomeScreen(navController: NavController) {
    var isActive by remember { mutableStateOf(true) }

    val categories = listOf(
        Category("Rau củ", R.drawable.vegetable),
        Category("Trái cây", R.drawable.fruit),
        Category("Thịt", R.drawable.meat)
    )
    val recipeSuggestions = listOf(
        RecipeSuggestion("Gà chiên nước mắm", R.drawable.recipe_chicken),
        RecipeSuggestion("Cá hấp bia", R.drawable.recipe_fish)
    )
    val newsFeeds = listOf(
        NewsFeed(
            "You Can Now Try the Hottest Sauce From...",
            R.drawable.news_hambuger,
            "Food News and Trends",
            "Courtney",
            "Sept 13, 2022"
        ),
        NewsFeed(
            "8 5-Ingredient Breakfast Sandwiches for Easy...",
            R.drawable.news_fastfood,
            "Kitchen Tips",
            "Sara Tane",
            "Sept 13, 2022"
        ),
        NewsFeed(
            "How to Make Pizza Chips — TikTok's New It Snack",
            R.drawable.news_pizza,
            "Food News and Trends",
            "Alice Knisley",
            "Sept 22, 2022"
        )
    )
    val exercises = listOf(
        Exercise("Cycling", R.drawable.cycling),
        Exercise("Running", R.drawable.run),
        Exercise("Tennis", R.drawable.tenis),
        Exercise("Baseball", R.drawable.baseball)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top app bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo ứng dụng",
                    modifier = Modifier
                        .width(120.dp)
                        .height(50.dp)
                        .padding(vertical = 4.dp),
                    contentScale = ContentScale.Fit
                )

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

        // Search
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
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Tìm kiếm", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Bạn muốn tìm kiếm gì?",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Lọc",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        // Banner pager
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
                            .clickable { navController.navigate("recipes") },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
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

                            Image(
                                painter = when (page) {
                                    0 -> painterResource(id = R.drawable.food_salmon_nobg)
                                    1 -> painterResource(id = R.drawable.food_chicken_nobg)
                                    2 -> painterResource(id = R.drawable.food_strawberry_nobg)
                                    else -> painterResource(id = R.drawable.food_salmon_nobg)
                                },
                                contentDescription = "Food Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

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
                                    onClick = { navController.navigate("recipes") },
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

                LaunchedEffect(Unit) {
                    while (isActive) {
                        delay(3000)
                        pagerState.animateScrollToPage((pagerState.currentPage + 1) % 3)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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

        // Categories
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Phân loại", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                items(categories) { category ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .width(120.dp)
                            .height(50.dp)
                            .clickable { navController.navigate("categories") }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = category.icon),
                                contentDescription = category.name,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = category.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1B1B1B)
                            )
                        }
                    }
                }
            }
        }

        // Nutrition values
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
                        // ĐI TỚI PICKER để chọn thực phẩm rồi nhảy qua NutritionDetail
                        navController.navigate("nutrition_detail")
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val fruits = listOf(
                FruitNutrition(
                    name = "Dứa/Thơm",
                    kcal = "48 kcal",
                    weight = "100 g",
                    image = R.drawable.pineapple,
                    nutrition = listOf(
                        NutritionData("0.12g", R.drawable.fat, "Fat"),
                        NutritionData("12.63g", R.drawable.finger_cricle, "Carbs"),
                        NutritionData("0.12g", R.drawable.protein, "Protein")
                    )
                ),
                FruitNutrition(
                    name = "Sầu riêng",
                    kcal = "885 kcal",
                    weight = "100 g",
                    image = R.drawable.durian,
                    nutrition = listOf(
                        NutritionData("0.12g", R.drawable.fat, "Fat"),
                        NutritionData("12.63g", R.drawable.finger_cricle, "Carbs"),
                        NutritionData("0.12g", R.drawable.protein, "Protein")
                    )
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                fruits.forEach { fruit ->
                    ExpandableFruitCard(fruit)
                }
            }
        }

        // Recipe suggestions
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
                    modifier = Modifier.clickable { navController.navigate("recipes") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recipeSuggestions) { recipe ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .width(180.dp)
                            .clickable {
                                navController.navigate("recipe_detail/${recipe.name}/${recipe.image}")
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = recipe.image),
                                contentDescription = recipe.name,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = recipe.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1B1B1B),
                                maxLines = 2,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                                        tint = if (index < 4) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text("15 phút", fontSize = 12.sp, color = Color.Gray)
                                Text("2 khẩu phần", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Hot News
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hot News", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Xem tất cả",
                    color = Color(0xFF20B2AA),
                    modifier = Modifier.clickable { navController.navigate("article_detail") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                newsFeeds.forEach { news ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("article_detail") }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = news.image),
                                contentDescription = news.title,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = news.category,
                                    color = Color(0xFF20B2AA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = news.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1B1B1B),
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.news_fastfood),
                                        contentDescription = "Tác giả",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                    )
                                    Text(
                                        text = news.author,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = news.date,
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

        // Activity and Exercise
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
                    Text("Activity and Exercise \uD83C\uDFCB", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                items(exercises) { exercise ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .width(120.dp)
                            .height(140.dp)
                            .clickable { /* TODO */ }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = exercise.icon),
                                contentDescription = exercise.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Fit
                            )
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
    }
}

// Reusable nutrition card
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

data class NutritionData(
    val value: String,
    val iconRes: Int,
    val label: String
)

data class FruitNutrition(
    val name: String,
    val kcal: String,
    val weight: String,
    val image: Int,
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
                Image(
                    painter = painterResource(id = fruit.image),
                    contentDescription = fruit.name,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF8E1)),
                    contentScale = ContentScale.Crop
                )

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
