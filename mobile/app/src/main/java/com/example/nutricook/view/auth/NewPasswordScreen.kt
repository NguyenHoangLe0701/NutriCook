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
import com.example.nutricook.view.auth.components.* // Import bộ component chung
import com.example.nutricook.viewmodel.auth.AuthEvent
import com.example.nutricook.viewmodel.auth.AuthViewModel

@Composable
fun NewPasswordScreen(
    oobCode: String, // Mã xác thực từ Deep Link
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Biến local để nhập liệu
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    // 1. Lắng nghe sự kiện Đổi mật khẩu thành công
    LaunchedEffect(uiState.isAuthSuccess) {
        // Lưu ý: Trong ViewModel, khi reset pass thành công, bạn nên set 1 biến cờ riêng
        // hoặc dùng message để nhận biết. Ở đây mình giả định check qua message hoặc cờ.
        // Tốt nhất là kiểm tra message "thành công" nếu isAuthSuccess không được bật (do chưa login)
        // Tuy nhiên, nếu ViewModel của bạn chưa có cờ riêng, ta có thể check message:
        if (uiState.message == "Đổi mật khẩu thành công!") {
            Toast.makeText(context, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
            onNavigateToLogin()
        }
    }

    // 2. Lắng nghe thông báo lỗi
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            if (it != "Đổi mật khẩu thành công!") { // Tránh hiện toast 2 lần
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
            viewModel.onEvent(AuthEvent.ConsumeMessage)
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
            Spacer(modifier = Modifier.height(40.dp))

            // Tiêu đề
            BigAuthTitle("Mật khẩu mới")
            Spacer(modifier = Modifier.height(12.dp))
            BigAuthSubtitle("Nhập mật khẩu mới đủ mạnh và đừng quên nó nữa nhé! \uD83E\uDD2B") // Icon mặt méo miệng

            Spacer(modifier = Modifier.height(50.dp))

            // Ô nhập Mật khẩu mới
            BigAuthTextField(
                value = newPass,
                onValueChange = { newPass = it },
                placeholder = "Mật khẩu mới",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Ô nhập Nhập lại mật khẩu
            BigAuthTextField(
                value = confirmPass,
                onValueChange = {
                    confirmPass = it
                    // Cập nhật vào ViewModel để check khớp
                    viewModel.onEvent(AuthEvent.ConfirmPasswordChanged(it))
                },
                placeholder = "Nhập lại mật khẩu",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Nút Xác nhận
            BigAuthButton(
                text = "Đổi mật khẩu",
                isLoading = uiState.isLoading,
                onClick = {
                    if (newPass.length < 6) {
                        Toast.makeText(context, "Mật khẩu phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show()
                    } else if (newPass != confirmPass) {
                        Toast.makeText(context, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show()
                    } else {
                        // Gửi sự kiện đổi mật khẩu kèm mã oobCode
                        viewModel.onEvent(AuthEvent.SubmitResetNewPassword(oobCode, newPass))
                    }
                }
            )
        }
    }
}