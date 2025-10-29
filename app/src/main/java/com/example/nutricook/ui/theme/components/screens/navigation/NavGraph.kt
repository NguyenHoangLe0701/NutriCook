package com.example.nutricook.ui.navigation
import com.example.nutricook.ui.theme.components.screens.notifications.NotificationsScreen
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
import com.example.nutricook.ui.theme.components.screens.recipes.IngredientsFilterScreen
import com.example.nutricook.R
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import com.example.nutricook.ui.theme.components.screens.home.NutritionDetailScreen
import com.example.nutricook.ui.theme.components.screens.recipes.IngredientDetailScreen
import com.example.nutricook.ui.screens.recipes.IngredientBrowserScreen
import com.example.nutricook.ui.screens.recipes.CreateRecipeScreen
import com.example.nutricook.ui.screens.recipes.RecipeDirectionsScreen
import com.example.nutricook.ui.screens.recipes.RecipeUploadSuccessScreen

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
   composable("recipes") { Scaffold( bottomBar = { BottomNavigationBar(navController) } ) { paddingValues -> Box( modifier = Modifier .padding(paddingValues) .fillMaxSize() ) { RecipeDiscoveryScreen(navController) } } }
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
      // ✅ Recipe Detail (chỉ 1 lần)
        composable("recipe_detail/{recipeTitle}/{imageRes}") { backStackEntry ->
            val recipeTitle = backStackEntry.arguments?.getString("recipeTitle") ?: ""
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: R.drawable.pizza
            RecipeDetailScreen(navController, recipeTitle, imageRes)
        }

        // ✅ Ingredients Filter
        composable("ingredients") {
            IngredientsFilterScreen(navController = navController)
        }

        // ✅ Ingredient Detail mới thêm
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
       composable("recipe_direction") {  RecipeDirectionsScreen(navController) }

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
        composable("notifications") {
             NotificationsScreen(navController)
            }
    }
}