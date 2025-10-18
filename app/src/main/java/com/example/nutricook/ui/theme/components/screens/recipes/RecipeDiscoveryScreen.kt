package com.example.nutricook.ui.screens.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

data class RecipeCard(
    val category: String,
    val categoryColor: Color,
    val title: String,
    val imageRes: Int,
    val userCount: Int,
    val additionalUsers: Int
)

@Composable
fun RecipeDiscoveryScreen(navController: NavController) {
    val recipes = listOf(
        RecipeCard(
            category = "Món ăn Việt Nam",
            categoryColor = Color(0xFF4CAF50),
            title = "Phở bò (mì nước Việt Nam)",
            imageRes = R.drawable.pizza,
            userCount = 3,
            additionalUsers = 5
        ),
        RecipeCard(
            category = "Công thức cá hồi",
            categoryColor = Color(0xFFFF8C00),
            title = "7 công thức cá hồi nướng khay cho những buổi tối bận rộn",
            imageRes = R.drawable.pizza,
            userCount = 2,
            additionalUsers = 3
        ),
        RecipeCard(
            category = "Công thức gà",
            categoryColor = Color(0xFF2196F3),
            title = "25 công thức gà nấu với dứa cho bữa tối ngọt ngào và đậm đà",
            imageRes = R.drawable.pizza,
            userCount = 4,
            additionalUsers = 21
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Bạn muốn nấu món gì?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* TODO: Grid/List toggle */ }) {
                Icon(Icons.Default.GridView, contentDescription = "Grid View")
            }
        }

        // Recipe Cards
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCardItem(
                    recipe = recipe,
                    onClick = { navController.navigate("recipe_detail/${recipe.title}") }
                )
            }
        }
    }
}

@Composable
fun RecipeCardItem(
    recipe: RecipeCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Category Label
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(recipe.categoryColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = recipe.category,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Recipe Title
                Text(
                    text = recipe.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // User Engagement
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // User avatars
                    repeat(recipe.userCount) { index ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    Color(0xFFE0E0E0),
                                    CircleShape
                                )
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Color(0xFF9E9E9E),
                                        CircleShape
                                    )
                            )
                        }
                        if (index < recipe.userCount - 1) {
                            Spacer(modifier = Modifier.width(-8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "+${recipe.additionalUsers} người khác",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Recipe Image
            Image(
                painter = painterResource(id = recipe.imageRes),
                contentDescription = recipe.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
        }
    }
}
