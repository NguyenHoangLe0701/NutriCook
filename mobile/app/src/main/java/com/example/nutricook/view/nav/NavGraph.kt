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
import androidx.compose.ui.Alignment
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
import com.example.nutricook.view.categories.FoodDetailScreen
import com.example.nutricook.view.debug.DataSeedScreen
import com.example.nutricook.view.home.HomeScreen
import com.example.nutricook.view.home.NutritionDetailScreen
import com.example.nutricook.view.hotnews.AllHotNewsScreen
import com.example.nutricook.view.hotnews.CreateHotNewsScreen
import com.example.nutricook.view.hotnews.HotNewsDetailScreen
import com.example.nutricook.view.intro.IntroScreen
import com.example.nutricook.view.intro.OnboardingScreen
import com.example.nutricook.view.newsfeed.NewsfeedScreen
import com.example.nutricook.view.notifications.NotificationsScreen
import com.example.nutricook.view.profile.AddMealScreen
import com.example.nutricook.view.profile.CustomFoodCalculatorScreen
import com.example.nutricook.view.profile.ExerciseDetailScreen
import com.example.nutricook.view.profile.ExerciseSuggestionsScreen
import com.example.nutricook.view.profile.ProfileScreen
import com.example.nutricook.view.profile.PublicProfileScreen
import com.example.nutricook.view.profile.RecipeGuidanceScreen
import com.example.nutricook.view.profile.SearchProfileScreen
import com.example.nutricook.view.profile.SettingsScreen
import com.example.nutricook.view.profile.UserActivitiesScreen
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
import com.example.nutricook.view.recipes.UserRecipeInfoScreen
import com.example.nutricook.view.recipes.UserRecipeNutritionFactsScreen
import com.example.nutricook.view.recipes.UserRecipeStepScreen
import com.example.nutricook.viewmodel.CreateRecipeViewModel
import com.example.nutricook.viewmodel.auth.AuthViewModel
import com.example.nutricook.viewmodel.profile.ActivitiesViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    val authVm: AuthViewModel = hiltViewModel()
    val authState by authVm.uiState.collectAsState()

    // Share CreateRecipeViewModel across all recipe creation steps
    val createRecipeViewModel: CreateRecipeViewModel = hiltViewModel()

    val startDestination = if (authState.currentUser != null) "home" else "intro"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ========== INTRO & AUTH ==========
        composable("intro") {
            if (authState.currentUser != null) {
                LaunchedEffect(Unit) { navController.navigate("home") { popUpTo("intro") { inclusive = true } } }
            } else {
                IntroScreen(navController)
            }
        }
        composable("onboarding") {
            if (authState.currentUser != null) {
                LaunchedEffect(Unit) { navController.navigate("home") { popUpTo("onboarding") { inclusive = true } } }
            } else {
                OnboardingScreen(navController)
            }
        }
        composable("login") {
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
                onForgotPassword = { },
                vm = authVm
            )
        }
        composable("register") {
            RegisterScreen(
                onGoLogin = { navController.navigate("login") { launchSingleTop = true } },
                onBack = { navController.popBackStack() }
            )
        }

        // ========== MAIN TABS ==========

        // 1. HOME
        composable("home") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    HomeScreen(navController)
                }
            }
        }

        // 2. NEWSFEED (CỘNG ĐỒNG)
        composable("newsfeed") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    NewsfeedScreen(navController = navController)
                }
            }
        }

        // 3. RECIPES
        composable("recipes") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    RecipeDiscoveryScreen(navController)
                }
            }
        }
        composable("recipe_discovery") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    RecipeDiscoveryScreen(navController)
                }
            }
        }

        // 4. CATEGORIES
        composable("categories") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CategoriesScreen(navController)
                }
            }
        }
        composable("food_detail/{foodId}") { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId")?.toLongOrNull() ?: 0L
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    FoodDetailScreen(navController = navController, foodId = foodId)
                }
            }
        }

        // 5. PROFILE
        composable("profile") {
            ProfileScreen(
                onOpenSettings = { navController.navigate("settings") },
                onOpenRecent = {
                    val uid = authState.currentUser?.id ?: return@ProfileScreen
                    navController.navigate("recent_activity/$uid")
                },
                // [ĐÃ SỬA] Xóa onEditAvatar vì ProfileScreen không cần nữa
                onOpenPosts = {
                    val uid = authState.currentUser?.id ?: return@ProfileScreen
                    navController.navigate("posts/$uid")
                },
                onOpenSaves = {
                    val uid = authState.currentUser?.id ?: return@ProfileScreen
                    navController.navigate("saves/$uid")
                },
                onOpenSearch = { navController.navigate("search_profiles") },
                onNavigateToCalculator = { navController.navigate("add_meal") },
                bottomBar = { BottomNavigationBar(navController) }
            )
        }

        // ========== FEATURE DETAILS ==========

        composable(
            route = "posts/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    NewsfeedScreen() // Có thể custom để filter theo userId sau này
                }
            }
        }

        composable("search_profiles") {
            SearchProfileScreen(
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { uid -> navController.navigate("public_profile/$uid") }
            )
        }

        composable(
            route = "public_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            PublicProfileScreen(
                onBack = { navController.popBackStack() },
                onPostClick = { }
            )
        }

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

        composable(
            route = "recent_activity/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val vm: ActivitiesViewModel = hiltViewModel()
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    UserActivitiesScreen(onBack = { navController.popBackStack() }, vm = vm)
                }
            }
        }

        composable(
            route = "saves/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bài viết đã lưu (Chi tiết)")
                }
            }
        }

        // ========== RECIPE CREATION & DETAILS ==========
        composable("create_recipe") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep1Screen(
                        navController = navController
                    )
                }
            }
        }
        composable("create_recipe_step1") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep1Screen(
                        navController = navController
                    )
                }
            }
        }
        composable("create_recipe_step2") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep2Screen(
                        navController = navController,
                        createRecipeViewModel = createRecipeViewModel
                    )
                }
            }
        }
        composable("create_recipe_step3") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep3Screen(
                        navController = navController,
                        createRecipeViewModel = createRecipeViewModel
                    )
                }
            }
        }
        composable("create_recipe_step4") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep4Screen(
                        navController = navController,
                        createRecipeViewModel = createRecipeViewModel
                    )
                }
            }
        }
        composable("nutrition_facts") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    NutritionFactsScreen(navController = navController)
                }
            }
        }
        composable("upload_success") { RecipeUploadSuccessScreen(navController) }

        composable("recipe_detail/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeDetailScreen(navController, recipeTitle, imageRes)
        }
        composable("method_group_detail/{methodName}") { backStackEntry ->
            val methodName = backStackEntry.arguments?.getString("methodName") ?: ""
            RecipeDetailScreen(navController, methodName)
        }
        composable("recipe_info/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: "Unknown"
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeInfoScreen(navController, recipeTitle, imageRes)
        }

        composable("recipe_step") { RecipeStepScreen(navController) }
        composable("recipe_step2") { RecipeStep2Screen(navController) }
        composable("recipe_step_final") { RecipeStepFinalScreen(navController) }

        // Dynamic routes for user recipe steps - số màn hình dựa vào số bước đã tạo
        composable(
            route = "recipe_step_{stepIndex}",
            arguments = listOf(navArgument("stepIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val stepIndex = backStackEntry.arguments?.getInt("stepIndex") ?: 0
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    UserRecipeStepScreen(navController = navController, recipeId = "", stepIndex = stepIndex)
                }
            }
        }

        // User recipe info screen
        composable("user_recipe_info/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    UserRecipeInfoScreen(navController = navController, recipeId = recipeId)
                }
            }
        }

        // Dynamic routes for user recipe steps with recipe ID
        composable(
            route = "user_recipe_step_{recipeId}_{stepIndex}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.StringType },
                navArgument("stepIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            val stepIndex = backStackEntry.arguments?.getInt("stepIndex") ?: 0
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    UserRecipeStepScreen(
                        navController = navController,
                        recipeId = recipeId,
                        stepIndex = stepIndex
                    )
                }
            }
        }

        // User recipe nutrition facts screen
        composable("user_recipe_nutrition_facts/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    UserRecipeNutritionFactsScreen(navController = navController, recipeId = recipeId)
                }
            }
        }
        composable("recipe_direction") { RecipeDirectionsScreen(navController) }
        composable("recipe_guidance") { RecipeGuidanceScreen(navController) }
        composable("nutrition_facts") { NutritionFactsScreen(navController) }
        composable("review_screen") { ReviewScreen(navController) }

        // ========== INGREDIENTS & EXERCISES ==========
        composable("ingredients") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    IngredientsFilterScreen(navController = navController)
                }
            }
        }
        composable("ingredient_browser") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    IngredientBrowserScreen(navController = navController)
                }
            }
        }
        composable("ingredient_detail/{ingredientName}") { backStackEntry ->
            val ingredientName = backStackEntry.arguments?.getString("ingredientName") ?: ""
            IngredientDetailScreen(navController, ingredientName)
        }
        composable("nutrition_detail") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    NutritionDetailScreen(navController = navController)
                }
            }
        }

        composable("exercise_suggestions") { ExerciseSuggestionsScreen(navController) }

        composable("add_meal") {
            val nutritionVm: com.example.nutricook.viewmodel.nutrition.NutritionViewModel = hiltViewModel()
            val profileVm: com.example.nutricook.viewmodel.profile.ProfileViewModel = hiltViewModel()
            val nutritionState by nutritionVm.ui.collectAsState()
            val profileState by profileVm.uiState.collectAsState()
            val todayLog = nutritionState.todayLog
            val caloriesTarget = profileState.profile?.nutrition?.caloriesTarget ?: 2000f

            AddMealScreen(
                navController = navController,
                initialCalories = todayLog?.calories ?: 0f,
                initialProtein = todayLog?.protein ?: 0f,
                initialFat = todayLog?.fat ?: 0f,
                initialCarb = todayLog?.carb ?: 0f,
                caloriesTarget = caloriesTarget,
                onSave = { cal, pro, fat, carb ->
                    nutritionVm.updateTodayNutrition(cal, pro, fat, carb)
                }
            )
        }

        composable("custom_food_calculator") {
            val nutritionVm: com.example.nutricook.viewmodel.nutrition.NutritionViewModel = hiltViewModel()
            CustomFoodCalculatorScreen(
                navController = navController,
                onSave = { name, calories, protein, fat, carb ->
                    nutritionVm.updateTodayNutrition(calories, protein, fat, carb)
                }
            )
        }

        composable("exercise_detail/{exerciseName}/{imageRes}/{duration}/{calories}/{difficulty}") { backStackEntry ->
            val exerciseName = backStackEntry.arguments?.getString("exerciseName") ?: "Unknown"
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.baseball
            val duration = backStackEntry.arguments?.getString("duration") ?: "15 phút"
            val calories = backStackEntry.arguments?.getString("calories")?.toIntOrNull() ?: 100
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Trung bình"
            ExerciseDetailScreen(navController, exerciseName, imageRes, duration, calories, difficulty)
        }
        composable("article_detail") { ArticleDetailScreen(navController) }

        // ========== HOT NEWS ==========
        composable("all_hot_news") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    AllHotNewsScreen(navController)
                }
            }
        }
        composable("create_hot_news") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateHotNewsScreen(navController)
                }
            }
        }
        composable("hot_news_detail/{articleId}") { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    HotNewsDetailScreen(articleId = articleId, navController = navController)
                }
            }
        }

        // ========== NOTIFICATIONS & SEED ==========
        composable("notifications") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    NotificationsScreen(navController)
                }
            }
        }
        composable("seed_data") { DataSeedScreen(navController) }

        // [ĐÃ SỬA] Đã xóa route "edit_profile" vì chức năng này đã có trong SettingsScreen
    }
}