package com.example.nutricook.view.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.view.auth.components.* // Import components giao diện chung (BigAuthTitle, etc.)
import com.example.nutricook.viewmodel.auth.AuthEvent
import com.example.nutricook.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun VerifyEmailScreen(
    email: String,
    onNavigateToHome: () -> Unit, // Callback để vào Home sau 5s
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Biến đếm ngược (Khởi đầu là 5 giây)
    var countdown by remember { mutableIntStateOf(5) }

    // 1. Kích hoạt việc kiểm tra trạng thái Email định kỳ (mỗi 3s)
    LaunchedEffect(Unit) {
        vm.startEmailVerificationCheck()
    }

    // 2. Lắng nghe thông báo (Toast/Snackbar) từ ViewModel
    LaunchedEffect(state.message) {
        state.message?.let {
            // Chỉ hiện thông báo nếu không phải là thông báo thành công (vì thành công đã có UI riêng)
            if (!state.isEmailVerified) {
                snackbarHostState.showSnackbar(it)
                vm.onEvent(AuthEvent.ConsumeMessage)
            }
        }
    }

    // 3. LOGIC TỰ ĐỘNG CHUYỂN MÀN HÌNH
    // Khi phát hiện email đã verify -> Đếm ngược -> Chuyển trang
    LaunchedEffect(state.isEmailVerified) {
        if (state.isEmailVerified) {
            // Vòng lặp đếm ngược
            while (countdown > 0) {
                delay(1000L) // Chờ 1 giây
                countdown--
            }
            // Hết giờ -> Vào Home
            onNavigateToHome()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.White // Nền trắng chuẩn
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- TRƯỜNG HỢP 1: ĐÃ XÁC THỰC THÀNH CÔNG ---
            if (state.isEmailVerified) {
                // Icon tick xanh to
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = BrandColor, // Màu xanh ngọc của App
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Xác thực thành công!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hiển thị đếm ngược
                Text(
                    text = "Đang đưa bạn đến trang chủ...",
                    fontSize = 16.sp,
                    color = TextGray
                )
                Text(
                    text = "trong $countdown giây",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Thanh loading nhỏ bên dưới
                Spacer(modifier = Modifier.height(24.dp))
                LinearProgressIndicator(
                    modifier = Modifier.width(150.dp),
                    color = BrandColor,
                    trackColor = Color(0xFFE5E7EB)
                )

            }
            // --- TRƯỜNG HỢP 2: CHƯA XÁC THỰC (ĐANG CHỜ) ---
            else {
                Text(text = "📩", fontSize = 80.sp)

                Spacer(modifier = Modifier.height(32.dp))

                BigAuthTitle("Kiểm tra hộp thư")

                Spacer(modifier = Modifier.height(16.dp))

                BigAuthSubtitle("Chúng tôi đã gửi liên kết xác thực đến:")
                Text(
                    text = email,
                    fontSize = 18.sp,
                    color = BrandColor,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                BigAuthSubtitle("Vui lòng bấm vào liên kết trong email. Màn hình này sẽ tự động cập nhật ngay khi bạn xác thực xong.")

                Spacer(modifier = Modifier.height(30.dp))

                // Loading spinner để user biết app đang chạy ngầm
                CircularProgressIndicator(
                    color = BrandColor,
                    modifier = Modifier.size(40.dp),
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Đang chờ xác nhận...",
                    color = TextGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top=12.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Nút gửi lại (ẩn đi nếu đã verify)
                TextButton(
                    onClick = { vm.onEvent(AuthEvent.ResendEmailVerification) }
                ) {
                    Text(
                        text = "Chưa nhận được email? Gửi lại",
                        color = TextGray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}