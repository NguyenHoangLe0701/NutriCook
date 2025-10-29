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
import androidx.compose.material.icons.filled.ArrowBack
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
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.nutricook.R

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
    val reviews: Int
)

@Composable
fun RecipeDiscoveryScreen(navController: NavController) {
    // ✅ Giữ dữ liệu ổn định, không tái tạo liên tục
    val categories = remember {
        listOf(
            RecipeCategory(
                "Vietnamese Food",
                Color(0xFFD6F4E0),
                "Vegetarian Pho (Vietnamese Noodle Soup)",
                R.drawable.pho,
                3,
                5
            ),
            RecipeCategory(
                "Salmon Recipes",
                Color(0xFFFFEBD2),
                "7 Sheet Pan Salmon Recipes for Busy Weeknights",
                R.drawable.supcahoi,
                2,
                3
            ),
            RecipeCategory(
                "Chicken Recipes",
                Color(0xFFE6F1FF),
                "25 Pineapple Chicken Recipes for Sweet and Savory Dinners",
                R.drawable.ga,
                4,
                21
            )
        )
    }

    val todayRecipes = remember {
        listOf(
            TodayRecipe(
                "Slow-Cooker Corned Beef and Cabbage",
                "Cook this in your slow cooker all day and enjoy the tenderness!",
                4.5,
                R.drawable.beefandcabbage,
                1250
            ),
            TodayRecipe(
                "One-Pan White Cheddar Mac and Cheese",
                "If you can make boxed macaroni and cheese, you can make this!",
                4.5,
                R.drawable.macandcheese,
                980
            ),
            TodayRecipe(
                "Simple Macaroni and Cheese",
                "A super satisfying, quick and easy dinner.",
                3.4,
                R.drawable.macaroniandcheese,
                850
            ),
            TodayRecipe(
                "Marie's Easy Slow Cooker Pot Roast",
                "Moist and juicy pot roast with carrots, onion and potatoes.",
                4.6,
                R.drawable.potroast,
                2382
            )
        )
    }

    val listState = rememberLazyListState()

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
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Text(
                    text = "Bạn muốn nấu món gì ?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { /* có thể mở bộ lọc */ }) {
                    Icon(Icons.Default.GridView, contentDescription = "Switch View")
                }
            }
        }

        // 🍱 Category Cards
        items(categories) { recipe ->
            RecipeCategoryCard(recipe) {
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
            TodayRecipeItem(recipe)
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
                        text = "+${recipe.additionalUsers}Khác",
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
fun TodayRecipeItem(recipe: TodayRecipe) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { },
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageRes)
                    .crossfade(true)
                    .build(),
                contentDescription = recipe.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
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

                Text(
                    text = recipe.description,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

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
                        text = "${recipe.rating}/5 (${recipe.reviews})",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
