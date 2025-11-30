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
import androidx.navigation.NavController
import com.example.nutricook.data.nutrition.GeminiNutritionService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.EntryPointAccessors

@EntryPoint
@InstallIn(ActivityComponent::class)
interface GeminiServiceEntryPointForCalculator {
    fun geminiService(): GeminiNutritionService
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFoodCalculatorScreen(
    navController: NavController,
    onSave: (String, Float, Float, Float, Float) -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    
    // Inject GeminiNutritionService
    val context = LocalContext.current
    val geminiService = remember {
        val activity = context as? androidx.activity.ComponentActivity ?: null
        if (activity != null) {
            EntryPointAccessors.fromActivity(
                activity,
                GeminiServiceEntryPointForCalculator::class.java
            ).geminiService()
        } else {
            null
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tính calories tự động", fontWeight = FontWeight.Bold) },
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
                CustomFoodInputDialog(
                    onDismiss = { 
                        showDialog = false
                        navController.popBackStack()
                    },
                    onAdd = { food ->
                        onSave(food.name, food.calories, food.protein, food.fat, food.carb)
                        showDialog = false
                        navController.popBackStack()
                    },
                    geminiService = geminiService
                )
            }
        }
    }
}

