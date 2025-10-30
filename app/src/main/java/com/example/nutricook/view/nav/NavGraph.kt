package com.example.nutricook.view.nav

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nutricook.R
import com.example.nutricook.ui.screens.profile.ProfileScreen
import com.example.nutricook.view.intro.IntroScreen
import com.example.nutricook.view.intro.OnboardingScreen
import com.example.nutricook.view.auth.LoginScreen
import com.example.nutricook.view.auth.RegisterScreen
import com.example.nutricook.view.home.HomeScreen
import com.example.nutricook.view.categories.CategoriesScreen
import com.example.nutricook.view.recipes.*
import com.example.nutricook.view.profile.ProfileScreen
import com.example.nutricook.view.profile.RecipeGuidanceScreen
import com.example.nutricook.view.profile.ExerciseSuggestionsScreen
import com.example.nutricook.view.recipes.IngredientsFilterScreen
import com.example.nutricook.view.notifications.NotificationsScreen
import com.example.nutricook.view.home.NutritionDetailScreen
import com.example.nutricook.view.recipes.IngredientDetailScreen
import com.example.nutricook.viewmodel.auth.AuthViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "intro") {

        // INTRO
        composable("intro") { IntroScreen(navController) }
        composable("onboarding") { OnboardingScreen(navController) }

        // LOGIN (khớp LoginScreen: onGoRegister, onBack, onForgotPassword, vm) :contentReference[oaicite:0]{index=0}
        composable("login") {
            val authVm: AuthViewModel = hiltViewModel()
            val state by authVm.uiState.collectAsState()

            // nếu đã login ok -> đi home và clear intro/login
            LaunchedEffect(state.currentUser) {
                if (state.currentUser != null) {
                    navController.navigate("home") {
                        // clear về start để back không quay lại login
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }

            LoginScreen(
                onGoRegister = { navController.navigate("register") },
                onBack = { navController.popBackStack() },
                onForgotPassword = {
                    // TODO: điều hướng tới màn quên mật khẩu nếu có
                },
                vm = authVm
            )
        }

        // REGISTER (khớp RegisterScreen: onGoLogin, onBack, vm) :contentReference[oaicite:1]{index=1}
        composable("register") {
            val authVm: AuthViewModel = hiltViewModel()

            RegisterScreen(
                onGoLogin = {
                    navController.navigate("login") {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() },
                vm = authVm
            )
        }

        // HOME (có bottom bar)
        composable("home") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    HomeScreen(navController)
                }
            }
        }

        // CATEGORIES (có bottom bar)
        composable("categories") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CategoriesScreen(navController)
                }
            }
        }

        // RECIPES (có bottom bar)
        composable("recipes") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    RecipeDiscoveryScreen(navController)
                }
            }
        }

        // PROFILE (giữ cách gọi cũ của ông — nếu ProfileScreen chuyển sang callback thì sửa tại đây)
        composable("profile") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    ProfileScreen(navController)
                }
            }
        }

        // alias cũ
        composable("recipe_discovery") {
            RecipeDiscoveryScreen(navController)
        }

        // Recipe Detail
        composable("recipe_detail/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
            val imageRes =
                backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeDetailScreen(navController, recipeTitle, imageRes)
        }

        // Ingredients Filter
        composable("ingredients") {
            IngredientsFilterScreen(navController = navController)
        }

        // Ingredient Detail
        composable("ingredient_detail/{ingredientName}") { backStackEntry ->
            val ingredientName = backStackEntry.arguments?.getString("ingredientName") ?: ""
            IngredientDetailScreen(navController, ingredientName)
        }

        composable("ingredient_browser") {
            IngredientBrowserScreen(navController = navController)
        }

        composable("nutrition_detail") {
            NutritionDetailScreen(navController = navController)
        }

        composable("create_recipe") {
            CreateRecipeScreen(navController = navController)
        }

        composable("upload_success") {
            RecipeUploadSuccessScreen(navController)
        }

        composable("recipe_direction") {
            RecipeDirectionsScreen(navController)
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
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Recent Activity Screen", modifier = Modifier.padding(16.dp))
                }
            }
        }

        composable("posts") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Posts Screen", modifier = Modifier.padding(16.dp))
                }
            }
        }

        composable("edit_profile") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Edit Profile Screen", modifier = Modifier.padding(16.dp))
                }
            }
        }

        composable("settings") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Settings Screen", modifier = Modifier.padding(16.dp))
                }
            }
        }

        composable("notifications") {
            NotificationsScreen(navController)
        }
    }
}
