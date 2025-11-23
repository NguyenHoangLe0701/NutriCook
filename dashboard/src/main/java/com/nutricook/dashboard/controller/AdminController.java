package com.nutricook.dashboard.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nutricook.dashboard.entity.Category;
import com.nutricook.dashboard.entity.FoodItem;
import com.nutricook.dashboard.entity.FoodUpdate;
import com.nutricook.dashboard.entity.User;
import com.nutricook.dashboard.entity.DailyLog;
import com.nutricook.dashboard.entity.NutritionStats;
import com.nutricook.dashboard.entity.Post;
import com.nutricook.dashboard.entity.Review;
import com.nutricook.dashboard.entity.AnalyticsData;
import java.util.ArrayList;
import com.nutricook.dashboard.repository.CategoryRepository;
import com.nutricook.dashboard.repository.FoodItemRepository;
import com.nutricook.dashboard.repository.FoodUpdateRepository;
import com.nutricook.dashboard.repository.UserRepository;
import com.nutricook.dashboard.service.FirestoreService;
import com.nutricook.dashboard.service.NotificationService;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private FirestoreService firestoreService;
    
    @Autowired(required = false)
    private NotificationService notificationService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private FoodUpdateRepository foodUpdateRepository;
    
    private final String UPLOAD_DIR = "uploads/";
    
    // Initialize some sample data after the application is ready
    @EventListener(ApplicationReadyEvent.class)
    public void init(ApplicationReadyEvent event) {
        try {
            // Create sample categories if none exist
            if (categoryRepository.count() == 0) {
                Category vegetables = new Category("Rau củ", "Các loại rau củ tươi ngon", "🥬", "#20B2AA");
                Category fruits = new Category("Trái cây", "Trái cây tươi ngon", "🍎", "#FF8C00");
                Category seafood = new Category("Hải sản", "Hải sản tươi sống", "🐟", "#DC143C");
                Category meat = new Category("Thịt", "Các loại thịt", "🍖", "#4169E1");
                
                categoryRepository.saveAll(List.of(vegetables, fruits, seafood, meat));
            }
            
            // Create admin user if none exists
            if (userRepository.count() == 0) {
                User admin = new User("admin", passwordEncoder.encode("12345"), "admin@nutricook.com", "Administrator");
                admin.setRole(User.UserRole.ADMIN);
                userRepository.save(admin);
                
                User user = new User("user1", passwordEncoder.encode("password123"), "user1@example.com", "Nguyễn Văn A");
                userRepository.save(user);
            }

        } catch (Exception e) {
            // DB might not be ready for DDL or user lacks permissions — log and skip sample-data creation
            System.err.println("[init] Database not ready for sample-data creation: " + e.getMessage());
        }
        // Khối tạo food mẫu đã bị xóa
    }
    
    // Dashboard - Tổng quan
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long userCount;
        try {
            if (firestoreService != null) {
                userCount = firestoreService.listUsersAsEntities().size();
                System.out.println("Loaded user count from Firestore");
            } else {
                userCount = userRepository.count();
            }
        } catch (Exception e) {
            System.err.println("Error loading from Firestore, falling back to H2: " + e.getMessage());
            userCount = userRepository.count();
        }
        model.addAttribute("userCount", userCount);
        model.addAttribute("foodCount", foodItemRepository.count());
        model.addAttribute("categoryCount", categoryRepository.count());
        model.addAttribute("updateCount", foodUpdateRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(1)));
        model.addAttribute("recentUpdates", foodUpdateRepository.findTop5ByOrderByCreatedAtDesc());
        model.addAttribute("title", "Tổng quan");
        model.addAttribute("subtitle", "Thống kê và hoạt động hệ thống");
        model.addAttribute("activeTab", "dashboard");
        return "admin/dashboard";
    }

    // User Management - Quản lý người dùng
    @GetMapping("/users")
    public String users(Model model) {
        List<User> users;
        try {
            if (firestoreService != null) {
                users = firestoreService.listUsersAsEntities();
                System.out.println("Loaded " + users.size() + " users from Firestore");
            } else {
                users = userRepository.findAll();
                System.out.println("Loaded " + users.size() + " users from H2 database");
            }
        } catch (Exception e) {
            System.err.println("Error loading from Firestore, falling back to H2: " + e.getMessage());
            users = userRepository.findAll();
        }
        model.addAttribute("users", users);
        model.addAttribute("newUser", new User());
        model.addAttribute("title", "Quản lý người dùng");
        model.addAttribute("subtitle", "Quản lý tài khoản người dùng");
        model.addAttribute("activeTab", "users");
        return "admin/users";
    }
    
    @PostMapping("/users")
    public String createUser(@ModelAttribute User user, 
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirectAttributes) {
        try {
            if (!user.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/admin/users";
            }
            if (userRepository.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại!");
                return "redirect:/admin/users";
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
                return "redirect:/admin/users";
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            try {
                if (firestoreService != null) {
                    firestoreService.saveUser(user);
                }
            } catch (Exception ignored) {
            }
            redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null && user.getRole() != User.UserRole.ADMIN) {
                userRepository.deleteById(id);
                try {
                    if (firestoreService != null) {
                        firestoreService.deleteUserCascade(String.valueOf(id));
                    }
                } catch (Exception ignored) {
                }
                redirectAttributes.addFlashAttribute("success", "Xóa người dùng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản admin!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        try {
            User user = null;
            if (firestoreService != null) {
                // Tìm user từ Firestore
                List<User> users = firestoreService.listUsersAsEntities();
                user = users.stream()
                    .filter(u -> u.getId() != null && u.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            }
            if (user == null) {
                user = userRepository.findById(id).orElse(null);
            }
            if (user == null) {
                return "redirect:/admin/users";
            }
            model.addAttribute("editUser", user);
            model.addAttribute("users", firestoreService != null ? firestoreService.listUsersAsEntities() : userRepository.findAll());
            model.addAttribute("newUser", new User());
            model.addAttribute("title", "Quản lý người dùng");
            model.addAttribute("subtitle", "Quản lý tài khoản người dùng");
            model.addAttribute("activeTab", "users");
            return "admin/users";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/users";
        }
    }
    
    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            User existing = null;
            // Tìm user từ Firestore hoặc H2
            if (firestoreService != null) {
                List<User> users = firestoreService.listUsersAsEntities();
                existing = users.stream()
                    .filter(u -> u.getId() != null && u.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            }
            if (existing == null) {
                existing = userRepository.findById(id).orElse(null);
            }
            if (existing == null) {
                redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại!");
                return "redirect:/admin/users";
            }
            
            // Kiểm tra username và email trùng lặp
            if (userRepository.existsByUsernameAndIdNot(user.getUsername(), id)) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại!");
                return "redirect:/admin/users";
            }
            if (userRepository.existsByEmailAndIdNot(user.getEmail(), id)) {
                redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
                return "redirect:/admin/users";
            }
            
            // Update user data
            existing.setFullName(user.getFullName());
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setAvatar(user.getAvatar());
            existing.setRole(user.getRole());
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            existing.setUpdatedAt(LocalDateTime.now());
            
            // Save to database
            userRepository.save(existing);
            
            // Save to Firestore
            try {
                if (firestoreService != null) {
                    firestoreService.saveUserWithDocId(String.valueOf(existing.getId()), existing);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật người dùng thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật người dùng: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null && user.getRole() != User.UserRole.ADMIN) {
                redirectAttributes.addFlashAttribute("success", 
                    "Đã cập nhật trạng thái người dùng '" + user.getUsername() + "' thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể thay đổi trạng thái admin!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    
    // Category Management - Quản lý danh mục
    @GetMapping("/categories")
    public String categories(Model model) {
        List<Category> categories;
        try {
            // Prefer the SQL repository (MySQL). Use Firestore only if repository is empty or unavailable.
            categories = categoryRepository.findAll();
            if (categories == null || categories.isEmpty()) {
                if (firestoreService != null) {
                    categories = firestoreService.listCategoriesAsEntities();
                    System.out.println("Loaded " + categories.size() + " categories from Firestore (fallback)");
                }
            } else {
                System.out.println("Loaded " + categories.size() + " categories from SQL repository");
            }
        } catch (Exception e) {
            System.err.println("Error loading from Firestore, falling back to H2: " + e.getMessage());
            categories = categoryRepository.findAll();
        }
        model.addAttribute("categories", categories);
        model.addAttribute("category", new Category());
        model.addAttribute("title", "Danh mục món ăn");
        model.addAttribute("subtitle", "Quản lý danh mục món ăn");
        model.addAttribute("activeTab", "categories");
        return "admin/categories";
    }
    
    @PostMapping("/categories")
    public String createCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            if (categoryRepository.existsByName(category.getName())) {
                redirectAttributes.addFlashAttribute("error", "Tên danh mục đã tồn tại!");
                return "redirect:/admin/categories";
            }
            Category savedCategory = categoryRepository.save(category);
            try {
                if (firestoreService != null) {
                    firestoreService.saveCategory(savedCategory);
                    System.out.println("Synced new category to Firestore: " + savedCategory.getId());
                }
            } catch (Exception e) {
                System.err.println("Failed to sync new category to Firestore: " + e.getMessage());
            }
            redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
    
    @GetMapping("/categories/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            model.addAttribute("category", category);
            model.addAttribute("title", "Chỉnh sửa danh mục");
            model.addAttribute("subtitle", "Cập nhật thông tin danh mục");
            model.addAttribute("activeTab", "categories");
            return "admin/edit-category";
        }
        return "redirect:/admin/categories";
    }
    
    @PostMapping("/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category, 
                               RedirectAttributes redirectAttributes) {
        try {
            Category existingCategory = categoryRepository.findById(id).orElse(null);
            if (existingCategory != null) {
                existingCategory.setName(category.getName());
                existingCategory.setDescription(category.getDescription());
                existingCategory.setIcon(category.getIcon());
                existingCategory.setColor(category.getColor());
                existingCategory.setUpdatedAt(LocalDateTime.now());
                
                categoryRepository.save(existingCategory);

                try {
                    if (firestoreService != null) {
                        firestoreService.saveCategory(existingCategory);
                        System.out.println("Synced updated category to Firestore: " + existingCategory.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to sync updated category to Firestore: " + e.getMessage());
                }
                redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
    
    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
         try {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
                return "redirect:/admin/categories";
            }
            List<FoodItem> foodsInCategory = foodItemRepository.findByCategory(category);
            if (!foodsInCategory.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể xóa danh mục '" + category.getName() + "' vì có " + 
                    foodsInCategory.size() + " món ăn thuộc danh mục này!");
                return "redirect:/admin/categories";
            }
            categoryRepository.deleteById(id);
            try {
                if (firestoreService != null) {
                    firestoreService.deleteCategory(id);
                    System.out.println("Deleted category from Firestore: " + id);
                }
            } catch (Exception e) {
                 System.err.println("Failed to delete category from Firestore: " + e.getMessage());
            }
            redirectAttributes.addFlashAttribute("success", "Xóa danh mục '" + category.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/api/categories")
    @ResponseBody 
    public List<Category> getCategoriesForMobile() {
        return categoryRepository.findAll();
    }

    // ==================================================================
    // FOOD ITEM MANAGEMENT (Đã sửa)
    // ==================================================================

    @GetMapping("/foods")
    public String foods(Model model) {
        List<FoodItem> foods;
        List<Category> categories;
        try {
            if (firestoreService != null) {
                foods = firestoreService.listFoodsAsEntities();
                System.out.println("Loaded " + foods.size() + " foods from Firestore");
            } else {
                foods = foodItemRepository.findAll();
                System.out.println("Loaded " + foods.size() + " foods from H2 database");
            }
        } catch (Exception e) {
            System.err.println("Error loading from Firestore, falling back to H2: " + e.getMessage());
            foods = foodItemRepository.findAll();
        }
        
        try {
            if (firestoreService != null) {
                categories = firestoreService.listCategoriesAsEntities();
            } else {
                categories = categoryRepository.findAll();
            }
        } catch (Exception e) {
            categories = categoryRepository.findAll();
        }
        
        model.addAttribute("foods", foods);
        model.addAttribute("categories", categories);
        model.addAttribute("foodItem", new FoodItem());
        model.addAttribute("title", "Quản lý món ăn");
        model.addAttribute("subtitle", "Quản lý danh sách món ăn");
        model.addAttribute("activeTab", "foods");
        return "admin/foods"; 
    }
    
    @PostMapping("/foods")
    public String createFood(@ModelAttribute FoodItem foodItem, 
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {
        try {
            if (foodItemRepository.existsByName(foodItem.getName())) {
                redirectAttributes.addFlashAttribute("error", "Tên món ăn đã tồn tại!");
                return "redirect:/admin/foods";
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                foodItem.setImageUrl("/uploads/" + fileName);
            }
            FoodItem savedFood = foodItemRepository.save(foodItem);
            try {
                if (firestoreService != null) {
                    Category cat = categoryRepository.findById(savedFood.getCategory().getId()).orElse(null);
                    savedFood.setCategory(cat);
                    firestoreService.saveFood(savedFood);
                    System.out.println("Synced new food to Firestore: " + savedFood.getId());
                }
            } catch (Exception e) {
                System.err.println("Failed to sync new food to Firestore: " + e.getMessage());
            }
            logFoodUpdate(null, savedFood, "CREATE");
            redirectAttributes.addFlashAttribute("success", "Thêm món ăn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm món ăn: " + e.getMessage());
        }
        return "redirect:/admin/foods";
    }

    @PostMapping("/api/foods/upload")
    @ResponseBody
    public ResponseEntity<String> uploadFood(
            @RequestParam("name") String name,
            @RequestParam("calories") String calories,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "description", required = false) String description,
            // price đã bị xóa
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "rating", required = false) Double rating,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                return ResponseEntity.badRequest().body("Category not found");
            }
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }
            FoodItem foodItem = new FoodItem(name, calories, description != null ? description : "", category);
            foodItem.setUser(user);
            foodItem.setAvailable(true);
            foodItem.setRating(rating != null ? rating : 0.0);
            foodItem.setReviews(0); // Mới upload nên chưa có review
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                foodItem.setImageUrl("/uploads/" + fileName);
            }
            FoodItem savedFood = foodItemRepository.save(foodItem);
            try {
                 if (firestoreService != null) {
                    savedFood.setCategory(category); 
                    firestoreService.saveFood(savedFood);
                    System.out.println("Synced new food (from API) to Firestore: " + savedFood.getId());
                 }
            } catch (Exception e) {
                 System.err.println("Failed to sync new food (from API) to Firestore: " + e.getMessage());
            }
            logFoodUpdate(null, savedFood, "CREATE");
            return ResponseEntity.ok("Food uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading food: " + e.getMessage());
        }
    }
    
    @GetMapping("/foods/{id}/edit")
    public String editFoodForm(@PathVariable Long id, Model model) {
        FoodItem foodItem = foodItemRepository.findById(id).orElse(null);
        if (foodItem != null) {
            List<Category> categories = categoryRepository.findAll();
            model.addAttribute("foodItem", foodItem);
            model.addAttribute("categories", categories);
            model.addAttribute("title", "Chỉnh sửa món ăn");
            model.addAttribute("subtitle", "Cập nhật thông tin món ăn");
            model.addAttribute("activeTab", "foods");
            return "admin/edit-food";
        }
        return "redirect:/admin/foods";
    }
    
    // === BẮT ĐẦU SỬA LỖI ===
    @PostMapping("/foods/{id}/edit")
    public String updateFood(@PathVariable Long id, 
                           @ModelAttribute FoodItem foodItem, // Đây là foodItem chỉ có ID category
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {
        try {
            FoodItem existingFood = foodItemRepository.findById(id).orElse(null);
            if (existingFood != null) {
                
                // 1. Lấy Category đầy đủ từ H2 dựa trên ID từ form
                Category categoryFromDb = categoryRepository.findById(foodItem.getCategory().getId()).orElse(null);
                if (categoryFromDb == null) {
                    redirectAttributes.addFlashAttribute("error", "Danh mục không hợp lệ!");
                    return "redirect:/admin/foods";
                }

                // Log dữ liệu cũ
                FoodItem oldFood = new FoodItem();
                oldFood.setName(existingFood.getName());
                oldFood.setCalories(existingFood.getCalories());
                oldFood.setDescription(existingFood.getDescription());
                oldFood.setAvailable(existingFood.getAvailable());
                
                // 2. Cập nhật các trường
                existingFood.setName(foodItem.getName());
                existingFood.setCalories(foodItem.getCalories());
                existingFood.setDescription(foodItem.getDescription());
                existingFood.setCategory(categoryFromDb); // <-- SỬA LỖI: Dùng category đầy đủ
                existingFood.setAvailable(foodItem.getAvailable());
                existingFood.setUpdatedAt(LocalDateTime.now());
                
                if (imageFile != null && !imageFile.isEmpty()) {
                    String fileName = saveImage(imageFile);
                    existingFood.setImageUrl("/uploads/" + fileName);
                }
                
                // 3. Lưu vào H2
                foodItemRepository.save(existingFood);
                
                // 4. Đồng bộ lên Firestore
                try {
                    if (firestoreService != null) {
                        // "existingFood" BÂY GIỜ đã có category đầy đủ
                        firestoreService.saveFood(existingFood);
                        System.out.println("Synced updated food to Firestore: " + existingFood.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to sync updated food to Firestore: " + e.getMessage());
                }
                
                logFoodUpdate(oldFood, existingFood, "UPDATE");
                redirectAttributes.addFlashAttribute("success", "Cập nhật món ăn thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật món ăn: " + e.getMessage());
        }
        return "redirect:/admin/foods";
    }
    // === KẾT THÚC SỬA LỖI ===
    
    @PostMapping("/foods/{id}/delete")
    public String deleteFood(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            FoodItem foodItem = foodItemRepository.findById(id).orElse(null);
            if (foodItem != null) {
                List<FoodUpdate> updatesToDelete = foodUpdateRepository.findByFoodItem(foodItem);
                if (!updatesToDelete.isEmpty()) {
                    foodUpdateRepository.deleteAll(updatesToDelete);
                }
                foodItemRepository.deleteById(id);
                try {
                    if (firestoreService != null) {
                        firestoreService.deleteFood(id);
                        System.out.println("Deleted food from Firestore: " + id);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to delete food from Firestore: " + e.getMessage());
                }
                redirectAttributes.addFlashAttribute("success", "Xóa món ăn thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy món ăn để xóa!");
            }
        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi xóa món ăn: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa món ăn. Hãy kiểm tra log server.");
        }
        return "redirect:/admin/foods";
    }
    
    @PostMapping("/foods/{id}/toggle-availability")
    public String toggleFoodAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Object obj = foodItemRepository.findById(id).orElse(null);
            FoodItem foodItem = obj != null ? (FoodItem) obj : null;
            if (foodItem != null) {
                foodItem.setAvailable(!foodItem.getAvailable());
                foodItem.setUpdatedAt(LocalDateTime.now());
                
                foodItemRepository.save(foodItem);

                try {
                    if (firestoreService != null) {
                        Category cat = categoryRepository.findById(foodItem.getCategory().getId()).orElse(null);
                        foodItem.setCategory(cat);
                        firestoreService.saveFood(foodItem); 
                        System.out.println("Synced toggle food to Firestore: " + foodItem.getId());
                    }
                } catch (Exception e) {
                    System.err.println("Failed to sync toggle food to Firestore: " + e.getMessage());
                }

                String status = foodItem.getAvailable() ? "kích hoạt" : "vô hiệu hóa";
                redirectAttributes.addFlashAttribute("success", 
                    "Đã " + status + " món ăn '" + foodItem.getName() + "' thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thay đổi trạng thái món ăn: " + e.getMessage());
        }
        return "redirect:/admin/foods";
    }
    
    // Food Updates Management - Lịch sử cập nhật
    @GetMapping("/updates")
    public String updates(Model model) {
        List<FoodUpdate> updates = foodUpdateRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("updates", updates);
        model.addAttribute("title", "Lịch sử cập nhật");
        model.addAttribute("subtitle", "Theo dõi các thay đổi về món ăn");
        model.addAttribute("activeTab", "updates");
        return "admin/updates";
    }
    
    // User Uploaded Foods Management - Quản lý món ăn người dùng upload
    @GetMapping("/user-uploaded-foods")
    public String userUploadedFoods(Model model) {
        List<FoodItem> userUploadedFoods;
        try {
            if (firestoreService != null) {
                userUploadedFoods = firestoreService.listFoodsAsEntities();
            } else {
                userUploadedFoods = foodItemRepository.findAll();
            }
        } catch (Exception e) {
            System.err.println("Error loading foods: " + e.getMessage());
            userUploadedFoods = foodItemRepository.findAll();
        }
        
        // Lọc chỉ các món ăn do người dùng upload (có user không null)
        userUploadedFoods = userUploadedFoods.stream()
            .filter(food -> food.getUser() != null)
            .sorted((a, b) -> {
                if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            })
            .toList();
        
        model.addAttribute("foods", userUploadedFoods);
        model.addAttribute("title", "Món ăn người dùng upload");
        model.addAttribute("subtitle", "Quản lý các món ăn được người dùng đăng tải");
        model.addAttribute("activeTab", "userUploadedFoods");
        return "admin/user-uploaded-foods";
    }
    
    // Search functionality - Tìm kiếm
    @GetMapping("/search")
    public String search(@RequestParam String query, Model model) {
        List<FoodItem> foodResults = foodItemRepository.findByNameContainingIgnoreCase(query);
        List<User> userResults = userRepository.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                               user.getFullName().toLowerCase().contains(query.toLowerCase()) ||
                               user.getEmail().toLowerCase().contains(query.toLowerCase()))
                .toList();
        
        model.addAttribute("foodResults", foodResults);
        model.addAttribute("userResults", userResults);
        model.addAttribute("query", query);
        model.addAttribute("title", "Kết quả tìm kiếm: " + query);
        model.addAttribute("subtitle", "Kết quả tìm kiếm cho: " + query);
        model.addAttribute("activeTab", "search");
        return "admin/search-results";
    }
    
    // Helper method to log food updates
    private void logFoodUpdate(FoodItem oldFood, FoodItem newFood, String action) {
        try {
            FoodUpdate update = new FoodUpdate();
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin != null) {
                update.setUser(admin);
            }
            if (newFood != null) {
                update.setFoodItem(newFood);
            } else if (oldFood != null) {
                update.setFoodItem(oldFood);
            }
            update.setAction(action);
            if (oldFood != null && newFood != null) {
                update.setOldData("Updated from: " + oldFood.getName());
                update.setNewData("Updated to: " + newFood.getName());
            } else if (newFood != null) {
                update.setNewData("Created: " + newFood.getName());
            } else if (oldFood != null) {
                update.setOldData("Deleted: " + oldFood.getName());
            }
            foodUpdateRepository.save(update);
        } catch (Exception e) {
            System.err.println("Error logging food update: " + e.getMessage());
        }
    }
    
    // Helper method to save uploaded images
    private String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        return fileName;
    }
    
    // ==========================================================
    // NUTRITION MANAGEMENT - Quản lý Calories người dùng
    // ==========================================================
    
    @GetMapping("/nutrition")
    public String nutrition(
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "filter", required = false, defaultValue = "all") String filter,
            @RequestParam(value = "period", required = false, defaultValue = "week") String period,
            Model model) {
        List<NutritionStats> allStats = new ArrayList<>();
        String errorMessage = null;
        
        try {
            if (firestoreService != null) {
                try {
                    if (userId != null && !userId.isEmpty()) {
                        // Chi tiết một user cụ thể
                        NutritionStats stats = null;
                        List<DailyLog> weeklyLogs = new ArrayList<>();
                        List<DailyLog> allLogs = new ArrayList<>();
                        
                        try {
                            stats = firestoreService.calculateNutritionStats(userId);
                        } catch (Exception e) {
                            System.err.println("Error calculating nutrition stats: " + e.getMessage());
                            e.printStackTrace();
                            errorMessage = "Không thể tải thống kê calories cho người dùng này: " + e.getMessage();
                            stats = null;
                        }
                        
                        try {
                            weeklyLogs = firestoreService.getUserDailyLogs(userId, 7);
                            if (weeklyLogs == null) {
                                weeklyLogs = new ArrayList<>();
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading weekly logs: " + e.getMessage());
                            weeklyLogs = new ArrayList<>();
                        }
                        
                        try {
                            allLogs = firestoreService.getAllUserDailyLogs(userId);
                            if (allLogs == null) {
                                allLogs = new ArrayList<>();
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading all logs: " + e.getMessage());
                            allLogs = new ArrayList<>();
                        }
                        
                        if (stats != null) {
                            model.addAttribute("selectedStats", stats);
                        }
                        model.addAttribute("weeklyLogs", weeklyLogs);
                        model.addAttribute("allLogs", allLogs);
                    } else {
                        // Danh sách tất cả users
                        try {
                            allStats = firestoreService.getAllUsersNutritionStats();
                            if (allStats == null) {
                                allStats = new ArrayList<>();
                            }
                        } catch (Exception e) {
                            System.err.println("Error loading all users nutrition stats: " + e.getMessage());
                            e.printStackTrace();
                            errorMessage = "Không thể tải danh sách người dùng: " + e.getMessage();
                            allStats = new ArrayList<>();
                        }
                        
                        // Áp dụng filter
                        if (!allStats.isEmpty()) {
                            try {
                                if ("high".equals(filter)) {
                                    allStats = allStats.stream()
                                        .filter(s -> s != null && s.getAverageCalories() > 2500f)
                                        .collect(java.util.stream.Collectors.toList());
                                } else if ("low".equals(filter)) {
                                    allStats = allStats.stream()
                                        .filter(s -> s != null && s.getAverageCalories() > 0f && s.getAverageCalories() < 1500f)
                                        .collect(java.util.stream.Collectors.toList());
                                } else if ("reached".equals(filter)) {
                                    allStats = allStats.stream()
                                        .filter(s -> s != null && s.getGoalAchievementRate() >= 80f)
                                        .collect(java.util.stream.Collectors.toList());
                                } else if ("not-reached".equals(filter)) {
                                    allStats = allStats.stream()
                                        .filter(s -> s != null && s.getGoalAchievementRate() < 80f && s.getDaysTracked() > 0)
                                        .collect(java.util.stream.Collectors.toList());
                                }
                                
                                // Sắp xếp theo calories trung bình
                                allStats.sort((a, b) -> Float.compare(b.getAverageCalories(), a.getAverageCalories()));
                            } catch (Exception e) {
                                System.err.println("Error filtering stats: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error in FirestoreService: " + e.getMessage());
                    e.printStackTrace();
                    errorMessage = "Lỗi kết nối với Firestore: " + e.getMessage();
                }
            } else {
                errorMessage = "FirestoreService chưa được cấu hình. Vui lòng kiểm tra cấu hình kết nối Firestore.";
                System.out.println("FirestoreService is null - nutrition data not available");
            }
        } catch (Exception e) {
            System.err.println("Unexpected error in nutrition controller: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "Đã xảy ra lỗi không mong muốn: " + e.getMessage();
        }
        
        // Đảm bảo tất cả attributes đều có giá trị, không null
        model.addAttribute("statsList", allStats != null ? allStats : new ArrayList<>());
        model.addAttribute("selectedUserId", userId != null ? userId : "");
        model.addAttribute("filter", filter != null ? filter : "all");
        model.addAttribute("period", period != null ? period : "week");
        model.addAttribute("title", "Quản lý Calories");
        model.addAttribute("subtitle", "Theo dõi và phân tích calories người dùng");
        model.addAttribute("activeTab", "nutrition");
        if (errorMessage != null) {
            model.addAttribute("error", errorMessage);
        }
        
        // Đảm bảo selectedStats, weeklyLogs, allLogs luôn có trong model (có thể null)
        if (!model.containsAttribute("selectedStats")) {
            model.addAttribute("selectedStats", null);
        }
        if (!model.containsAttribute("weeklyLogs")) {
            model.addAttribute("weeklyLogs", new ArrayList<>());
        }
        if (!model.containsAttribute("allLogs")) {
            model.addAttribute("allLogs", new ArrayList<>());
        }
        
        return "admin/nutrition";
    }
    
    @GetMapping("/nutrition/{userId}")
    public String nutritionDetail(@PathVariable String userId, Model model) {
        return nutrition(userId, "all", "week", model);
    }
    
    // ==========================================================
    // POSTS MANAGEMENT - Quản lý Posts
    // ==========================================================
    
    @GetMapping("/posts")
    public String posts(Model model) {
        List<Post> posts = new ArrayList<>();
        try {
            if (firestoreService != null) {
                posts = firestoreService.getAllPosts();
            }
        } catch (Exception e) {
            System.err.println("Error loading posts: " + e.getMessage());
            e.printStackTrace();
        }
        
        model.addAttribute("posts", posts != null ? posts : new ArrayList<>());
        model.addAttribute("title", "Quản lý Posts");
        model.addAttribute("subtitle", "Quản lý bài viết từ người dùng");
        model.addAttribute("activeTab", "posts");
        return "admin/posts";
    }
    
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            if (firestoreService != null) {
                boolean success = firestoreService.deletePost(id);
                if (success) {
                    redirectAttributes.addFlashAttribute("success", "Xóa bài viết thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa bài viết!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa bài viết: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }
    
    // ==========================================================
    // REVIEWS MANAGEMENT - Quản lý Reviews
    // ==========================================================
    
    @GetMapping("/reviews")
    public String reviews(Model model) {
        List<Review> reviews = new ArrayList<>();
        try {
            if (firestoreService != null) {
                reviews = firestoreService.getAllReviews();
            }
        } catch (Exception e) {
            System.err.println("Error loading reviews: " + e.getMessage());
            e.printStackTrace();
        }
        
        model.addAttribute("reviews", reviews != null ? reviews : new ArrayList<>());
        model.addAttribute("title", "Quản lý Reviews");
        model.addAttribute("subtitle", "Quản lý đánh giá món ăn");
        model.addAttribute("activeTab", "reviews");
        return "admin/reviews";
    }
    
    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            if (firestoreService != null) {
                boolean success = firestoreService.deleteReview(id);
                if (success) {
                    redirectAttributes.addFlashAttribute("success", "Xóa đánh giá thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa đánh giá!");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa đánh giá: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }
    
    // ==========================================================
    // ANALYTICS & REPORTS - Thống kê và báo cáo
    // ==========================================================
    
    @GetMapping("/analytics")
    public String analytics(@RequestParam(required = false, defaultValue = "7") int days, Model model) {
        AnalyticsData analytics = new AnalyticsData();
        
        try {
            if (firestoreService != null) {
                // Lấy tổng số users
                try {
                    List<User> users = firestoreService.listUsersAsEntities();
                    analytics.setTotalUsers((long) users.size());
                } catch (Exception e) {
                    analytics.setTotalUsers(userRepository.count());
                }
                
                // Lấy tổng số posts
                try {
                    List<Post> posts = firestoreService.getAllPosts();
                    analytics.setTotalPosts((long) posts.size());
                } catch (Exception e) {
                    analytics.setTotalPosts(0L);
                }
                
                // Lấy tổng số reviews
                try {
                    List<Review> reviews = firestoreService.getAllReviews();
                    analytics.setTotalReviews((long) reviews.size());
                    
                    // Tính average rating
                    if (!reviews.isEmpty()) {
                        double totalRating = reviews.stream()
                            .mapToInt(r -> r.getRating())
                            .sum();
                        analytics.setAverageRating(totalRating / reviews.size());
                    }
                } catch (Exception e) {
                    analytics.setTotalReviews(0L);
                }
                
                // Lấy tổng số food items
                analytics.setTotalFoodItems(foodItemRepository.count());
                
                // Tính tổng calories tracked
                try {
                    Long totalCalories = firestoreService.getTotalCaloriesTracked();
                    analytics.setTotalCaloriesTracked(totalCalories != null ? totalCalories : 0L);
                } catch (Exception e) {
                    analytics.setTotalCaloriesTracked(0L);
                }
                
                // Lấy active users
                try {
                    Long activeUsers = firestoreService.getActiveUsersCount();
                    analytics.setActiveUsers(activeUsers != null ? activeUsers : 0L);
                } catch (Exception e) {
                    analytics.setActiveUsers(0L);
                }
                
                // Lấy thống kê theo số ngày được chọn
                try {
                    List<AnalyticsData.DailyStats> dailyStats = firestoreService.getDailyStats(days);
                    analytics.setDailyStats(dailyStats);
                    
                    // Tính toán giá trị hôm nay (phần tử đầu tiên trong list)
                    if (dailyStats != null && !dailyStats.isEmpty()) {
                        AnalyticsData.DailyStats todayStats = dailyStats.get(0);
                        model.addAttribute("todayNewUsers", todayStats.getNewUsers());
                        model.addAttribute("todayNewPosts", todayStats.getNewPosts());
                        model.addAttribute("todayNewReviews", todayStats.getNewReviews());
                    } else {
                        model.addAttribute("todayNewUsers", 0L);
                        model.addAttribute("todayNewPosts", 0L);
                        model.addAttribute("todayNewReviews", 0L);
                    }
                } catch (Exception e) {
                    analytics.setDailyStats(new ArrayList<>());
                    model.addAttribute("todayNewUsers", 0L);
                    model.addAttribute("todayNewPosts", 0L);
                    model.addAttribute("todayNewReviews", 0L);
                }
            } else {
                // Fallback nếu không có Firestore
                analytics.setTotalUsers(userRepository.count());
                analytics.setTotalFoodItems(foodItemRepository.count());
                analytics.setTotalPosts(0L);
                analytics.setTotalReviews(0L);
                analytics.setDailyStats(new ArrayList<>());
                model.addAttribute("todayNewUsers", 0L);
                model.addAttribute("todayNewPosts", 0L);
                model.addAttribute("todayNewReviews", 0L);
            }
            
            // Tính toán star classes cho rating (cho cả 2 trường hợp)
            List<String> starClasses = new ArrayList<>();
            if (analytics != null && analytics.getAverageRating() != null) {
                double rating = analytics.getAverageRating();
                for (int i = 1; i <= 5; i++) {
                    if (i <= rating) {
                        starClasses.add("fa-solid fa-star text-yellow-400 text-xl");
                    } else if (i - 0.5 <= rating) {
                        starClasses.add("fa-solid fa-star-half-stroke text-yellow-400 text-xl");
                    } else {
                        starClasses.add("fa-regular fa-star text-gray-300 text-xl");
                    }
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    starClasses.add("fa-regular fa-star text-gray-300 text-xl");
                }
            }
            model.addAttribute("starClasses", starClasses);
        } catch (Exception e) {
            System.err.println("Error loading analytics: " + e.getMessage());
            e.printStackTrace();
        }
        
        model.addAttribute("analytics", analytics);
        model.addAttribute("days", days);
        model.addAttribute("title", "Analytics & Reports");
        model.addAttribute("subtitle", "Thống kê và báo cáo hệ thống");
        model.addAttribute("activeTab", "analytics");
        return "admin/analytics";
    }
    
    // ==========================================================
    // NOTIFICATIONS MANAGEMENT - Quản lý thông báo
    // ==========================================================
    
    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("title", "Quản lý Notifications");
        model.addAttribute("subtitle", "Gửi và quản lý thông báo");
        model.addAttribute("activeTab", "notifications");
        return "admin/notifications";
    }
    
    @PostMapping("/notifications/send")
    public String sendNotification(@RequestParam String title,
                                   @RequestParam String message,
                                   @RequestParam(required = false, defaultValue = "all") String target,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (notificationService == null) {
                redirectAttributes.addFlashAttribute("error", "Dịch vụ thông báo chưa được kích hoạt. Vui lòng kiểm tra cấu hình Firebase.");
                return "redirect:/admin/notifications";
            }

            int sentCount = 0;
            String targetName = "";

            switch (target) {
                case "all":
                    sentCount = notificationService.sendNotificationToAll(title, message);
                    targetName = "tất cả người dùng";
                    break;
                case "active":
                    sentCount = notificationService.sendNotificationToActive(title, message);
                    targetName = "người dùng hoạt động";
                    break;
                case "new":
                    sentCount = notificationService.sendNotificationToNew(title, message);
                    targetName = "người dùng mới";
                    break;
                default:
                    sentCount = notificationService.sendNotificationToAll(title, message);
                    targetName = "tất cả người dùng";
            }

            if (sentCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    String.format("Đã gửi thông báo thành công đến %d %s!", sentCount, targetName));
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Không có người dùng nào có FCM token để nhận thông báo. Vui lòng kiểm tra lại.");
            }
        } catch (Exception e) {
            System.err.println("Error sending notification: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi thông báo: " + e.getMessage());
        }
        return "redirect:/admin/notifications";
    }
    
    // ==========================================================
    // EXPORT EXCEL - Xuất dữ liệu ra Excel
    // ==========================================================
    
    @GetMapping("/nutrition/export")
    public void exportNutritionToExcel(
            @RequestParam(value = "userId", required = false) String userId,
            HttpServletResponse response) throws IOException {
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=nutrition_data.xlsx");
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Nutrition Data");
        
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Người dùng", "Email", "Calories TB", "Mục tiêu", "Số ngày", "Đạt mục tiêu", "Tỉ lệ %"};
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get data
        List<NutritionStats> statsList = new ArrayList<>();
        try {
            if (firestoreService != null) {
                if (userId != null && !userId.isEmpty()) {
                    NutritionStats stats = firestoreService.calculateNutritionStats(userId);
                    if (stats != null) {
                        statsList.add(stats);
                    }
                } else {
                    statsList = firestoreService.getAllUsersNutritionStats();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading nutrition data for export: " + e.getMessage());
        }
        
        // Write data rows
        int rowNum = 1;
        for (NutritionStats stats : statsList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stats.getUserName() != null ? stats.getUserName() : "N/A");
            row.createCell(1).setCellValue(stats.getUserEmail() != null ? stats.getUserEmail() : "N/A");
            row.createCell(2).setCellValue(stats.getAverageCalories());
            row.createCell(3).setCellValue(stats.getCaloriesTarget());
            row.createCell(4).setCellValue(stats.getDaysTracked());
            row.createCell(5).setCellValue(stats.getDaysReachedGoal());
            row.createCell(6).setCellValue(stats.getGoalAchievementRate());
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Write to response
        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}