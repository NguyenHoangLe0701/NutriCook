package com.example.nutricook.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ExerciseSuggestionsScreen(navController: NavController) {
    val exercises = listOf(
        Exercise("Bóng chày", "15 phút", 120, R.drawable.baseball, "Trung bình"),
        Exercise("Bóng rổ", "15 phút", 150, R.drawable.basketball, "Cao"),
        Exercise("Leo núi", "15 phút", 200, R.drawable.mountain, "Cao"),
        Exercise("Đạp xe", "15 phút", 100, R.drawable.cycling, "Trung bình"),
        Exercise("Đá banh", "45 phút", 400, R.drawable.football, "Thấp"),
        Exercise("Chạy bộ", "15 phút", 180, R.drawable.run, "Cao")
    )

    var selectedCalories by remember { mutableStateOf(150) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("← Quay lại")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hoạt động thể thao cho bạn",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.feed), // tạo tạm 1 ảnh trong drawable
                        contentDescription = "Exercise",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bạn muốn đốt cháy bao nhiêu calo?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(100, 150, 200, 300).forEach { calories ->
                            FilterChip(
                                onClick = { selectedCalories = calories },
                                label = { Text("${calories} kcal") },
                                selected = selectedCalories == calories,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF20B2AA),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(
                text = "Bài tập đề xuất",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(exercises.chunked(2)) { rowExercises ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowExercises.forEach { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO: Start exercise */ }
                    )
                }
                if (rowExercises.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = exercise.imageRes),
                contentDescription = exercise.name,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exercise.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exercise.duration,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${exercise.caloriesBurned} kcal",
                fontSize = 10.sp,
                color = Color(0xFF20B2AA)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(
                        when (exercise.difficulty) {
                            "Thấp" -> Color(0xFF4CAF50)
                            "Trung bình" -> Color(0xFFFF9800)
                            "Cao" -> Color(0xFFF44336)
                            else -> Color.Gray
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = exercise.difficulty,
                    fontSize = 8.sp,
                    color = Color.White
                )
            }
        }
    }
}
