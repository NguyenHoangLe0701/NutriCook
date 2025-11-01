package com.example.nutricook.view.recipes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeStepFinalScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Mở profile nếu cần */ }) {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_sample),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Step 03 of 3",
                color = Color(0xFF3AC7BF),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Text(
                text = "Complete",
                color = Color.Gray,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Cook the brisket for about 8 hours. An hour before serving, stir in the cabbage and cook for 1 more hour.",
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Image(
                painter = painterResource(id = R.drawable.beefandcabbage),
                contentDescription = "Food Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { navController.navigate("home") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(52.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Finish",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_next_arrow),
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
