package com.example.nutricook.view.categories

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nutricook.data.catalog.Food
import com.example.nutricook.viewmodel.categories.CategoriesViewModel

data class CategoryTab(val name: String, val icon: Int, val color: Color)

@Composable
fun CategoriesScreen(navController: NavController, vm: CategoriesViewModel = viewModel()) {
    val st by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().background(Color.White)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text("Danh mục", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }

        when {
            st.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            st.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Lỗi: ${st.error}", color = Color.Red) }
            else -> {
                // Tabs
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(st.categories.size) { i ->
                        val c = st.categories[i]
                        CategoryTabItem(
                            category = CategoryTab(c.name, c.iconRes, c.color),
                            isSelected = st.selectedIndex == i,
                            onClick = { vm.select(i) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(st.foods) { food ->
                        FoodItemCard(food = food) {
                            // navController.navigate("food_detail/${food.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTabItem(category: CategoryTab, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) category.color.copy(alpha = 0.1f) else Color.Transparent)
                .border(width = if (isSelected) 2.dp else 0.dp, color = category.color, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(id = category.icon), contentDescription = category.name, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = category.name,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) category.color else Color.Gray
        )
    }
}

@Composable
fun FoodItemCard(food: Food, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.8f).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
            Image(painter = painterResource(id = food.imageRes), contentDescription = food.name, modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(food.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 2, lineHeight = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("${food.kcalPer100g.toInt()} kcal / 100g", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
