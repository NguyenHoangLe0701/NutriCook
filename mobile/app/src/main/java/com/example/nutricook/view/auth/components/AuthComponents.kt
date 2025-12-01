package com.example.nutricook.view.auth.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- MÀU SẮC CHỦ ĐẠO ---
val BrandColor = Color(0xFF2DC1A6) // Màu xanh ngọc
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF9CA3AF)

// --- TIÊU ĐỀ TO (32sp) ---
@Composable
fun BigAuthTitle(text: String) {
    Text(
        text = text,
        fontSize = 32.sp, // Font to
        fontWeight = FontWeight.Bold,
        color = TextDark,
        lineHeight = 40.sp
    )
}

// --- SUBTITLE TO (18sp) ---
@Composable
fun BigAuthSubtitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp, // Font to dễ đọc
        color = TextGray,
        lineHeight = 26.sp
    )
}

// --- Ô NHẬP LIỆU TO (Text 18sp) ---
@Composable
fun BigAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp), // Bo tròn mềm mại
        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp), // Chữ nhập vào to
        placeholder = { Text(placeholder, fontSize = 18.sp, color = TextGray) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandColor,
                modifier = Modifier.size(28.dp) // Icon to
            )
        },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrandColor,
            unfocusedBorderColor = Color(0xFFE5E7EB),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

// --- NÚT BẤM TO (Text 20sp) ---
@Composable
fun BigAuthButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp), // Nút cao hơn để dễ bấm
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandColor),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(30.dp))
        } else {
            Text(
                text = text,
                fontSize = 20.sp, // Chữ trong nút to
                fontWeight = FontWeight.Bold
            )
        }
    }
}