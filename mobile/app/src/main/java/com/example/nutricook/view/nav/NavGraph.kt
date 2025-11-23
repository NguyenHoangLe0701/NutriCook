package com.example.nutricook.view.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nutricook.R
import com.example.nutricook.view.articles.ArticleDetailScreen
import com.example.nutricook.view.auth.LoginScreen
import com.example.nutricook.view.auth.RegisterScreen
import com.example.nutricook.view.categories.CategoriesScreen
import com.example.nutricook.view.debug.DataSeedScreen
import com.example.nutricook.view.home.HomeScreen
import com.example.nutricook.view.home.NutritionDetailScreen
import com.example.nutricook.view.intro.IntroScreen
import com.example.nutricook.view.intro.OnboardingScreen
import com.example.nutricook.view.newsfeed.FeedScreen
import com.example.nutricook.view.notifications.NotificationsScreen
import com.example.nutricook.view.profile.ExerciseDetailScreen
import com.example.nutricook.view.profile.ExerciseSuggestionsScreen
import com.example.nutricook.view.profile.ProfileScreen
import com.example.nutricook.view.profile.PublicProfileScreen
import com.example.nutricook.view.profile.RecipeGuidanceScreen
import com.example.nutricook.view.profile.SearchProfileScreen
import com.example.nutricook.view.profile.SettingsScreen
import com.example.nutricook.view.profile.UserActivitiesScreen
import com.example.nutricook.view.recipes.CreateRecipeScreen
import com.example.nutricook.view.recipes.CreateRecipeStep1Screen
import com.example.nutricook.view.recipes.CreateRecipeStep2Screen
import com.example.nutricook.view.recipes.CreateRecipeStep3Screen
import com.example.nutricook.view.recipes.CreateRecipeStep4Screen
import com.example.nutricook.view.recipes.IngredientBrowserScreen
import com.example.nutricook.view.recipes.IngredientDetailScreen
import com.example.nutricook.view.recipes.IngredientsFilterScreen
import com.example.nutricook.view.recipes.NutritionFactsScreen
import com.example.nutricook.view.recipes.RecipeDetailScreen
import com.example.nutricook.view.recipes.RecipeDirectionsScreen
import com.example.nutricook.view.recipes.RecipeDiscoveryScreen
import com.example.nutricook.view.recipes.RecipeInfoScreen
import com.example.nutricook.view.recipes.RecipeStep2Screen
import com.example.nutricook.view.recipes.RecipeStepFinalScreen
import com.example.nutricook.view.recipes.RecipeStepScreen
import com.example.nutricook.view.recipes.RecipeUploadSuccessScreen
import com.example.nutricook.view.recipes.ReviewScreen
import com.example.nutricook.viewmodel.auth.AuthViewModel
import com.example.nutricook.viewmodel.newsfeed.PostViewModel
import com.example.nutricook.viewmodel.profile.ActivitiesViewModel
import com.example.nutricook.viewmodel.profile.SavesViewModel

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
                    RecipeDiscoveryScreen(navController)
                }
            }
        }

        // ========== PROFILE (BOTTOM) ==========
        composable("profile") {
            ProfileScreen(
                onOpenSettings = { navController.navigate("settings") },
                onOpenRecent = {
                    val uid = authState.currentUser?.id ?: return@ProfileScreen
                    navController.navigate("recent_activity/$uid")
                },
                onOpenPosts = {
                    val uid = authState.currentUser?.id ?: return@ProfileScreen
                    navController.navigate("posts/$uid")
                },
                onOpenSaves = {
                    val uid = authState.currentUser?.id ?: return@ProfileScreen
                    navController.navigate("saves/$uid")
                },
                onOpenSearch = {
                    navController.navigate("search_profiles")
                },
                bottomBar = { BottomNavigationBar(navController) }
            )
        }

        // ========== SEARCH PROFILES ==========
        composable("search_profiles") {
            SearchProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { uid ->
                    navController.navigate("public_profile/$uid")
                }
            )
        }

        // ========== PUBLIC PROFILE ==========
        composable(
            route = "public_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            PublicProfileScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { post ->
                    // TODO: Navigate to post detail
                }
            )
        }

        // ========== SETTINGS ==========
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
                bottomBar = { BottomNavigationBar(navController) },
                navController = navController
            )
        }

        // ========== INGREDIENTS ==========
        composable("ingredients") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    IngredientsFilterScreen(navController = navController)
                }
            }
        }

        // ========== INGREDIENT BROWSER ==========
        composable("ingredient_browser") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    IngredientBrowserScreen(navController = navController)
                }
            }
        }

        // ========== NUTRITION DETAIL ==========
        composable("nutrition_detail") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    NutritionDetailScreen(navController = navController)
                }
            }
        }

        // ========== CREATE RECIPE ==========
        composable("create_recipe") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep1Screen(navController = navController)
                }
            }
        }
        
        // ========== CREATE RECIPE STEP 1 ==========
        composable("create_recipe_step1") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep1Screen(navController = navController)
                }
            }
        }
        
        // ========== CREATE RECIPE STEP 2 ==========
        composable("create_recipe_step2") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep2Screen(navController = navController)
                }
            }
        }
        
        // ========== CREATE RECIPE STEP 3 ==========
        composable("create_recipe_step3") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep3Screen(navController = navController)
                }
            }
        }
        
        // ========== CREATE RECIPE STEP 4 ==========
        composable("create_recipe_step4") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep4Screen(navController = navController)
                }
            }
        }

        // ========== NOTIFICATIONS ==========
        composable("notifications") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    NotificationsScreen(navController)
                }
            }
        }

        // ========== RECENT ACTIVITY ==========
        composable(
            route = "recent_activity/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val vm: ActivitiesViewModel = hiltViewModel()
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    UserActivitiesScreen(
                        onBack = { navController.popBackStack() },
                        vm = vm
                    )
                }
            }
        }

        // ========== POSTS (NEWS FEED) ==========
        composable(
            route = "posts/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val vm: PostViewModel = hiltViewModel()

            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    FeedScreen(
                        vm = vm,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // ========== SAVES ==========

        // ========== EDIT PROFILE ==========
        composable("edit_profile") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    // TODO: Edit Profile UI
                }
            }
        }

        // ========== DETAIL SCREENS ==========
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

        composable("upload_success") { RecipeUploadSuccessScreen(navController) }
        composable("recipe_direction") { RecipeDirectionsScreen(navController) }
        composable("recipe_guidance") { RecipeGuidanceScreen(navController) }
        composable("exercise_suggestions") { ExerciseSuggestionsScreen(navController) }
        
        composable("exercise_detail/{exerciseName}/{imageRes}/{duration}/{calories}/{difficulty}") { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "Unknown"
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.baseball
            val duration = backStackEntry.arguments?.getString("duration") ?: "15 phút"
            val calories = backStackEntry.arguments?.getString("calories")?.toIntOrNull() ?: 100
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Trung bình"
            ExerciseDetailScreen(
                navController = navController,
                exerciseName = exerciseName,
                exerciseImageRes = imageRes,
                exerciseDuration = duration,
                exerciseCalories = calories,
                exerciseDifficulty = difficulty
            )
        }

        // ========== RECIPE STEPS ==========
        composable("recipe_info/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: "Unknown Recipe"
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeInfoScreen(navController, recipeTitle, imageRes)
        }

        composable("recipe_step") { RecipeStepScreen(navController) }
        composable("recipe_step2") { RecipeStep2Screen(navController) }
        composable("recipe_step_final") { RecipeStepFinalScreen(navController) }

        composable("nutrition_facts") { NutritionFactsScreen(navController) }
        composable("review_screen") { ReviewScreen(navController) }

        // ========== DEBUG ==========
        composable("seed_data") { DataSeedScreen(navController) }

        // ========== ARTICLES ==========
        composable("article_detail") { ArticleDetailScreen(navController) }
    }
}

// ĐÃ XÓA: Phần BottomNavigationBar trùng lặp ở đây
// Nó sẽ tự động dùng BottomNavigationBar từ file BottomNavigationBar.kt