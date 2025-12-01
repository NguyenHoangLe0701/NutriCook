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
    // 👇 THÊM: Callback điều hướng đến màn hình nhập mã thủ công
    onNavigateToManualCodeReset: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Xử lý khi gửi email thành công -> Chuyển màn hình
    LaunchedEffect(uiState.isAuthSuccess) {
        // Kiểm tra cờ isAuthSuccess (được set true khi gửi email thành công trong ViewModel)
        if (uiState.isAuthSuccess && uiState.message?.contains("khôi phục") == true) {
            // Hiển thị thông báo (Toast)
            Toast.makeText(context, "Đã gửi email khôi phục. Kiểm tra hộp thư.", Toast.LENGTH_LONG).show()

            // Xóa cờ thành công để không bị kích hoạt lại khi quay lại màn hình
            viewModel.onEvent(AuthEvent.ConsumeAuthSuccess)

            // Điều hướng sang màn hình nhập mã
            onNavigateToManualCodeReset()
        }
    }

    // Xử lý thông báo lỗi/thành công từ ViewModel (Toast)
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            // Chỉ hiện Toast nếu không phải là thông báo thành công (tránh double-Toast khi chuyển trang)
            if (it.contains("khôi phục").not()) {
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

            BigAuthTitle("Quên mật khẩu")
            Spacer(modifier = Modifier.height(12.dp))
            BigAuthSubtitle("Đừng lo! Nhập email của bạn để gửi yêu cầu lấy lại mật khẩu. Bạn sẽ nhận được Mã khôi phục (oobCode) qua email. \uD83E\uDD14")

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
                onClick = {
                    if (uiState.email.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập Email", Toast.LENGTH_SHORT).show()
                    } else {
                        // Gửi sự kiện gửi email
                        viewModel.onEvent(AuthEvent.SubmitForgotPassword(uiState.email))
                    }
                }
            )
        }
    }
}