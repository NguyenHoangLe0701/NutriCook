package com.example.nutricook.view.nav

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

// -------------------- Routes --------------------
private object Routes {
    const val INTRO = "intro"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Bottom tabs
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val RECIPES = "recipes"
    const val PROFILE = "profile"

    // Others used by HomeScreen
    const val RECIPE_DISCOVERY = "recipe_discovery"
    const val RECIPE_DETAIL = "recipe_detail/{recipeTitle}" // HomeScreen đang navigate 1 param
    const val INGREDIENTS = "ingredients"
    const val INGREDIENT_DETAIL = "ingredient_detail/{ingredientName}"
    const val INGREDIENT_BROWSER = "ingredient_browser"
    const val NUTRITION_DETAIL = "nutrition_detail"
    const val CREATE_RECIPE = "create_recipe"
    const val UPLOAD_SUCCESS = "upload_success"
    const val RECIPE_DIRECTION = "recipe_direction"
    const val RECIPE_GUIDANCE = "recipe_guidance"
    const val EXERCISE_SUGGESTIONS = "exercise_suggestions"
    const val RECENT_ACTIVITY = "recent_activity"
    const val POSTS = "posts"
    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"

    // Extra placeholders
    const val RECIPE_SUGGESTIONS = "recipe_suggestions"
    const val NEWS_FEEDS = "news_feeds"
    const val NEWS_DETAIL = "news_detail/{title}"
}

private val ProtectedRoutes = listOf(
    Routes.HOME, Routes.CATEGORIES, Routes.RECIPES, Routes.PROFILE,
    Routes.RECIPE_DISCOVERY, Routes.RECIPE_DETAIL, Routes.INGREDIENTS,
    Routes.INGREDIENT_DETAIL, Routes.INGREDIENT_BROWSER, Routes.NUTRITION_DETAIL,
    Routes.CREATE_RECIPE, Routes.UPLOAD_SUCCESS, Routes.RECIPE_DIRECTION,
    Routes.RECIPE_GUIDANCE, Routes.EXERCISE_SUGGESTIONS, Routes.RECENT_ACTIVITY,
    Routes.POSTS, Routes.EDIT_PROFILE, Routes.SETTINGS, Routes.NOTIFICATIONS,
    Routes.RECIPE_SUGGESTIONS, Routes.NEWS_FEEDS, Routes.NEWS_DETAIL
)

// -------------------- BottomBar --------------------
data class BottomNavItem(
    val title: String,
    val iconRes: Int,
    val iconResSelected: Int,
    val route: String
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Trang chủ", R.drawable.ic_home, R.drawable.ic_home_filled, Routes.HOME),
        BottomNavItem("Khám phá", R.drawable.ic_category, R.drawable.ic_category_filled, Routes.CATEGORIES),
        BottomNavItem("Hoạt động", R.drawable.ic_recipe, R.drawable.ic_recipe_filled, Routes.RECIPES),
        BottomNavItem("Tôi", R.drawable.ic_profile, R.drawable.ic_profile_filled, Routes.PROFILE)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color.White)
    ) {
        val density = LocalDensity.current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val jumpDp = 6.dp
                val targetTranslationPx = with(density) { if (isSelected) -jumpDp.toPx() else 0f }
                val animatedTranslationY by animateFloatAsState(
                    targetValue = targetTranslationPx,
                    animationSpec = tween(durationMillis = 180)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            }
                        }
                        .graphicsLayer { translationY = animatedTranslationY }
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isSelected) item.iconResSelected else item.iconRes
                            ),
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isSelected) {
                        Text(text = item.title, fontSize = 12.sp, color = Color(0xFF12B3AD))
                    }
                }
            }
        }
    }
}

