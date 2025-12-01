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
fun RegisterScreen(
    onGoLogin: () -> Unit,
    onBack: () -> Unit = {},
    // 👇 QUAN TRỌNG: Đã thêm tham số này để khớp với NavGraph
    onRegisterSuccess: (String) -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fullName by rememberSaveable { mutableStateOf("") }
    var agreed by rememberSaveable { mutableStateOf(false) }

    val showSnack: (String) -> Unit = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            vm.onEvent(AuthEvent.ConsumeMessage)
        }
    }

    // Khi đăng ký thành công -> Gọi callback chuyển màn hình
    LaunchedEffect(state.isRegisterSuccess) {
        if (state.isRegisterSuccess) {
            onRegisterSuccess(state.email)
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
            Surface(
                modifier = Modifier
                    .align(Alignment.Start)
                    .size(36.dp)
                    .clip(CircleShape),
                shape = CircleShape,
                color = Color.White,
                tonalElevation = 1.dp
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Quay lại")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tạo tài khoản mới",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Oh hello, hy vọng sẽ sớm gặp lại bạn.\nHoàn tất đăng ký nhé!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(14.dp)) {

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Họ và tên") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Text,
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
                        value = state.email,
                        onValueChange = { vm.onEvent(AuthEvent.EmailChanged(it)) },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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
                        value = state.password,
                        onValueChange = { vm.onEvent(AuthEvent.PasswordChanged(it)) },
                        label = { Text("Mật khẩu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        visualTransformation = PasswordVisualTransformation(),
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
                        value = state.confirmPassword,
                        onValueChange = { vm.onEvent(AuthEvent.ConfirmPasswordChanged(it)) },
                        label = { Text("Nhập lại mật khẩu") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        visualTransformation = PasswordVisualTransformation(),
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

            Spacer(Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = Teal)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tôi đồng ý với Điều khoản Dịch vụ và Chính sách quyền riêng tư.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                enabled = agreed && !state.isLoading,
                onClick = {
                    val email = state.email.trim()
                    val pass = state.password
                    val confirmPass = state.confirmPassword
                    val name = fullName.trim()

                    when {
                        name.isBlank()     -> showSnack("Vui lòng nhập họ tên")
                        email.isBlank()    -> showSnack("Vui lòng nhập email")
                        pass.length < 6    -> showSnack("Mật khẩu tối thiểu 6 ký tự")
                        confirmPass != pass -> showSnack("Mật khẩu nhập lại không khớp")
                        !agreed            -> showSnack("Bạn cần đồng ý điều khoản")
                        else               -> vm.onEvent(AuthEvent.SubmitRegister(name))
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
                    Text("Đăng ký", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Footer chuyển qua đăng nhập
            Row {
                Text("Đã có tài khoản? ")
                Text(
                    "Đăng nhập",
                    color = Orange,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onGoLogin)
                )
            }
        }
    }
}