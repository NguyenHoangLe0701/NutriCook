package com.example.nutricook.view.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * IngredientsFilterScreen:
 * - Dải chữ cái ở trên (A..Z) => chọn chữ cái để filter
 * - Dưới là lưới (2 cột) hiển thị các thẻ nguyên liệu tương ứng
 *
 * Tái sử dụng: có thể truyền danh sách nguyên liệu thực tế từ repository nếu cần.
 */

@Composable
fun IngredientsFilterScreen(
    navController: NavController? = null, // null nếu gọi nội bộ, hoặc truyền NavController nếu cần route
    ingredients: List<String> = sampleIngredients()
) {
    // Dải chữ cái A..Z
    val alphabet = ('A'..'Z').map { it.toString() }
    var selectedLetter by remember { mutableStateOf<String?>(null) } // null = show all
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(vertical = 12.dp)
    ) {
        // Header line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Nguyên liệu", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            navController?.let {
                TextButton(onClick = { it.popBackStack() }) {
                    Text("Đóng")
                }
            }
        }

        // Dải chữ cái
        FlowRowAlpha(alphabet = alphabet, selected = selectedLetter) { letter ->
            selectedLetter = if (selectedLetter == letter) null else letter
        }

        // Ô tìm kiếm
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Tìm nguyên liệu...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lọc nguyên liệu theo ký tự và từ khóa
        val filtered = remember(ingredients, selectedLetter, query) {
            ingredients.filter { ing ->
                val matchesLetter = selectedLetter?.let { ing.uppercase().startsWith(it) } ?: true
                val matchesQuery = query.isBlank() || ing.contains(query, ignoreCase = true)
                matchesLetter && matchesQuery
            }
        }

        // Lưới 2 cột hiển thị nguyên liệu
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { name ->
                IngredientCard(name = name, onClick = {
                    // Có thể điều hướng sang IngredientDetailScreen
                    navController?.navigate("ingredientDetail/$name")
                })
            }
        }
    }
}

@Composable
private fun IngredientCard(name: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = name, modifier = Modifier.padding(horizontal = 8.dp), fontSize = 14.sp)
        }
    }
}

@Composable
private fun FlowRowAlpha(
    alphabet: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // “All” chip
        FilterChip(
            selected = selected == null,
            onClick = { onSelect("") },
            label = { Text("All") }
        )
        alphabet.forEach { letter ->
            FilterChip(
                selected = selected == letter,
                onClick = { onSelect(letter) },
                label = { Text(letter) }
            )
        }
    }
}

/** Dummy sample data - thay bằng nguồn thực khi cần */
private fun sampleIngredients(): List<String> {
    return listOf(
        "Bánh mì tròn", "Đậu hầm", "Bánh chuối nướng", "Thanh Cookies", "Bánh quy Ý giòn",
        "Bánh quy", "Bánh cuộn nhân pho mát", "Bánh Blondies", "Bloody Marys", "Bánh Pie Việt Quất",
        "Súp củ dền", "Bánh mì", "Bữa sáng và nửa buổi", "Bánh Burrito bữa sáng", "Món casserole cho bữa sáng",
        "Khoai tây bữa sáng", "Cà chua", "Dưa leo", "Dứa", "Gừng", "Hành", "Tỏi", "Cá hồi", "Gà",
        "Thịt bò", "Rau cải", "Đậu phụ", "Sữa", "Bơ"
    )
}
