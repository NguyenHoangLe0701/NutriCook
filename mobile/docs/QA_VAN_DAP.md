# Câu Hỏi Vấn Đáp - NutriCook Mobile App

## 📋 Mục lục

1. [Giao diện sử dụng thư viện nào?](#1-giao-diện-sử-dụng-thư-viện-nào)
2. [Mô hình kiến trúc đang sử dụng?](#2-mô-hình-kiến-trúc-đang-sử-dụng)
3. [Cách triển khai giao diện?](#3-cách-triển-khai-giao-diện)
4. [Làm sao lấy dữ liệu lên dashboard real-time?](#4-làm-sao-lấy-dữ-liệu-lên-dashboard-real-time)
5. [Làm sao dashboard truyền dữ liệu xuống mobile real-time?](#5-làm-sao-dashboard-truyền-dữ-liệu-xuống-mobile-real-time)
6. [Cách xử lý dữ liệu trong app?](#6-cách-xử-lý-dữ-liệu-trong-app)

---

## 1. Giao diện sử dụng thư viện nào?

### Trả lời:

**Giao diện sử dụng Jetpack Compose** - Thư viện UI hiện đại của Android.

### Thư viện chính:

```kotlin
// File: mobile/app/build.gradle.kts

dependencies {
    // --- Compose Core ---
    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.foundation:foundation:1.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")
    
    // --- Compose Navigation ---
    implementation("androidx.navigation:navigation-compose:2.8.3")
    
    // --- Activity Compose ---
    implementation("androidx.activity:activity-compose:1.9.3")
    
    // --- Lifecycle với Compose ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
}
```

### Code ví dụ - Tạo màn hình:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/profile/CustomFoodCalculatorScreen.kt

@Composable
fun CustomFoodCalculatorScreen(
    navController: NavController,
    onSave: (String, Float, Float, Float, Float) -> Unit
) {
    // State management
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    
    // UI Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // TextField để nhập tên món ăn
        OutlinedTextField(
            value = foodName,
            onValueChange = { foodName = it },
            label = { Text("Tên món ăn") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                // Icon để trigger Gemini
                IconButton(onClick = { /* Gọi Gemini API */ }) {
                    Icon(Icons.Outlined.AutoAwesome, "Tự động tính")
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // TextField để nhập calories
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories (kcal)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Button để lưu
        Button(
            onClick = {
                onSave(
                    foodName,
                    calories.toFloatOrNull() ?: 0f,
                    protein.toFloatOrNull() ?: 0f,
                    fat.toFloatOrNull() ?: 0f,
                    carb.toFloatOrNull() ?: 0f
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu món ăn")
        }
    }
}
```

### Ưu điểm của Jetpack Compose:

- ✅ **Declarative UI** - Mô tả UI theo trạng thái, không cần XML
- ✅ **Reactive** - Tự động cập nhật khi state thay đổi
- ✅ **Type-safe** - Compile-time safety
- ✅ **Less boilerplate** - Ít code hơn so với View system

---

## 2. Mô hình kiến trúc đang sử dụng?

### Trả lời:

**Sử dụng mô hình MVVM (Model-View-ViewModel) + Repository Pattern + Dependency Injection (Hilt)**

### Sơ đồ kiến trúc:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Screen 1   │  │   Screen 2   │  │   Screen 3   │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                 │                │
└─────────┼─────────────────┼─────────────────┼────────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                    ViewModel Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ ViewModel 1 │  │ ViewModel 2  │  │ ViewModel 3  │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                 │                │
└─────────┼─────────────────┼─────────────────┼────────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                   Repository Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Repository 1│  │ Repository 2 │  │ Repository 3 │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                 │                │
└─────────┼─────────────────┼─────────────────┼────────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                    Data Source Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Firestore   │  │   Storage    │  │  Local DB    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### Code ví dụ - ViewModel:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/viewmodel/nutrition/NutritionViewModel.kt

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repo: NutritionRepository
) : ViewModel() {

    // State Flow để quản lý UI state
    private val _ui = MutableStateFlow(NutritionUiState())
    val ui = _ui.asStateFlow()

    init {
        loadData()
    }

    // Load dữ liệu từ Repository
    fun loadData() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        try {
            val weekHistory = repo.getWeeklyHistory()
            val today = repo.getTodayLog()
            
            _ui.update {
                it.copy(
                    loading = false,
                    history = weekHistory,
                    todayLog = today ?: DailyLog(calories = 0f, protein = 0f, fat = 0f, carb = 0f)
                )
            }
        } catch (e: Exception) {
            _ui.update { it.copy(loading = false, message = e.message) }
        }
    }

    // Cập nhật dinh dưỡng
    fun updateTodayNutrition(cal: Float, pro: Float, fat: Float, carb: Float) = viewModelScope.launch {
        try {
            repo.updateTodayNutrition(cal, pro, fat, carb)
            loadData() // Reload để cập nhật UI
            _ui.update { it.copy(message = "Đã cập nhật dinh dưỡng!") }
        } catch (e: Exception) {
            _ui.update { it.copy(message = "Lỗi: ${e.message}") }
        }
    }
}
```

### Code ví dụ - Repository:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/nutrition/NutritionRepository.kt

class NutritionRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun uid() = auth.currentUser?.uid ?: ""
    private fun logsCol() = db.collection("users").document(uid()).collection("daily_logs")

    // Lấy dữ liệu hôm nay
    suspend fun getTodayLog(): DailyLog? {
        val dateId = getTodayDateId()
        return getLogForDate(dateId)
    }

    // Cập nhật dinh dưỡng (cộng dồn)
    suspend fun updateTodayNutrition(calories: Float, protein: Float, fat: Float, carb: Float) {
        val dateId = getTodayDateId()
        updateNutritionForDate(dateId, calories, protein, fat, carb)
    }

    // Cập nhật với transaction để đảm bảo tính nhất quán
    suspend fun updateNutritionForDate(dateId: String, calories: Float, protein: Float, fat: Float, carb: Float) {
        val docRef = logsCol().document(dateId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)

            if (snapshot.exists()) {
                // Đã có dữ liệu -> CỘNG DỒN
                val current = snapshot.toObject(DailyLog::class.java)!!
                transaction.update(docRef, mapOf(
                    "calories" to (current.calories + calories),
                    "protein" to (current.protein + protein),
                    "fat" to (current.fat + fat),
                    "carb" to (current.carb + carb)
                ))
            } else {
                // Chưa có -> TẠO MỚI
                val newLog = DailyLog(
                    dateId = dateId,
                    calories = calories,
                    protein = protein,
                    fat = fat,
                    carb = carb
                )
                transaction.set(docRef, newLog)
            }
        }.await()
    }
}
```

### Dependency Injection với Hilt:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/NutriCookApp.kt

@HiltAndroidApp
class NutriCookApp : Application()

// Trong ViewModel
@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repo: NutritionRepository
) : ViewModel()

// Trong Repository
class NutritionRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
)
```

---

## 3. Cách triển khai giao diện?

### Trả lời:

**Sử dụng Jetpack Compose với State hoisting và StateFlow để quản lý state**

### Code ví dụ - Kết nối UI với ViewModel:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/profile/ProfileScreens.kt

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    // Collect state từ ViewModel
    val uiState by viewModel.ui.collectAsState()
    val profile = uiState.profile
    
    // UI
    Column(modifier = Modifier.fillMaxSize()) {
        // Hiển thị profile
        if (profile != null) {
            Text(text = profile.user.displayName)
            Text(text = profile.user.email)
        }
        
        // Calories Tracking Card
        CaloriesTrackingCard(
            todayLog = uiState.todayLog,
            history = uiState.history,
            onAddMealClick = {
                navController.navigate("add_meal")
            }
        )
    }
}
```

### Code ví dụ - State hoisting:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/profile/CustomFoodCalculatorScreen.kt

@Composable
fun CustomFoodCalculatorScreen(
    navController: NavController,
    onSave: (String, Float, Float, Float, Float) -> Unit
) {
    // Local state
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carb by remember { mutableStateOf("") }
    
    // Auto-trigger Gemini với debouncing
    LaunchedEffect(foodName) {
        if (foodName.trim().length >= 3 && 
            (calories.isBlank() || calories == "0")) {
            delay(1500) // Debounce 1.5 giây
            
            // Gọi Gemini API
            val nutrition = geminiService.calculateNutrition(foodName.trim())
            if (nutrition != null) {
                calories = nutrition.calories.toInt().toString()
                protein = String.format("%.1f", nutrition.protein)
                fat = String.format("%.1f", nutrition.fat)
                carb = String.format("%.1f", nutrition.carb)
            }
        }
    }
    
    // UI
    Column {
        OutlinedTextField(
            value = foodName,
            onValueChange = { foodName = it },
            label = { Text("Tên món ăn") }
        )
        
        // ... các field khác
        
        Button(onClick = {
            onSave(
                foodName,
                calories.toFloatOrNull() ?: 0f,
                protein.toFloatOrNull() ?: 0f,
                fat.toFloatOrNull() ?: 0f,
                carb.toFloatOrNull() ?: 0f
            )
        }) {
            Text("Lưu")
        }
    }
}
```

---

## 4. Làm sao lấy dữ liệu lên dashboard real-time?

### Trả lời:

**Dashboard đọc dữ liệu từ Firebase Firestore real-time. Khi mobile app cập nhật dữ liệu vào Firestore, dashboard tự động nhận được thay đổi.**

### Luồng hoạt động:

```
Mobile App                    Firebase Firestore              Dashboard
    │                                │                            │
    │─── Update data ───────────────>│                            │
    │                                │                            │
    │                                │─── Real-time sync ───────>│
    │                                │                            │
    │                                │<─── Read data ────────────│
```

### Code Mobile - Lưu dữ liệu vào Firestore:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/nutrition/NutritionRepository.kt

class NutritionRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // Cập nhật dinh dưỡng vào Firestore
    suspend fun updateNutritionForDate(
        dateId: String, 
        calories: Float, 
        protein: Float, 
        fat: Float, 
        carb: Float
    ) {
        val docRef = db
            .collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("daily_logs")
            .document(dateId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)

            if (snapshot.exists()) {
                // Cộng dồn
                val current = snapshot.toObject(DailyLog::class.java)!!
                transaction.update(docRef, mapOf(
                    "calories" to (current.calories + calories),
                    "protein" to (current.protein + protein),
                    "fat" to (current.fat + fat),
                    "carb" to (current.carb + carb),
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            } else {
                // Tạo mới
                val newLog = DailyLog(
                    dateId = dateId,
                    calories = calories,
                    protein = protein,
                    fat = fat,
                    carb = carb
                )
                transaction.set(docRef, newLog)
            }
        }.await()
    }
}
```

### Code Dashboard - Đọc dữ liệu từ Firestore:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/service/FirestoreService.java

@Service
public class FirestoreService {
    private final Firestore firestore;

    public FirestoreService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Lấy danh sách DailyLog của một user
     */
    public List<DailyLog> getUserDailyLogs(String userId) throws Exception {
        CollectionReference logsCol = firestore
            .collection("users")
            .document(userId)
            .collection("daily_logs");
        
        QuerySnapshot snap = logsCol.get().get();
        List<DailyLog> logs = new ArrayList<>();
        
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            
            DailyLog log = new DailyLog();
            log.setDateId(doc.getId());
            
            // Parse calories, protein, fat, carb
            Object calObj = data.get("calories");
            if (calObj instanceof Number) {
                log.setCalories(((Number) calObj).floatValue());
            }
            
            // ... parse các field khác
            
            logs.add(log);
        }
        
        return logs;
    }

    /**
     * Lấy tất cả DailyLog của tất cả users (cho admin)
     */
    public List<DailyLog> getAllDailyLogs() throws Exception {
        CollectionReference usersCol = firestore.collection("users");
        QuerySnapshot usersSnap = usersCol.get().get();
        List<DailyLog> allLogs = new ArrayList<>();
        
        for (DocumentSnapshot userDoc : usersSnap.getDocuments()) {
            String userId = userDoc.getId();
            CollectionReference logsCol = userDoc.getReference().collection("daily_logs");
            QuerySnapshot logsSnap = logsCol.get().get();
            
            for (DocumentSnapshot logDoc : logsSnap.getDocuments()) {
                Map<String, Object> data = logDoc.getData();
                if (data == null) continue;
                
                DailyLog log = new DailyLog();
                log.setDateId(logDoc.getId());
                log.setUserId(userId);
                
                // Parse data...
                allLogs.add(log);
            }
        }
        
        return allLogs;
    }
}
```

### Code Dashboard Controller - API endpoint:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private FirestoreService firestoreService;

    /**
     * API endpoint để lấy dữ liệu DailyLog
     */
    @GetMapping("/daily-logs")
    public ResponseEntity<List<DailyLog>> getAllDailyLogs() {
        try {
            List<DailyLog> logs = firestoreService.getAllDailyLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * API endpoint để lấy DailyLog của một user cụ thể
     */
    @GetMapping("/users/{userId}/daily-logs")
    public ResponseEntity<List<DailyLog>> getUserDailyLogs(@PathVariable String userId) {
        try {
            List<DailyLog> logs = firestoreService.getUserDailyLogs(userId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
```

### Real-time với Firestore Listener (Dashboard):

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/service/FirestoreRealtimeService.java

@Service
public class FirestoreRealtimeService {
    private final Firestore firestore;
    private ListenerRegistration listenerRegistration;

    public FirestoreRealtimeService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Lắng nghe thay đổi real-time từ Firestore
     */
    public void listenToDailyLogs(String userId, Consumer<List<DailyLog>> onUpdate) {
        CollectionReference logsCol = firestore
            .collection("users")
            .document(userId)
            .collection("daily_logs");

        listenerRegistration = logsCol.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                System.err.println("Error listening to daily logs: " + error.getMessage());
                return;
            }

            if (snapshot != null) {
                List<DailyLog> logs = new ArrayList<>();
                for (DocumentSnapshot doc : snapshot.getDocuments()) {
                    DailyLog log = parseDailyLog(doc);
                    logs.add(log);
                }
                onUpdate.accept(logs);
            }
        });
    }

    /**
     * Dừng lắng nghe
     */
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
```

---

## 5. Làm sao dashboard truyền dữ liệu xuống mobile real-time?

### Trả lời:

**Dashboard cập nhật dữ liệu vào Firestore, mobile app sử dụng Firestore Snapshot Listener để lắng nghe thay đổi real-time.**

### Luồng hoạt động:

```
Dashboard                    Firebase Firestore              Mobile App
    │                                │                            │
    │─── Update data ───────────────>│                            │
    │                                │                            │
    │                                │─── Real-time sync ───────>│
    │                                │                            │
    │                                │                            │─── Update UI
```

### Code Dashboard - Cập nhật dữ liệu:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/service/FirestoreService.java

@Service
public class FirestoreService {
    
    /**
     * Cập nhật DailyLog từ dashboard
     */
    public void updateDailyLog(String userId, String dateId, DailyLog log) throws Exception {
        DocumentReference docRef = firestore
            .collection("users")
            .document(userId)
            .collection("daily_logs")
            .document(dateId);

        Map<String, Object> data = new HashMap<>();
        data.put("calories", log.getCalories());
        data.put("protein", log.getProtein());
        data.put("fat", log.getFat());
        data.put("carb", log.getCarb());
        data.put("updatedAt", FieldValue.serverTimestamp());

        docRef.set(data, SetOptions.merge()).get();
    }
}
```

### Code Mobile - Lắng nghe thay đổi real-time:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/nutrition/NutritionRepository.kt

class NutritionRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    /**
     * Lắng nghe thay đổi real-time của DailyLog
     */
    fun getTodayLogFlow(): Flow<DailyLog?> = callbackFlow {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        val dateId = getTodayDateId()
        
        val docRef = db
            .collection("users")
            .document(uid)
            .collection("daily_logs")
            .document(dateId)

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val log = snapshot.toObject(DailyLog::class.java)
                trySend(log)
            } else {
                trySend(null)
            }
        }

        awaitClose { registration.remove() }
    }
}
```

### Code ViewModel - Sử dụng Flow:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/viewmodel/nutrition/NutritionViewModel.kt

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repo: NutritionRepository
) : ViewModel() {

    init {
        // Lắng nghe thay đổi real-time
        repo.getTodayLogFlow()
            .onEach { log ->
                _ui.update { 
                    it.copy(todayLog = log ?: DailyLog(calories = 0f, protein = 0f, fat = 0f, carb = 0f))
                }
            }
            .catch { e ->
                _ui.update { it.copy(message = e.message) }
            }
            .launchIn(viewModelScope)
    }
}
```

### Code UI - Hiển thị dữ liệu real-time:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/profile/ProfileScreens.kt

@Composable
fun CaloriesTrackingCard(
    todayLog: DailyLog?,
    onAddMealClick: () -> Unit
) {
    // UI tự động cập nhật khi todayLog thay đổi
    Card {
        Column {
            Text("Calories hôm nay: ${todayLog?.calories ?: 0f}")
            Text("Protein: ${todayLog?.protein ?: 0f}g")
            Text("Fat: ${todayLog?.fat ?: 0f}g")
            Text("Carb: ${todayLog?.carb ?: 0f}g")
            
            Button(onClick = onAddMealClick) {
                Text("Thêm bữa ăn")
            }
        }
    }
}
```

---

## 6. Cách xử lý dữ liệu trong app?

### Trả lời:

**Sử dụng StateFlow để quản lý state, Coroutines để xử lý async operations, và Flow để xử lý stream data.**

### Code ví dụ - State Management:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/viewmodel/nutrition/NutritionViewModel.kt

data class NutritionUiState(
    val loading: Boolean = false,
    val history: List<DailyLog> = emptyList(),
    val todayLog: DailyLog? = null,
    val selectedDateLog: DailyLog? = null,
    val selectedDateId: String? = null,
    val message: String? = null
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val repo: NutritionRepository
) : ViewModel() {

    // StateFlow để quản lý UI state
    private val _ui = MutableStateFlow(NutritionUiState())
    val ui = _ui.asStateFlow()

    // Load dữ liệu
    fun loadData() = viewModelScope.launch {
        _ui.update { it.copy(loading = true) }
        try {
            val weekHistory = repo.getWeeklyHistory()
            val today = repo.getTodayLog()
            
            _ui.update {
                it.copy(
                    loading = false,
                    history = weekHistory,
                    todayLog = today
                )
            }
        } catch (e: Exception) {
            _ui.update { 
                it.copy(
                    loading = false, 
                    message = e.message
                )
            }
        }
    }

    // Cập nhật dữ liệu
    fun updateTodayNutrition(cal: Float, pro: Float, fat: Float, carb: Float) = viewModelScope.launch {
        try {
            repo.updateTodayNutrition(cal, pro, fat, carb)
            loadData() // Reload để cập nhật UI
            _ui.update { it.copy(message = "Đã cập nhật!") }
        } catch (e: Exception) {
            _ui.update { it.copy(message = "Lỗi: ${e.message}") }
        }
    }
}
```

### Code ví dụ - Sử dụng trong UI:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/profile/ProfileScreens.kt

@Composable
fun ProfileScreen(
    viewModel: NutritionViewModel = hiltViewModel()
) {
    // Collect state từ ViewModel
    val uiState by viewModel.ui.collectAsState()
    
    // Hiển thị loading
    if (uiState.loading) {
        CircularProgressIndicator()
        return
    }
    
    // Hiển thị dữ liệu
    Column {
        // Hiển thị calories hôm nay
        Text("Calories: ${uiState.todayLog?.calories ?: 0f}")
        
        // Hiển thị message nếu có
        uiState.message?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        
        // Button để thêm bữa ăn
        Button(onClick = {
            // Navigate to add meal screen
        }) {
            Text("Thêm bữa ăn")
        }
    }
}
```

---

## Tóm tắt

### Thư viện UI:
- **Jetpack Compose** - Declarative UI framework

### Kiến trúc:
- **MVVM** - Model-View-ViewModel
- **Repository Pattern** - Tách biệt data source
- **Hilt** - Dependency Injection

### Real-time Sync:
- **Firebase Firestore** - Database real-time
- **Snapshot Listener** - Lắng nghe thay đổi
- **StateFlow** - Quản lý state reactive

### Xử lý dữ liệu:
- **Coroutines** - Async operations
- **Flow** - Stream data
- **StateFlow** - State management

