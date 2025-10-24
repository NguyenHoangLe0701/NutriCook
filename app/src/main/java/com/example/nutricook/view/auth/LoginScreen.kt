package com.example.nutricook.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private val Teal = Color(0xFF20B2AA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onGoRegister: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // Snackbar: lambda (String) -> Unit (không trả về Job)
    val showSnack: (String) -> Unit = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    // Nhận message từ VM (giữ chức năng cũ)
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.onEvent(AuthEvent.ConsumeMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Đăng nhập") }) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header — giữ UI như bản cũ
                Text(
                    text = "Chào mừng trở lại!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Đăng nhập để tiếp tục",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(40.dp))

                // Form — giữ Card + bo góc + màu viền TextField teal khi focus
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.email,
                            onValueChange = { vm.onEvent(AuthEvent.EmailChanged(it)) },
                            label = { Text("Email") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Email, null) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Teal,
                                focusedLabelColor = Teal
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.password,
                            onValueChange = { vm.onEvent(AuthEvent.PasswordChanged(it)) },
                            label = { Text("Mật khẩu") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            visualTransformation = if (isPasswordVisible)
                                VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                val text = if (isPasswordVisible) "Ẩn" else "Hiện"
                                TextButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Text(text)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Teal,
                                focusedLabelColor = Teal
                            )
                        )

                        Spacer(Modifier.height(8.dp))

                        TextButton(
                            onClick = { /* TODO: Quên mật khẩu */ },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Quên mật khẩu?") }

                        Spacer(Modifier.height(24.dp))

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
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Đăng nhập")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row {
                    Text("Chưa có tài khoản? ", color = Color.Gray)
                    TextButton(onClick = onGoRegister) {
                        Text("Đăng ký", color = Teal, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
