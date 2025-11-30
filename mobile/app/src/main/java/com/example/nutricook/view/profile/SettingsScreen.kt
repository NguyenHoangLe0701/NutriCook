package com.example.nutricook.view.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.viewmodel.profile.ProfileSharedEvent
import com.example.nutricook.viewmodel.profile.ProfileSharedViewModel

// Màu gradient header
private val HeaderStart = Color(0xFFFFE0C6)
private val HeaderEnd = Color(0xFFCCE7FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    vm: ProfileSharedViewModel = hiltViewModel(),
    navController: androidx.navigation.NavController? = null
) {
    val ui by vm.uiState.collectAsState()
    val context = LocalContext.current

    // --- 1. LAUNCHER CHỌN ẢNH TỪ THƯ VIỆN ---
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                vm.onEvent(ProfileSharedEvent.UpdateAvatar(uri.toString()))
                Toast.makeText(context, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // --- 2. [QUAN TRỌNG] LẮNG NGHE KẾT QUẢ TỪ VIEWMODEL ---
    // Hiện Toast khi ViewModel báo thành công hoặc lỗi
    LaunchedEffect(ui.message) {
        if (ui.message != null) {
            Toast.makeText(context, ui.message, Toast.LENGTH_SHORT).show()
            vm.onEvent(ProfileSharedEvent.Consume) // Xóa message sau khi hiện
        }
    }

    // Dialog flags
    var showName by remember { mutableStateOf(false) }
    var showEmail by remember { mutableStateOf(false) }
    var showBio by remember { mutableStateOf(false) } // [MỚI] State cho dialog Bio
    var showDob by remember { mutableStateOf(false) }
    var showGender by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showAvatarOptions by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        bottomBar = bottomBar
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header gradient với avatar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    // Gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(HeaderStart, HeaderEnd)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Top bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color(0xFF374151),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            Text(
                                text = "Cài đặt",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color(0xFF1F2937)
                            )

                            Spacer(Modifier.weight(1f))
                            Spacer(Modifier.width(48.dp))
                        }

                        Spacer(Modifier.height(16.dp))

                        // Avatar + Button
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val avatarUrl = ui.myProfile?.user?.avatarUrl
                            val initials = ui.fullName.firstOrNull()?.uppercase() ?: "U"

                            // Avatar Display
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFFFFC166)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(avatarUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = initials,
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Change picture button
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFFB8E6DC),
                                shadowElevation = 0.dp,
                                modifier = Modifier.clickable {
                                    showAvatarOptions = true
                                }
                            ) {
                                Text(
                                    text = "+ Đổi ảnh đại diện",
                                    color = Color(0xFF0D7C74),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Account section title
                Text(
                    text = "Tài khoản",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )

                // Settings items
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsItemCard(
                        icon = Icons.Outlined.Person,
                        label = "Họ và tên",
                        value = ui.fullName,
                        onClick = { showName = true }
                    )

                    // [MỚI] Thêm mục chỉnh sửa Bio
                    SettingsItemCard(
                        icon = Icons.Outlined.Info,
                        label = "Tiểu sử (Bio)",
                        value = ui.bio.ifBlank { "Chưa cập nhật" },
                        onClick = { showBio = true }
                    )

                    SettingsItemCard(
                        icon = Icons.Outlined.Email,
                        label = "Email",
                        value = ui.email,
                        onClick = { showEmail = true }
                    )

                    SettingsItemCard(
                        icon = Icons.Outlined.Lock,
                        label = "Đổi mật khẩu",
                        value = "••••••••••••",
                        onClick = { showPassword = true }
                    )

                    SettingsItemCard(
                        icon = Icons.Outlined.CalendarMonth,
                        label = "Ngày sinh",
                        value = ui.dayOfBirth.ifBlank { "Chưa cập nhật" },
                        onClick = { showDob = true }
                    )

                    SettingsItemCard(
                        icon = Icons.Outlined.Person,
                        label = "Giới tính",
                        value = when(ui.gender.ifBlank { "Male" }) {
                            "Male" -> "Nam"
                            "Female" -> "Nữ"
                            "Other" -> "Khác"
                            else -> ui.gender
                        },
                        onClick = { showGender = true }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Debug Seed button
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { navController?.navigate("seed_data") }
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔧",
                            fontSize = 26.sp,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "Debug: Seed Firestore",
                            color = Color(0xFF2E7D32),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Logout button
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEB)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { showLogoutConfirm = true }
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = "Đăng xuất",
                            color = Color(0xFFDC2626),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(100.dp))
            }

            // Loading overlay
            if (ui.saving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }

    // ===== DIALOGS =====
    if (showName) {
        var text by remember(ui.fullName) { mutableStateOf(ui.fullName) }
        BaseEditDialog(
            title = "Họ và tên",
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

    // [MỚI] Dialog chỉnh sửa Bio
    if (showBio) {
        var text by remember(ui.bio) { mutableStateOf(ui.bio) }
        BaseEditDialog(
            title = "Tiểu sử",
            value = text,
            onValueChange = { text = it },
            onDismiss = { showBio = false },
            onSave = {
                vm.onEvent(ProfileSharedEvent.BioChanged(text))
                vm.onEvent(ProfileSharedEvent.SaveSettings)
                showBio = false
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
            title = "Ngày sinh (dd/MM/yyyy)",
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
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color(0xFF9333EA),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Chọn giới tính",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Vui lòng chọn giới tính của bạn",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    GenderChoice("Nam", selected == "Male") { selected = "Male" }
                    GenderChoice("Nữ", selected == "Female") { selected = "Female" }
                    GenderChoice("Khác", selected == "Other") { selected = "Other" }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.onEvent(ProfileSharedEvent.GenderChanged(selected))
                        vm.onEvent(ProfileSharedEvent.SaveSettings)
                        showGender = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9333EA)
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Xác nhận", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showGender = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6B7280)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Hủy", fontSize = 15.sp)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    if (showPassword) {
        var old by remember { mutableStateOf("") }
        var new by remember { mutableStateOf("") }
        var showOldPassword by remember { mutableStateOf(false) }
        var showNewPassword by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPassword = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Đổi mật khẩu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Nhập mật khẩu cũ và mật khẩu mới",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = old,
                        onValueChange = { old = it },
                        label = { Text("Mật khẩu cũ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        visualTransformation = if (showOldPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showOldPassword = !showOldPassword }) {
                                Icon(
                                    imageVector = if (showOldPassword)
                                        Icons.Outlined.Visibility
                                    else
                                        Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = new,
                        onValueChange = { new = it },
                        label = { Text("Mật khẩu mới") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF59E0B),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        visualTransformation = if (showNewPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = if (showNewPassword)
                                        Icons.Outlined.Visibility
                                    else
                                        Icons.Outlined.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.onEvent(ProfileSharedEvent.ChangePassword(old, new))
                        showPassword = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B)
                    ),
                    modifier = Modifier.height(44.dp),
                    enabled = old.isNotBlank() && new.isNotBlank()
                ) {
                    Text("Đổi mật khẩu", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPassword = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6B7280)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Hủy", fontSize = 15.sp)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Logout confirmation dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Logout,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Xác nhận đăng xuất",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?",
                    fontSize = 15.sp,
                    color = Color(0xFF6B7280)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Text("Đăng xuất", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Hủy")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Avatar options dialog
    if (showAvatarOptions) {
        AlertDialog(
            onDismissRequest = { showAvatarOptions = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Đổi ảnh đại diện",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1F2937)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Chọn nguồn ảnh",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // 1. Camera option (Tạm thời dùng chung Photo Picker để tránh crash do FileProvider)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAvatarOptions = false
                                singlePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                                Toast.makeText(context, "Đang mở camera/thư viện...", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF9FAFB)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = "Chụp ảnh mới",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }

                    // 2. Gallery option (Đã hoạt động)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAvatarOptions = false
                                singlePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF9FAFB)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = "Chọn từ thư viện",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }

                    // Remove avatar option (if avatar exists)
                    if (!ui.myProfile?.user?.avatarUrl.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAvatarOptions = false
                                    // Gửi sự kiện xóa ảnh (gửi chuỗi rỗng)
                                    vm.onEvent(ProfileSharedEvent.UpdateAvatar(""))
                                    Toast.makeText(context, "Đang xóa ảnh...", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEF2F2)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    text = "Xóa ảnh đại diện",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(
                    onClick = { showAvatarOptions = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6B7280)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text("Hủy", fontSize = 15.sp)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}

@Composable
private fun SettingsItemCard(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(26.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
                if (value.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFBCC1CC)
                    )
                }
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFD1D5DB),
                modifier = Modifier.size(22.dp)
            )
        }
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
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = Color(0xFF0EA5E9),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1F2937)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Nhập thông tin mới",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06B6D4),
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF06B6D4)
                ),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    "Lưu thay đổi",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF6B7280)
                ),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    "Hủy",
                    fontSize = 15.sp
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
private fun GenderChoice(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFF3E8FF) else Color(0xFFF9FAFB)
        ),
        border = if (selected)
            BorderStroke(2.dp, Color(0xFF9333EA))
        else
            BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) Color(0xFF9333EA) else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (selected) Color(0xFF9333EA) else Color(0xFFD1D5DB),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) Color(0xFF9333EA) else Color(0xFF1F2937)
            )
        }
    }
}