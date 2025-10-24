package com.example.nutricook.view.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.nutricook.view.auth.LoginScreen
import com.example.nutricook.view.auth.RegisterScreen
import com.example.nutricook.view.home.HomeScreen
import com.example.nutricook.viewmodel.auth.AuthViewModel
import com.example.nutricook.viewmodel.auth.AuthEvent

private object Routes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val vm: AuthViewModel = hiltViewModel()
    val authState by vm.uiState.collectAsState()

    // Điều khiển điều hướng tập trung tại đây
    LaunchedEffect(authState.currentUser) {
        val user = authState.currentUser
        if (user != null) {
            // Đã đăng nhập - điều hướng đến Home
            if (nav.currentDestination?.route != Routes.HOME) {
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                    launchSingleTop = true
                }
            }
        } else {
            // Chưa đăng nhập - điều hướng đến Auth
            if (nav.currentDestination?.route != Routes.LOGIN &&
                nav.currentDestination?.route != Routes.REGISTER) {
                nav.navigate(Routes.AUTH) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = nav, startDestination = Routes.AUTH) {

        // Nhóm Auth
        navigation(startDestination = Routes.LOGIN, route = Routes.AUTH) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onGoRegister = {
                        nav.navigate(Routes.REGISTER)
                    }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onGoLogin = {
                        nav.popBackStack()
                    }
                )
            }
        }

        // Home
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    // Logout sẽ được xử lý bởi LaunchedEffect ở trên
                    vm.onEvent(AuthEvent.Logout)
                }
            )
        }
    }
}