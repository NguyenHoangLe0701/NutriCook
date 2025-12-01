package com.example.nutricook.view.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
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
fun PhoneVerificationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var phoneNumberInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

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

            if (!uiState.isOtpSent) {
                BigAuthTitle("Xác thực SĐT")
                Spacer(modifier = Modifier.height(12.dp))
                BigAuthSubtitle("Nhập số điện thoại để nhận mã xác thực.")
            } else {
                BigAuthTitle("Nhập mã OTP")
                Spacer(modifier = Modifier.height(12.dp))
                BigAuthSubtitle("Mã 6 số đã được gửi tới ${phoneNumberInput}")
            }

            Spacer(modifier = Modifier.height(50.dp))

            if (!uiState.isOtpSent) {
                // Màn hình nhập SĐT
                BigAuthTextField(
                    value = phoneNumberInput,
                    onValueChange = { phoneNumberInput = it },
                    placeholder = "Số điện thoại (+84...)",
                    icon = Icons.Default.Phone
                )

                Spacer(modifier = Modifier.height(40.dp))

                BigAuthButton(
                    text = "Gửi mã OTP",
                    isLoading = uiState.isLoading,
                    onClick = {
                        val activity = context as? Activity
                        if (activity != null && phoneNumberInput.isNotBlank()) {
                            val formattedPhone = if (phoneNumberInput.startsWith("0"))
                                "+84${phoneNumberInput.substring(1)}"
                            else phoneNumberInput

                            viewModel.onEvent(AuthEvent.SendPhoneOtp(activity, formattedPhone))
                        } else {
                            Toast.makeText(context, "SĐT không hợp lệ", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                // Màn hình nhập OTP
                BigAuthTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 6) otpInput = it },
                    placeholder = "Nhập mã 6 số",
                    icon = Icons.Default.Lock
                )

                Spacer(modifier = Modifier.height(40.dp))

                BigAuthButton(
                    text = "Xác nhận",
                    isLoading = uiState.isLoading,
                    onClick = {
                        viewModel.onEvent(AuthEvent.VerifyPhoneOtp(otpInput))
                    }
                )
            }
        }
    }
}