# Kiến Trúc Dashboard - Monolithic & MVC

## 📋 Tổng quan

Dashboard NutriCook sử dụng **kiến trúc Monolithic** với **mô hình MVC (Model-View-Controller)**.

---

## 🏗️ Monolithic Architecture

### Định nghĩa:
**Monolithic** = Tất cả components được đóng gói trong một ứng dụng duy nhất, deploy một lần.

### Dấu hiệu Monolithic trong Dashboard:

#### 1. **Single Deployable Unit (JAR file duy nhất)**

```bash
# File: dashboard/target/dashboard-0.0.1-SNAPSHOT.jar
# Tất cả code được đóng gói vào 1 file JAR
```

```xml
<!-- File: dashboard/pom.xml -->
<artifactId>dashboard</artifactId>
<version>0.0.1-SNAPSHOT</version>
<!-- Chỉ có 1 artifact, không tách thành nhiều services -->
```

#### 2. **Tất cả components trong một codebase**

```
dashboard/
├── src/main/java/com/nutricook/dashboard/
│   ├── controller/          ← Tất cả controllers ở đây
│   │   ├── AdminController.java
│   │   ├── ApiController.java
│   │   ├── FirestoreController.java
│   │   └── LoginController.java
│   ├── service/             ← Tất cả services ở đây
│   │   ├── FirestoreService.java
│   │   ├── NotificationService.java
│   │   └── CloudinaryService.java
│   ├── repository/          ← Tất cả repositories ở đây
│   │   ├── UserRepository.java
│   │   ├── FoodItemRepository.java
│   │   └── CategoryRepository.java
│   ├── entity/              ← Tất cả entities ở đây
│   └── config/              ← Tất cả configs ở đây
└── src/main/resources/
    └── templates/           ← Tất cả views ở đây
```

**→ Không tách thành nhiều microservices riêng biệt**

#### 3. **Single Database Connection**

```java
// File: dashboard/src/main/resources/application.properties

# Chỉ có 1 database connection cho toàn bộ app
spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}

# Chỉ có 1 Firebase connection
firebase.enabled=true
```

**→ Không có service discovery, không có API gateway riêng**

#### 4. **Single Application Entry Point**

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/DashboardApplication.java

@SpringBootApplication
public class DashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
        // Chỉ có 1 main method, chạy toàn bộ application
    }
}
```

**→ Chỉ có 1 process chạy tất cả chức năng**

---

## 🎯 MVC Pattern (Model-View-Controller)

### Định nghĩa:
**MVC** = Tách biệt logic thành 3 phần: Model (dữ liệu), View (giao diện), Controller (xử lý request).

### Sơ đồ MVC trong Dashboard:

```
┌─────────────────────────────────────────────────────────┐
│                    REQUEST                               │
│              (HTTP GET/POST)                            │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              CONTROLLER LAYER                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │  AdminController.java                            │   │
│  │  - Nhận request từ browser                       │   │
│  │  - Gọi Service để xử lý logic                    │   │
│  │  - Thêm dữ liệu vào Model                        │   │
│  │  - Trả về tên View template                      │   │
│  └──────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
         ▼                       ▼
┌──────────────────┐    ┌──────────────────┐
│  SERVICE LAYER   │    │   MODEL (Data)   │
│  - Business      │    │   - Entity       │
│    Logic         │    │   - Repository   │
│  - Firestore     │    │   - Database     │
│    Operations    │    │                  │
└──────────────────┘    └──────────────────┘
         │                       │
         └───────────┬───────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  VIEW LAYER                             │
│  ┌──────────────────────────────────────────────────┐   │
│  │  dashboard.html (Thymeleaf Template)            │   │
│  │  - Nhận dữ liệu từ Model                        │   │
│  │  - Render HTML với dữ liệu                      │   │
│  │  - Trả về HTML cho browser                       │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 Code Ví Dụ - MVC trong Dashboard

### 1. **CONTROLLER** - Nhận request, xử lý, trả về view

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/controller/AdminController.java

@Controller                    // ← Đánh dấu là Controller
@RequestMapping("/admin")     // ← Mapping URL
public class AdminController {
    
    @Autowired
    private FirestoreService firestoreService;  // ← Inject Service
    
