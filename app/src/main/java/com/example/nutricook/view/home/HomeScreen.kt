package com.example.nutricook.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.viewmodel.auth.AuthEvent
import com.example.nutricook.viewmodel.auth.AuthViewModel
import kotlinx.coroutines.launch

private val Teal = Color(0xFF20B2AA)
private val Bg = Color(0xFFF8F9FA)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val email = state.currentUser?.email.orEmpty()
    val initial = email.firstOrNull()?.uppercase() ?: "N"

    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }

    fun toast(msg: String) = scope.launch { snackbarHost.showSnackbar(msg) }

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NutriCook", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = Teal)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Đăng xuất") },
                            leadingIcon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                confirmLogout = true
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header chào người dùng
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar chữ cái
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        color = Teal.copy(alpha = 0.12f),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Teal
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Xin chào, ${if (email.isBlank()) "Khách" else email}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Chúc bạn một ngày nhiều năng lượng! ⚡",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Nút hành động nhanh
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickAction(
                    icon = { Icon(Icons.Outlined.Restaurant, null, tint = Teal) },
                    title = "Công thức",
                    onClick = { toast("Mở Công thức (TODO)") },
                    modifier = Modifier.weight(1f)
                )
                QuickAction(
                    icon = { Icon(Icons.Outlined.FavoriteBorder, null, tint = Teal) },
                    title = "Ưa thích",
                    onClick = { toast("Mở Ưa thích (TODO)") },
                    modifier = Modifier.weight(1f)
                )
                QuickAction(
                    icon = { Icon(Icons.Outlined.LocalGroceryStore, null, tint = Teal) },
                    title = "Mua sắm",
                    onClick = { toast("Mở danh sách mua sắm (TODO)") },
                    modifier = Modifier.weight(1f)
                )
                QuickAction(
                    icon = { Icon(Icons.Outlined.CalendarMonth, null, tint = Teal) },
                    title = "Lịch ăn",
                    onClick = { toast("Mở lịch ăn (TODO)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Section: Gợi ý hôm nay
            Text(
                text = "Gợi ý hôm nay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SuggestCard(
                    title = "Salad ức gà",
                    subtitle = "~420 kcal • 25’ • Dễ",
                    onClick = { toast("Xem chi tiết Salad ức gà (TODO)") }
                )
                SuggestCard(
                    title = "Yến mạch chuối",
                    subtitle = "~350 kcal • 10’ • Siêu nhanh",
                    onClick = { toast("Xem chi tiết Yến mạch chuối (TODO)") }
                )
                SuggestCard(
                    title = "Cơm gạo lứt + cá hồi",
                    subtitle = "~550 kcal • 30’ • Cân bằng",
                    onClick = { toast("Xem chi tiết Cơm + cá hồi (TODO)") }
                )
            }

            Spacer(Modifier.weight(1f))

            // Nút Đăng xuất nổi bật
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { confirmLogout = true },
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Đăng xuất", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Bạn sẽ quay lại màn hình đăng nhập",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { confirmLogout = true }) {
                        Text("Thoát")
                    }
                }
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Xác nhận đăng xuất") },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi NutriCook?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmLogout = false
                    vm.onEvent(AuthEvent.Logout)
                    // AppNav quan sát currentUser=null để gọi onLogout() ở ngoài
                }) { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) { Text("Huỷ") }
            }
        )
    }
}

@Composable
private fun QuickAction(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Teal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { icon() }
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SuggestCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Divider(color = Bg)
            Spacer(Modifier.height(8.dp))
            Text(
                "Xem chi tiết",
                color = Teal,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(onClick = onClick)
            )
        }
    }
}
