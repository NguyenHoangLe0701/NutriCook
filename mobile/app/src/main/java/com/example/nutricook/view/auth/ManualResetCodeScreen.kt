package com.example.nutricook.view.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.view.auth.components.*
import com.example.nutricook.viewmodel.auth.AuthEvent
import com.example.nutricook.viewmodel.auth.AuthViewModel

@Composable
fun ManualResetCodeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var codeInput by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    // Xử lý khi đổi mật khẩu thành công
    LaunchedEffect(uiState.isAuthSuccess) {
        if (uiState.isAuthSuccess && uiState.message == "Đổi mật khẩu thành công!") {
            Toast.makeText(context, "Đổi mật khẩu thành công! Hãy đăng nhập lại.", Toast.LENGTH_LONG).show()
            viewModel.onEvent(AuthEvent.ConsumeAuthSuccess) // Xóa cờ thành công
            onNavigateToLogin()
        }
    }

    // Xử lý thông báo lỗi/thành công từ ViewModel
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            if (it != "Đổi mật khẩu thành công!") {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
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

            BigAuthTitle("Nhập Mã Khôi Phục")
            Spacer(modifier = Modifier.height(12.dp))
            BigAuthSubtitle("Vui lòng kiểm tra email để lấy Mã khôi phục (oobCode) và thiết lập mật khẩu mới.")

            Spacer(modifier = Modifier.height(50.dp))

            // 1. Ô nhập Mã khôi phục (oobCode)
            BigAuthTextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                placeholder = "Mã khôi phục (oobCode) từ email",
                icon = Icons.Default.Key
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Ô nhập Mật khẩu mới
            BigAuthTextField(
                value = newPass,
                onValueChange = { newPass = it },
                placeholder = "Mật khẩu mới",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Ô nhập lại Mật khẩu
            BigAuthTextField(
                value = confirmPass,
                onValueChange = {
                    confirmPass = it
                    // Cần cập nhật confirmPass để ViewModel check khớp
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
                    if (newPass != confirmPass) {
                        Toast.makeText(context, "Mật khẩu nhập lại không khớp.", Toast.LENGTH_SHORT).show()
                        return@BigAuthButton
                    }
                    if (codeInput.isBlank() || newPass.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
                        return@BigAuthButton
                    }

                    // Gửi mã khôi phục và mật khẩu mới lên ViewModel
                    viewModel.onEvent(AuthEvent.SubmitResetNewPassword(codeInput, newPass))
                }
            )
        }
    }
}