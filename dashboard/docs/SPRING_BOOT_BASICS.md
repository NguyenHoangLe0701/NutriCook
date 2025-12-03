# Spring Boot - Kiến Thức Cơ Bản

## 📖 Tổng quan

Spring Boot là một framework Java giúp bạn tạo ứng dụng web nhanh chóng và dễ dàng. Hãy tưởng tượng bạn đang xây một ngôi nhà - Spring Boot cung cấp cho bạn bộ công cụ và bản vẽ có sẵn, bạn chỉ cần lắp ráp lại thôi!

---

## 🏗️ Câu chuyện: Xây dựng một quán cà phê

Hãy tưởng tượng bạn đang mở một quán cà phê tên là "NutriCook". Để quán hoạt động, bạn cần:

1. **Quầy thu ngân (Controller)** - Nhận order từ khách hàng
2. **Đầu bếp (Service)** - Chế biến món ăn theo logic
3. **Kho nguyên liệu (Repository)** - Lưu trữ và lấy nguyên liệu
4. **Nguyên liệu (Entity)** - Định nghĩa từng loại nguyên liệu
5. **Cửa hàng (Application)** - Nơi tất cả hoạt động

Spring Boot giúp bạn tổ chức tất cả những thứ này một cách có hệ thống!

---

## 📁 Cấu trúc thư mục Spring Boot - "Bản đồ của quán cà phê"

Khi bạn tạo một dự án Spring Boot, bạn sẽ thấy cấu trúc như sau:

```
dashboard/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── nutricook/
│   │   │           └── dashboard/
│   │   │               ├── DashboardApplication.java  ← 🏪 Cửa hàng chính
│   │   │               ├── controller/                ← 💰 Quầy thu ngân
│   │   │               ├── service/                   ← 👨‍🍳 Đầu bếp
│   │   │               ├── repository/                ← 📦 Kho nguyên liệu
│   │   │               ├── entity/                    ← 🥛 Nguyên liệu
│   │   │               └── config/                    ← ⚙️ Quy định của quán
│   │   └── resources/
│   │       ├── application.properties                 ← 📋 Cấu hình quán
│   │       ├── templates/                             ← 🖼️ Menu mẫu (HTML)
│   │       └── static/                                ← 🎨 Trang trí (CSS, JS)
│   └── test/                                          ← 🧪 Phòng kiểm tra
└── pom.xml                                            ← 📦 Danh sách dụng cụ
```

---

## 🎯 Chi tiết từng Folder và Nhiệm vụ

### 1. 📂 `src/main/java/.../DashboardApplication.java` - Cửa hàng chính

**Nhiệm vụ:** Đây là nơi khởi động ứng dụng Spring Boot.

**Câu chuyện:** Đây giống như việc mở cửa quán cà phê. Khi bạn chạy file này, toàn bộ hệ thống sẽ bắt đầu hoạt động.

**Code ví dụ:**

```java
package com.nutricook.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // ← Dấu hiệu: "Đây là ứng dụng Spring Boot!"
public class DashboardApplication {
    
    public static void main(String[] args) {
        // Khởi động ứng dụng - giống như mở cửa quán
        SpringApplication.run(DashboardApplication.class, args);
    }
}
```

**Giải thích:**
- `@SpringBootApplication`: Annotation "ma thuật" - báo cho Spring biết đây là ứng dụng chính
- `SpringApplication.run()`: Khởi động server, quét các component, kết nối database...

**Chạy ứng dụng:**
```bash
# Cách 1: Dùng Maven
mvn spring-boot:run

# Cách 2: Chạy trực tiếp file Java
java -jar target/dashboard-0.0.1-SNAPSHOT.jar
```

---

### 2. 📂 `controller/` - Quầy thu ngân

**Nhiệm vụ:** Nhận request từ người dùng (HTTP GET, POST...), xử lý và trả về response.

**Câu chuyện:** Giống như quầy thu ngân trong quán cà phê. Khách hàng đến đặt món, nhân viên nhận order, giao cho bếp, rồi trả món cho khách.

**Code ví dụ:**

