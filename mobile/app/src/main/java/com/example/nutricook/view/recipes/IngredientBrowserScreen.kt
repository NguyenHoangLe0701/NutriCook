package com.example.nutricook.view.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.viewmodel.QueryViewModel

@Composable
fun IngredientBrowserScreen(navController: NavController, queryVM: QueryViewModel = hiltViewModel()) {
    val alphabet = ('A'..'Z').toList()
    var selectedLetter by remember { mutableStateOf('A') }

    // Load from Firestore
    val firebaseIngredients by queryVM.ingredients
    val firebaseMealTypes by queryVM.mealTypes
    val firebaseDietTypes by queryVM.dietTypes
    val isLoading by queryVM.isLoading

    LaunchedEffect(Unit) {
        queryVM.loadIngredients()
        queryVM.loadMealTypes()
        queryVM.loadDietTypes()
    }

    val allIngredients = if (firebaseIngredients.isNotEmpty()) {
        // Group ingredients by first letter
        firebaseIngredients.groupBy { 
            (it["letter"] as? String)?.get(0) ?: 'A'
        }.mapValues { (_, items) ->
            items.mapNotNull { it["name"] as? String }
        }
    } else {
        com.example.nutricook.data.SampleData.allIngredients
    }
    val ingredientList = allIngredients[selectedLetter] ?: emptyList()

    val mealTypes = if (firebaseMealTypes.isNotEmpty()) {
        firebaseMealTypes
    } else {
        com.example.nutricook.data.SampleData.mealTypes
    }

    val dietTypes = if (firebaseDietTypes.isNotEmpty()) {
        firebaseDietTypes
    } else {
        com.example.nutricook.data.SampleData.dietTypes
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🔹 Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                    Text(
                        text = "Nguyên liệu & Món ăn",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // 🔸 Thanh A–Z
            item {
                Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                alphabet.forEach { letter ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (letter == selectedLetter) Color(0xFF00BFA5)
                                else Color(0xFFF2F2F2)
                            )
                            .clickable { selectedLetter = letter }
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            text = letter.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (letter == selectedLetter) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        // 🔹 Danh sách nguyên liệu
        item {
            Text(
                text = "Nguyên liệu bắt đầu bằng chữ $selectedLetter",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(ingredientList) { ingredient ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9F9F9))
                    .padding(vertical = 14.dp, horizontal = 20.dp)
            ) {
                Text(
                    text = ingredient,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
            }
        }

        // 🔹 Nhóm loại món ăn
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Loại món ăn",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1A237E)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(mealTypes) { meal ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF9F9F9))
                            .padding(vertical = 18.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = meal,
                            fontSize = 16.sp,
                            color = Color(0xFF212121),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 🔹 Chế độ dinh dưỡng
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Chế độ dinh dưỡng",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1A237E)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dietTypes) { diet ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF9F9F9))
                            .padding(vertical = 18.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diet,
                            fontSize = 16.sp,
                            color = Color(0xFF212121),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
        }
    }
}
