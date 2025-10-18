package com.example.nutricook.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

data class ProfileMenuItem(
    val title: String,
    val subtitle: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ProfileScreen(navController: NavController) {
    val profileMenuItems = listOf(
        ProfileMenuItem(
            title = "Recent Activity (10)",
            icon = Icons.Default.History,
            onClick = { navController.navigate("recent_activity") }
        ),
        ProfileMenuItem(
            title = "Post (0)",
            icon = Icons.Default.PostAdd,
            onClick = { navController.navigate("posts") }
        ),
        ProfileMenuItem(
            title = "Công Thức",
            subtitle = "Hướng dẫn nấu ăn & tính calo",
            icon = Icons.Default.Restaurant,
            onClick = { navController.navigate("recipe_guidance") }
        )
    )

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
                Text(
                    text = "Hồ sơ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { navController.navigate("edit_profile") }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Color(0xFF20B2AA))
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF20B2AA))
                }
            }
        }

        item {
            // Profile Picture Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                Color(0xFFE3F2FD),
                                Color(0xFFF3E5F5)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFE0E0E0), CircleShape)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF9E9E9E), CircleShape)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Verified Badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF4CAF50), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Verified",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            // Username
            Text(
                text = "Admin User",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // Statistics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("0", "Bài đăng")
                StatItem("200", "Đang theo dõi")
                StatItem("46", "Theo dõi")
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        items(profileMenuItems) { item ->
            ProfileMenuItemCard(item = item)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // FatSecret Section
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
                        text = "FatSecret của tôi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple chart representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFE3F2FD),
                                        Color(0xFFBBDEFB),
                                        Color(0xFF90CAF9)
                                    )
                                ),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        // Simple line representation
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(2.dp)
                                .background(Color(0xFF20B2AA))
                                .align(Alignment.CenterStart)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileMenuItemCard(item: ProfileMenuItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { item.onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color(0xFF20B2AA),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                if (item.subtitle != null) {
                    Text(
                        text = item.subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