    /**
     * CONTROLLER: Nhận request GET /admin/dashboard
     * - Gọi Service để lấy dữ liệu
     * - Thêm dữ liệu vào Model
     * - Trả về tên View template
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {  // ← Model để truyền dữ liệu
        // 1. Gọi Service để lấy dữ liệu (Business Logic)
        long userCount = firestoreService.listUsersAsEntities().size();
        
        // 2. Thêm dữ liệu vào Model
        model.addAttribute("userCount", userCount);
        model.addAttribute("foodCount", foodItemRepository.count());
        model.addAttribute("title", "Tổng quan");
        
        // 3. Trả về tên View template
        return "admin/dashboard";  // ← Sẽ render admin/dashboard.html
    }
}
```

**→ Controller không chứa business logic, chỉ điều phối**

### 2. **SERVICE** - Xử lý business logic

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/service/FirestoreService.java

@Service                        // ← Đánh dấu là Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirestoreService {
    
    private final Firestore firestore;
    
    /**
     * SERVICE: Xử lý business logic
     * - Tương tác với Firestore (Data Source)
     * - Xử lý logic nghiệp vụ
     * - Trả về dữ liệu cho Controller
     */
    public List<User> listUsersAsEntities() throws Exception {
        // 1. Lấy dữ liệu từ Firestore (Data Source)
        CollectionReference users = firestore.collection("users");
        QuerySnapshot snap = users.get().get();
        
        // 2. Xử lý logic (parse, transform)
        List<User> out = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            User u = parseUser(data);  // ← Business logic
            out.add(u);
        }
        
        // 3. Trả về dữ liệu
        return out;
    }
}
```

**→ Service chứa business logic, không biết về View**

### 3. **MODEL** - Dữ liệu (Entity, Repository)

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/entity/User.java

@Entity                         // ← Đánh dấu là Entity (Model)
@Table(name = "users")
public class User {
    @Id
    private Long id;
    
    private String username;
    private String email;
    private String fullName;
    
    // Getters and Setters
}
```

```java
// File: dashboard/src/main/java/com/nutricook/dashboard/repository/UserRepository.java

@Repository                     // ← Đánh dấu là Repository (Data Access)
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByOrderByCreatedAtDesc();
    User findByUsername(String username);
}
```

**→ Model đại diện cho dữ liệu, Repository truy cập database**

### 4. **VIEW** - Giao diện (Thymeleaf Template)

```html
<!-- File: dashboard/src/main/resources/templates/admin/dashboard.html -->

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${title}">Dashboard</title>
</head>
<body>
    <div class="container">
        <!-- VIEW: Hiển thị dữ liệu từ Model -->
        <h1 th:text="${title}">Tổng quan</h1>
        
        <!-- Lấy dữ liệu từ Model (do Controller truyền vào) -->
        <div class="stats">
            <p>Người dùng: <span th:text="${userCount}">0</span></p>
            <p>Món ăn: <span th:text="${foodCount}">0</span></p>
        </div>
    </div>
</body>
</html>
```

**→ View chỉ hiển thị dữ liệu, không chứa business logic**

---

## 🔄 Luồng hoạt động MVC

### Ví dụ: User truy cập `/admin/dashboard`

```
1. Browser gửi request
   GET /admin/dashboard
   ↓
2. Controller nhận request
   AdminController.dashboard(Model model)
   ↓
3. Controller gọi Service
   firestoreService.listUsersAsEntities()
   ↓
4. Service lấy dữ liệu từ Model/Repository
   UserRepository.findAll() hoặc Firestore
   ↓
5. Service trả về dữ liệu cho Controller
   List<User> users
   ↓
6. Controller thêm dữ liệu vào Model
   model.addAttribute("users", users)
   ↓
7. Controller trả về tên View
   return "admin/dashboard"
   ↓
8. Thymeleaf render View với dữ liệu từ Model
   dashboard.html + Model data
   ↓
9. Browser nhận HTML và hiển thị
   HTML response
```

---

## ✅ Tóm tắt

### Monolithic:
- ✅ **1 JAR file** chứa tất cả
- ✅ **1 codebase** cho toàn bộ app
- ✅ **1 database connection**
- ✅ **1 process** chạy tất cả

### MVC:
- ✅ **Controller** - Nhận request, điều phối
- ✅ **Service** - Xử lý business logic
- ✅ **Model** - Dữ liệu (Entity, Repository)
- ✅ **View** - Giao diện (Thymeleaf template)

### Ưu điểm:
- ✅ **Đơn giản** - Dễ phát triển, deploy
- ✅ **Nhanh** - Không có network overhead giữa services
- ✅ **Dễ debug** - Tất cả code trong một nơi

### Nhược điểm:
- ❌ **Khó scale** - Phải scale toàn bộ app
- ❌ **Coupling** - Components phụ thuộc nhau
- ❌ **Deploy** - Phải deploy lại toàn bộ khi có thay đổi

