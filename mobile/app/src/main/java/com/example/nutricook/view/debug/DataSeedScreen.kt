package com.example.nutricook.view.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nutricook.viewmodel.SeedViewModel
import kotlinx.coroutines.launch

@Composable
fun DataSeedScreen(navController: NavController, vm: SeedViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    val status by vm.status

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔧 Data Seeder", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        
        Text(
            "Push sample data from app to Firestore.\nAfter seeding, queries will fetch real data.",
            style = MaterialTheme.typography.bodySmall
        )
        
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { scope.launch { vm.seedReviews() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("📝 Seed Reviews")
        }
        Spacer(Modifier.height(12.dp))
        
        Button(
            onClick = { scope.launch { vm.seedCategories() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🍱 Seed Categories")
        }
        Spacer(Modifier.height(12.dp))
        
        Button(
            onClick = { scope.launch { vm.seedRecipes() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🍳 Seed Recipes")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedVegetables() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🥕 Seed Vegetables")
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedFruits() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🍎 Seed Fruits")
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedSeafood() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🐟 Seed Seafood")
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedAllFoods() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text("🌾 Seed ALL Foods")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedIngredients() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🛒 Seed Ingredients")
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedMealTypes() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🍽️ Seed Meal Types")
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedDietTypes() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🥗 Seed Diet Types")
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedCookingTips() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("👨‍🍳 Seed Cooking Tips")
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { scope.launch { vm.seedCalorieInfo() } },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("📊 Seed Calorie Info")
        }

        Spacer(Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                "Status: $status",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
