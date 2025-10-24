package com.example.nutricook.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.viewmodel.auth.AuthEvent
import com.example.nutricook.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.launch

private val Teal   = Color(0xFF20B2AA)
private val Orange = Color(0xFFFF8A00)
private val Bg     = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onGoRegister: () -> Unit,
    onBack: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val showSnack: (String) -> Unit = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.onEvent(AuthEvent.ConsumeMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nút back nhỏ dạng tròn (như mock)
            Surface(
                modifier = Modifier
                    .align(Alignment.Start)
                    .size(36.dp)
                    .clip(CircleShape),
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 1.dp,
                shadowElevation = 0.dp
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Quay lại")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Tiêu đề + mô tả
            Text(
                text = "Đăng nhập tài khoản",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Chúng mình gặp lại nhé, đăng nhập để xem bài viết mới!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(20.dp))

            // Ô nhập – kiểu “thẻ trắng bo góc” như mock
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = state.email,
                        onValueChange = { vm.onEvent(AuthEvent.EmailChanged(it)) },
                        label = { Text("Email") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = Teal) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            focusedLabelColor = Teal,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = state.password,
                        onValueChange = { vm.onEvent(AuthEvent.PasswordChanged(it)) },
                        label = { Text("Mật khẩu") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = Teal) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = if (isPasswordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Text(if (isPasswordVisible) "Ẩn" else "Hiện")
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal,
                            focusedLabelColor = Teal,
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                }
            }

            // “Quên mật khẩu?” canh phải, nhấn vào phần màu cam
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text("Quên ")
                Text(
                    "mật khẩu?",
                    color = Orange,
                    modifier = Modifier.clickable(onClick = onForgotPassword)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Nút đăng nhập xanh ngọc bo tròn, có elevation
            Button(
                enabled = !state.isLoading,
                onClick = {
                    val email = state.email.trim()
                    val pass = state.password
                    when {
                        email.isEmpty() -> showSnack("Vui lòng nhập email")
                        pass.isEmpty() -> showSnack("Vui lòng nhập mật khẩu")
                        else -> vm.onEvent(AuthEvent.SubmitLogin)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Đăng nhập", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(28.dp))

            // “Chưa có tài khoản? Đăng ký” ở cuối
            Row {
                Text("Chưa có tài khoản? ")
                Text(
                    "Đăng ký",
                    color = Orange,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onGoRegister)
                )
            }
        }
    }
}
