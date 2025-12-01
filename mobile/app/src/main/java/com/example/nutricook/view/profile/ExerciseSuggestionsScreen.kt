package com.example.nutricook.view.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R
import kotlin.math.abs

data class Exercise(
    val name: String,
    val duration: String,
    val caloriesBurned: Int,
    val imageRes: Int,
    val difficulty: String
)

@Composable
fun ExerciseSuggestionsScreen(navController: NavController) {
    val allExercises = remember {
        listOf(
            // ~100 kcal exercises
            Exercise("Đạp xe", "15 phút", 100, R.drawable.cycling, "Trung bình"),
            Exercise("Đi bộ nhanh", "20 phút", 100, R.drawable.run, "Thấp"),
            Exercise("Yoga nhẹ", "30 phút", 100, R.drawable.baseball, "Thấp"),
            Exercise("Bơi lội nhẹ", "15 phút", 100, R.drawable.cycling, "Trung bình"),
            
            // ~150 kcal exercises
            Exercise("Bóng rổ", "15 phút", 150, R.drawable.basketball, "Cao"),
            Exercise("Chạy bộ nhẹ", "15 phút", 150, R.drawable.run, "Trung bình"),
            Exercise("Nhảy dây", "15 phút", 150, R.drawable.tenis, "Cao"),
            Exercise("Aerobic", "20 phút", 150, R.drawable.baseball, "Trung bình"),
            
            // ~200 kcal exercises
            Exercise("Leo núi", "15 phút", 200, R.drawable.mountain, "Cao"),
            Exercise("Chạy bộ", "20 phút", 200, R.drawable.run, "Cao"),
            Exercise("Bóng chày", "20 phút", 200, R.drawable.baseball, "Trung bình"),
            Exercise("Quần vợt", "20 phút", 200, R.drawable.tenis, "Cao"),
            Exercise("Bơi lội", "20 phút", 200, R.drawable.cycling, "Cao"),
            
            // ~300 kcal exercises
            Exercise("Chạy bộ cường độ cao", "30 phút", 300, R.drawable.run, "Cao"),
            Exercise("Đạp xe địa hình", "30 phút", 300, R.drawable.cycling, "Cao"),
            Exercise("Bóng đá", "30 phút", 300, R.drawable.football, "Cao"),
            Exercise("Bơi lội cường độ cao", "25 phút", 300, R.drawable.cycling, "Cao"),
            Exercise("HIIT", "25 phút", 300, R.drawable.mountain, "Cao")
        )
    }

    var selectedCalories by remember { mutableStateOf<Int?>(null) } // null = hiển thị tất cả
    var showAll by remember { mutableStateOf(true) } // Mặc định hiển thị tất cả
    
    // Lọc bài tập theo calories đã chọn hoặc hiển thị tất cả
    val filteredExercises = remember(selectedCalories, showAll) {
        if (showAll || selectedCalories == null) {
            // Hiển thị tất cả exercises
            allExercises
        } else {
            // Lọc bài tập theo calories đã chọn (cho phép sai số ±25%)
            val tolerance = (selectedCalories!! * 0.25).toInt() // 25% dung sai
            val minCalories = (selectedCalories!! - tolerance).coerceAtLeast(0)
            val maxCalories = selectedCalories!! + tolerance
            
            val exactMatches = allExercises.filter { exercise ->
                exercise.caloriesBurned in minCalories..maxCalories
            }
            
            if (exactMatches.isNotEmpty()) {
                // Sắp xếp theo độ gần với mục tiêu
                exactMatches.sortedBy { abs(it.caloriesBurned - selectedCalories!!) }
            } else {
                // Nếu không tìm thấy trong khoảng, lấy 4 bài tập gần nhất
                allExercises.sortedBy { abs(it.caloriesBurned - selectedCalories!!) }.take(4)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Header với gradient background ---
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8F9FA),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1C1C1E),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hoạt động thể thao",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E)
                        )
                        Text(
                            text = "Chọn bài tập phù hợp với bạn",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // --- Bộ chọn calo (cVân đối hơn) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Bạn muốn đốt cháy bao nhiêu calo?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Bộ lọc calories - cân đối hơn
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hàng đầu: 100, 150
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(100, 150).forEach { calories ->
                                FilterChip(
                                    onClick = { 
                                        selectedCalories = calories
                                        showAll = false
                                    },
                                    label = { 
                                        Text(
                                            "${calories} kcal",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ) 
                                    },
                                    selected = selectedCalories == calories && !showAll,
                                    enabled = true,
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF20B2AA),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF3F4F6)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedCalories == calories && !showAll,
                                        selectedBorderColor = Color(0xFF20B2AA),
                                        borderColor = Color(0xFFE5E7EB),
                                        selectedBorderWidth = 2.dp,
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }
                        
                        // Hàng thứ hai: 200, 300
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(200, 300).forEach { calories ->
                                FilterChip(
                                    onClick = { 
                                        selectedCalories = calories
                                        showAll = false
                                    },
                                    label = { 
                                        Text(
                                            "${calories} kcal",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ) 
                                    },
                                    selected = selectedCalories == calories && !showAll,
                                    enabled = true,
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF20B2AA),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF3F4F6)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedCalories == calories && !showAll,
                                        selectedBorderColor = Color(0xFF20B2AA),
                                        borderColor = Color(0xFFE5E7EB),
                                        selectedBorderWidth = 2.dp,
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }
                        
                        // Nút "Tất cả"
                        FilterChip(
                            onClick = { 
                                showAll = true
                                selectedCalories = null
                            },
                            label = { 
                                Text(
                                    "Tất cả",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                ) 
                            },
                            selected = showAll,
                            modifier = Modifier.fillMaxWidth(),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF20B2AA),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF3F4F6)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = showAll,
                                selectedBorderColor = Color(0xFF20B2AA),
                                borderColor = Color(0xFFE5E7EB),
                                selectedBorderWidth = 2.dp,
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (showAll) "Tất cả bài tập" else "Bài tập đề xuất",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    if (filteredExercises.isNotEmpty()) {
                        Text(
                            text = "${filteredExercises.size} bài tập",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }

        // --- Danh sách bài tập đã lọc ---
        if (filteredExercises.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "😔",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Không tìm thấy bài tập phù hợp",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Vui lòng chọn mức calories khác",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        } else {
            items(filteredExercises.chunked(2)) { rowExercises ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top // Căn trên để các card cùng hàng có cùng chiều cao
                ) {
                    rowExercises.forEach { exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            modifier = Modifier.weight(1f), // Chiều cao được set trong ExerciseCard
                            onClick = {
                                navController.navigate(
                                    "exercise_detail/${exercise.name}/${exercise.imageRes}/${exercise.duration}/${exercise.caloriesBurned}/${exercise.difficulty}"
                                )
                            }
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
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp) // Chiều cao cố định để tất cả card cân bằng
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Phân bố đều các phần tử
        ) {
            // Icon với nền gradient hoặc màu teal nhạt
            Box(
                modifier = Modifier
                    .size(80.dp) // Giảm từ 100dp xuống 80dp để cân bằng
                    .background(
                        Color(0xFFE0F7FA),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = exercise.imageRes),
                    contentDescription = exercise.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color(0xFF20B2AA))
                )
            }

            // Tên bài tập - cố định chiều cao để cân bằng
            Text(
                text = exercise.name,
                fontSize = 15.sp, // Giảm từ 16sp xuống 15sp
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                textAlign = TextAlign.Center,
                maxLines = 2, // Tối đa 2 dòng
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp, // Chiều cao dòng cố định
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp) // Chiều cao cố định cho 2 dòng
            )

            // Thời gian + kcal
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = exercise.duration,
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "${exercise.caloriesBurned} kcal",
                    fontSize = 12.sp,
                    color = Color(0xFF20B2AA),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Mức độ với design hiện đại hơn
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (exercise.difficulty) {
                    "Thấp" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                    "Trung bình" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                    "Cao" -> Color(0xFFF44336).copy(alpha = 0.1f)
                    else -> Color.Gray.copy(alpha = 0.1f)
                }
            ) {
                Text(
                    text = exercise.difficulty,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (exercise.difficulty) {
                        "Thấp" -> Color(0xFF4CAF50)
                        "Trung bình" -> Color(0xFFFF9800)
                        "Cao" -> Color(0xFFF44336)
                        else -> Color.Gray
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
