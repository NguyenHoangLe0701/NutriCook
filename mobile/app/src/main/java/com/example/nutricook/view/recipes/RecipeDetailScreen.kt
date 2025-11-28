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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

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

@Composable
fun RecipeDetailScreen(navController: NavController, recipeTitle: String, imageRes: Int) {
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
                    .background(Color(0xFFFFEBD2))
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(bottom = 20.dp)
            ) {
                // Hình nền và nội dung
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 48.dp)) {
                    Text(
                        text = "Salmon Recipes",
                        color = Color(0xFFFF7A00),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "7 Sheet Pan Salmon Recipes for Busy Weeknights",
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray.copy(0.3f))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text("+3 others", color = Color.Gray, fontSize = 13.sp)
                    }
                }

                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-16).dp, y = 12.dp)
                        .clip(RoundedCornerShape(70.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Description Section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Description 🧑‍🍳",
                    color = Color(0xFF2A2D34),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "There's nothing like a one-pan meal to make hectic weeknights (or Sunday meal prep) so much simpler. These healthy salmon and vegetable dinners come together like a dream — just throw everything onto a sheet pan, season, and bake. From simple salmon bakes with roasted asparagus to deceptively easy, restaurant-worthy recipes that'll impress everyone at your table, you'll find a convenient new favorite in this collection of our best sheet pan salmon recipes.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Recipes Section
            Text(
                text = "Recipes 🍽️",
                color = Color(0xFF2A2D34),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val recipes = listOf(
                RecipeItem("Everything Salmon Sheet Pan Dinner", "30 minutes", "4 Servings", "Nicolemmom", R.drawable.sample_food_1),
                RecipeItem("Sheet Pan Lemon Garlic Salmon", "25 minutes", "4 Servings", "Fioa", R.drawable.sample_food_2),
                RecipeItem("Best Salmon Bake", "35 minutes", "4 Servings", "MAGGIE120", R.drawable.sample_food_3),
                RecipeItem("Simple Seafood Sheet Pan Meal", "40 minutes", "8 Servings", "Juliana Hale", R.drawable.sample_food_4)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                recipes.forEach { recipe ->
                    RecipeCardItem(recipe)
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
    val image: Int
)

@Composable
fun RecipeCardItem(recipe: RecipeItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Image(
                painter = painterResource(id = recipe.image),
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(recipe.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${recipe.time}  •  ${recipe.servings}", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(recipe.author, color = Color(0xFF20B2AA), fontSize = 12.sp)
            }
        }
    }
}
