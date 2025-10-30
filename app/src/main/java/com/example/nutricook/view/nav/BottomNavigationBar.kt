package com.example.nutricook.view.nav

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nutricook.R

data class BottomNavItem(
    val title: String,
    val iconRes: Int,
    val iconResSelected: Int,
    val route: String
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Trang chủ", R.drawable.ic_home, R.drawable.ic_home_filled, "home"),
        BottomNavItem("Danh mục", R.drawable.ic_category, R.drawable.ic_category_filled, "categories"),
        BottomNavItem("Công thức", R.drawable.ic_recipe, R.drawable.ic_recipe_filled, "recipes"),
        BottomNavItem("Hồ sơ", R.drawable.ic_profile, R.drawable.ic_profile_filled, "profile")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.White)
    ) {
        val density = LocalDensity.current

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val jumpDp = 6.dp
                val targetTranslationPx = with(density) { if (isSelected) -jumpDp.toPx() else 0f }

                val animatedTranslationY by animateFloatAsState(
                    targetValue = targetTranslationPx,
                    animationSpec = tween(durationMillis = 180)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        }
                        .graphicsLayer { translationY = animatedTranslationY }
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // ✅ Giữ icon cố định 28×28dp và scale vừa khung
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isSelected) item.iconResSelected else item.iconRes
                            ),
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSelected) {
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            color = Color(0xFF12B3AD)
                        )
                    }
                }
            }
        }
    }
}
