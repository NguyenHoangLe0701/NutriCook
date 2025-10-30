package com.example.nutricook.view.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.example.nutricook.viewmodel.auth.AuthEvent
import com.example.nutricook.viewmodel.auth.AuthViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    // lấy AuthViewModel 1 lần ở ngoài
    val authVm: AuthViewModel = hiltViewModel()
    val authState by authVm.uiState.collectAsState()

    // nếu đã có currentUser -> start từ home, còn không -> intro
    val startDestination = remember(authState.currentUser) {
        if (authState.currentUser != null) "home" else "intro"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // INTRO ---------------------------------------------------
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

        // ONBOARDING ----------------------------------------------
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

        // LOGIN ---------------------------------------------------
        composable("login") {
            // nếu đã login thì push sang home
            LaunchedEffect(authState.currentUser) {
                if (authState.currentUser != null) {
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

        // REGISTER ------------------------------------------------
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

        // HOME (có bottom bar) ------------------------------------
        composable("home") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    HomeScreen(navController)
                }
            }
        }

        // CATEGORIES ----------------------------------------------
        composable("categories") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CategoriesScreen(navController)
                }
            }
        }

        // RECIPES -------------------------------------------------
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

        // PROFILE -------------------------------------------------
        composable("profile") {
            ProfileScreen(
                onOpenSettings = { navController.navigate("settings") },
                bottomBar = { BottomNavigationBar(navController) }
            )
        }

        // SETTINGS (có logout) ------------------------------------
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    // ✅ dùng event thay vì gọi hàm private
                    authVm.onEvent(AuthEvent.Logout)
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                bottomBar = { BottomNavigationBar(navController) }
            )
        }

        // RECIPE DETAIL -------------------------------------------
        composable("recipe_detail/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
            val imageRes =
                backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeDetailScreen(navController, recipeTitle, imageRes)
        }

        // INGREDIENTS ---------------------------------------------
        composable("ingredients") {
            IngredientsFilterScreen(navController = navController)
        }

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

        // MÀN PHỤ -------------------------------------------------
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

        composable("notifications") {
            NotificationsScreen(navController)
        }
    }
}
