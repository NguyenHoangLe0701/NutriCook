package com.example.nutricook.ui.screens.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

data class FoodItem(
    val name: String,
    val calories: String,
    val imageRes: Int
)

data class CategoryTab(
    val name: String,
    val icon: Int,
    val color: Color
)

@Composable
fun CategoriesScreen(navController: NavController) {
    val categories = listOf(
        CategoryTab("Rau củ", R.drawable.salad, Color(0xFF20B2AA)),
        CategoryTab("Trái cây", R.drawable.fruit, Color(0xFFFF8C00)),
        CategoryTab("Hải sản", R.drawable.seafood, Color(0xFFDC143C)),
        CategoryTab("Seafood", R.drawable.seafood, Color(0xFF4169E1))
    )
    
    val vegetables = listOf(
        FoodItem("Cà rốt", "52 kcal", R.drawable.carrot),
        FoodItem("Khoai tây", "104 kcal", R.drawable.potato),
        FoodItem("Nghệ (bột)", "354 kcal", R.drawable.turmeric_powder),
        FoodItem("Cà tím", "24 kcal", R.drawable.eggpalnt),
        FoodItem("Cà chua", "18 kcal", R.drawable.tomato),
        FoodItem("Củ dền", "26 kcal", R.drawable.beetroot),
        FoodItem("Ngô", "132 kcal", R.drawable.corn),
        FoodItem("Ớt chuông", "26 kcal", R.drawable.bell_pepper),
        FoodItem("Hành tây", "42 kcal", R.drawable.onion)
    )
    
    val fruits = listOf(
        FoodItem("Chuối", "89 kcal", R.drawable.banana),
        FoodItem("Bơ", "130 kcal", R.drawable.butter),
        FoodItem("Sung", "74 kcal", R.drawable.figs),
        FoodItem("Anh đào ngọt", "63 kcal", R.drawable.cheries),
        FoodItem("Dừa", "354 kcal", R.drawable.cocunut),
        FoodItem("Nho", "69 kcal", R.drawable.grape),
        FoodItem("Sầu riêng", "885 kcal", R.drawable.durian),
        FoodItem("Thơm", "48 kcal", R.drawable.pineapple),
        FoodItem("Cam", "47 kcal", R.drawable.orange)
    )
    
    val seafood = listOf(
        FoodItem("Tôm", "99 kcal", R.drawable.pizza),
        FoodItem("Cá hồi", "208 kcal", R.drawable.pizza),
        FoodItem("Cua", "97 kcal", R.drawable.pizza),
        FoodItem("Mực", "92 kcal", R.drawable.pizza)
    )
    
    val fishAndSeafood = listOf(
        FoodItem("Cá ngừ", "132 kcal", R.drawable.pizza),
        FoodItem("Cá thu", "205 kcal", R.drawable.pizza),
        FoodItem("Tôm hùm", "89 kcal", R.drawable.pizza),
        FoodItem("Sò điệp", "88 kcal", R.drawable.pizza),
        FoodItem("Cá tuyết", "82 kcal", R.drawable.pizza),
        FoodItem("Hàu", "68 kcal", R.drawable.pizza)
    )
    
    var selectedCategory by remember { mutableStateOf(0) }
    
    val currentFoodList = when (selectedCategory) {
        0 -> vegetables
        1 -> fruits
        2 -> seafood
        3 -> fishAndSeafood
        else -> vegetables
    }

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
                text = "Danh mục",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // Category Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(categories.size) { index ->
                CategoryTabItem(
                    category = categories[index],
                    isSelected = selectedCategory == index,
                    onClick = { selectedCategory = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Food Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentFoodList) { food ->
                FoodItemCard(food = food)
            }
        }
    }
}

@Composable
fun CategoryTabItem(
    category: CategoryTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) category.color.copy(alpha = 0.1f) else Color.Transparent
                )
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = category.color,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = category.icon),
                contentDescription = category.name,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = category.name,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) category.color else Color.Gray
        )
    }
}

@Composable
fun FoodItemCard(food: FoodItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = food.imageRes),
                contentDescription = food.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = food.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = food.calories,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
