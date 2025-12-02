# Câu Hỏi Vấn Đáp - NutriCook Dashboard

## 📋 Mục lục

1. [Giao diện sử dụng thư viện nào?](#1-giao-diện-sử-dụng-thư-viện-nào)
2. [Mô hình kiến trúc đang sử dụng?](#2-mô-hình-kiến-trúc-đang-sử-dụng)
3. [Cách triển khai giao diện?](#3-cách-triển-khai-giao-diện)
4. [Làm sao lấy dữ liệu từ Firestore real-time?](#4-làm-sao-lấy-dữ-liệu-từ-firestore-real-time)
5. [Làm sao truyền dữ liệu xuống mobile real-time?](#5-làm-sao-truyền-dữ-liệu-xuống-mobile-real-time)
6. [Cách xử lý dữ liệu trong dashboard?](#6-cách-xử-lý-dữ-liệu-trong-dashboard)

---

## 1. Giao diện sử dụng thư viện nào?

### Trả lời:

**Dashboard sử dụng Thymeleaf (Server-side templating) + Tailwind CSS (Utility-first CSS framework)**

### Thư viện chính:

```xml
<!-- File: dashboard/pom.xml -->

<dependencies>
    <!-- Thymeleaf - Server-side templating -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    
    <!-- Spring Security với Thymeleaf -->
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>
    
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

### Tailwind CSS:

```json
// File: dashboard/package.json
{
  "devDependencies": {
    "tailwindcss": "^3.x.x"
  }
}
```

### Code ví dụ - Template Thymeleaf:

```html
<!-- File: dashboard/src/main/resources/templates/admin/dashboard.html -->

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${title}">Dashboard</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container mx-auto p-4">
        <!-- Thymeleaf syntax -->
        <h1 th:text="${title}">Tổng quan</h1>
        <p th:text="${subtitle}">Thống kê và hoạt động hệ thống</p>
        
        <!-- Hiển thị dữ liệu từ Model -->
        <div class="grid grid-cols-4 gap-4">
            <div class="bg-white p-6 rounded-lg shadow">
                <h2 class="text-2xl font-bold" th:text="${userCount}">0</h2>
                <p class="text-gray-600">Người dùng</p>
            </div>
            
            <div class="bg-white p-6 rounded-lg shadow">
                <h2 class="text-2xl font-bold" th:text="${foodCount}">0</h2>
                <p class="text-gray-600">Món ăn</p>
            </div>
        </div>
        
        <!-- Thymeleaf loops -->
        <div th:each="update : ${recentUpdates}">
            <p th:text="${update.title}">Update title</p>
        </div>
    </div>
</body>
</html>
```

### Ưu điểm của Thymeleaf:

- ✅ **Server-side rendering** - Render HTML trên server
- ✅ **Natural templates** - HTML có thể mở trực tiếp trong browser
- ✅ **Spring integration** - Tích hợp tốt với Spring Boot
- ✅ **Security** - Hỗ trợ Spring Security

### Ưu điểm của Tailwind CSS:

- ✅ **Utility-first** - Nhanh chóng tạo UI với utility classes
- ✅ **Responsive** - Dễ dàng tạo responsive design
- ✅ **Customizable** - Có thể customize theme

---

## 2. Mô hình kiến trúc đang sử dụng?

### Trả lời:

**Sử dụng mô hình MVC (Model-View-Controller) + Service Layer + Repository Pattern**

### Sơ đồ kiến trúc:

```
┌─────────────────────────────────────────────────────────────┐
│                      View Layer (Thymeleaf)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ dashboard.html│  │ users.html  │  │ foods.html   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                          ↑
                          │
┌─────────────────────────────────────────────────────────────┐
│                   Controller Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │AdminController│ │ApiController │ │FirestoreCtrl │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼─────────────────┼─────────────────┼──────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │FirestoreService│ │NotificationSvc│ │CloudinarySvc │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
└─────────┼─────────────────┼─────────────────┼──────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────┐
│                    Data Source Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  Firestore   │  │   MySQL/H2   │  │  Cloudinary  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### Code ví dụ - Controller:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private FirestoreService firestoreService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Hiển thị trang dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long userCount;
        try {
            if (firestoreService != null) {
                // Lấy từ Firestore
                userCount = firestoreService.listUsersAsEntities().size();
            } else {
                // Fallback về MySQL/H2
                userCount = userRepository.count();
            }
        } catch (Exception e) {
            userCount = userRepository.count();
        }
        
        // Thêm dữ liệu vào Model để truyền sang View
        model.addAttribute("userCount", userCount);
        model.addAttribute("foodCount", foodItemRepository.count());
        model.addAttribute("categoryCount", categoryRepository.count());
        model.addAttribute("title", "Tổng quan");
        model.addAttribute("subtitle", "Thống kê và hoạt động hệ thống");
        
        // Trả về tên template (sẽ render dashboard.html)
        return "admin/dashboard";
    }
    
    /**
     * Hiển thị danh sách users
     */
    @GetMapping("/users")
    public String users(Model model) {
        List<User> userList;
        try {
            if (firestoreService != null) {
                userList = firestoreService.listUsersAsEntities();
            } else {
                userList = userRepository.findAll();
            }
        } catch (Exception e) {
            userList = userRepository.findAll();
        }
        
        model.addAttribute("users", userList);
        model.addAttribute("title", "Quản lý người dùng");
        return "admin/users";
    }
}
```

### Code ví dụ - Service:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/service/FirestoreService.java

@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirestoreService {
    
    private final Firestore firestore;
    
    public FirestoreService(Firestore firestore) {
        this.firestore = firestore;
    }
    
    /**
     * Lấy danh sách users từ Firestore
     */
    public List<User> listUsersAsEntities() throws Exception {
        CollectionReference users = firestore.collection("users");
        QuerySnapshot snap = users.get().get();
        List<User> out = new ArrayList<>();
        
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            
            User u = new User();
            u.setUsername((String) data.get("username"));
            u.setEmail((String) data.get("email"));
            // ... parse các field khác
            
            out.add(u);
        }
        
        return out;
    }
    
    /**
     * Lấy DailyLog của một user
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
     * Cập nhật DailyLog
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

### Code ví dụ - Repository (JPA):

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/repository/UserRepository.java

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrderByCreatedAtDesc();
    User findByUsername(String username);
    User findByEmail(String email);
}
```

---

## 3. Cách triển khai giao diện?

### Trả lời:

**Sử dụng Thymeleaf template với Model để truyền dữ liệu từ Controller sang View**

### Code ví dụ - Controller truyền dữ liệu:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java

@GetMapping("/nutrition")
public String nutrition(Model model) {
    try {
        // Lấy dữ liệu từ Firestore
        List<DailyLog> allLogs = new ArrayList<>();
        if (firestoreService != null) {
            // Lấy tất cả users
            List<User> users = firestoreService.listUsersAsEntities();
            for (User user : users) {
                List<DailyLog> userLogs = firestoreService.getUserDailyLogs(user.getId().toString());
                allLogs.addAll(userLogs);
            }
        }
        
        // Tính toán thống kê
        NutritionStats stats = calculateNutritionStats(allLogs);
        
        // Thêm vào Model
        model.addAttribute("logs", allLogs);
        model.addAttribute("stats", stats);
        model.addAttribute("title", "Quản lý dinh dưỡng");
        
        return "admin/nutrition";
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        return "admin/nutrition";
    }
}
```

### Code ví dụ - Template hiển thị dữ liệu:

```html
<!-- File: dashboard/src/main/resources/templates/admin/nutrition.html -->

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${title}">Nutrition</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container mx-auto p-4">
        <h1 th:text="${title}">Quản lý dinh dưỡng</h1>
        
        <!-- Hiển thị thống kê -->
        <div class="grid grid-cols-4 gap-4 mb-6">
            <div class="bg-white p-6 rounded-lg shadow">
                <h2 class="text-2xl font-bold" th:text="${stats.totalCalories}">0</h2>
                <p class="text-gray-600">Tổng calories</p>
            </div>
            
            <div class="bg-white p-6 rounded-lg shadow">
                <h2 class="text-2xl font-bold" th:text="${stats.totalProtein}">0</h2>
                <p class="text-gray-600">Tổng protein (g)</p>
            </div>
        </div>
        
        <!-- Bảng danh sách logs -->
        <table class="min-w-full bg-white">
            <thead>
                <tr>
                    <th>User ID</th>
                    <th>Date</th>
                    <th>Calories</th>
                    <th>Protein</th>
                    <th>Fat</th>
                    <th>Carb</th>
                </tr>
            </thead>
            <tbody>
                <!-- Thymeleaf loop -->
                <tr th:each="log : ${logs}">
                    <td th:text="${log.userId}">userId</td>
                    <td th:text="${log.dateId}">2024-12-02</td>
                    <td th:text="${log.calories}">0</td>
                    <td th:text="${log.protein}">0</td>
                    <td th:text="${log.fat}">0</td>
                    <td th:text="${log.carb}">0</td>
                </tr>
            </tbody>
        </table>
    </div>
</body>
</html>
```

### Code ví dụ - Form submit:

```html
<!-- Form để cập nhật dữ liệu -->
<form th:action="@{/admin/nutrition/update}" method="post">
    <input type="hidden" th:name="userId" th:value="${userId}">
    <input type="hidden" th:name="dateId" th:value="${dateId}">
    
    <input type="number" th:name="calories" th:value="${log.calories}" step="0.1">
    <input type="number" th:name="protein" th:value="${log.protein}" step="0.1">
    
    <button type="submit">Cập nhật</button>
</form>
```

```java
// Controller xử lý form submit
@PostMapping("/nutrition/update")
public String updateNutrition(
    @RequestParam String userId,
    @RequestParam String dateId,
    @RequestParam Float calories,
    @RequestParam Float protein,
    @RequestParam Float fat,
    @RequestParam Float carb,
    RedirectAttributes redirectAttributes
) {
    try {
        DailyLog log = new DailyLog();
        log.setCalories(calories);
        log.setProtein(protein);
        log.setFat(fat);
        log.setCarb(carb);
        
        firestoreService.updateDailyLog(userId, dateId, log);
        
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật thành công!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
    }
    
    return "redirect:/admin/nutrition";
}
```

---

## 4. Làm sao lấy dữ liệu từ Firestore real-time?

### Trả lời:

**Dashboard đọc dữ liệu từ Firestore bằng cách gọi FirestoreService. Firestore tự động sync real-time, nhưng dashboard cần refresh để xem thay đổi mới nhất.**

### Luồng hoạt động:

```
Mobile App                    Firebase Firestore              Dashboard
    │                                │                            │
    │─── Update data ───────────────>│                            │
    │                                │                            │
    │                                │<─── Read data ────────────│
    │                                │                            │
    │                                │─── Return data ──────────>│
```

### Code Dashboard - Đọc dữ liệu từ Firestore:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/service/FirestoreService.java

@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirestoreService {
    
    private final Firestore firestore;
    
    /**
     * Lấy danh sách tất cả DailyLog từ Firestore
     */
    public List<DailyLog> getAllDailyLogs() throws Exception {
        CollectionReference usersCol = firestore.collection("users");
        QuerySnapshot usersSnap = usersCol.get().get();
        List<DailyLog> allLogs = new ArrayList<>();
        
        // Lặp qua tất cả users
        for (DocumentSnapshot userDoc : usersSnap.getDocuments()) {
            String userId = userDoc.getId();
            
            // Lấy collection daily_logs của user này
            CollectionReference logsCol = userDoc.getReference().collection("daily_logs");
            QuerySnapshot logsSnap = logsCol.get().get();
            
            // Parse từng log
            for (DocumentSnapshot logDoc : logsSnap.getDocuments()) {
                Map<String, Object> data = logDoc.getData();
                if (data == null) continue;
                
                DailyLog log = new DailyLog();
                log.setDateId(logDoc.getId());
                log.setUserId(userId);
                
                // Parse calories
                Object calObj = data.get("calories");
                if (calObj instanceof Number) {
                    log.setCalories(((Number) calObj).floatValue());
                }
                
                // Parse protein
                Object proObj = data.get("protein");
                if (proObj instanceof Number) {
                    log.setProtein(((Number) proObj).floatValue());
                }
                
                // Parse fat
                Object fatObj = data.get("fat");
                if (fatObj instanceof Number) {
                    log.setFat(((Number) fatObj).floatValue());
                }
                
                // Parse carb
                Object carbObj = data.get("carb");
                if (carbObj instanceof Number) {
                    log.setCarb(((Number) carbObj).floatValue());
                }
                
                allLogs.add(log);
            }
        }
        
        return allLogs;
    }
    
    /**
     * Lấy DailyLog của một user cụ thể
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
            
            DailyLog log = parseDailyLog(doc, data);
            logs.add(log);
        }
        
        return logs;
    }
    
    /**
     * Helper method để parse DailyLog
     */
    private DailyLog parseDailyLog(DocumentSnapshot doc, Map<String, Object> data) {
        DailyLog log = new DailyLog();
        log.setDateId(doc.getId());
        
        // Parse các field
        if (data.get("calories") instanceof Number) {
            log.setCalories(((Number) data.get("calories")).floatValue());
        }
        if (data.get("protein") instanceof Number) {
            log.setProtein(((Number) data.get("protein")).floatValue());
        }
        if (data.get("fat") instanceof Number) {
            log.setFat(((Number) data.get("fat")).floatValue());
        }
        if (data.get("carb") instanceof Number) {
            log.setCarb(((Number) data.get("carb")).floatValue());
        }
        
        return log;
    }
}
```

### Code Controller - Sử dụng Service:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java

@GetMapping("/nutrition")
public String nutrition(Model model) {
    try {
        List<DailyLog> logs;
        
        if (firestoreService != null) {
            // Lấy từ Firestore
            logs = firestoreService.getAllDailyLogs();
        } else {
            // Fallback về MySQL/H2
            logs = new ArrayList<>(); // Hoặc lấy từ repository
        }
        
        model.addAttribute("logs", logs);
        model.addAttribute("title", "Quản lý dinh dưỡng");
        
        return "admin/nutrition";
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        return "admin/nutrition";
    }
}
```

### Real-time với WebSocket (Tùy chọn):

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/config/WebSocketConfig.java

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new FirestoreRealtimeHandler(), "/ws/firestore")
            .setAllowedOrigins("*");
    }
}
```

---

## 5. Làm sao truyền dữ liệu xuống mobile real-time?

### Trả lời:

**Dashboard cập nhật dữ liệu vào Firestore. Mobile app sử dụng Firestore Snapshot Listener để tự động nhận thay đổi real-time.**

### Luồng hoạt động:

```
Dashboard                    Firebase Firestore              Mobile App
    │                                │                            │
    │─── Update data ───────────────>│                            │
    │                                │                            │
    │                                │─── Real-time sync ────────>│
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
        
        // Cập nhật vào Firestore
        docRef.set(data, SetOptions.merge()).get();
        
        // Mobile app sẽ tự động nhận thay đổi qua Snapshot Listener
    }
    
    /**
     * Tạo mới DailyLog
     */
    public void createDailyLog(String userId, String dateId, DailyLog log) throws Exception {
        DocumentReference docRef = firestore
            .collection("users")
            .document(userId)
            .collection("daily_logs")
            .document(dateId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("dateId", dateId);
        data.put("calories", log.getCalories());
        data.put("protein", log.getProtein());
        data.put("fat", log.getFat());
        data.put("carb", log.getCarb());
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());
        
        docRef.set(data).get();
    }
}
```

### Code Controller - API endpoint:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/FirestoreController.java

@RestController
@RequestMapping("/api/firestore")
public class FirestoreController {
    
    private final FirestoreService firestoreService;
    
    /**
     * API endpoint để cập nhật DailyLog
     */
    @PutMapping("/users/{userId}/daily-logs/{dateId}")
    public ResponseEntity<?> updateDailyLog(
        @PathVariable String userId,
        @PathVariable String dateId,
        @RequestBody DailyLog log
    ) {
        try {
            firestoreService.updateDailyLog(userId, dateId, log);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã cập nhật thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    /**
     * API endpoint để tạo mới DailyLog
     */
    @PostMapping("/users/{userId}/daily-logs")
    public ResponseEntity<?> createDailyLog(
        @PathVariable String userId,
        @RequestBody DailyLog log
    ) {
        try {
            String dateId = log.getDateId();
            firestoreService.createDailyLog(userId, dateId, log);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã tạo thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
```

### Code Mobile - Lắng nghe thay đổi (Đã có trong mobile app):

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

        // Snapshot Listener - tự động nhận thay đổi
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val log = snapshot.toObject(DailyLog::class.java)
                trySend(log) // Gửi dữ liệu mới đến Flow
            } else {
                trySend(null)
            }
        }

        awaitClose { registration.remove() }
    }
}
```

---

## 6. Cách xử lý dữ liệu trong dashboard?

### Trả lời:

**Sử dụng Spring MVC với Model để truyền dữ liệu, Service layer để xử lý business logic, và FirestoreService để tương tác với Firestore.**

### Code ví dụ - Xử lý dữ liệu trong Controller:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java

@GetMapping("/nutrition")
public String nutrition(Model model) {
    try {
        List<DailyLog> allLogs = new ArrayList<>();
        
        // 1. Lấy dữ liệu từ Firestore
        if (firestoreService != null) {
            List<User> users = firestoreService.listUsersAsEntities();
            for (User user : users) {
                List<DailyLog> userLogs = firestoreService.getUserDailyLogs(user.getId().toString());
                allLogs.addAll(userLogs);
            }
        }
        
        // 2. Tính toán thống kê
        NutritionStats stats = new NutritionStats();
        float totalCalories = 0f;
        float totalProtein = 0f;
        float totalFat = 0f;
        float totalCarb = 0f;
        
        for (DailyLog log : allLogs) {
            totalCalories += log.getCalories();
            totalProtein += log.getProtein();
            totalFat += log.getFat();
            totalCarb += log.getCarb();
        }
        
        stats.setTotalCalories(totalCalories);
        stats.setTotalProtein(totalProtein);
        stats.setTotalFat(totalFat);
        stats.setTotalCarb(totalCarb);
        stats.setTotalUsers(allLogs.stream()
            .map(DailyLog::getUserId)
            .distinct()
            .count());
        
        // 3. Thêm vào Model để truyền sang View
        model.addAttribute("logs", allLogs);
        model.addAttribute("stats", stats);
        model.addAttribute("title", "Quản lý dinh dưỡng");
        
        return "admin/nutrition";
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        return "admin/nutrition";
    }
}
```

### Code ví dụ - Export dữ liệu ra Excel:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java

@GetMapping("/nutrition/export")
public void exportNutrition(HttpServletResponse response) throws Exception {
    // 1. Lấy dữ liệu
    List<DailyLog> logs = firestoreService.getAllDailyLogs();
    
    // 2. Tạo Excel workbook
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Nutrition Data");
    
    // 3. Tạo header row
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("User ID");
    headerRow.createCell(1).setCellValue("Date");
    headerRow.createCell(2).setCellValue("Calories");
    headerRow.createCell(3).setCellValue("Protein");
    headerRow.createCell(4).setCellValue("Fat");
    headerRow.createCell(5).setCellValue("Carb");
    
    // 4. Thêm dữ liệu
    int rowNum = 1;
    for (DailyLog log : logs) {
        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue(log.getUserId());
        row.createCell(1).setCellValue(log.getDateId());
        row.createCell(2).setCellValue(log.getCalories());
        row.createCell(3).setCellValue(log.getProtein());
        row.createCell(4).setCellValue(log.getFat());
        row.createCell(5).setCellValue(log.getCarb());
    }
    
    // 5. Gửi file về client
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=nutrition_data.xlsx");
    
    workbook.write(response.getOutputStream());
    workbook.close();
}
```

### Code ví dụ - Xử lý form submit:

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java

@PostMapping("/nutrition/update")
public String updateNutrition(
    @RequestParam String userId,
    @RequestParam String dateId,
    @RequestParam Float calories,
    @RequestParam Float protein,
    @RequestParam Float fat,
    @RequestParam Float carb,
    RedirectAttributes redirectAttributes
) {
    try {
        // 1. Validation
        if (calories < 0 || protein < 0 || fat < 0 || carb < 0) {
            redirectAttributes.addFlashAttribute("error", "Giá trị không hợp lệ!");
            return "redirect:/admin/nutrition";
        }
        
        // 2. Tạo DailyLog object
        DailyLog log = new DailyLog();
        log.setCalories(calories);
        log.setProtein(protein);
        log.setFat(fat);
        log.setCarb(carb);
        
        // 3. Cập nhật vào Firestore
        firestoreService.updateDailyLog(userId, dateId, log);
        
        // 4. Thông báo thành công
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật thành công!");
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
    }
    
    return "redirect:/admin/nutrition";
}
```

---

## Tóm tắt

### Thư viện UI:
- **Thymeleaf** - Server-side templating engine
- **Tailwind CSS** - Utility-first CSS framework

### Kiến trúc:
- **MVC** - Model-View-Controller
- **Service Layer** - Business logic
- **Repository Pattern** - Data access

### Real-time Sync:
- **Firebase Firestore** - Database real-time
- **Dashboard đọc** - Gọi FirestoreService để lấy dữ liệu
- **Dashboard ghi** - Cập nhật vào Firestore, mobile tự động nhận

### Xử lý dữ liệu:
- **Spring MVC** - Request/Response handling
- **Model** - Truyền dữ liệu từ Controller sang View
- **Service** - Xử lý business logic và tương tác với Firestore

