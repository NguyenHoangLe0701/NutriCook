package com.example.nutricook.view.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions          // ✅ ĐÚNG
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.nutricook.R
import com.example.nutricook.data.catalog.CookedVariant
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
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp)
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
    onChangeMethod: (CookedVariant?) -> Unit
) {
    val food = st.food!!
    val focus = LocalFocusManager.current

    // ✅ Tính /100g sau chế biến chỉ dựa vào lossFactor & extraFatGPer100g
    val loss = (st.method?.lossFactor ?: 1.0).coerceIn(0.0, 1.0)
    val extraFatPer100 = (st.method?.extraFatGPer100g ?: 0.0).coerceAtLeast(0.0)
    val cookedKcalPer100 = food.kcalPer100g * loss + extraFatPer100 * 9.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Ảnh + tổng kcal
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = food.imageRes),
                        contentDescription = food.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Column(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("${st.kcalTotal}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Calories", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Chọn cách chế biến (đẹp hơn)
        if (food.cookedVariants.isNotEmpty()) {
            item {
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = st.method?.type ?: "Cách chế biến",
                        onValueChange = {},
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        label = { Text("Cách chế biến") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        supportingText = {
                            val note = st.method?.note
                            if (!note.isNullOrBlank()) Text(note)
                        }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("— Không áp dụng —") },
                            onClick = { onChangeMethod(null); expanded = false }
                        )
                        food.cookedVariants.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(m.type, fontWeight = FontWeight.SemiBold)
                                        val info = buildString {
                                            append("Loss×${round1(m.lossFactor * 100)}%  ")
                                            if (m.extraFatGPer100g > 0) append("+${round1(m.extraFatGPer100g)}g dầu/100g")
                                        }
                                        if (info.isNotBlank()) Text(info, fontSize = 12.sp, color = Color.Gray)
                                        if (!m.note.isNullOrBlank())
                                            Text(m.note!!, fontSize = 12.sp, color = Color(0xFF666666))
                                    }
                                },
                                onClick = { onChangeMethod(m); expanded = false }
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        // Nhập trực tiếp số gram + nút ±
        item {
            var gramsText by remember(st.grams) { mutableStateOf(st.grams.toString()) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                FilledTonalButton(onClick = {
                    val v = (st.grams - 10).coerceAtLeast(0)
                    onChangeGrams(v); gramsText = v.toString()
                }) { Text("-10g") }

                Spacer(Modifier.width(12.dp))

                OutlinedTextField(
                    value = gramsText,
                    onValueChange = { new ->
                        val onlyDigits = new.filter { it.isDigit() }
                        gramsText = onlyDigits.take(4)
                        val parsed = gramsText.toIntOrNull()
                        if (parsed != null) onChangeGrams(parsed.coerceIn(0, 2000))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    label = { Text("Gram") },
                    supportingText = { Text("0 – 2000 g") },
                    modifier = Modifier.width(140.dp),
                    trailingIcon = { Text("g") }
                )

                Spacer(Modifier.width(12.dp))

                FilledTonalButton(onClick = {
                    val v = (st.grams + 10).coerceAtMost(2000)
                    onChangeGrams(v); gramsText = v.toString()
                    focus.clearFocus()
                }) { Text("+10g") }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // 3 ô P/F/C
        item {
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
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Chi tiết + so sánh /100g
        item {
            Text("✨ Thông tin dinh dưỡng", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            NutritionRow("Món", food.name, R.drawable.flash_circle_nutri)
            NutritionRow("Khối lượng đã chọn", "${st.grams} g", R.drawable.flash_circle_nutri)

            // Hai dòng /100g rõ ràng để tránh hiểu nhầm
            NutritionRow("Năng lượng chuẩn /100g", "${food.kcalPer100g.toInt()} kcal", R.drawable.flash_circle_nutri)
            NutritionRow("Ước tính sau chế biến /100g", "${cookedKcalPer100.roundToInt()} kcal", R.drawable.flash_circle_nutri)

            NutritionRow("Tổng năng lượng", "${st.kcalTotal} kcal", R.drawable.flash_circle_nutri)

            // % kcal theo macro
            val p = st.proteinG
            val c = st.carbG
            val f = st.fatG
            val kcalFromP = (p ?: 0.0) * 4
            val kcalFromC = (c ?: 0.0) * 4
            val kcalFromF = (f ?: 0.0) * 9
            val kcalSum   = kcalFromP + kcalFromC + kcalFromF
            fun pct(x: Double) = if (kcalSum > 0) "${round1(x / kcalSum * 100)}%" else "--"

            Spacer(Modifier.height(4.dp))
            NutritionRow(
                name = "Đạm (Protein)",
                value = p?.let { "${round1(it)} g" } ?: "--",
                iconRes = R.drawable.protein,
                percent = if (p != null) pct(kcalFromP) else ""
            )
            NutritionRow(
                name = "Tinh bột (Carb)",
                value = c?.let { "${round1(it)} g" } ?: "--",
                iconRes = R.drawable.finger_cricle,
                percent = if (c != null) pct(kcalFromC) else ""
            )
            NutritionRow(
                name = "Chất béo (Fat)",
                value = f?.let { "${round1(it)} g" } ?: "--",
                iconRes = R.drawable.fat,
                percent = if (f != null) pct(kcalFromF) else ""
            )

            Spacer(modifier = Modifier.height(12.dp))
            NutritionRow("Khẩu phần tham chiếu", "${food.servingSizeGrams ?: 100} g", R.drawable.flash_circle_nutri)
            NutritionRow("Tỉ lệ ăn được", "${((food.ediblePortion ?: 1.0) * 100).toInt()}%", R.drawable.flash_circle_nutri)
        }
    }
}

@Composable
private fun NutritionBox(value: String, label: String, iconRes: Int) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF6F6F6))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = iconRes), contentDescription = label, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

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
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
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
private fun Double.roundToInt() = kotlin.math.round(this).toInt()
