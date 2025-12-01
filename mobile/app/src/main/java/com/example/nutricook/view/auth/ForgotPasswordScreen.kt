package com.example.nutricook.view.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.view.auth.components.* // Import components từ package mới
import com.example.nutricook.viewmodel.auth.AuthEvent
import com.example.nutricook.viewmodel.auth.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(AuthEvent.ConsumeMessage)
        }
    }

    Scaffold(
        topBar = {
            IconButton(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            BigAuthTitle("Quên mật khẩu")
            Spacer(modifier = Modifier.height(12.dp))
            BigAuthSubtitle("Đừng lo! Nhập email của bạn để lấy lại mật khẩu nhé. \uD83E\uDD14")

            Spacer(modifier = Modifier.height(50.dp))

            BigAuthTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEvent(AuthEvent.EmailChanged(it)) },
                placeholder = "Nhập Email",
                icon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(40.dp))

            BigAuthButton(
                text = "Gửi yêu cầu",
                isLoading = uiState.isLoading,
                onClick = { viewModel.onEvent(AuthEvent.SubmitForgotPassword(uiState.email)) }
            )
        }
    }
}