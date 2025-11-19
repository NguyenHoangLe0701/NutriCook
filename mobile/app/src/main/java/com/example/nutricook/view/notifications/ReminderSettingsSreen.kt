package com.example.nutricook.view.notifications

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var remindersEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("07:00") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Cài đặt nhắc nhở",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Bật/tắt nhắc nhở
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bật nhắc nhở hằng ngày")
            Switch(
                checked = remindersEnabled,
                onCheckedChange = {
                    remindersEnabled = it
                    if (it) {
                        NotificationScheduler.scheduleDailyReminders(context)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Chọn giờ nhắc
        Text("Thời gian nhắc hiện tại: $reminderTime", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                showTimePicker(context) { hour, minute ->
                    reminderTime = String.format("%02d:%02d", hour, minute)
                    scope.launch {
                        // sau này có thể lưu vào Firestore hoặc SharedPreferences ở đây
                    }
                }
            }
        ) {
            Text("Đổi giờ nhắc nhở")
        }
    }
}

fun showTimePicker(context: Context, onTimeSelected: (Int, Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(selectedHour, selectedMinute)
        },
        hour,
        minute,
        true
    ).show()
}
