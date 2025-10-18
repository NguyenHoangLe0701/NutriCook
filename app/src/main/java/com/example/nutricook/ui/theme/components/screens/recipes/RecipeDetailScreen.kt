package com.example.nutricook.ui.screens.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun RecipeDetailScreen(navController: NavController, recipeTitle: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        item {
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
        }
        
        item {
            // Recipe Image
            Image(
                painter = painterResource(id = R.drawable.pizza),
                contentDescription = recipeTitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp)
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            // Recipe Title
            Text(
                text = recipeTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        item {
            // Recipe Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip("30 phút", Icons.Default.AccessTime)
                InfoChip("4 người", Icons.Default.Person)
                InfoChip("Dễ", Icons.Default.Star)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Ingredients Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Nguyên liệu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
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
                        else -> listOf(
                            "Nguyên liệu cơ bản",
                            "Gia vị tùy chọn",
                            "Rau củ tươi"
                        )
                    }
                    
                    ingredients.forEach { ingredient ->
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
                            Text(
                                text = ingredient,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            // Instructions Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Cách làm",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
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
                        recipeTitle.contains("gà") -> listOf(
                            "1. Rửa sạch gà, để ráo nước",
                            "2. Cắt dứa thành miếng nhỏ",
                            "3. Ướp gà với gia vị 30 phút",
                            "4. Xào gà với dứa đến chín vàng",
                            "5. Nêm nếm lại cho vừa ăn"
                        )
                        else -> listOf(
                            "1. Chuẩn bị nguyên liệu",
                            "2. Chế biến theo hướng dẫn",
                            "3. Nêm nếm gia vị",
                            "4. Hoàn thành và thưởng thức"
                        )
                    }
                    
                    instructions.forEachIndexed { index, instruction ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF20B2AA),
                                modifier = Modifier.width(24.dp)
                            )
                            Text(
                                text = instruction,
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
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
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Add to favorites */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yêu thích")
                }
                
                Button(
                    onClick = { /* TODO: Start cooking */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20B2AA))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bắt đầu nấu")
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                Color(0xFFE0F2F1),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF20B2AA)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF20B2AA),
            fontWeight = FontWeight.Medium
        )
    }
}
