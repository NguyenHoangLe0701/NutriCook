package com.example.nutricook.view.intro

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.nutricook.R
import com.example.nutricook.ui.theme.Cyan
import com.example.nutricook.ui.theme.Orange
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
@Composable
fun IntroScreen(navController: NavController) {
    var showVitamins by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = (-40).dp),
        contentAlignment = Alignment.TopCenter
    ) {

        Image(
            painter = painterResource(id = R.drawable.bg_shield_light),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(450.dp)
                .align(Alignment.TopCenter)
                .offset(y = 0.dp)
        )
        if (showVitamins) {
            Box(
                modifier = Modifier
                    .matchParentSize()    // phủ toàn màn hình
                    .offset(y = 260.dp),  // chỉnh đúng tâm khiên
                contentAlignment = Alignment.TopCenter
            ) {
                VitaminBubbles()
            }
        }
        // 🛡️ Ảnh shield + text
        Image(
            painter = painterResource(id = R.drawable.shield),
            contentDescription = "Shield Icon",
            modifier = Modifier
                .height(401.dp)
                .width(400.dp)
                .offset(y = 163.dp)
        )
            // ✨ Khi bật showVitamins → hiện 8 quả bóng bay ra

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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


    // ⏳ Điều khiển thời gian hiển thị + chuyển màn
    LaunchedEffect(Unit) {
        delay(800L)
        showVitamins = true
        delay(2500L)
        navController.navigate("onboarding") {
            popUpTo("intro") { inclusive = true }
            launchSingleTop = true
        }
    }
}
@Composable
fun VitaminBubbles() {
    val vitamins = listOf(
        R.drawable.vitamin_b,
        R.drawable.vitamin_b6_2,
        R.drawable.vitamin_k,
        R.drawable.vitamin_na,
        R.drawable.vitamin_b6,
        R.drawable.vitamin_b6_3,
        R.drawable.vitamin_a,
        R.drawable.vitamin_a_2
    )

    // 🌈 Góc bay ra quanh khiên
    val angles = listOf(20, 70, 110, 160, 225, 255, 300, 340)

    // 🔹 Tạo khoảng cách khác nhau (xa gần)
    val radii = listOf(500f, 500f, 550f, 500f, 700f, 500f, 500f, 550f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = 60.dp), // nằm giữa khiên
        contentAlignment = Alignment.TopCenter
    ) {
        vitamins.forEachIndexed { index, img ->
            val alpha = remember { Animatable(0f) }
            val scale = remember { Animatable(0.3f) }
            val offsetX = remember { Animatable(0f) }
            val offsetY = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                val angleRad = Math.toRadians(angles[index].toDouble())
                val radius = radii[index] // mỗi vitamin có khoảng cách khác nhau
                val dx = (radius * kotlin.math.cos(angleRad)).toFloat()
                val dy = (radius * kotlin.math.sin(angleRad)).toFloat()

                // 🟢 Pha 1: xuất hiện ở giữa khiên
                launch {
                    alpha.animateTo(1f, tween(500, easing = EaseOutCubic))
                    scale.animateTo(1f, tween(500, easing = EaseOutCubic))
                }

                // 💥 Pha 2: bung ra cùng lúc
                delay(600L)
                launch {
                    offsetX.animateTo(dx, tween(1000, easing = EaseOutCubic))
                }
                launch {
                    offsetY.animateTo(dy, tween(1000, easing = EaseOutCubic))
                }
            }

            Image(
                painter = painterResource(id = img),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        translationX = offsetX.value
                        translationY = offsetY.value
                    }
                    .scale(scale.value)
                    .alpha(alpha.value)
            )
        }
    }
}



