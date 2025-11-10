package com.example.nutricook.view.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

@Composable
fun NutritionFactsScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ==== Nội dung cuộn được ====
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp) // chừa khoảng trống cho button
        ) {
            // ==== Header ====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                Text(
                    text = "Nutrition Facts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E1E1E)
                )

                Image(
                    painter = painterResource(id = R.drawable.avatar_sample),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==== Calories chart ====
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_chart_calories),
                    contentDescription = "Calories Chart",
                    modifier = Modifier.size(180.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("473 Calories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==== Summary row (Fat / Carbs / Protein) ====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutrientSummaryItem("20g", "Fat", R.drawable.fat)
                NutrientSummaryItem("50g", "Carbs", R.drawable.finger_cricle)
                NutrientSummaryItem("24g", "Protein", R.drawable.protein)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ==== Nutrition Facts Expandable List ====
            NutritionFactsExpandableList()

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ==== Review Button cố định ====
        Button(
            onClick = { navController.navigate("review_screen") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(52.dp)
        ) {
            Text(
                text = "Review",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// ======= Component nhỏ =======
@Composable
fun NutrientSummaryItem(value: String, label: String, iconRes: Int) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(20.dp)
            )
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

// ======= Nutrition Facts Expandable List =======
@Composable
fun NutritionFactsExpandableList() {
    val facts = listOf(
        NutritionParent(
            title = "Total Fat",
            value = "20g",
            percent = "25%",
            children = listOf(
                NutritionChild("Saturated Fat", "7g", "33%"),
                NutritionChild("Trans Fat", "0g", "0%"),
                NutritionChild("Polyunsaturated Fat", "0.042g", "0%"),
                NutritionChild("Monounsaturated Fat", "0.014g", "0%")
            )
        ),
        NutritionParent(
            title = "Cholesterol",
            value = "100mg",
            percent = "33%",
            children = listOf(
                NutritionChild("Saturated Fat", "0.009g", "0%"),
                NutritionChild("Trans Fat", "-", "0%"),
                NutritionChild("Polyunsaturated Fat", "0.042g", "0%"),
                NutritionChild("Monounsaturated Fat", "0.014g", "0%")
            )
        ),
        NutritionParent("Sodium", "1281mg", "56%", emptyList()),
        NutritionParent("Total Carbohydrate", "50g", "18%", emptyList()),
        NutritionParent("Protein", "24g", "45%", emptyList()),
        NutritionParent("Vitamin", "", "45%", emptyList())
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "✨ Nutrition Facts",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E1E),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        facts.forEach { fact ->
            NutritionParentItem(fact)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NutritionParentItem(fact: NutritionParent) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = fact.children.isNotEmpty()) { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bolt_blue),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(fact.title, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(fact.value, color = Color(0xFF02B6A3), fontWeight = FontWeight.SemiBold)
            }
            Text(fact.percent, color = Color(0xFF38427A), fontWeight = FontWeight.SemiBold)
        }

        if (expanded && fact.children.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            var columnHeight by remember { mutableStateOf(0) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            ) {
                // Đường kẻ xanh
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 12.dp)
                        .width(2.dp)
                        .height(with(LocalDensity.current) { columnHeight.toDp() })
                        .background(Color(0xFF3AC7BF))
                        .align(Alignment.TopStart)
                )

                Column(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .onGloballyPositioned { coordinates ->
                            columnHeight = coordinates.size.height
                        }
                ) {
                    fact.children.forEach { child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFFFFF6E5), shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_bolt_yellow),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(child.title, color = Color(0xFF1E1E1E), fontSize = 14.sp)
                                    if (child.value.isNotEmpty()) {
                                        Text(
                                            text = child.value,
                                            color = Color(0xFF9CA3AF),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                            Text(child.percent, color = Color(0xFF9CA3AF), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ======= Data model =======
data class NutritionParent(
    val title: String,
    val value: String,
    val percent: String,
    val children: List<NutritionChild>
)

data class NutritionChild(
    val title: String,
    val value: String,
    val percent: String
)
