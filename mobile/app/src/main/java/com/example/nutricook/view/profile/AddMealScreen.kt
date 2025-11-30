package com.example.nutricook.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nutricook.data.nutrition.GeminiNutritionService
import com.example.nutricook.viewmodel.nutrition.NutritionViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.EntryPointAccessors

@EntryPoint
@InstallIn(ActivityComponent::class)
interface GeminiServiceEntryPointForAddMeal {
    fun geminiService(): GeminiNutritionService
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    navController: NavController,
    initialCalories: Float,
    initialProtein: Float,
    initialFat: Float,
    initialCarb: Float,
    caloriesTarget: Float,
    onSave: (Float, Float, Float, Float) -> Unit,
    nutritionVm: NutritionViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(true) }
    
    // Inject GeminiNutritionService
    val context = LocalContext.current
    val geminiService = remember {
        val activity = context as? androidx.activity.ComponentActivity ?: null
        if (activity != null) {
            EntryPointAccessors.fromActivity(
                activity,
                GeminiServiceEntryPointForAddMeal::class.java
            ).geminiService()
        } else {
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm bữa ăn", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showDialog) {
                ProfessionalNutritionDialog(
                    initialCalories = initialCalories,
                    initialProtein = initialProtein,
                    initialFat = initialFat,
                    initialCarb = initialCarb,
                    caloriesTarget = caloriesTarget,
                    onDismiss = { 
                        showDialog = false
                        navController.popBackStack()
                    },
                    onSave = { cal, pro, fat, carb ->
                        onSave(cal, pro, fat, carb)
                        showDialog = false
                        navController.popBackStack()
                    },
                    geminiService = geminiService,
                    onNavigateToCalculator = {
                        showDialog = false
                        navController.navigate("custom_food_calculator")
                    }
                )
            }
        }
    }
}

