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
import androidx.navigation.navDeepLink
import com.example.nutricook.R
// Import các màn hình Auth
import com.example.nutricook.view.auth.ForgotPasswordScreen
import com.example.nutricook.view.auth.ManualResetCodeScreen
import com.example.nutricook.view.auth.NewPasswordScreen
import com.example.nutricook.view.auth.PhoneVerificationScreen
import com.example.nutricook.view.articles.ArticleDetailScreen
import com.example.nutricook.view.auth.LoginScreen
import com.example.nutricook.view.auth.RegisterScreen
import com.example.nutricook.view.auth.VerifyEmailScreen
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
import com.example.nutricook.view.profile.FollowListScreen // Import FollowListScreen
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
import com.example.nutricook.view.recipes.RecipeDirectionsScreen
import com.example.nutricook.view.recipes.RecipeDiscoveryScreen
import com.example.nutricook.view.recipes.RecipeInfoScreen
import com.example.nutricook.view.recipes.MethodGroupDetailScreen
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

    val createRecipeViewModel: CreateRecipeViewModel = hiltViewModel()

    // Luôn bắt đầu từ intro để hiển thị màn hình intro cho mọi người dùng
    val startDestination = "intro"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ========== INTRO & AUTH ==========
        composable("intro") {
            IntroScreen(navController = navController, authViewModel = authVm)
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
                onForgotPassword = { navController.navigate("forgot_password") },
                vm = authVm
            )
        }

        // Màn hình Đăng ký: Xong thì sang Verify Email
        composable("register") {
            RegisterScreen(
                onGoLogin = { navController.navigate("login") { launchSingleTop = true } },
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { email ->
                    navController.navigate("verify_email/$email") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // Màn hình Verify Email: Xong thì vào Home
        composable(
            route = "verify_email/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyEmailScreen(
                email = email,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("intro") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Forgot Password -> Chuyển sang Manual Code Reset
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToManualCodeReset = { navController.navigate("manual_code_reset") }
            )
        }

        // Nhập Mã Khôi Phục Thủ Công
        composable("manual_code_reset") {
            ManualResetCodeScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("forgot_password") { inclusive = true } // Xóa màn hình Forgot Password khỏi backstack
                    }
                }
            )
        }

        composable("phone_verification") {
            PhoneVerificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Màn hình New Password (Giữ lại cho Deep Link)
        composable(
            route = "new_password?oobCode={oobCode}",
            arguments = listOf(
                navArgument("oobCode") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "https://nutricook-fff8f.firebaseapp.com/__/auth/action?apiKey={apiKey}&mode=resetPassword&oobCode={oobCode}&continueUrl={continueUrl}&lang={lang}" }
            )
        ) { backStackEntry ->
            val oobCode = backStackEntry.arguments?.getString("oobCode") ?: ""

            if (oobCode.isNotBlank()) {
                NewPasswordScreen(
                    oobCode = oobCode,
                    onNavigateToLogin = {
                        navController.navigate("login") {
                            popUpTo("intro") { inclusive = true }
                        }
                    }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Lỗi: Không tìm thấy mã khôi phục oobCode. Vui lòng thử lại từ email.")
                }
            }
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
                navController = navController,
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
                onOpenSearch = { navController.navigate("search_profiles") },
                onNavigateToCalculator = { navController.navigate("add_meal") },

                // [CẬP NHẬT] Điều hướng Follow List kèm tên
                onOpenFollowers = { uid, name ->
                    navController.navigate("follow_list/$uid/0?name=$name")
                },
                onOpenFollowing = { uid, name ->
                    navController.navigate("follow_list/$uid/1?name=$name")
                },

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
                    NewsfeedScreen()
                }
            }
        }

        // [MỚI] Màn hình danh sách Follow nhận thêm biến "name"
        composable(
            route = "follow_list/{userId}/{initialTab}?name={name}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("initialTab") { type = NavType.IntType },
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = "Danh sách"
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
            val targetName = backStackEntry.arguments?.getString("name") ?: "Danh sách"

            FollowListScreen(
                userId = userId,
                initialTab = initialTab,
                targetName = targetName, // Truyền tên vào Header
                onBack = { navController.popBackStack() },
                onUserClick = { targetUid ->
                    navController.navigate("public_profile/$targetUid")
                }
            )
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
                onPostClick = { },
                // [CẬP NHẬT] Điều hướng Follow List từ Public Profile
                onOpenFollowers = { uid, name ->
                    navController.navigate("follow_list/$uid/0?name=$name")
                },
                onOpenFollowing = { uid, name ->
                    navController.navigate("follow_list/$uid/1?name=$name")
                }
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
                        navController = navController,
                        createRecipeViewModel = createRecipeViewModel
                    )
                }
            }
        }
        composable("create_recipe_step1") {
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep1Screen(
                        navController = navController,
                        createRecipeViewModel = createRecipeViewModel
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
                    NutritionFactsScreen(
                        navController = navController,
                        createRecipeViewModel = createRecipeViewModel
                    )
                }
            }
        }
        composable("upload_success") { RecipeUploadSuccessScreen(navController) }

        composable("recipe_info/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: "Unknown"
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeInfoScreen(navController, recipeTitle, imageRes)
        }
        composable("method_group_detail/{methodName}") { backStackEntry ->
            val methodName = backStackEntry.arguments?.getString("methodName") ?: ""
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    MethodGroupDetailScreen(navController, methodName)
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

        // Edit recipe route
        composable(
            route = "edit_recipe_{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            LaunchedEffect(recipeId) {
                if (recipeId.isNotEmpty()) {
                    createRecipeViewModel.loadRecipeForEdit(recipeId)
                }
            }
            Scaffold(bottomBar = { BottomNavigationBar(navController) }) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    CreateRecipeStep1Screen(
                        navController = navController,
                        createRecipeViewModel = createRecipeViewModel
                    )
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
        composable("nutrition_facts") {
            NutritionFactsScreen(
                navController = navController,
                createRecipeViewModel = createRecipeViewModel
            )
        }
        composable(
            route = "review_screen/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
            ReviewScreen(navController = navController, recipeId = recipeId)
        }

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

            // Lấy dateId từ state (nếu đang xem ngày khác)
            val selectedDateId = nutritionState.selectedDateId
            val displayLog = if (selectedDateId != null) {
                nutritionState.selectedDateLog
            } else {
                nutritionState.todayLog
            }

            val caloriesTarget = profileState.profile?.nutrition?.caloriesTarget ?: 2000f

            AddMealScreen(
                navController = navController,
                initialCalories = displayLog?.calories ?: 0f,
                initialProtein = displayLog?.protein ?: 0f,
                initialFat = displayLog?.fat ?: 0f,
                initialCarb = displayLog?.carb ?: 0f,
                caloriesTarget = caloriesTarget,
                selectedDateId = selectedDateId,
                onSave = { cal, pro, fat, carb ->
                    if (selectedDateId != null) {
                        nutritionVm.updateNutritionForDate(selectedDateId, cal, pro, fat, carb)
                    } else {
                        nutritionVm.updateTodayNutrition(cal, pro, fat, carb)
                    }
                }
            )
        }

        composable("custom_food_calculator") {
            val nutritionVm: com.example.nutricook.viewmodel.nutrition.NutritionViewModel = hiltViewModel()
            CustomFoodCalculatorScreen(
                navController = navController,
                onSave = { _, calories, protein, fat, carb ->
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

    }
}