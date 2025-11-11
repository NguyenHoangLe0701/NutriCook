package com.example.nutricook.view.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nutricook.R
import com.example.nutricook.viewmodel.fooddetail.FoodDetailState
import com.example.nutricook.viewmodel.fooddetail.FoodDetailViewModel
import kotlin.math.round

@Composable
fun NutritionDetailScreen(
    navController: NavController,
    foodId: String,
    defaultGrams: Int = 100,
    vm: FoodDetailViewModel = viewModel()
) {
    val st by vm.state.collectAsState()

    LaunchedEffect(foodId) { vm.load(foodId, defaultGrams) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = Color(0xFF1B1B1B)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Thông tin dinh dưỡng",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B)
            )
        }

        when {
            st.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            st.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Lỗi: ${st.error}", color = Color.Red)
            }
            else -> Content(
                st = st,
                onChangeGrams = vm::setGrams,
                onChangeMethod = vm::setMethod
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    st: FoodDetailState,
    onChangeGrams: (Int) -> Unit,
    onChangeMethod: (com.example.nutricook.data.catalog.CookedVariant?) -> Unit
) {
    val food = st.food!!

    Spacer(modifier = Modifier.size(16.dp))

    // Ảnh món + tổng kcal
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = food.imageRes),
            contentDescription = food.name
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${st.kcalTotal}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Calories", color = Color.Gray)
        }
    }

    Spacer(modifier = Modifier.size(16.dp))

    // Dropdown chọn cách chế biến (nếu có trong JSON) — dùng API menuAnchor mới
    val methods = food.cookedVariants
    var expanded by remember { mutableStateOf(false) }
    if (methods.isNotEmpty()) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            TextField(
                readOnly = true,
                value = st.method?.type ?: "Cách chế biến",
                onValueChange = {},
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth(),
                label = { Text("Cách chế biến") }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("— Không áp dụng —") },
                    onClick = {
                        onChangeMethod(null)
                        expanded = false
                    }
                )
                methods.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.type) },
                        onClick = {
                            onChangeMethod(m)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Sửa cảnh báo: không safe-call thừa trên non-null receiver
        val note = st.method?.note
        if (!note.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Ghi chú: $note",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }

    Spacer(modifier = Modifier.size(12.dp))

    // Điều chỉnh grams (stepper)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(onClick = { onChangeGrams((st.grams - 10).coerceAtLeast(0)) }) { Text("-10g") }
        Spacer(Modifier.width(12.dp))
        Text("${st.grams} g", fontWeight = FontWeight.Medium, fontSize = 16.sp)
        Spacer(Modifier.width(12.dp))
        FilledTonalButton(onClick = { onChangeGrams((st.grams + 10).coerceAtMost(2000)) }) { Text("+10g") }
    }

    Spacer(modifier = Modifier.size(20.dp))

    // 3 ô P/F/C — KHÔNG nhân thêm grams lần nữa (VM đã tính)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NutritionBox(
            value = st.fatG?.let { "${round1(it)}g" } ?: "--",
            label = "Chất béo",
            iconRes = R.drawable.fat
        )
        NutritionBox(
            value = st.carbG?.let { "${round1(it)}g" } ?: "--",
            label = "Tinh bột",
            iconRes = R.drawable.finger_cricle
        )
        NutritionBox(
            value = st.proteinG?.let { "${round1(it)}g" } ?: "--",
            label = "Chất đạm",
            iconRes = R.drawable.protein
        )
    }

    Spacer(modifier = Modifier.size(24.dp))

    // Chi tiết
    Text("✨ Thông tin dinh dưỡng", fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.size(8.dp))

    NutritionRow("Món", food.name, R.drawable.flash_circle_nutri)
    NutritionRow("Khẩu phần tham chiếu", "${food.servingSizeGrams ?: 100} g", R.drawable.flash_circle_nutri)
    NutritionRow("Tỉ lệ ăn được", "${((food.ediblePortion ?: 1.0) * 100).toInt()}%", R.drawable.flash_circle_nutri)
    NutritionRow("Năng lượng chuẩn", "${food.kcalPer100g.toInt()} kcal / 100g", R.drawable.flash_circle_nutri)
    NutritionRow("Khối lượng đã chọn", "${st.grams} g", R.drawable.flash_circle_nutri)
    NutritionRow("Tổng năng lượng", "${st.kcalTotal} kcal", R.drawable.flash_circle_nutri)
}

@Composable
private fun NutritionBox(value: String, label: String, iconRes: Int) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .background(Color(0xFFF6F6F6), shape = RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(32.dp)
        )
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

// percent để mặc định "", tránh cảnh báo “always empty”
@Composable
private fun NutritionRow(
    name: String,
    value: String,
    iconRes: Int,
    percent: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = name,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium)
            Text(value, color = Color(0xFF8C8C8C), fontSize = 12.sp)
        }
        if (percent.isNotBlank()) {
            Text(percent, color = Color(0xFF1B1B1B), fontWeight = FontWeight.Bold)
        }
    }
}

private fun round1(x: Double) = round(x * 10) / 10.0
