# Hướng Dẫn Thư Viện Giao Diện - NutriCook

## 📋 Tổng quan

NutriCook sử dụng **Jetpack Compose** - thư viện UI hiện đại của Android để xây dựng toàn bộ giao diện. Tất cả các component UI đều được xây dựng bằng Compose, không cần thư viện bên ngoài cho các component cơ bản.

---

## 🎨 Vòng Tròn Tính Calories - Dùng Canvas

### Câu hỏi: Vòng tròn tính calories dùng thư viện gì?

### Trả lời: **KHÔNG dùng thư viện bên ngoài!** Sử dụng **Canvas** trong Jetpack Compose.

### Code Implementation:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/profile/ExerciseDetailScreen.kt

Canvas(
    modifier = Modifier.fillMaxSize()
) {
    val strokeWidthPx = 22.dp.toPx()
    val radius = (size.minDimension - strokeWidthPx) / 2
    val center = Offset(size.width / 2, size.height / 2)
    
    // 1. Vẽ vòng tròn nền (track)
    drawCircle(
        color = Color(0xFFE8F5E9), // Màu xám nhạt
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    )
    
    // 2. Vẽ vòng tròn progress (với gradient)
    if (animatedProgress > 0f) {
        val sweepAngle = 360f * animatedProgress
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFF20B2AA),  // Teal
                    Color(0xFF2DD4BF), // Light teal
                    Color(0xFF20B2AA)  // Teal
                )
            ),
            startAngle = -90f,        // Bắt đầu từ trên
            sweepAngle = sweepAngle,   // Góc quét theo progress
            useCenter = false,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }
}
```

### Animation cho vòng tròn:

```kotlin
// Sử dụng animateFloatAsState từ Compose
val animatedProgress by animateFloatAsState(
    targetValue = progress, // progress = currentSeconds / totalSeconds
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    ),
    label = "progress"
)
```

**Giải thích:**
- **Canvas**: Component của Compose để vẽ custom graphics
- **drawCircle()**: Vẽ vòng tròn nền
- **drawArc()**: Vẽ cung tròn progress với gradient
- **animateFloatAsState()**: Animation mượt mà cho progress

---

## 📚 Danh Sách Thư Viện UI

### 1. **Jetpack Compose Core** (Thư viện chính)

```kotlin
// File: mobile/app/build.gradle.kts

