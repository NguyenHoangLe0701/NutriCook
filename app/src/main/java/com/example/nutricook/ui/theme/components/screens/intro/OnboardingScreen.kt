package com.example.nutricook.ui.screens.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.nutricook.R

private data class OnboardingPage(val imageRes: Int, val title: String, val subtitle: String)

@Composable
fun OnboardingScreen(navController: NavController) {
    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.salad,
            title = "Bắt đầu với các menu đơn giản",
            subtitle = "Xây dựng thói quen ăn uống khoa học dễ dàng."
        ),
        OnboardingPage(
            imageRes = R.drawable.pizza,
            title = "Với những công thức nấu ăn tuyệt vời",
            subtitle = "Tổng hợp hàng ngàn công thức nấu ăn từ khắp nơi trên thế giới."
        ),
        OnboardingPage(
            imageRes = R.drawable.gym,
            title = "Kết hợp thực hành",
            subtitle = "Gợi ý các bài tập nhẹ nhàng đi kèm để đạt hiệu quả."
        )
    )

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val p = pages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = p.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = p.title, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = p.subtitle,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Indicator (simple dots)
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            repeat(pages.size) { index ->
                val size = if (pagerState.currentPage == index) 10.dp else 6.dp
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(size)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { navController.navigate("login") }) {
                Text("Bỏ qua")
            }
            Button(onClick = {
                if (pagerState.currentPage < pages.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    navController.navigate("login")
                }
            }) {
                Text(if (pagerState.currentPage < pages.lastIndex) "Tiếp tục →" else "Bắt đầu")
            }
        }
    }
}


