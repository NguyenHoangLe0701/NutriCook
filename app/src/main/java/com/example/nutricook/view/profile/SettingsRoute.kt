// file: com/example/nutricook/view/profile/SettingsRoute.kt
package com.example.nutricook.view.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.model.nutrition.Goal
import com.example.nutricook.viewmodel.profile.ProfileViewModel

/**
 * Route bọc SettingsScreen:
 * - Lấy targets & goal hiện tại từ ProfileViewModel
 * - Khi người dùng xác nhận trong dialog: gọi VM lưu -> flow phát state mới -> UI cập nhật
 */
@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel()
) {
    val targets by vm.targets.collectAsState()
    val prof by vm.nutrition.collectAsState()

    SettingsScreen(
        onBack = onBack,
        onLogout = onLogout,

        // Hiển thị chỉ số hiện tại
        caloriesTarget = targets.calories,
        proteinG = targets.proteinG,
        fatG = targets.fatG,
        carbG = targets.carbG,

        // Goal hiện tại để tick sẵn
        currentGoal = prof.goal,

        // Người dùng chọn goal + set thủ công chỉ số
        onGoalPicked = { goal: Goal, kcal: Int, pro: Double, fat: Double, carb: Double ->
            vm.setGoalWithManualTargets(goal, kcal, pro, fat, carb)
        },

        // Reset mặc định (theo logic recalc mặc định của bạn)
        onResetGoal = { vm.resetMyFat() },

        bottomBar = bottomBar
    )
}