```java
package com.nutricook.dashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

@Controller                    // ← Đánh dấu: "Tôi là Controller!"
@RequestMapping("/admin")     // ← Tất cả URL bắt đầu bằng /admin
public class AdminController {
    
    @Autowired                // ← Spring tự động "chèn" Service vào đây
    private UserService userService;
    
    /**
     * Xử lý khi khách truy cập: GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // 1. Gọi Service để lấy dữ liệu (giống như giao order cho bếp)
        long userCount = userService.countUsers();
        
        // 2. Thêm dữ liệu vào Model (chuẩn bị dữ liệu để trả về)
        model.addAttribute("userCount", userCount);
        model.addAttribute("title", "Tổng quan");
        
        // 3. Trả về tên view template (giống như đưa món cho khách)
        return "admin/dashboard";  // ← Sẽ tìm file: templates/admin/dashboard.html
    }
    
    /**
     * Xử lý khi khách submit form: POST /admin/users
     */
    @PostMapping("/users")
    public String createUser(@RequestParam String username, 
                            @RequestParam String email) {
        // Xử lý tạo user mới
        userService.createUser(username, email);
        
        // Redirect về trang danh sách
        return "redirect:/admin/users";  // ← Chuyển hướng sang URL khác
    }
}
```

**Các Annotation quan trọng:**
- `@Controller`: Đánh dấu class này là Controller
- `@GetMapping("/path")`: Xử lý request GET /path
- `@PostMapping("/path")`: Xử lý request POST /path
- `@RequestMapping("/path")`: Tiền tố cho tất cả URL trong class
- `@Autowired`: Tự động inject dependency (Service, Repository...)

**Luồng hoạt động:**
```
1. User truy cập: http://localhost:8080/admin/dashboard
2. Controller nhận request: AdminController.showDashboard()
3. Controller gọi Service: userService.countUsers()
4. Service trả về dữ liệu
5. Controller thêm vào Model: model.addAttribute(...)
6. Controller trả về view: "admin/dashboard"
7. Spring render file: templates/admin/dashboard.html
8. User nhận HTML response
```

---

### 3. 📂 `service/` - Đầu bếp (Business Logic)

**Nhiệm vụ:** Chứa logic nghiệp vụ của ứng dụng. Xử lý dữ liệu, quy tắc kinh doanh.

**Câu chuyện:** Đầu bếp nhận order từ quầy thu ngân, lấy nguyên liệu từ kho, chế biến theo công thức, rồi trả món cho quầy.

**Code ví dụ:**

```java
package com.nutricook.dashboard.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service  // ← Đánh dấu: "Tôi là Service!"
public class UserService {
    
    @Autowired  // ← Tự động inject Repository
    private UserRepository userRepository;
    
    /**
     * Business Logic: Đếm số lượng users
     * - Service không biết về HTTP request
     * - Chỉ xử lý logic nghiệp vụ
     */
    public long countUsers() {
        return userRepository.count();  // ← Gọi Repository để lấy dữ liệu
    }
    
    /**
     * Business Logic: Tạo user mới với validation
     */
    public User createUser(String username, String email) {
        // 1. Validate dữ liệu (quy tắc nghiệp vụ)
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống!");
        }
        
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username đã tồn tại!");
        }
        
        // 2. Tạo đối tượng User
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());
        
        // 3. Lưu vào database qua Repository
        return userRepository.save(user);
    }
    
    /**
     * Business Logic: Lấy danh sách users có phân trang
     */
    public List<User> getAllUsers(int page, int size) {
        // Xử lý logic phân trang, sắp xếp...
        return userRepository.findAll(PageRequest.of(page, size)).getContent();
    }
}
```

**Nguyên tắc:**
- ✅ Service chứa **business logic** (quy tắc nghiệp vụ)
- ✅ Service **không biết** về HTTP, Controller, View
- ✅ Service có thể gọi nhiều Repository khác nhau
- ✅ Service có thể gọi Service khác

---

### 4. 📂 `repository/` - Kho nguyên liệu (Data Access)

**Nhiệm vụ:** Truy cập database, thực hiện các thao tác CRUD (Create, Read, Update, Delete).

**Câu chuyện:** Như kho nguyên liệu của quán. Đầu bếp cần gì thì đến kho lấy. Kho không biết món gì, chỉ biết lưu trữ và cung cấp nguyên liệu.

**Code ví dụ:**

```java
package com.nutricook.dashboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository  // ← Đánh dấu: "Tôi là Repository!"
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Data JPA tự động tạo method implementation!
    
    // Tìm user theo username
    Optional<User> findByUsername(String username);
    
    // Kiểm tra username đã tồn tại chưa
    boolean existsByUsername(String username);
    
    // Tìm users theo email (case insensitive)
    List<User> findByEmailIgnoreCase(String email);
    
    // Tìm users sắp xếp theo ngày tạo (mới nhất trước)
    List<User> findAllByOrderByCreatedAtDesc();
    
    // Tìm users theo tên (containing = LIKE)
    List<User> findByFullNameContaining(String name);
}
```

