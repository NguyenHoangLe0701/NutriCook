package com.example.nutricook.ui.screens.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState 

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

    val exercises = listOf(
        Exercise("Bóng chày", "15 phút", 120, R.drawable.baseball, "Trung bình"),
        Exercise("Bóng rổ", "15 phút", 150, R.drawable.basketball, "Cao")
    )

    val nutritionItem = NutritionItem(
        name = "Dứa/Thơm",
        calories = 473,
        protein = "20g",
        fat = "24g",
        carbs = "50g",
        imageRes = R.drawable.pizza
    )

    val notifications = listOf(
        NotificationItem("Mdodocook tải công thức mới", "Good New Orleans Creole Gumbo", "vài giây trước", R.drawable.sample_food_1),
        NotificationItem("tcn5 tải công thức mới", "Noodle Casserole", "1 phút trước", R.drawable.sample_food_2)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
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
                text = "Chi tiết công thức",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* TODO: Share */ }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        }

        // Recipe Image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = recipeTitle,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .height(250.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recipe Title
        Text(
            text = recipeTitle,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Recipe Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoChip("30 phút")
            InfoChip("4 người")
            InfoChip("Dễ")
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
    text = "Các món ăn khác",
    fontSize = 18.sp,
    fontWeight = FontWeight.Bold,
    modifier = Modifier
        .padding(horizontal = 16.dp)
        .clickable { navController.navigate("ingredients") } // chuyển hướng
)

Spacer(modifier = Modifier.height(8.dp))

        // Ingredients Section
        SectionCard(title = "Nguyên liệu") {
            val ingredients = when {
                recipeTitle.contains("Phở") -> listOf(
                    "500g bánh phở tươi",
                    "300g thịt bò",
                    "1 củ hành tây",
                    "2 củ gừng",
                    "Hành lá, rau thơm",
                    "Nước mắm, muối, tiêu"
                )
                recipeTitle.contains("cá hồi") -> listOf(
                    "4 miếng cá hồi",
                    "2 củ khoai tây",
                    "1 bó măng tây",
                    "Dầu olive",
                    "Muối, tiêu, tỏi",
                    "Chanh tươi"
                )
                recipeTitle.contains("gà") -> listOf(
                    "1 con gà (1.5kg)",
                    "1 quả dứa",
                    "2 củ hành tây",
                    "Tỏi, gừng",
                    "Nước mắm, đường",
                    "Dầu ăn"
                )
                else -> listOf("Nguyên liệu cơ bản", "Gia vị tùy chọn", "Rau củ tươi")
            }

            ingredients.forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF20B2AA), RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(it, fontSize = 14.sp, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Instructions
        SectionCard(title = "Cách làm") {
            val instructions = when {
                recipeTitle.contains("Phở") -> listOf(
                    "1. Luộc thịt bò với nước lạnh, vớt bọt",
                    "2. Thêm hành tây, gừng vào nồi nước dùng",
                    "3. Nêm nếm gia vị cho vừa ăn",
                    "4. Trần bánh phở qua nước sôi",
                    "5. Xếp thịt bò, hành lá lên trên",
                    "6. Chan nước dùng nóng và thưởng thức"
                )
                recipeTitle.contains("cá hồi") -> listOf(
                    "1. Ướp cá hồi với muối, tiêu, tỏi",
                    "2. Cắt khoai tây thành miếng vừa ăn",
                    "3. Xếp cá và rau củ lên khay nướng",
                    "4. Rưới dầu olive và nướng 20 phút",
                    "5. Vắt chanh lên trước khi ăn"
                )
                else -> listOf(
                    "1. Chuẩn bị nguyên liệu",
                    "2. Chế biến theo hướng dẫn",
                    "3. Nêm nếm gia vị",
                    "4. Hoàn thành và thưởng thức"
                )
            }

            instructions.forEach { Text(it, fontSize = 14.sp, color = Color.Black) }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yêu thích")
            }

            Button(
                onClick = {},
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20B2AA))
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bắt đầu nấu")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Exercise Section
Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        text = "Hoạt động thể thao",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    TextButton(onClick = { navController.navigate("exercise_suggestions") }) {
        Text("Xem thêm →")
    }
}

Spacer(modifier = Modifier.height(8.dp))

Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    exercises.take(2).forEach { // chỉ hiện 2 bài đầu
        ExerciseCard(exercise = it, modifier = Modifier.weight(1f), onClick = {})
    }
}

        Spacer(modifier = Modifier.height(24.dp))

        // Calorie Section
        SectionCard(title = "Đánh giá calo") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFE0F7F5), RoundedCornerShape(50))
                ) {
                    Text(
                        "${nutritionItem.calories}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF12B3AD),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Chất đạm: ${nutritionItem.protein}", fontSize = 12.sp, color = Color.Gray)
                Text("Chất béo: ${nutritionItem.fat}", fontSize = 12.sp, color = Color.Gray)
                Text("Carb: ${nutritionItem.carbs}", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notifications Section
        Text(
            text = "Thông báo đồ ăn",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            notifications.forEach { n ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = n.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(n.title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(n.subtitle, fontSize = 12.sp, color = Color.Gray)
                            Text(n.time, fontSize = 10.sp, color = Color(0xFF9E9E9E))
                        }
                    }
                }
            }
        }
    }
}

// Gộp card section để tái sử dụng
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

// Thêm vào cuối file
// ----------------------

@Composable
fun InfoChip(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF12B3AD).copy(alpha = 0.1f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFF12B3AD),
            fontSize = 13.sp
        )
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(Color.White)
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = exercise.imageRes),
                contentDescription = exercise.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(exercise.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${exercise.duration} • ${exercise.caloriesBurned} cal", fontSize = 12.sp, color = Color.Gray)
            Text("Độ khó: ${exercise.difficulty}", fontSize = 12.sp, color = Color(0xFF12B3AD))
        }
    }
}