package com.example.nutricook.view.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.view.auth.components.* // Import components
import com.example.nutricook.viewmodel.auth.AuthEvent
import com.example.nutricook.viewmodel.auth.AuthViewModel

@Composable
fun NewPasswordScreen(
    oobCode: String,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isAuthSuccess) {
        if (uiState.isAuthSuccess) {
            Toast.makeText(context, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show()
            onNavigateToLogin()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            if (!uiState.isAuthSuccess) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.onEvent(AuthEvent.ConsumeMessage)
            }
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            BigAuthTitle("Mật khẩu mới")
            Spacer(modifier = Modifier.height(12.dp))
            BigAuthSubtitle("Nhập mật khẩu mới và đừng quên nó nữa nhé! \uD83E\uDD2B")

            Spacer(modifier = Modifier.height(50.dp))

            BigAuthTextField(
                value = newPass,
                onValueChange = { newPass = it },
                placeholder = "Mật khẩu mới",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            BigAuthTextField(
                value = confirmPass,
                onValueChange = {
                    confirmPass = it
                    viewModel.onEvent(AuthEvent.ConfirmPasswordChanged(it))
                },
                placeholder = "Nhập lại mật khẩu",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            BigAuthButton(
                text = "Đổi mật khẩu",
                isLoading = uiState.isLoading,
                onClick = {
                    viewModel.onEvent(AuthEvent.SubmitResetNewPassword(oobCode, newPass))
                }
            )
        }
    }
}