**Giải thích:**
- `JpaRepository<User, Long>`: 
  - `User` = Entity class (bảng trong database)
  - `Long` = Kiểu dữ liệu của Primary Key (ID)
- Spring Data JPA tự động tạo implementation dựa trên tên method
- Không cần viết code SQL! Spring tự generate

**Các method có sẵn từ JpaRepository:**
```java
// Không cần khai báo, đã có sẵn:
userRepository.save(user);           // Lưu hoặc cập nhật
userRepository.findById(1L);         // Tìm theo ID
userRepository.findAll();            // Lấy tất cả
userRepository.deleteById(1L);       // Xóa theo ID
userRepository.count();              // Đếm số lượng
userRepository.existsById(1L);       // Kiểm tra tồn tại
```

**Custom Query (nếu cần):**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Query method name (Spring tự hiểu)
    List<User> findByAgeGreaterThan(int age);
    
    // Custom query với @Query
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> searchByEmail(@Param("email") String email);
    
    // Native SQL query
    @Query(value = "SELECT * FROM users WHERE age > :age", nativeQuery = true)
    List<User> findUsersOlderThan(@Param("age") int age);
}
```

---

### 5. 📂 `entity/` - Nguyên liệu (Database Tables)

**Nhiệm vụ:** Định nghĩa các class đại diện cho bảng trong database. Mỗi Entity = 1 bảng.

**Câu chuyện:** Như định nghĩa từng loại nguyên liệu (cà phê, sữa, đường...). Mỗi loại có đặc tính riêng.

**Code ví dụ:**

```java
package com.nutricook.dashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity              // ← Đánh dấu: "Tôi là Entity (bảng database)!"
@Table(name = "users")  // ← Tên bảng trong database
public class User {
    
    @Id              // ← Đánh dấu: "Đây là Primary Key!"
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← Tự động tăng ID
    private Long id;
    
    @Column(nullable = false, unique = true)  // ← NOT NULL và UNIQUE
    private String username;
    
    @Column(nullable = false)
    private String email;
    
    private String fullName;
    
    @Column(name = "created_at")  // ← Tên cột trong database
    private LocalDateTime createdAt;
    
    // Constructor
    public User() {}  // ← Bắt buộc có constructor rỗng
    
