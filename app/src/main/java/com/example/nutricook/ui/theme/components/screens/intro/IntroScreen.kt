package com.example.nutricook.ui.screens.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import com.example.nutricook.ui.theme.Cyan
import com.example.nutricook.ui.theme.Orange
import com.example.nutricook.R

@Composable
fun IntroScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.shield), // Thay bằng shield từ Figma
            contentDescription = "Shield Icon",
            modifier = Modifier.fillMaxWidth(0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Dinh dưỡng", fontSize = 32.sp, color = Cyan)
        Text(text = "Thực phẩm", fontSize = 32.sp, color = Orange)
    }

    LaunchedEffect(Unit) {
        delay(1500L)
        navController.navigate("onboarding") {
            popUpTo("intro") { inclusive = true }
            launchSingleTop = true
        }
    }
}