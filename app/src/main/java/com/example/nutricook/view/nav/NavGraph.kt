package com.example.nutricook.view.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.nutricook.R
import com.example.nutricook.view.auth.LoginScreen
import com.example.nutricook.view.auth.RegisterScreen
import com.example.nutricook.view.categories.CategoriesScreen
import com.example.nutricook.view.home.HomeScreen
import com.example.nutricook.view.home.NutritionDetailScreen
import com.example.nutricook.view.intro.IntroScreen
import com.example.nutricook.view.intro.OnboardingScreen
import com.example.nutricook.view.notifications.NotificationsScreen
import com.example.nutricook.view.profile.ExerciseSuggestionsScreen
import com.example.nutricook.view.profile.ProfileScreen
import com.example.nutricook.view.profile.RecipeGuidanceScreen
import com.example.nutricook.view.profile.SettingsScreen
import com.example.nutricook.view.recipes.CreateRecipeScreen
import com.example.nutricook.view.recipes.IngredientBrowserScreen
import com.example.nutricook.view.recipes.IngredientDetailScreen
import com.example.nutricook.view.recipes.IngredientsFilterScreen
import com.example.nutricook.view.recipes.RecipeDetailScreen
import com.example.nutricook.view.recipes.RecipeDirectionsScreen
import com.example.nutricook.view.recipes.RecipeDiscoveryScreen
import com.example.nutricook.view.recipes.RecipeUploadSuccessScreen
import com.example.nutricook.viewmodel.auth.AuthViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    // lấy auth ngoài cùng để quyết định start
    val authVm: AuthViewModel = hiltViewModel()
    val authState by authVm.uiState.collectAsState()

    // nếu đã đăng nhập thì bỏ intro/login
    val startDestination = if (authState.currentUser != null) "home" else "intro"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ========== INTRO ==========
        composable("intro") {
            if (authState.currentUser != null) {
                LaunchedEffect(Unit) {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                    }
                }
            } else {
                IntroScreen(navController)
            }
        }

        // ========== ONBOARDING ==========
        composable("onboarding") {
            if (authState.currentUser != null) {
                LaunchedEffect(Unit) {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            } else {
                OnboardingScreen(navController)
            }
        }

        // ========== LOGIN ==========
        composable("login") {
            val state = authState

            LaunchedEffect(state.currentUser) {
                if (state.currentUser != null) {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            LoginScreen(
                onGoRegister = { navController.navigate("register") },
                onBack = { navController.popBackStack() },
                onForgotPassword = { /* TODO */ },
                vm = authVm
            )
        }

        // ========== REGISTER ==========
        composable("register") {
            RegisterScreen(
                onGoLogin = {
                    navController.navigate("login") {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ========== HOME (BOTTOM) ==========
        composable("home") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    HomeScreen(navController)
                }
            }
        }

        // ========== CATEGORIES (BOTTOM) ==========
        composable("categories") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CategoriesScreen(navController)
                }
            }
        }

        // ========== RECIPES (BOTTOM) ==========
        composable("recipes") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    // màn này load list món ăn
                    RecipeDiscoveryScreen(navController)  // :contentReference[oaicite:1]{index=1}
                }
            }
        }

        // ========== PROFILE (BOTTOM) ==========
        composable("profile") {
            ProfileScreen(
                onOpenSettings = { navController.navigate("settings") },
                bottomBar = { BottomNavigationBar(navController) }
            )
        }

        // ========== SETTINGS (BOTTOM) ==========
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    authVm.signOut()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                bottomBar = { BottomNavigationBar(navController) }
            )
        }

        // ========== INGREDIENTS (BOTTOM) ==========
        composable("ingredients") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    IngredientsFilterScreen(navController = navController)
                }
            }
        }

        // ========== INGREDIENT BROWSER (BOTTOM) ==========
        composable("ingredient_browser") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    IngredientBrowserScreen(navController = navController)
                }
            }
        }

        // ========== NUTRITION DETAIL (BOTTOM) ==========
        composable("nutrition_detail") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    NutritionDetailScreen(navController = navController)
                }
            }
        }

        // ========== CREATE RECIPE (BOTTOM) ==========
        composable("create_recipe") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    CreateRecipeScreen(navController = navController)
                }
            }
        }

        // ========== NOTIFICATIONS (BOTTOM) ==========
        composable("notifications") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    NotificationsScreen(navController)
                }
            }
        }

        // ========== RECENT ACTIVITY (BOTTOM) ==========
        composable("recent_activity") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Recent Activity Screen", modifier = Modifier.padding(16.dp))
                }
            }
        }

        // ========== POSTS (BOTTOM) ==========
        composable("posts") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Posts Screen", modifier = Modifier.padding(16.dp))
                }
            }
        }

        // ========== EDIT PROFILE (BOTTOM) ==========
        composable("edit_profile") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Edit Profile Screen", modifier = Modifier.padding(16.dp))
                }
            }
        }

        // ========== DETAIL MÀN – KHÔNG BOTTOM ==========
        composable("recipe_detail/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
            val imageRes =
                backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeDetailScreen(navController, recipeTitle, imageRes)
        }

        composable("ingredient_detail/{ingredientName}") { backStackEntry ->
            val ingredientName = backStackEntry.arguments?.getString("ingredientName") ?: ""
            IngredientDetailScreen(navController, ingredientName)
        }

        composable("upload_success") {
            // màn này tự điều hướng về home bằng nút, không cần bottom
            RecipeUploadSuccessScreen(navController)  // :contentReference[oaicite:2]{index=2}
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
    }
}
