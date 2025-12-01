package com.example.nutricook.view.intro

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nutricook.R
import com.example.nutricook.ui.theme.Cyan
import com.example.nutricook.ui.theme.Orange
import com.example.nutricook.viewmodel.intro.IntroViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IntroScreen(
    navController: NavController,
    viewModel: IntroViewModel = hiltViewModel()
) {
    var showVitamins by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) } // Flag để tránh navigate nhiều lần
    var startTime by remember { mutableStateOf<Long?>(null) }
    val preloadState by viewModel.preloadState.collectAsState()
    val preloadProgress by viewModel.preloadProgress.collectAsState()
    val preloadMessage by viewModel.preloadMessage.collectAsState()

    // Bắt đầu preload data và đếm thời gian khi màn hình được hiển thị
    LaunchedEffect(Unit) {
        startTime = System.currentTimeMillis()
        android.util.Log.d("IntroScreen", "Starting preload at ${startTime}")
        viewModel.startPreload()
    }

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
                    .matchParentSize()
                    .offset(y = 260.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                VitaminBubbles()
            }
        }
        
        Image(
            painter = painterResource(id = R.drawable.shield),
            contentDescription = "Shield Icon",
            modifier = Modifier
                .height(401.dp)
                .width(400.dp)
                .offset(y = 163.dp)
        )

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
            
            // Hiển thị progress indicator và message khi đang preload
            if (preloadState == PreloadState.LOADING || preloadState == PreloadState.COMPLETED) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Cyan,
                    progress = { if (preloadState == PreloadState.COMPLETED) 1f else preloadProgress / 100f }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (preloadState == PreloadState.COMPLETED) "Đã tải xong dữ liệu!" else preloadMessage,
                    fontSize = 14.sp,
                    color = if (preloadState == PreloadState.COMPLETED) Cyan else Color.Gray,
                    fontWeight = if (preloadState == PreloadState.COMPLETED) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                if (preloadState == PreloadState.LOADING) {
                    Text(
                        text = "$preloadProgress%",
                        fontSize = 16.sp,
                        color = Cyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    // Điều khiển animation
    LaunchedEffect(Unit) {
        delay(800L)
        showVitamins = true
    }
    
    // Điều khiển navigation - đảm bảo màn hình hiển thị tối thiểu 5 giây
    // Sử dụng một LaunchedEffect duy nhất để tránh race condition
    LaunchedEffect(startTime) {
        if (startTime == null) return@LaunchedEffect
        
        val minDisplayTime = 5000L // Tối thiểu 5 giây
        val maxWaitTime = 45000L // Tối đa 45 giây
        
        android.util.Log.d("IntroScreen", "Navigation LaunchedEffect started")
        
        // Chờ tối thiểu 5 giây - ĐẢM BẢO màn hình luôn hiển thị ít nhất 5 giây
        delay(minDisplayTime)
        
        android.util.Log.d("IntroScreen", "Min display time (5s) reached, checking preload state: $preloadState")
        
        // Sau 5 giây, check preload state
        var preloadDone = preloadState == PreloadState.COMPLETED || preloadState == PreloadState.ERROR
        
        // Nếu chưa xong, chờ thêm (tối đa 40 giây nữa)
        if (!preloadDone) {
            val checkInterval = 500L
            var checkElapsed = 0L
            val maxCheckTime = maxWaitTime - minDisplayTime // 40 giây còn lại
            
            while (!preloadDone && checkElapsed < maxCheckTime) {
                delay(checkInterval)
                checkElapsed += checkInterval
                
                // Check lại preload state
                val currentState = preloadState
                preloadDone = currentState == PreloadState.COMPLETED || currentState == PreloadState.ERROR
                
                if (preloadDone) {
                    android.util.Log.d("IntroScreen", "Preload completed after ${checkElapsed}ms additional wait")
                }
            }
        } else {
            android.util.Log.d("IntroScreen", "Preload already completed")
        }
        
        // Navigate sau khi đã chờ đủ thời gian
        if (!hasNavigated) {
            hasNavigated = true
            val totalElapsed = System.currentTimeMillis() - startTime!!
            android.util.Log.d("IntroScreen", "Navigating to onboarding after ${totalElapsed}ms")
            navController.navigate("onboarding") {
                popUpTo("intro") { inclusive = true }
                launchSingleTop = true
            }
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

    val angles = listOf(20, 70, 110, 160, 225, 255, 300, 340)
    val radii = listOf(500f, 500f, 550f, 500f, 700f, 500f, 500f, 550f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = 60.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        vitamins.forEachIndexed { index, img ->
            val alpha = remember { Animatable(0f) }
            val scale = remember { Animatable(0.3f) }
            val offsetX = remember { Animatable(0f) }
            val offsetY = remember { Animatable(0f) }

            LaunchedEffect(Unit) {
                val angleRad = Math.toRadians(angles[index].toDouble())
                val radius = radii[index]
                val dx = (radius * kotlin.math.cos(angleRad)).toFloat()
                val dy = (radius * kotlin.math.sin(angleRad)).toFloat()

                launch {
                    alpha.animateTo(1f, tween(500, easing = EaseOutCubic))
                    scale.animateTo(1f, tween(500, easing = EaseOutCubic))
                }

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

enum class PreloadState {
    IDLE,
    LOADING,
    COMPLETED,
    ERROR
}
