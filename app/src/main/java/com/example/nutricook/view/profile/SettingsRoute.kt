package com.example.nutricook.view.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.model.nutrition.Goal
import com.example.nutricook.viewmodel.profile.ProfileViewModel

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val pvm: ProfileViewModel = hiltViewModel()
    val tgt = pvm.targets.collectAsState().value

    SettingsScreen(
        onBack = onBack,
        onLogout = onLogout,

        // Hiển thị mục tiêu hiện tại
        caloriesTarget = tgt.calories,
        proteinG = tgt.proteinG,
        fatG = tgt.fatG,
        carbG = tgt.carbG,

        // Đổi Goal và ghi đè chỉ số thủ công
        onGoalPicked = { goal: Goal, kcal: Int, pro: Double, fat: Double, carb: Double ->
            pvm.setGoalWithManualTargets(goal, kcal, pro, fat, carb)
        },

        // Reset về mặc định
        onResetGoal = { pvm.resetMyFat() }
    )
}