// -------------------- AppNav --------------------
@Composable
fun AppNav() {
    val nav = rememberNavController()

    // Theo dõi trạng thái đăng nhập
    val vm: AuthViewModel = hiltViewModel()
    val authState by vm.uiState.collectAsState()

    // BẮT ĐĂNG NHẬP: nếu chưa có user -> startDestination = LOGIN
    val startDest = if (authState.currentUser != null) Routes.HOME else Routes.LOGIN

    // Auto-redirect khi thay đổi đăng nhập
    LaunchedEffect(authState.currentUser) {
        val user = authState.currentUser
        val current = nav.currentDestination?.route
        if (user != null) {
            if (current in setOf(Routes.LOGIN, Routes.REGISTER, Routes.INTRO, Routes.ONBOARDING) || current == null) {
                nav.navigate(Routes.HOME) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else {
            if (current != null && ProtectedRoutes.any { current.startsWith(it.substringBefore("/{")) }) {
                nav.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = nav, startDestination = startDest) {

        // ---------- Auth ----------
        composable(Routes.LOGIN) {
            LoginScreen(
                onGoRegister = { nav.navigate(Routes.REGISTER) },
                onBack = { /* Ở LOGIN không cần back */ },
                onForgotPassword = { /* TODO */ }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onGoLogin = { nav.popBackStack() },
                onBack = { nav.popBackStack() }
            )
        }

        // (optional) giới thiệu/onboarding
        composable(Routes.INTRO) { IntroScreen(nav) }
        composable(Routes.ONBOARDING) { OnboardingScreen(nav) }

        // ---------- Tabs có bottom bar ----------
        composable(Routes.HOME) {
            Scaffold(bottomBar = { BottomNavigationBar(nav) }) { padding ->
                Box(Modifier.padding(padding)) { HomeScreen(nav) }
            }
        }
        composable(Routes.CATEGORIES) {
            Scaffold(bottomBar = { BottomNavigationBar(nav) }) { padding ->
                Box(Modifier.padding(padding)) { CategoriesScreen(nav) }
            }
        }
        composable(Routes.RECIPES) {
            Scaffold(bottomBar = { BottomNavigationBar(nav) }) { padding ->
                Box(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) { RecipeDiscoveryScreen(nav) }
            }
        }

        // ---------- PROFILE KHÔNG có bottom bar + truyền onLogout ----------
        composable(Routes.PROFILE) {
            Scaffold { padding ->
                Box(Modifier.padding(padding)) {
                    ProfileScreen(
                        onOpenSettings = { /* nav.navigate(Routes.SETTINGS) */ },
                        onEditAvatar = { /* TODO: picker ảnh */ },
                        onLogout = {
                            vm.onEvent(AuthEvent.Logout)           // <- logout thật
                            nav.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }    // clear backstack
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

        // ---------- Others ----------
        composable(Routes.RECIPE_DISCOVERY) { RecipeDiscoveryScreen(nav) }
        composable(Routes.RECIPE_DETAIL) { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
            RecipeDetailScreen(nav, recipeTitle, R.drawable.pizza)
        }
        composable(Routes.INGREDIENTS) { IngredientsFilterScreen(navController = nav) }
        composable(Routes.INGREDIENT_DETAIL) { backStackEntry ->
            val ingredientName = backStackEntry.arguments?.getString("ingredientName") ?: ""
            IngredientDetailScreen(nav, ingredientName)
        }
        composable(Routes.INGREDIENT_BROWSER) { IngredientBrowserScreen(navController = nav) }
        composable(Routes.NUTRITION_DETAIL) { NutritionDetailScreen(navController = nav) }
        composable(Routes.CREATE_RECIPE) { CreateRecipeScreen(navController = nav) }
        composable(Routes.UPLOAD_SUCCESS) { RecipeUploadSuccessScreen(navController = nav) }
        composable(Routes.RECIPE_DIRECTION) { RecipeDirectionsScreen(nav) }
        composable(Routes.RECIPE_GUIDANCE) { RecipeGuidanceScreen(nav) }
        composable(Routes.EXERCISE_SUGGESTIONS) { ExerciseSuggestionsScreen(nav) }

        // placeholders nếu cần
        composable(Routes.RECENT_ACTIVITY) {
            Scaffold { padding -> Box(Modifier.padding(padding)) { Text("Recent Activity Screen", Modifier.padding(16.dp)) } }
        }
        composable(Routes.POSTS) {
            Scaffold { padding -> Box(Modifier.padding(padding)) { Text("Posts Screen", Modifier.padding(16.dp)) } }
        }
        composable(Routes.EDIT_PROFILE) {
            Scaffold { padding -> Box(Modifier.padding(padding)) { Text("Edit Profile Screen", Modifier.padding(16.dp)) } }
        }
        composable(Routes.SETTINGS) {
            Scaffold { padding -> Box(Modifier.padding(padding)) { Text("Settings Screen", Modifier.padding(16.dp)) } }
        }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(nav) }
    }
}