// Compose Core
implementation("androidx.compose.ui:ui:1.7.0")
implementation("androidx.compose.material3:material3:1.3.0")
implementation("androidx.compose.foundation:foundation:1.7.0")
implementation("androidx.compose.ui:ui-tooling-preview:1.7.0")
implementation("androidx.compose.material:material-icons-extended:1.7.0")
```

**Chức năng:**
- ✅ Tất cả UI components (Button, Text, Card, etc.)
- ✅ Layout system (Column, Row, Box, LazyColumn, etc.)
- ✅ Material Design 3 components
- ✅ Icons (Material Icons Extended)

---

### 2. **Compose Navigation**

```kotlin
implementation("androidx.navigation:navigation-compose:2.8.3")
```

**Chức năng:**
- ✅ Điều hướng giữa các màn hình
- ✅ Deep linking
- ✅ Navigation graph

**Ví dụ sử dụng:**
```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.HOME
) {
    composable(Routes.HOME) { HomeScreen(navController) }
    composable(Routes.PROFILE) { ProfileScreen(navController) }
}
```

---

### 3. **Activity Compose**

```kotlin
implementation("androidx.activity:activity-compose:1.9.3")
```

**Chức năng:**
- ✅ Tích hợp Compose với Activity
- ✅ Permission handling
- ✅ Result callbacks

---

### 4. **Lifecycle với Compose**

```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
```

**Chức năng:**
- ✅ ViewModel với Compose
- ✅ Lifecycle-aware components
- ✅ State management

---

### 5. **Hilt (Dependency Injection)**

```kotlin
implementation("com.google.dagger:hilt-android:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
```

**Chức năng:**
- ✅ Dependency injection
- ✅ ViewModel injection
- ✅ Repository injection

---

### 6. **Coil (Image Loading)**

```kotlin
implementation("io.coil-kt:coil-compose:2.7.0")
```

**Chức năng:**
- ✅ Load ảnh từ URL
- ✅ Cache ảnh
- ✅ Placeholder và error handling

**Ví dụ sử dụng:**
```kotlin
AsyncImage(
    model = imageUrl,
    contentDescription = "Food image",
    modifier = Modifier.size(100.dp),
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error)
)
```

---

### 7. **Material Components**

```kotlin
implementation("com.google.android.material:material:1.12.0")
```

**Chức năng:**
- ✅ Material Design components (không dùng Compose)
- ✅ Sử dụng cho một số component cũ

---

## 🎨 Các Giao Diện Nổi Bật và Thư Viện

### 1. **Vòng Tròn Progress (Circular Progress)**

**Thư viện:** Canvas (Compose built-in)

**Vị trí:**
- `ExerciseDetailScreen.kt` - Vòng tròn tiến trình tập luyện
- `ProfileScreens.kt` - Vòng tròn calories trong profile
- `NutritionFactsScreen.kt` - Vòng tròn calories trong recipe

**Code:**
```kotlin
Canvas(modifier = Modifier.size(200.dp)) {
    // Vẽ vòng tròn progress
    drawArc(...)
}
```

---

### 2. **Linear Progress Bar**

**Thư viện:** LinearProgressIndicator (Compose built-in)

**Vị trí:**
- `ExerciseDetailScreen.kt` - Progress bar dưới vòng tròn
- `ProfileScreens.kt` - Progress bar calories

**Code:**
```kotlin
LinearProgressIndicator(
    progress = { currentSeconds.toFloat() / totalSeconds },
    modifier = Modifier
        .fillMaxWidth()
        .height(10.dp),
    color = Color(0xFF20B2AA),
    trackColor = Color(0xFFE0E0E0)
)
```

---

### 3. **LazyColumn (Danh sách cuộn)**

**Thư viện:** LazyColumn (Compose built-in)

**Vị trí:**
- Tất cả các màn hình có danh sách (Home, Profile, Recipes, etc.)

**Code:**
```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    items(foodItems) { item ->
        FoodItemCard(item)
    }
}
```

---

### 4. **Card với Shadow**

**Thư viện:** Card + Modifier.shadow (Compose built-in)

**Vị trí:**
- Tất cả các màn hình

**Code:**
```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .shadow(12.dp, RoundedCornerShape(24.dp)),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
        containerColor = Color.White
    )
) {
    // Content
}
```

---

### 5. **Gradient Background**

**Thư viện:** Brush (Compose built-in)

**Vị trí:**
- `ExerciseDetailScreen.kt` - Background gradient
- `HomeScreen.kt` - Header gradient

**Code:**
```kotlin
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
    // Content
}
```

---

### 6. **Animated Progress**

**Thư viện:** animateFloatAsState (Compose built-in)

**Vị trí:**
- `ExerciseDetailScreen.kt` - Animation cho circular progress
- `ProfileScreens.kt` - Animation cho calories progress

**Code:**
```kotlin
val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    ),
    label = "progress"
)
```

---

### 7. **Image Loading với Coil**

**Thư viện:** Coil (Thư viện bên ngoài)

**Vị trí:**
- Tất cả các màn hình load ảnh từ URL

**Code:**
```kotlin
AsyncImage(
    model = imageUrl,
    contentDescription = "Food image",
    modifier = Modifier.size(100.dp),
    placeholder = painterResource(R.drawable.placeholder),
    error = painterResource(R.drawable.error)
)
```

---

### 8. **TopAppBar (Navigation Bar)**

**Thư viện:** TopAppBar (Compose Material3)

**Vị trí:**
- Tất cả các màn hình có navigation

**Code:**
```kotlin
TopAppBar(
    title = { Text("NutriCook") },
    navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, "Back")
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color(0xFF20B2AA)
    )
)
```

---

### 9. **Bottom Navigation**

**Thư viện:** NavigationBar (Compose Material3)

**Vị trí:**
- `NavGraph.kt` - Bottom navigation bar

**Code:**
```kotlin
NavigationBar {
    NavigationBarItem(
        icon = { Icon(Icons.Default.Home, "Home") },
        label = { Text("Trang chủ") },
        selected = currentRoute == Routes.HOME,
        onClick = { navController.navigate(Routes.HOME) }
    )
}
```

---

### 10. **TextField với Validation**

**Thư viện:** OutlinedTextField (Compose Material3)

**Vị trí:**
- Tất cả các form (Login, SignUp, Add Food, etc.)

**Code:**
```kotlin
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    label = { Text("Email") },
    isError = emailError != null,
    supportingText = { Text(emailError ?: "") },
    modifier = Modifier.fillMaxWidth()
)
```

---

## 🎯 Tóm Tắt

### Thư viện Built-in (Compose):
- ✅ **Canvas** - Vẽ vòng tròn progress, custom graphics
- ✅ **LinearProgressIndicator** - Progress bar
- ✅ **Card** - Card component với shadow
- ✅ **LazyColumn/LazyRow** - Danh sách cuộn
- ✅ **Brush** - Gradient backgrounds
- ✅ **animateFloatAsState** - Animation
- ✅ **TopAppBar** - Navigation bar
- ✅ **NavigationBar** - Bottom navigation
- ✅ **OutlinedTextField** - Input fields

### Thư viện Bên Ngoài:
- ✅ **Coil** - Image loading từ URL
- ✅ **Hilt** - Dependency injection
- ✅ **Navigation Compose** - Navigation system

### Không dùng thư viện bên ngoài cho:
- ❌ Circular progress (dùng Canvas)
- ❌ Charts (có thể dùng Canvas nếu cần)
- ❌ Animations (dùng Compose animations)
- ❌ UI components cơ bản (dùng Material3)

---

## 💡 Lưu Ý

1. **Vòng tròn calories KHÔNG dùng thư viện bên ngoài** - Tất cả được vẽ bằng Canvas trong Compose
2. **Tất cả UI components đều từ Compose Material3** - Không cần thư viện UI bên ngoài
3. **Chỉ dùng Coil cho image loading** - Vì Compose chưa có AsyncImage built-in tốt
4. **Animation đều dùng Compose animations** - Không cần thư viện animation bên ngoài

---

## 📁 File Locations

### UI Components:
- `mobile/app/src/main/java/com/example/nutricook/view/` - Tất cả UI screens
- `mobile/app/src/main/java/com/example/nutricook/view/profile/ExerciseDetailScreen.kt` - Vòng tròn progress
- `mobile/app/src/main/java/com/example/nutricook/view/profile/ProfileScreens.kt` - Calories circular progress

### Dependencies:
- `mobile/app/build.gradle.kts` - Tất cả dependencies

---

## ✅ Kết Luận

NutriCook sử dụng **100% Jetpack Compose** cho UI, không cần thư viện UI bên ngoài. Vòng tròn calories được vẽ bằng **Canvas** - một component built-in của Compose, không phải thư viện bên ngoài.

**Ưu điểm:**
- ✅ Nhẹ - Không cần thư viện UI nặng
- ✅ Nhanh - Compose render nhanh
- ✅ Linh hoạt - Custom được mọi thứ với Canvas
- ✅ Modern - Sử dụng công nghệ mới nhất của Android

