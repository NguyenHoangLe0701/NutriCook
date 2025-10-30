package com.example.nutricook.view.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.nutricook.viewmodel.profile.ProfileSharedEvent
import com.example.nutricook.viewmodel.profile.ProfileSharedViewModel

// màu header
private val HeaderStart = Color(0xFFFFE0C6)
private val HeaderEnd = Color(0xFFCCE7FF)

// màu nền từng dòng (đổi sắc nhẹ để phân biệt)
private val RowColors = listOf(
    Color(0xFFFFFFFF),          // trắng
    Color(0xFFF8FBFF),          // xanh rất nhạt
    Color(0xFFFFFAF4),          // cam rất nhạt
    Color(0xFFF6FFFA),          // xanh mint rất nhạt
    Color(0xFFFFFFFF)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    vm: ProfileSharedViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()

    // dialog flags
    var showName by remember { mutableStateOf(false) }
    var showEmail by remember { mutableStateOf(false) }
    var showDob by remember { mutableStateOf(false) }
    var showGender by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        bottomBar = bottomBar
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // header gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(HeaderStart, HeaderEnd)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // TOP BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(shape = CircleShape, color = Color.White) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF27324A)
                            )
                        }
                    }

                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        ),
                        color = Color(0xFF27324A)
                    )

                    Spacer(Modifier.size(40.dp))
                }

                // AVATAR + BUTTON
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val avatarUrl = ui.myProfile?.user?.avatarUrl
                    val initials = ui.fullName.firstOrNull()?.uppercase() ?: "U"

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFFFFC980)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = initials,
                                fontSize = 40.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFBEEFE4)
                    ) {
                        Text(
                            text = "+ Change Profile Picture",
                            color = Color(0xFF106D65),
                            modifier = Modifier
                                .clickable {
                                    // TODO: mở picker ảnh -> vm.onEvent(...)
                                }
                                .padding(horizontal = 28.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Account",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7A84A0),
                    modifier = Modifier.padding(horizontal = 18.dp)
                )

                Spacer(Modifier.height(6.dp))

                // Full name
                SettingsItemRow(
                    icon = Icons.Outlined.Person,
                    label = "Full name",
                    value = ui.fullName,
                    background = RowColors[0],
                    onClick = { showName = true }
                )

                // Email
                SettingsItemRow(
                    icon = Icons.Outlined.Email,
                    label = "Email",
                    value = ui.email,
                    background = RowColors[1],
                    onClick = { showEmail = true }
                )

                // Change password
                SettingsItemRow(
                    icon = Icons.Outlined.VpnKey,
                    label = "Change Password",
                    value = "************",
                    background = RowColors[2],
                    onClick = { showPassword = true }
                )

                // Day of birth
                SettingsItemRow(
                    icon = Icons.Outlined.CalendarMonth,
                    label = "Day of birth",
                    value = ui.dayOfBirth.ifBlank { "Chưa đặt" },
                    background = RowColors[3],
                    onClick = { showDob = true }
                )

                // Gender
                SettingsItemRow(
                    icon = Icons.Outlined.Groups,
                    label = "Gender",
                    value = ui.gender.ifBlank { "Male" },
                    background = RowColors[4],
                    onClick = { showGender = true }
                )

                Spacer(Modifier.height(14.dp))

                // LOGOUT
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF1F1))
                        .clickable { onLogout() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFE63535),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Logout",
                        color = Color(0xFFE63535),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    )
                }

                Spacer(Modifier.height(90.dp))
            }

            // overlay loading
            if (ui.saving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    // ===== DIALOGS =====
    if (showName) {
        var text by remember(ui.fullName) { mutableStateOf(ui.fullName) }
        BaseEditDialog(
            title = "Full name",
            value = text,
            onValueChange = { text = it },
            onDismiss = { showName = false },
            onSave = {
                vm.onEvent(ProfileSharedEvent.FullNameChanged(text))
                vm.onEvent(ProfileSharedEvent.SaveSettings)
                showName = false
            }
        )
    }

    if (showEmail) {
        var text by remember(ui.email) { mutableStateOf(ui.email) }
        BaseEditDialog(
            title = "Email",
            value = text,
            onValueChange = { text = it },
            onDismiss = { showEmail = false },
            onSave = {
                vm.onEvent(ProfileSharedEvent.EmailChanged(text))
                vm.onEvent(ProfileSharedEvent.SaveSettings)
                showEmail = false
            }
        )
    }

    if (showDob) {
        var text by remember(ui.dayOfBirth) { mutableStateOf(ui.dayOfBirth) }
        BaseEditDialog(
            title = "Day of birth (dd/MM/yyyy)",
            value = text,
            onValueChange = { text = it },
            onDismiss = { showDob = false },
            onSave = {
                vm.onEvent(ProfileSharedEvent.DobChanged(text))
                vm.onEvent(ProfileSharedEvent.SaveSettings)
                showDob = false
            }
        )
    }

    if (showGender) {
        var selected by remember(ui.gender) { mutableStateOf(ui.gender.ifBlank { "Male" }) }
        AlertDialog(
            onDismissRequest = { showGender = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.onEvent(ProfileSharedEvent.GenderChanged(selected))
                    vm.onEvent(ProfileSharedEvent.SaveSettings)
                    showGender = false
                }) { Text("Lưu") }
            },
            dismissButton = {
                TextButton(onClick = { showGender = false }) { Text("Hủy") }
            },
            title = { Text("Gender") },
            text = {
                Column {
                    GenderChoice("Male", selected == "Male") { selected = "Male" }
                    GenderChoice("Female", selected == "Female") { selected = "Female" }
                    GenderChoice("Other", selected == "Other") { selected = "Other" }
                }
            }
        )
    }

    if (showPassword) {
        var old by remember { mutableStateOf("") }
        var new by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPassword = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.onEvent(ProfileSharedEvent.ChangePassword(old, new))
                    showPassword = false
                }) { Text("Đổi") }
            },
            dismissButton = {
                TextButton(onClick = { showPassword = false }) { Text("Hủy") }
            },
            title = { Text("Change password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = old,
                        onValueChange = { old = it },
                        label = { Text("Old password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = new,
                        onValueChange = { new = it },
                        label = { Text("New password") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    background: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6C748B),
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                ),
                color = Color(0xFF212529)
            )
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFAEB5C3)
                )
            }
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFAEB5C3),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BaseEditDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
private fun GenderChoice(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (selected) Color(0xFF0EA5E9) else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (selected) Color(0xFF0EA5E9) else Color(0xFFCBD5E1),
                    shape = CircleShape
                )
        )
        Spacer(Modifier.width(10.dp))
        Text(text)
    }
}
