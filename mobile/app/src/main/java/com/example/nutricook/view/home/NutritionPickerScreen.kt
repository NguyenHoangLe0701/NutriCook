package com.example.nutricook.view.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    var query by remember { mutableStateOf("") }

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

    val filtered = remember(foods, query) {
        if (query.isBlank()) foods
        else foods.filter { it.name.contains(query.trim(), ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
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

        // Search (48dp touch target, tối ưu Pixel 8 Pro)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Tìm tên thực phẩm…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp)
                .heightIn(min = 56.dp)
        )

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Lỗi: $error", color = Color.Red)
            }
            else -> {
                // Dùng LazyColumn, mỗi item là 1 Row chứa tối đa 2 thẻ (2 cột)
                val rows: List<List<Food>> = remember(filtered) { filtered.chunked(2) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(rows) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FoodCell(
                                food = pair[0],
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    navController.navigate(
                                        "nutrition_detail/${pair[0].id}?grams=$defaultGrams"
                                    )
                                }
                            )
                            if (pair.size > 1) {
                                FoodCell(
                                    food = pair[1],
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate(
                                            "nutrition_detail/${pair[1].id}?grams=$defaultGrams"
                                        )
                                    }
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodCell(
    food: Food,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(148.dp)                 // cao hơn cho màn dài (Pixel 8 Pro)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = food.imageRes),
                contentDescription = food.name,
                modifier = Modifier
                    .size(86.dp)              // icon lớn, rõ trên DPI cao
                    .padding(top = 2.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = food.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${food.kcalPer100g.toInt()} kcal / 100g",
                    color = Color(0xFF6B6B6B),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
