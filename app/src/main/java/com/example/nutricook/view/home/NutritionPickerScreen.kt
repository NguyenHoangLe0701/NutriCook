package com.example.nutricook.view.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nutricook.R
import com.example.nutricook.data.catalog.AssetCatalogRepository
import com.example.nutricook.data.catalog.Food

@Composable
fun NutritionPickerScreen(
    navController: NavController,
    defaultGrams: Int = 100
) {
    val ctx = LocalContext.current
    val repo = remember { AssetCatalogRepository(ctx.applicationContext) }

    var foods by remember { mutableStateOf<List<Food>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            val list = repo.getAllFoods()
            foods = list.sortedBy { it.name.lowercase() }
            loading = false
        } catch (e: Exception) {
            error = e.message ?: "Không thể tải danh sách thực phẩm"
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color(0xFF1B1B1B)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Chọn thực phẩm",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1B1B1B)
            )
        }

        Spacer(Modifier.height(12.dp))

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Lỗi: $error", color = Color.Red)
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(foods, key = { it.id }) { food ->
                        FoodCell(
                            name = food.name,
                            kcalPer100g = food.kcalPer100g,
                            imageRes = food.imageRes,
                            onClick = {
                                navController.navigate(
                                    "nutrition_detail/${food.id}?grams=$defaultGrams"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodCell(
    name: String,
    kcalPer100g: Double,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier
                    .size(70.dp)
                    .padding(top = 4.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${kcalPer100g.toInt()} kcal / 100g",
                    color = Color.Gray
                )
            }
        }
    }
}
