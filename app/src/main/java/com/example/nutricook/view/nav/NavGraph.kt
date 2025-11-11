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
import com.example.nutricook.view.home.HomeScreen
import com.example.nutricook.view.home.NutritionDetailScreen
import com.example.nutricook.view.home.NutritionPickerScreen
import com.example.nutricook.view.intro.IntroScreen
import com.example.nutricook.view.intro.OnboardingScreen
import com.example.nutricook.view.notifications.NotificationsScreen
import com.example.nutricook.view.profile.ExerciseSuggestionsScreen
import com.example.nutricook.view.profile.OtherProfileScreen
import com.example.nutricook.view.profile.ProfileScreen
import com.example.nutricook.view.profile.RecipeGuidanceScreen
import com.example.nutricook.view.profile.SettingsScreen
import com.example.nutricook.view.profile.UserActivitiesScreen
import com.example.nutricook.view.profile.UserPostsScreen
import com.example.nutricook.view.profile.UserSavesScreen
import com.example.nutricook.view.recipes.CreateRecipeScreen
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
import com.example.nutricook.viewmodel.profile.ActivitiesViewModel
import com.example.nutricook.viewmodel.profile.PostViewModel
import com.example.nutricook.viewmodel.profile.SavesViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val authVm: AuthViewModel = hiltViewModel()
    val authState by authVm.uiState.collectAsState()

    val startDestination = if (authState.currentUser != null) "home" else "intro"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // INTRO
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

        // ONBOARDING
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

        // LOGIN
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

        // REGISTER
        composable("register") {
            RegisterScreen(
                onGoLogin = { navController.navigate("login") { launchSingleTop = true } },
                onBack = { navController.popBackStack() }
            )
        }

        // HOME (BOTTOM)
        composable("home") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    HomeScreen(navController)
                }
            }
        }

        // CATEGORIES (BOTTOM)
        composable("categories") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CategoriesScreen(navController)
                }
            }
        }

        // RECIPES (BOTTOM)
        composable("recipes") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) { RecipeDiscoveryScreen(navController) }
            }
        }

        // PROFILE (BOTTOM)
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
                onOpenOtherProfile = { uid ->
                    navController.navigate("user_profile/$uid")
                },
                bottomBar = { BottomNavigationBar(navController) }
            )
        }

        // SETTINGS (BOTTOM)
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

        // INGREDIENTS (BOTTOM)
        composable("ingredients") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) { IngredientsFilterScreen(navController = navController) }
            }
        }

        // INGREDIENT BROWSER (BOTTOM)
        composable("ingredient_browser") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) { IngredientBrowserScreen(navController = navController) }
            }
        }

        // ====== NUTRITION (PICKER + DETAIL) ======

        // 1) Picker KHÔNG tham số: chọn thực phẩm rồi đẩy qua detail
        composable("nutrition_detail") {
            // Nếu muốn hiện bottom bar, bọc bằng Scaffold như các màn BOTTOM khác
            NutritionPickerScreen(
                navController = navController,
                defaultGrams = 100
            )
        }

        // 2) Detail CÓ tham số: foodId + grams (tuỳ chọn, mặc định 100)
        composable(
            route = "nutrition_detail/{foodId}?grams={grams}",
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType },
                navArgument("grams") {
                    type = NavType.IntType
                    defaultValue = 100
                }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")!!
            val grams = backStackEntry.arguments?.getInt("grams") ?: 100
            NutritionDetailScreen(
                navController = navController,
                foodId = foodId,
                defaultGrams = grams
            )
        }

        // ====== RECIPES DETAIL (NO BOTTOM) ======
        composable("recipe_detail/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
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

        // RECIPE STEPS
        composable("recipe_info/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: "Unknown Recipe"
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeInfoScreen(navController, recipeTitle, imageRes)
        }
        composable("recipe_step") { RecipeStepScreen(navController) }
        composable("recipe_step2") { RecipeStep2Screen(navController) }
        composable("recipe_step_final") { RecipeStepFinalScreen(navController) }

        // NUTRITION FACTS (nếu vẫn muốn giữ trang tổng quan)
        composable("nutrition_facts") { NutritionFactsScreen(navController) }

        composable("review_screen") { ReviewScreen(navController) }

        // ARTICLE DETAIL
        composable("article_detail") { ArticleDetailScreen(navController) }

        // ====== USER-AWARE SCREENS ======
        // Recent Activity
        composable(
            route = "recent_activity/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val vm: ActivitiesViewModel = hiltViewModel()
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    UserActivitiesScreen(
                        onBack = { navController.popBackStack() },
                        vm = vm
                    )
                }
            }
        }

        // Posts
        composable(
            route = "posts/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val vm: PostViewModel = hiltViewModel()
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    UserPostsScreen(
                        onBack = { navController.popBackStack() },
                        vm = vm
                    )
                }
            }
        }

        // Saves
        composable(
            route = "saves/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val vm: SavesViewModel = hiltViewModel()
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    UserSavesScreen(
                        onBack = { navController.popBackStack() },
                        vm = vm
                    )
                }
            }
        }

        // Xem hồ sơ người khác (NO bottom bar). ViewModel đọc userId từ SavedStateHandle("userId")
        composable(
            route = "user_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            OtherProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
