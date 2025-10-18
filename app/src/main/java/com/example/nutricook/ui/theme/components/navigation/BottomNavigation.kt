package com.example.nutricook.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nutricook.R
data class BottomNavItem(
    val title: String,
    val iconRes: Int? = null,      // cho ảnh PNG
    val iconVector: ImageVector? = null,  // cho icon vector
    val route: String
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Trang chủ", iconVector = Icons.Default.Home, route = "home"),
        BottomNavItem("Danh mục", iconVector = Icons.Default.Dashboard, route = "categories"),
        BottomNavItem("Công thức", iconRes = R.drawable.feed, route = "recipes"),
        BottomNavItem("Hồ sơ", iconVector = Icons.Default.Person, route = "profile")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.iconVector != null) {
                        Icon(
                            item.iconVector,
                            contentDescription = item.title,
                            modifier = Modifier.size(26.dp) // kích thước vector icon
                        )
                    } else if (item.iconRes != null) {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.title,
                            modifier = Modifier.size(26.dp), // chỉnh cho PNG bằng với icon vector
                            tint = Color.Unspecified // giữ nguyên màu ảnh PNG, không bị overlay màu xám
                        )
                    }
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF20B2AA),
                    selectedTextColor = Color(0xFF20B2AA),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
