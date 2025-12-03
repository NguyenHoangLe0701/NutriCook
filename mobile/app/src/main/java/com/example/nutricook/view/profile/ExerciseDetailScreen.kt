package com.example.nutricook.view.profile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R
import com.example.nutricook.service.ExerciseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ExerciseStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val duration: Int // giây
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    navController: NavController,
    exerciseName: String,
    exerciseImageRes: Int,
    exerciseDuration: String,
    exerciseCalories: Int,
    exerciseDifficulty: String
) {
    val context = LocalContext.current
    val exerciseSteps = remember(exerciseName) { getExerciseSteps(exerciseName) }
    val totalSeconds = remember(exerciseName) {
        exerciseDuration.replace(" phút", "").toIntOrNull()?.times(60) ?: 900
    }
    
    // Timer state - Reset khi chuyển sang exercise khác
    var currentSeconds by remember(exerciseName) { mutableStateOf(0) }
    var caloriesBurned by remember(exerciseName) { mutableStateOf(0) }
    var isRunning by remember(exerciseName) { mutableStateOf(false) }
    
    // Service connection để update notification - Phải khai báo trước LaunchedEffect
    var service: ExerciseService? by remember { mutableStateOf(null) }
    var isServiceBound by remember { mutableStateOf(false) }
    
    // QUAN TRỌNG: Kiểm tra và sync state với service khi chuyển exercise
    LaunchedEffect(exerciseName) {
        // Đợi service bind xong
        delay(500)
        
        // Kiểm tra xem service có đang chạy exercise khác không
        if (isServiceBound && service != null) {
            val serviceExerciseName = service!!.getExerciseName()
            val serviceIsRunning = service!!.getIsRunning()
            val serviceHasActive = service!!.hasActiveExercise()
            
            // Nếu service đang chạy exercise khác, KHÔNG reset state ở đây
            // State sẽ được sync từ service
            if (serviceHasActive && serviceExerciseName != exerciseName) {
                // Service đang chạy exercise khác, sync state từ service
                currentSeconds = service!!.getCurrentSeconds()
                caloriesBurned = service!!.getCaloriesBurned()
                isRunning = serviceIsRunning
                // KHÔNG reset service, để user có thể resume exercise cũ
                return@LaunchedEffect
            }
        }
        
        // Nếu không có exercise đang chạy hoặc cùng exercise, reset state về mặc định
        currentSeconds = 0
        caloriesBurned = 0
        isRunning = false
    }
    
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val exerciseBinder = binder as? ExerciseService.ExerciseBinder
                service = exerciseBinder?.getService()
                isServiceBound = true
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
                isServiceBound = false
            }
        }
    }
    
    // Bind service và update UI từ service
    LaunchedEffect(Unit) {
        try {
            val intent = Intent(context, ExerciseService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Update UI từ service liên tục - Chạy song song với timer trong service
    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // Update mỗi 500ms để UI mượt
            try {
                if (isServiceBound && service != null) {
                    val serviceExerciseName = service!!.getExerciseName()
                    val newSeconds = service!!.getCurrentSeconds()
                    val newCalories = service!!.getCaloriesBurned()
                    val newRunning = service!!.getIsRunning()
                    
                    // QUAN TRỌNG: Chỉ update state nếu cùng exercise hoặc không có exercise nào đang chạy
                    // Nếu service đang chạy exercise khác, không update state ở màn hình này
                    if (serviceExerciseName == exerciseName || serviceExerciseName.isEmpty()) {
                        // Update state từ service - UI sync với service
                        currentSeconds = newSeconds
                        caloriesBurned = newCalories
                        isRunning = newRunning
                    } else {
                        // Service đang chạy exercise khác, reset state về 0
                        currentSeconds = 0
                        caloriesBurned = 0
                        isRunning = false
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Auto calculate step index based on current time
    val currentStepIndex = remember(currentSeconds, exerciseSteps) {
        var index = 0
        var accumulated = 0
        for (i in exerciseSteps.indices) {
            if (currentSeconds >= accumulated) {
                index = i
            }
            accumulated += exerciseSteps[i].duration
            if (currentSeconds < accumulated) break
        }
        index
    }
    
    // Sync với service khi có (optional - để notification update)
    LaunchedEffect(isRunning, currentSeconds, caloriesBurned) {
        if (isRunning && isServiceBound) {
            // Service đã được sync với state trong startExercise
            // Không cần update gì thêm
        }
    }
    
    val progress = if (totalSeconds > 0) currentSeconds.toFloat() / totalSeconds else 0f
    val currentStep = exerciseSteps.getOrNull(currentStepIndex) ?: exerciseSteps.firstOrNull()
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            context.unbindService(serviceConnection)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        exerciseName, 
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF20B2AA)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF20B2AA).copy(alpha = 0.1f),
                            Color(0xFFF5F5F5)
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Card với gradient
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF20B2AA).copy(alpha = 0.05f),
                                            Color(0xFFFF6B35).copy(alpha = 0.05f)
                                        )
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .shadow(12.dp, CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF20B2AA).copy(alpha = 0.2f),
                                                    Color.White
                                                )
                                            ),
                                            CircleShape
                                        )
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = exerciseImageRes),
                                        contentDescription = exerciseName,
                                        modifier = Modifier.size(100.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Text(
                                    text = exerciseName,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    InfoChip(
                                        icon = Icons.Default.AccessTime,
                                        text = exerciseDuration,
                                        gradient = Brush.horizontalGradient(
                                            listOf(Color(0xFF20B2AA), Color(0xFF2DD4BF))
                                        )
                                    )
                                    InfoChip(
                                        icon = Icons.Default.LocalFireDepartment,
                                        text = "${exerciseCalories} kcal",
                                        gradient = Brush.horizontalGradient(
                                            listOf(Color(0xFFFF6B35), Color(0xFFFF8A65))
                                        )
                                    )
                                    DifficultyChip(difficulty = exerciseDifficulty)
                                }
                            }
                        }
                    }
                }
                
                // Timer Card với circular progress
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tiến trình tập luyện",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            Spacer(modifier = Modifier.height(28.dp))
                            
                            Box(
                                modifier = Modifier.size(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Animated circular progress
                                val animatedProgress by animateFloatAsState(
                                    targetValue = progress,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "progress"
                                )
                                
                                Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    val strokeWidthPx = 22.dp.toPx()
                                    val radius = (size.minDimension - strokeWidthPx) / 2
                                    val center = Offset(size.width / 2, size.height / 2)
                                    
                                    // Track
                                    drawCircle(
                                        color = Color(0xFFE8F5E9),
                                        radius = radius,
                                        center = center,
                                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                                    )
                                    
                                    // Progress with gradient
                                    if (animatedProgress > 0f) {
                                        val sweepAngle = 360f * animatedProgress
                                        drawArc(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(
                                                    Color(0xFF20B2AA),
                                                    Color(0xFF2DD4BF),
                                                    Color(0xFF20B2AA)
                                                )
                                            ),
                                            startAngle = -90f,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                                            topLeft = Offset(center.x - radius, center.y - radius),
                                            size = Size(radius * 2, radius * 2)
                                        )
                                    }
                                }
                                
                                // Timer display
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = formatTime(currentSeconds),
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF20B2AA)
                                    )
                                    Text(
                                        text = "/ ${formatTime(totalSeconds)}",
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color(0xFFFF6B35),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = "$caloriesBurned / $exerciseCalories kcal",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFF6B35)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(28.dp))
                            
                            // Control Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            // Reset local state
                                            isRunning = false
                                            currentSeconds = 0
                                            caloriesBurned = 0
                                            
                                            // Reset service
                                            val intent = Intent(context, ExerciseService::class.java).apply {
                                                action = ExerciseService.ACTION_RESET
                                            }
                                            context.startService(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF20B2AA)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reset", fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = {
                                        try {
                                            if (isRunning) {
                                                // Pause exercise
                                                val intent = Intent(context, ExerciseService::class.java).apply {
                                                    action = ExerciseService.ACTION_PAUSE
                                                }
                                                context.startService(intent)
                                                // UI sẽ update từ service
                                            } else {
                                                // QUAN TRỌNG: Kiểm tra xem có exercise khác đang dừng không
                                                var shouldStartNewExercise = true
                                                if (isServiceBound && service != null) {
                                                    val serviceExerciseName = service!!.getExerciseName()
                                                    val serviceHasActive = service!!.hasActiveExercise()
                                                    val serviceIsRunning = service!!.getIsRunning()
                                                    
                                                    // Nếu có exercise khác đang dừng, resume exercise đó thay vì start exercise mới
                                                    if (serviceHasActive && serviceExerciseName != exerciseName && !serviceIsRunning) {
                                                        // Resume exercise cũ
                                                        val resumeIntent = Intent(context, ExerciseService::class.java).apply {
                                                            action = ExerciseService.ACTION_RESUME
                                                        }
                                                        context.startService(resumeIntent)
                                                        // Chuyển về màn hình exercise cũ
                                                        // TODO: Navigate to serviceExerciseName screen
                                                        shouldStartNewExercise = false
                                                    }
                                                }
                                                
                                                // Start exercise mới hoặc resume exercise hiện tại
                                                if (shouldStartNewExercise) {
                                                    val intent = Intent(context, ExerciseService::class.java).apply {
                                                        action = ExerciseService.ACTION_START
                                                        putExtra(ExerciseService.EXTRA_EXERCISE_NAME, exerciseName)
                                                        putExtra(ExerciseService.EXTRA_TOTAL_SECONDS, totalSeconds)
                                                        putExtra(ExerciseService.EXTRA_TOTAL_CALORIES, exerciseCalories)
                                                        // Gửi currentSeconds hiện tại để resume nếu cùng exercise
                                                        putExtra(ExerciseService.EXTRA_CURRENT_SECONDS, currentSeconds)
                                                    }
                                                    
                                                    // Start foreground service
                                                    try {
                                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                            context.startForegroundService(intent)
                                                        } else {
                                                            @Suppress("DEPRECATION")
                                                            context.startService(intent)
                                                        }
                                                        
                                                        // Bind service sau 1s để service start xong và timer chạy
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            delay(1000)
                                                            try {
                                                                if (!isServiceBound) {
                                                                    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                                                                }
                                                            } catch (e: Exception) {
                                                                e.printStackTrace()
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        // Fallback nếu foreground service fail
                                                        try {
                                                            context.startService(intent)
                                                        } catch (e2: Exception) {
                                                            e2.printStackTrace()
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF20B2AA)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                                ) {
                                    Icon(
                                        if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        if (isRunning) "Tạm dừng" else if (currentSeconds == 0) "Bắt đầu" else "Tiếp tục",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Linear Progress
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = Color(0xFF20B2AA),
                                trackColor = Color(0xFFE0E0E0)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}% hoàn thành",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                if (isRunning) {
                                    Text(
                                        text = "Đang chạy...",
                                        fontSize = 14.sp,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Current Step Card
                if (currentStep != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(10.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF20B2AA))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF20B2AA).copy(alpha = 0.12f),
                                                Color(0xFF2DD4BF).copy(alpha = 0.06f)
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    Brush.radialGradient(
                                                        listOf(Color(0xFF20B2AA), Color(0xFF2DD4BF))
                                                    ),
                                                    CircleShape
                                                )
                                                .shadow(4.dp, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${currentStep.stepNumber}",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = "Bước hiện tại",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF20B2AA)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = currentStep.title,
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Text(
                                        text = currentStep.description,
                                        fontSize = 15.sp,
                                        color = Color(0xFF424242),
                                        lineHeight = 22.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(14.dp))
                                    
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                Color(0xFFF5F5F5),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = Color(0xFF20B2AA),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Thời gian: ${currentStep.duration}s",
                                            fontSize = 14.sp,
                                            color = Color(0xFF424242),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Steps Guide
                item {
                    Text(
                        text = "Hướng dẫn chi tiết",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(exerciseSteps) { step ->
                    ExerciseStepItem(
                        step = step,
                        isActive = step.stepNumber == currentStep?.stepNumber,
                        isCompleted = step.stepNumber < (currentStep?.stepNumber ?: 0)
                    )
                }
                
                // Completion Card
                if (currentSeconds >= totalSeconds && totalSeconds > 0) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(12.dp, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "🎉 Hoàn thành!",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Bạn đã đốt cháy $exerciseCalories kcal",
                                    fontSize = 18.sp,
                                    color = Color.White.copy(alpha = 0.95f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tuyệt vời! Hãy tiếp tục duy trì thói quen này nhé 💪",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(gradient, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = Color.White, 
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DifficultyChip(difficulty: String) {
    val (bgColor, textColor) = when (difficulty) {
        "Thấp" -> Color(0xFF4CAF50) to Color.White
        "Trung bình" -> Color(0xFFFF9800) to Color.White
        "Cao" -> Color(0xFFF44336) to Color.White
        else -> Color.Gray to Color.White
    }
    
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = difficulty,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun ExerciseStepItem(
    step: ExerciseStep,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isActive -> Color(0xFFE8F5E9)
                isCompleted -> Color(0xFFF1F8E9)
                else -> Color.White
            }
        ),
        border = when {
            isActive -> androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF20B2AA))
            isCompleted -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
            else -> null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 6.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Step number circle - smaller and cleaner
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        when {
                            isCompleted -> Color(0xFF4CAF50)
                            isActive -> Color(0xFF20B2AA)
                            else -> Color(0xFFE0E0E0)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "${step.stepNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else Color(0xFF757575)
                    )
                }
            }
            
            // Step content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = step.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isActive -> Color(0xFF20B2AA)
                            isCompleted -> Color(0xFF4CAF50)
                            else -> Color.Black
                        }
                    )
                    
                    // Time badge
                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFFF5F5F5),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${step.duration}s",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Text(
                    text = step.description,
                    fontSize = 14.sp,
                    color = Color(0xFF616161),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

fun getExerciseSteps(exerciseName: String): List<ExerciseStep> {
    return when (exerciseName) {
        "Bóng chày" -> listOf(
            ExerciseStep(1, "Khởi động", "Chạy tại chỗ và xoay tay trong 2 phút", 120),
            ExerciseStep(2, "Cầm gậy đúng cách", "Giữ gậy với tay trái ở trên, tay phải ở dưới", 60),
            ExerciseStep(3, "Tư thế đứng", "Đứng nghiêng, chân trái trước, trọng tâm ở chân sau", 60),
            ExerciseStep(4, "Thực hiện swing", "Xoay hông và đánh gậy từ sau ra trước", 180),
            ExerciseStep(5, "Lặp lại", "Thực hiện 20 lần swing với tốc độ vừa phải", 300),
            ExerciseStep(6, "Thư giãn", "Hít thở sâu và thả lỏng cơ thể", 60)
        )
        "Bóng rổ" -> listOf(
            ExerciseStep(1, "Khởi động", "Chạy tại chỗ và nhảy nhẹ trong 2 phút", 120),
            ExerciseStep(2, "Bounce và Catch", "Bật bóng liên tục và bắt bóng", 90),
            ExerciseStep(3, "Dribbling", "Dẫn bóng qua lại giữa hai tay", 120),
            ExerciseStep(4, "Shooting", "Ném bóng vào rổ từ các vị trí khác nhau", 240),
            ExerciseStep(5, "Layup", "Thực hiện layup từ hai bên", 180),
            ExerciseStep(6, "Thư giãn", "Đi bộ và hít thở sâu", 60)
        )
        "Leo núi" -> listOf(
            ExerciseStep(1, "Khởi động", "Kéo giãn cơ và xoay khớp trong 3 phút", 180),
            ExerciseStep(2, "Tìm điểm bám", "Quan sát và xác định các điểm bám an toàn", 60),
            ExerciseStep(3, "Bắt đầu leo", "Bám chặt và di chuyển từng bước một", 120),
            ExerciseStep(4, "Giữ thăng bằng", "Phân bổ trọng lượng đều và giữ cơ thể thẳng", 180),
            ExerciseStep(5, "Leo lên cao", "Tiếp tục leo với tốc độ ổn định", 240),
            ExerciseStep(6, "Xuống núi", "Di chuyển chậm và cẩn thận khi xuống", 180),
            ExerciseStep(7, "Thư giãn", "Nghỉ ngơi và bổ sung nước", 60)
        )
        "Đạp xe" -> listOf(
            ExerciseStep(1, "Chuẩn bị", "Kiểm tra xe và điều chỉnh yên xe phù hợp", 60),
            ExerciseStep(2, "Khởi động", "Đạp nhẹ nhàng trong 3 phút", 180),
            ExerciseStep(3, "Tăng tốc", "Đạp với tốc độ vừa phải, duy trì nhịp tim", 300),
            ExerciseStep(4, "Leo dốc", "Đạp mạnh hơn khi gặp đoạn dốc", 180),
            ExerciseStep(5, "Ổn định", "Duy trì tốc độ ổn định trên đường bằng", 180),
            ExerciseStep(6, "Thư giãn", "Đạp chậm lại và thả lỏng", 60)
        )
        "Chạy bộ" -> listOf(
            ExerciseStep(1, "Khởi động", "Đi bộ nhanh trong 2 phút", 120),
            ExerciseStep(2, "Bắt đầu chạy", "Chạy chậm với nhịp độ thoải mái", 180),
            ExerciseStep(3, "Tăng tốc", "Tăng dần tốc độ chạy", 120),
            ExerciseStep(4, "Duy trì", "Chạy với tốc độ ổn định", 240),
            ExerciseStep(5, "Nước rút", "Chạy nhanh hơn trong 1 phút cuối", 60),
            ExerciseStep(6, "Hạ nhiệt", "Chạy chậm lại và đi bộ", 60),
            ExerciseStep(7, "Thư giãn", "Kéo giãn cơ và nghỉ ngơi", 60)
        )
        else -> listOf(
            ExerciseStep(1, "Khởi động", "Làm nóng cơ thể trong 3 phút", 180),
            ExerciseStep(2, "Thực hành", "Thực hiện các động tác cơ bản", 420),
            ExerciseStep(3, "Nâng cao", "Tăng độ khó và cường độ", 240),
            ExerciseStep(4, "Thư giãn", "Nghỉ ngơi và hạ nhiệt", 60)
        )
    }
}