    // Getters and Setters (Spring cần để set/get dữ liệu)
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    // ... các getter/setter khác
}
```

**Các Annotation quan trọng:**
- `@Entity`: Đánh dấu class là Entity
- `@Table(name = "...")`: Tên bảng trong database
- `@Id`: Primary Key
- `@GeneratedValue`: Tự động tăng giá trị ID
- `@Column`: Cấu hình cột (nullable, unique, length...)
- `@OneToMany`, `@ManyToOne`: Quan hệ giữa các bảng

**Ví dụ Entity có quan hệ:**

```java
@Entity
@Table(name = "food_items")
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    // Quan hệ: Một FoodItem thuộc một Category
    @ManyToOne
    @JoinColumn(name = "category_id")  // ← Foreign key
    private Category category;
    
    // Quan hệ: Một FoodItem có nhiều Reviews
    @OneToMany(mappedBy = "foodItem", cascade = CascadeType.ALL)
    private List<Review> reviews;
}
```

**Entity trong database:**
```
Table: users
+----+----------+------------------+---------------------+
| id | username | email            | created_at          |
+----+----------+------------------+---------------------+
| 1  | john     | john@email.com   | 2024-01-01 10:00:00|
| 2  | jane     | jane@email.com   | 2024-01-02 11:00:00|
+----+----------+------------------+---------------------+
```

---

### 6. 📂 `config/` - Quy định của quán (Configuration)

**Nhiệm vụ:** Cấu hình các component của ứng dụng (Security, Database, Firebase, Cloudinary...).

**Câu chuyện:** Như quy định của quán cà phê: giờ mở cửa, cách phục vụ, quy tắc an toàn...

**Code ví dụ - SecurityConfig:**

```java
package com.nutricook.dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration        // ← Đánh dấu: "Tôi là file cấu hình!"
@EnableWebSecurity    // ← Bật Spring Security
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/login", "/css/**", "/js/**").permitAll()  // ← Cho phép truy cập không cần đăng nhập
                .antMatchers("/admin/**").hasRole("ADMIN")  // ← Chỉ ADMIN mới vào được /admin
                .anyRequest().authenticated()  // ← Các URL khác cần đăng nhập
            .and()
            .formLogin()
                .loginPage("/login")  // ← Trang đăng nhập
                .defaultSuccessUrl("/admin/dashboard");  // ← Sau khi đăng nhập thành công, chuyển đến đây
    }
}
```

**Code ví dụ - DatabaseConfig:**

```java
@Configuration
public class DatabaseConfig {
    
    @Bean  // ← Tạo một bean cho Spring quản lý
    public DataSource dataSource() {
        // Cấu hình kết nối database
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/nutricook")
            .username("root")
            .password("password")
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .build();
    }
}
```

**Các Annotation trong Config:**
- `@Configuration`: Đánh dấu class là configuration
- `@Bean`: Tạo object cho Spring quản lý
- `@EnableWebSecurity`: Bật Spring Security
- `@ConditionalOnProperty`: Chỉ khởi tạo khi có property nhất định

---

### 7. 📂 `resources/application.properties` - Cấu hình quán

**Nhiệm vụ:** File cấu hình ứng dụng (database, server port, file upload size...).

**Câu chuyện:** Như bảng thông báo trong quán: giờ mở cửa, địa chỉ, số điện thoại...

**Code ví dụ:**

```properties
# Server Configuration
server.port=8080  # ← Port server chạy

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/nutricook
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update  # ← Tự động tạo/cập nhật bảng
spring.jpa.show-sql=true              # ← Hiển thị SQL queries

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Thymeleaf Configuration (Template Engine)
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
```

**Giải thích các giá trị:**
- `server.port`: Port server chạy (mặc định 8080)
- `spring.datasource.*`: Thông tin kết nối database
- `spring.jpa.hibernate.ddl-auto`:
  - `create`: Tạo mới bảng mỗi lần khởi động (xóa dữ liệu cũ!)
  - `update`: Tự động tạo/cập nhật bảng (an toàn hơn)
  - `none`: Không làm gì cả
- `spring.jpa.show-sql`: Hiển thị SQL queries trong console (hữu ích để debug)

**Cách đọc biến môi trường:**

```properties
# Sử dụng biến môi trường với giá trị mặc định
server.port=${PORT:8080}  # ← Nếu không có PORT, dùng 8080

spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
```

---

### 8. 📂 `resources/templates/` - Menu mẫu (HTML Views)

**Nhiệm vụ:** Chứa các file HTML template (dùng Thymeleaf để render).

**Câu chuyện:** Như menu mẫu của quán. Controller cung cấp dữ liệu, template hiển thị thành HTML.

**Code ví dụ - Thymeleaf Template:**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${title}">Dashboard</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <!-- Hiển thị dữ liệu từ Model -->
        <h1 th:text="${title}">Tổng quan</h1>
        
        <div class="stats">
            <p>Người dùng: <span th:text="${userCount}">0</span></p>
            <p>Món ăn: <span th:text="${foodCount}">0</span></p>
        </div>
        
        <!-- Lặp qua danh sách users -->
        <table>
            <tr th:each="user : ${users}">
                <td th:text="${user.id}">1</td>
                <td th:text="${user.username}">john</td>
                <td th:text="${user.email}">john@email.com</td>
            </tr>
        </table>
        
        <!-- Form submit -->
        <form th:action="@{/admin/users}" method="post">
            <input type="text" name="username" placeholder="Username">
            <input type="email" name="email" placeholder="Email">
            <button type="submit">Tạo User</button>
        </form>
    </div>
</body>
</html>
```

**Thymeleaf Syntax:**
- `th:text="${variable}"`: Hiển thị giá trị biến
- `th:each="item : ${list}"`: Lặp qua danh sách
- `th:href="@{/path}"`: Link URL
- `th:action="@{/path}"`: Form action URL
- `th:if="${condition}"`: Hiển thị có điều kiện

---

### 9. 📂 `resources/static/` - Trang trí (CSS, JS, Images)

**Nhiệm vụ:** Chứa các file tĩnh (CSS, JavaScript, hình ảnh) được phục vụ trực tiếp.

**Câu chuyện:** Như trang trí quán: màu sắc, hình ảnh, nhạc nền...

**Cấu trúc:**

```
resources/static/
├── css/
│   ├── style.css
│   └── base.css
├── js/
│   ├── app.js
│   └── chart.js
└── images/
    ├── logo.png
    └── banner.jpg
```

**Sử dụng trong HTML:**

```html
<!-- Link CSS -->
<link rel="stylesheet" th:href="@{/css/style.css}">

<!-- Script JS -->
<script th:src="@{/js/app.js}"></script>

<!-- Image -->
<img th:src="@{/images/logo.png}" alt="Logo">
```

**Truy cập trực tiếp:**
- File: `resources/static/css/style.css`
- URL: `http://localhost:8080/css/style.css`

---

## 🔄 Luồng hoạt động hoàn chỉnh - "Một ngày trong quán cà phê"

Hãy xem điều gì xảy ra khi khách hàng đặt món:

### Tình huống: Khách hàng truy cập `/admin/dashboard`

```
1. Khách hàng (Browser)
   ↓
   GET http://localhost:8080/admin/dashboard
   ↓
2. Quầy thu ngân (Controller)
   AdminController.showDashboard(Model model)
   ↓
   "Tôi cần số lượng users để hiển thị trên dashboard"
   ↓
3. Đầu bếp (Service)
   UserService.countUsers()
   ↓
   "Tôi cần đếm số users trong kho"
   ↓
4. Kho nguyên liệu (Repository)
   UserRepository.count()
   ↓
   SELECT COUNT(*) FROM users
   ↓
5. Database
   Trả về: 42
   ↓
6. Repository → Service
   return 42
   ↓
7. Service → Controller
   return 42
   ↓
8. Controller thêm vào Model
   model.addAttribute("userCount", 42)
   ↓
9. Controller trả về View
   return "admin/dashboard"
   ↓
10. Menu mẫu (Thymeleaf Template)
    templates/admin/dashboard.html
    + Model data (userCount = 42)
    ↓
11. Render thành HTML
    <p>Người dùng: 42</p>
    ↓
12. Khách hàng nhận HTML
    Hiển thị trên browser
```

**Code minh họa đầy đủ:**

```java
// 1. Controller
@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long count = userService.countUsers();  // ← Gọi Service
        model.addAttribute("userCount", count);
        return "admin/dashboard";
    }
}

// 2. Service
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public long countUsers() {
        return userRepository.count();  // ← Gọi Repository
    }
}

// 3. Repository
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring tự động tạo method count()
}

// 4. Entity
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    // ...
}
```

---

## 🎓 Các Annotation quan trọng

### @SpringBootApplication
- **Nhiệm vụ:** Đánh dấu class chính của ứng dụng
- **Vị trí:** Class có method `main()`
- **Ví dụ:**
```java
@SpringBootApplication
public class DashboardApplication {
    public static void main(String[] args) {
        SpringApplication.run(DashboardApplication.class, args);
    }
}
```

### @Controller vs @RestController
- **@Controller:** Dùng cho web app (trả về HTML)
- **@RestController:** Dùng cho REST API (trả về JSON)
- **Ví dụ:**

```java
// Web Controller - trả về HTML
@Controller
public class AdminController {
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";  // ← Trả về template
    }
}

// REST Controller - trả về JSON
@RestController
@RequestMapping("/api")
public class ApiController {
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();  // ← Trả về JSON
    }
}
```

### @Autowired
- **Nhiệm vụ:** Tự động inject dependency (Dependency Injection)
- **Ví dụ:**
```java
@Service
public class UserService {
    @Autowired  // ← Spring tự động tìm UserRepository và chèn vào
    private UserRepository userRepository;
}
```

**Cách khác (khuyến nghị):**
```java
// Dùng constructor injection (tốt hơn)
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### @RequestMapping, @GetMapping, @PostMapping
- **@RequestMapping("/path")**: Tiền tố cho tất cả URL trong class
- **@GetMapping("/path")**: Xử lý GET request
- **@PostMapping("/path")**: Xử lý POST request
- **Ví dụ:**
```java
@Controller
@RequestMapping("/admin")  // ← Tất cả URL bắt đầu bằng /admin
public class AdminController {
    
    @GetMapping("/dashboard")  // ← URL: GET /admin/dashboard
    public String dashboard() {
        return "admin/dashboard";
    }
    
    @PostMapping("/users")  // ← URL: POST /admin/users
    public String createUser() {
        return "redirect:/admin/users";
    }
}
```

---

## 🧩 Dependency Injection (DI) - "Tự động cung cấp công cụ"

**Câu chuyện:** Thay vì bạn phải tự tìm và mang dụng cụ từ kho, Spring tự động đưa cho bạn những gì cần thiết.

**Không dùng DI (cách cũ - khó bảo trì):**

```java
@Service
public class UserService {
    private UserRepository userRepository;
    
    public UserService() {
        // Phải tự tạo object - khó test, khó thay đổi
        this.userRepository = new UserRepositoryImpl();
    }
}
```

**Dùng DI (cách mới - dễ bảo trì):**

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    // Spring tự động inject UserRepository vào đây
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**Lợi ích:**
- ✅ Dễ test (có thể mock Repository)
- ✅ Dễ thay đổi (chỉ cần thay implementation)
- ✅ Loose coupling (ít phụ thuộc)

---

## 📝 Tóm tắt - "Bản đồ nhanh"

| Folder/File | Nhiệm vụ | Ví dụ trong "Quán cà phê" |
|------------|----------|---------------------------|
| `DashboardApplication.java` | Khởi động ứng dụng | Mở cửa quán |
| `controller/` | Nhận HTTP request | Quầy thu ngân |
| `service/` | Business logic | Đầu bếp |
| `repository/` | Truy cập database | Kho nguyên liệu |
| `entity/` | Định nghĩa bảng DB | Định nghĩa nguyên liệu |
| `config/` | Cấu hình ứng dụng | Quy định quán |
| `application.properties` | File cấu hình | Bảng thông báo |
| `templates/` | HTML views | Menu mẫu |
| `static/` | CSS, JS, images | Trang trí quán |

---

## 🚀 Next Steps - "Nâng cao kỹ năng"

Sau khi nắm vững cơ bản, bạn có thể học thêm:

1. **Spring Security** - Bảo mật ứng dụng
2. **Spring Data JPA** - Query nâng cao
3. **REST API** - Tạo API cho mobile app
4. **Spring Boot Testing** - Viết unit test, integration test
5. **Spring Profiles** - Quản lý môi trường (dev, prod)
6. **Exception Handling** - Xử lý lỗi chuyên nghiệp
7. **Validation** - Validate dữ liệu đầu vào
8. **File Upload** - Upload hình ảnh, file

---

## 💡 Tips & Tricks

### 1. Debug trong Spring Boot
```java
@GetMapping("/debug")
public String debug() {
    // In ra console để debug
    System.out.println("Debug: User count = " + userService.countUsers());
    
    // Hoặc dùng logger (tốt hơn)
    log.debug("User count: {}", userService.countUsers());
    
    return "admin/dashboard";
}
```

### 2. Xem SQL queries
Trong `application.properties`:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### 3. Hot reload (tự động reload khi code thay đổi)
Thêm dependency:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 4. Kiểm tra các beans đã load
Thêm vào `application.properties`:
```properties
# Hiển thị tất cả beans đã được load
debug=true
```

---

## ❓ FAQ - "Câu hỏi thường gặp"

### Q: Tại sao phải tách Controller, Service, Repository?
**A:** Để code dễ bảo trì, dễ test, tuân theo nguyên tắc Single Responsibility (mỗi class chỉ làm một việc).

### Q: `@Autowired` có bắt buộc không?
**A:** Không. Bạn có thể dùng constructor injection (khuyến nghị hơn).

### Q: Repository là interface, code ở đâu?
**A:** Spring Data JPA tự động tạo implementation dựa trên tên method!

### Q: Làm sao để chạy ứng dụng?
**A:** 
```bash
# Cách 1: Dùng Maven
mvn spring-boot:run

# Cách 2: Chạy JAR
java -jar target/dashboard-0.0.1-SNAPSHOT.jar
```

### Q: Ứng dụng chạy ở đâu?
**A:** Mặc định là `http://localhost:8080`. Có thể đổi trong `application.properties`:
```properties
server.port=9090
```

---

## 🎉 Kết luận

Spring Boot giúp bạn xây dựng ứng dụng web nhanh chóng với ít code nhất. Hiểu rõ cấu trúc và nhiệm vụ của từng folder sẽ giúp bạn phát triển ứng dụng hiệu quả hơn!

**Nhớ:**
- 🎯 Controller nhận request → gọi Service → trả về View
- 🎯 Service xử lý logic → gọi Repository → trả về dữ liệu
- 🎯 Repository truy cập database → trả về Entity
- 🎯 Entity đại diện cho bảng trong database

Chúc bạn code vui vẻ! 🚀

