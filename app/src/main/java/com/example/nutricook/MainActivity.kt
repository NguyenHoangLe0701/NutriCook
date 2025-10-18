package com.example.nutricook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.nutricook.ui.navigation.NavGraph
import com.example.nutricook.ui.theme.NutriCookTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriCookTheme {
                Surface {
                    val navController = rememberNavController() // Tạo NavHostController
                    NavGraph(navController = navController) //navController vào NavGraph
                }
            }
        }
    }
}