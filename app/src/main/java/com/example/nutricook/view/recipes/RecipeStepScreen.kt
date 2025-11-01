package com.example.nutricook.view.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeStepScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Avatar hoặc icon người dùng góc phải
                    IconButton(onClick = { /* Profile */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_sample),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF9FAFC))
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 🧾 Tiêu đề bước
            Text(
                text = "Step 01 of 3",
                fontSize = 20.sp,
                color = Color(0xFF2AB9A7),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Preparation of raw materials",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 📋 Danh sách nguyên liệu
            val ingredients = listOf(
                Pair("Carrots, peeled and cut into matchstick pieces", R.drawable.carrot),
                Pair("Red potatoes, quartered", R.drawable.red_potato),
                Pair("Cabbage, coarsely chopped", R.drawable.cabbage),
                Pair("Onion, peeled and cut into bite-sized pieces", R.drawable.onion)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ingredients.size) { index ->
                    val (title, imageRes) = ingredients[index]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = title,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }
            }

            // 🟩 Nút Continue
            Button(
                onClick = { navController.navigate("recipe_step2") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2AD1C0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Next",
                    tint = Color.White
                )
            }
        }
    }
}
