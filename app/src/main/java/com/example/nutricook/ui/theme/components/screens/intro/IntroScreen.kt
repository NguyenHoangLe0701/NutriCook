package com.example.nutricook.ui.screens.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.sp
import com.example.nutricook.R
import com.example.nutricook.ui.theme.Cyan
import com.example.nutricook.ui.theme.Orange

@Composable
fun IntroScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {

        Image(
            painter = painterResource(id = R.drawable.bg_shield_light),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = 0.dp)
        )

        // 🛡️ Ảnh shield + text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.shield),
                contentDescription = "Shield Icon",
                modifier = Modifier
                    .height(331.dp)
                    .width(300.dp)
                    .offset(y = 163.dp)
            )

            Spacer(modifier = Modifier.height(300.dp))

            Text(
                text = "Nutrition",
                fontSize = 30.sp,
                color = Cyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Foods",
                fontSize = 30.sp,
                color = Orange,
                fontWeight = FontWeight.Bold
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(3000L)
        navController.navigate("onboarding") {
            popUpTo("intro") { inclusive = true }
            launchSingleTop = true
        }
    }
}
