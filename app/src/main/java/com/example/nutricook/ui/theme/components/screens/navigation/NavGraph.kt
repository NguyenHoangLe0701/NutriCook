package com.example.nutricook.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nutricook.ui.screens.intro.IntroScreen
import com.example.nutricook.ui.screens.intro.OnboardingScreen
import com.example.nutricook.ui.screens.auth.LoginScreen
import com.example.nutricook.ui.screens.auth.RegisterScreen
import com.example.nutricook.ui.screens.home.HomeScreen
import com.example.nutricook.ui.screens.categories.CategoriesScreen
import com.example.nutricook.ui.screens.recipes.RecipeDiscoveryScreen
import com.example.nutricook.ui.screens.recipes.RecipeDetailScreen
import com.example.nutricook.ui.screens.profile.ProfileScreen
import com.example.nutricook.ui.screens.profile.RecipeGuidanceScreen
import com.example.nutricook.ui.screens.profile.ExerciseSuggestionsScreen
import com.example.nutricook.R
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") { IntroScreen(navController) }
        composable("onboarding") { OnboardingScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("home") { 
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                    HomeScreen(navController)
                }
            }
        }
        composable("categories") { 
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                    CategoriesScreen(navController)
                }
            }
        }
        composable("recipes") {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Box(
            modifier = androidx.compose.ui.Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.feed),
                contentDescription = "Công thức",
                modifier = androidx.compose.ui.Modifier.size(200.dp)
            )
        }
    }
}
        composable("profile") { 
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                    ProfileScreen(navController)
                }
            }
        }
        composable("recipe_discovery") { 
            RecipeDiscoveryScreen(navController)
        }
        composable("recipe_detail/{recipeTitle}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
            RecipeDetailScreen(navController, recipeTitle)
        }
        composable("recipe_guidance") { 
            RecipeGuidanceScreen(navController)
        }
        composable("exercise_suggestions") { 
            ExerciseSuggestionsScreen(navController)
        }
        composable("recent_activity") { 
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                    Text("Recent Activity Screen", modifier = androidx.compose.ui.Modifier.padding(16.dp))
                }
            }
        }
        composable("posts") { 
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                    Text("Posts Screen", modifier = androidx.compose.ui.Modifier.padding(16.dp))
                }
            }
        }
        composable("edit_profile") { 
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                    Text("Edit Profile Screen", modifier = androidx.compose.ui.Modifier.padding(16.dp))
                }
            }
        }
        composable("settings") { 
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
                    Text("Settings Screen", modifier = androidx.compose.ui.Modifier.padding(16.dp))
                }
            }
        }
    }
}