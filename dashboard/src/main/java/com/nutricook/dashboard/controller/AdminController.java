package com.nutricook.dashboard.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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
import com.nutricook.dashboard.repository.CategoryRepository;
import com.nutricook.dashboard.repository.FoodItemRepository;
import com.nutricook.dashboard.repository.FoodUpdateRepository;
import com.nutricook.dashboard.repository.UserRepository;
import com.nutricook.dashboard.service.FirestoreService;
import com.nutricook.dashboard.service.NotificationService;
import com.nutricook.dashboard.service.CloudinaryService;

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
    
    @Autowired(required = false)
    private CloudinaryService cloudinaryService;
    
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
        
        // Auto-migrate local images to Cloudinary in background
        // This runs asynchronously to not block server startup
        new Thread(() -> {
            try {
                // Wait a bit for all services to be fully initialized
                Thread.sleep(5000); // 5 seconds delay
                System.out.println("🔄 Starting automatic image migration to Cloudinary...");
                autoMigrateAndSyncImages();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Auto-migration thread interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("❌ Error during auto-migration: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        // Khối tạo food mẫu đã bị xóa
    }
    
    /**
     * Tự động migrate và sync tất cả hình ảnh local lên Cloudinary và Firestore
     * Chạy trong background thread để không làm chậm server startup
     */
    private void autoMigrateAndSyncImages() {
        try {
            if (cloudinaryService == null || !cloudinaryService.isConfigured()) {
                System.out.println("⚠️ CloudinaryService not available or not configured. Skipping auto-migration.");
                return;
            }
            
            if (firestoreService == null) {
                System.out.println("⚠️ FirestoreService not available. Skipping auto-migration.");
                return;
            }
            
            List<FoodItem> allFoods = foodItemRepository.findAll();
            int migratedCount = 0;
            int syncedCount = 0;
            int skippedCount = 0;
            int errorCount = 0;
            
            System.out.println("🔍 Checking " + allFoods.size() + " FoodItems for local images...");
            
            for (FoodItem food : allFoods) {
                try {
                    String imageUrl = food.getImageUrl();
                    
                    // Kiểm tra xem có local URL cần migrate không
                    if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                        String fileName = imageUrl.substring("/uploads/".length());
                        Path filePath = Paths.get(UPLOAD_DIR + fileName);
                        
                        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                            System.out.println("   🔄 Migrating FoodItem ID: " + food.getId() + " (" + food.getName() + ")");
                            
                            // Upload lên Cloudinary
                            String cloudinaryUrl = cloudinaryService.uploadImageFromFile(filePath);
                            food.setImageUrl(cloudinaryUrl);
                            
                            // Đảm bảo category được load đầy đủ
                            if (food.getCategory() != null && food.getCategory().getId() != null) {
                                Category category = categoryRepository.findById(food.getCategory().getId()).orElse(null);
                                if (category != null) {
                                    food.setCategory(category);
                                }
                            }
                            
                            // Lưu vào database
                            foodItemRepository.save(food);
                            
                            // Sync lên Firestore
                            firestoreService.saveFood(food);
                            
                            migratedCount++;
                            syncedCount++;
                            System.out.println("   ✅ Migrated and synced: " + food.getId());
                        } else {
                            skippedCount++;
                            System.out.println("   ⚠️ File not found for FoodItem ID: " + food.getId());
                        }
                    } else if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.contains("cloudinary.com")) {
                        // Nếu có URL nhưng không phải Cloudinary và không phải local, chỉ sync lại
                        if (food.getCategory() != null && food.getCategory().getId() != null) {
                            Category category = categoryRepository.findById(food.getCategory().getId()).orElse(null);
                            if (category != null) {
                                food.setCategory(category);
                            }
                        }
                        firestoreService.saveFood(food);
                        syncedCount++;
                    }
                    
                } catch (Exception e) {
                    errorCount++;
                    System.err.println("   ❌ Error processing FoodItem ID: " + food.getId() + " - " + e.getMessage());
                }
            }
            
            System.out.println("==========================================");
            System.out.println("✅ Auto-migration completed!");
            System.out.println("   Migrated to Cloudinary: " + migratedCount);
            System.out.println("   Synced to Firestore: " + syncedCount);
            System.out.println("   Skipped (file not found): " + skippedCount);
            System.out.println("   Errors: " + errorCount);
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("❌ Error during auto-migration: " + e.getMessage());
            e.printStackTrace();
        }
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
        long userRecipeCount = 0;
        try {
            if (firestoreService != null) {
                System.out.println("🔍 Attempting to count user recipes from Firestore...");
                userRecipeCount = firestoreService.countUserRecipes();
                System.out.println("✅ Loaded user recipe count from Firestore: " + userRecipeCount);
            } else {
                System.err.println("⚠️ FirestoreService is null, cannot load user recipes");
            }
        } catch (Exception e) {
            System.err.println("❌ Error counting user recipes from Firestore: " + e.getMessage());
            e.printStackTrace();
            // Thử fallback bằng cách list và count
            try {
                if (firestoreService != null) {
                    System.out.println("🔄 Trying fallback: listing recipes...");
                    List<com.nutricook.dashboard.entity.UserRecipe> recipes = firestoreService.listUserRecipes();
                    userRecipeCount = recipes != null ? recipes.size() : 0;
                    System.out.println("✅ Fallback count: " + userRecipeCount);
                }
            } catch (Exception e2) {
                System.err.println("❌ Fallback also failed: " + e2.getMessage());
                userRecipeCount = 0;
            }
        }
        
        model.addAttribute("userCount", userCount);
        model.addAttribute("foodCount", foodItemRepository.count());
        model.addAttribute("categoryCount", categoryRepository.count());
        model.addAttribute("userRecipeCount", userRecipeCount);
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
        model.addAttribute("title", "Danh mục nguyên liệu");
        model.addAttribute("subtitle", "Quản lý danh mục nguyên liệu");
        model.addAttribute("activeTab", "categories");
        return "admin/categories";
    }
    
    // Lấy nguyên liệu theo category
    @GetMapping("/categories/{id}/ingredients")
    public String getIngredientsByCategory(@PathVariable Long id, 
                                           @RequestParam(value = "search", required = false) String search,
                                           @RequestParam(value = "filterCalories", required = false) String filterCalories,
                                           Model model) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return "redirect:/admin/categories";
        }
        
        List<FoodItem> ingredients;
        try {
            if (firestoreService != null) {
                // Lấy tất cả foods từ Firestore và lọc theo category
                List<FoodItem> allFoods = firestoreService.listFoodsAsEntities();
                ingredients = allFoods.stream()
                    .filter(food -> food.getCategory() != null && food.getCategory().getId().equals(category.getId()))
                    .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                    .toList();
            } else {
                ingredients = foodItemRepository.findByCategoryOrderByNameAsc(category);
            }
        } catch (Exception e) {
            System.err.println("Error loading ingredients: " + e.getMessage());
            ingredients = foodItemRepository.findByCategoryOrderByNameAsc(category);
        }
        
        // Apply filters
        List<FoodItem> filteredIngredients = new ArrayList<>(ingredients);
        
        // Filter by search
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            filteredIngredients = filteredIngredients.stream()
                .filter(food -> 
                    (food.getName() != null && food.getName().toLowerCase().contains(searchLower)) ||
                    (food.getDescription() != null && food.getDescription().toLowerCase().contains(searchLower))
                )
                .toList();
        }
        
        // Filter by calories
        if (filterCalories != null && !filterCalories.isEmpty()) {
            if ("low".equals(filterCalories)) {
                filteredIngredients = filteredIngredients.stream()
                    .filter(food -> {
                        try {
                            String calStr = food.getCalories();
                            if (calStr != null && calStr.contains("kcal")) {
                                double cal = Double.parseDouble(calStr.replaceAll("[^0-9.]", ""));
                                return cal < 50;
                            }
                            return false;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .toList();
            } else if ("medium".equals(filterCalories)) {
                filteredIngredients = filteredIngredients.stream()
                    .filter(food -> {
                        try {
                            String calStr = food.getCalories();
                            if (calStr != null && calStr.contains("kcal")) {
                                double cal = Double.parseDouble(calStr.replaceAll("[^0-9.]", ""));
                                return cal >= 50 && cal <= 150;
                            }
                            return false;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .toList();
            } else if ("high".equals(filterCalories)) {
                filteredIngredients = filteredIngredients.stream()
                    .filter(food -> {
                        try {
                            String calStr = food.getCalories();
                            if (calStr != null && calStr.contains("kcal")) {
                                double cal = Double.parseDouble(calStr.replaceAll("[^0-9.]", ""));
                                return cal > 150;
                            }
                            return false;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .toList();
            }
        }
        
        // Lấy tất cả categories để hiển thị trong form thêm nguyên liệu
        List<Category> allCategories;
        try {
            if (firestoreService != null) {
                allCategories = firestoreService.listCategoriesAsEntities();
            } else {
                allCategories = categoryRepository.findAll();
            }
        } catch (Exception e) {
            allCategories = categoryRepository.findAll();
        }
        
        model.addAttribute("category", category);
        model.addAttribute("ingredients", filteredIngredients);
        model.addAttribute("categories", allCategories);
        model.addAttribute("foodItem", new FoodItem());
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("filterCalories", filterCalories != null ? filterCalories : "");
        model.addAttribute("title", "Nguyên liệu: " + category.getName());
        model.addAttribute("subtitle", "Danh sách nguyên liệu trong danh mục " + category.getName());
        model.addAttribute("activeTab", "categories");
        return "admin/category-ingredients";
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
    public String foods() {
        // Redirect to categories page since food management is now done within categories
        return "redirect:/admin/categories";
    }
    
    @PostMapping("/foods")
    public String createFood(@ModelAttribute FoodItem foodItem,
                           @RequestParam(value = "category.id", required = false) Long categoryId,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           @RequestParam(value = "returnToCategory", required = false) Long returnToCategory,
                           @RequestParam(value = "vitaminA", required = false) Double vitaminA,
                           @RequestParam(value = "vitaminB1", required = false) Double vitaminB1,
                           @RequestParam(value = "vitaminB2", required = false) Double vitaminB2,
                           @RequestParam(value = "vitaminB3", required = false) Double vitaminB3,
                           @RequestParam(value = "vitaminB6", required = false) Double vitaminB6,
                           @RequestParam(value = "vitaminB9", required = false) Double vitaminB9,
                           @RequestParam(value = "vitaminB12", required = false) Double vitaminB12,
                           @RequestParam(value = "vitaminC", required = false) Double vitaminC,
                           @RequestParam(value = "vitaminD", required = false) Double vitaminD,
                           @RequestParam(value = "vitaminE", required = false) Double vitaminE,
                           @RequestParam(value = "vitaminK", required = false) Double vitaminK,
                           RedirectAttributes redirectAttributes) {
        try {
            // Nếu có returnToCategory (từ trang category-ingredients), ưu tiên sử dụng nó
            // Đây là trường hợp khi thêm nguyên liệu từ trang category-ingredients
            Long finalCategoryId = null;
            if (returnToCategory != null && returnToCategory > 0) {
                finalCategoryId = returnToCategory;
            } else if (categoryId != null && categoryId > 0) {
                finalCategoryId = categoryId;
            }
            
            // Validate category
            if (finalCategoryId == null || finalCategoryId <= 0) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn danh mục!");
                if (returnToCategory != null && returnToCategory > 0) {
                    return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
                }
                return "redirect:/admin/categories";
            }
            
            // Load and set category
            Category category = categoryRepository.findById(finalCategoryId).orElse(null);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
                if (returnToCategory != null && returnToCategory > 0) {
                    return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
                }
                return "redirect:/admin/categories";
            }
            foodItem.setCategory(category);
            
            if (foodItemRepository.existsByName(foodItem.getName())) {
                redirectAttributes.addFlashAttribute("error", "Tên món ăn đã tồn tại!");
                if (returnToCategory != null) {
                    return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
                }
                return "redirect:/admin/categories";
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                System.out.println("==========================================");
                System.out.println("📸 Processing image upload for new FoodItem...");
                System.out.println("   File name: " + imageFile.getOriginalFilename());
                System.out.println("   File size: " + imageFile.getSize() + " bytes");
                System.out.println("   Content type: " + imageFile.getContentType());
                System.out.println("   CloudinaryService is null: " + (cloudinaryService == null));
                
                if (cloudinaryService != null) {
                    boolean isConfigured = cloudinaryService.isConfigured();
                    System.out.println("   CloudinaryService.isConfigured(): " + isConfigured);
                    
                    // Kiểm tra xem Cloudinary có được cấu hình đúng không
                    if (isConfigured) {
                        System.out.println("✅ CloudinaryService is available and configured, attempting upload...");
                        try {
                            String imageUrl = cloudinaryService.uploadImage(imageFile);
                            foodItem.setImageUrl(imageUrl);
                            System.out.println("✅ Image uploaded to Cloudinary successfully!");
                            System.out.println("   Cloudinary URL: " + imageUrl);
                            System.out.println("==========================================");
                        } catch (Exception e) {
                            System.err.println("❌ Error uploading image to Cloudinary!");
                            System.err.println("   Exception: " + e.getClass().getName());
                            System.err.println("   Message: " + e.getMessage());
                            System.err.println("   Stack trace:");
                            e.printStackTrace();
                            // Fallback to local storage if Cloudinary fails
                            System.out.println("⚠️ Falling back to local storage...");
                            String fileName = saveImage(imageFile);
                            foodItem.setImageUrl("/uploads/" + fileName);
                            System.out.println("⚠️ Saved to local storage: /uploads/" + fileName);
                            System.out.println("==========================================");
                        }
                    } else {
                        System.err.println("❌ WARNING: CloudinaryService exists but Cloudinary is NOT configured!");
                        System.err.println("   Please set the following environment variables:");
                        System.err.println("   - CLOUDINARY_CLOUD_NAME");
                        System.err.println("   - CLOUDINARY_API_KEY");
                        System.err.println("   - CLOUDINARY_API_SECRET");
                        System.err.println("   Or update in application.properties");
                        System.err.println("   Falling back to local storage...");
                        String fileName = saveImage(imageFile);
                        foodItem.setImageUrl("/uploads/" + fileName);
                        System.out.println("⚠️ Saved to local storage: /uploads/" + fileName);
                        System.out.println("==========================================");
                    }
                } else {
                    System.err.println("❌ WARNING: CloudinaryService is NULL!");
                    System.err.println("   This means CloudinaryConfig bean was not created properly.");
                    System.err.println("   Check if Cloudinary dependencies are in pom.xml");
                    System.err.println("   Falling back to local storage...");
                    // Fallback to local storage if CloudinaryService is not available
                    String fileName = saveImage(imageFile);
                    foodItem.setImageUrl("/uploads/" + fileName);
                    System.out.println("⚠️ Saved to local storage: /uploads/" + fileName);
                    System.out.println("==========================================");
                }
            } else {
                System.out.println("⚠️ No image file provided or file is empty");
            }
            
            // Set các giá trị vitamin chi tiết
            foodItem.setVitaminA(vitaminA);
            foodItem.setVitaminB1(vitaminB1);
            foodItem.setVitaminB2(vitaminB2);
            foodItem.setVitaminB3(vitaminB3);
            foodItem.setVitaminB6(vitaminB6);
            foodItem.setVitaminB9(vitaminB9);
            foodItem.setVitaminB12(vitaminB12);
            foodItem.setVitaminC(vitaminC);
            foodItem.setVitaminD(vitaminD);
            foodItem.setVitaminE(vitaminE);
            foodItem.setVitaminK(vitaminK);
            
            // Tính tổng vitamin (trung bình)
            foodItem.calculateTotalVitamin();
            
            FoodItem savedFood = foodItemRepository.save(foodItem);
            System.out.println("💾 Saved FoodItem to database. ID: " + savedFood.getId() + ", ImageURL: " + savedFood.getImageUrl());
            try {
                if (firestoreService != null) {
                    // Category đã được set rồi, không cần load lại
                    firestoreService.saveFood(savedFood);
                    System.out.println("✅ Synced new food to Firestore: " + savedFood.getId() + " with imageUrl: " + savedFood.getImageUrl());
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to sync new food to Firestore: " + e.getMessage());
                e.printStackTrace();
            }
            logFoodUpdate(null, savedFood, "CREATE");
            redirectAttributes.addFlashAttribute("success", "Thêm nguyên liệu thành công!");
            if (returnToCategory != null) {
                return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm món ăn: " + e.getMessage());
            if (returnToCategory != null) {
                return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
            }
        }
        return "redirect:/admin/categories";
    }
    
    /**
     * Import nhiều nguyên liệu từ file JSON
     */
    @PostMapping("/categories/{categoryId}/ingredients/import")
    public String importIngredientsJson(
            @PathVariable Long categoryId,
            @RequestParam("jsonFile") MultipartFile jsonFile,
            RedirectAttributes redirectAttributes) {
        try {
            // Validate file
            if (jsonFile == null || jsonFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file JSON!");
                return "redirect:/admin/categories/" + categoryId + "/ingredients";
            }
            
            // Validate file type
            String contentType = jsonFile.getContentType();
            if (contentType == null || (!contentType.equals("application/json") && 
                !jsonFile.getOriginalFilename().toLowerCase().endsWith(".json"))) {
                redirectAttributes.addFlashAttribute("error", "File phải có định dạng JSON!");
                return "redirect:/admin/categories/" + categoryId + "/ingredients";
            }
            
            // Parse JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = new String(jsonFile.getBytes(), "UTF-8");
            List<Map<String, Object>> ingredientsList = objectMapper.readValue(
                jsonContent, 
                new TypeReference<List<Map<String, Object>>>() {}
            );
            
            if (ingredientsList == null || ingredientsList.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "File JSON không chứa dữ liệu hoặc không đúng định dạng!");
                return "redirect:/admin/categories/" + categoryId + "/ingredients";
            }
            
            // Load category
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
                return "redirect:/admin/categories/" + categoryId + "/ingredients";
            }
            
            // Process each ingredient
            int successCount = 0;
            int failCount = 0;
            List<String> errorMessages = new ArrayList<>();
            
            for (int i = 0; i < ingredientsList.size(); i++) {
                Map<String, Object> ingredientData = ingredientsList.get(i);
                try {
                    // Extract required fields - handle both String and Number types
                    String name = getStringOrNumberValue(ingredientData, "name");
                    if (name == null || name.trim().isEmpty()) {
                        errorMessages.add("Dòng " + (i + 1) + ": Thiếu tên nguyên liệu");
                        failCount++;
                        continue;
                    }
                    name = name.trim();
                    
                    // Check if ingredient already exists
                    if (foodItemRepository.existsByName(name.trim())) {
                        errorMessages.add("Dòng " + (i + 1) + ": Nguyên liệu '" + name + "' đã tồn tại");
                        failCount++;
                        continue;
                    }
                    
                    // Extract calories - handle both String and Number types
                    String calories = getStringOrNumberValue(ingredientData, "calories");
                    if (calories == null || calories.trim().isEmpty()) {
                        calories = "0 kcal";
                    } else {
                        // Remove any whitespace
                        calories = calories.trim();
                        // If it's a number (no "kcal"), add "kcal"
                        if (!calories.toLowerCase().contains("kcal")) {
                            try {
                                // Try to parse as number
                                double calValue = Double.parseDouble(calories);
                                calories = String.format("%.1f", calValue).replace(".0", "").replace(",", ".") + " kcal";
                            } catch (NumberFormatException e) {
                                // If not a number, add "kcal" anyway
                                calories = calories + " kcal";
                            }
                        }
                    }
                    
                    // Extract unit - support both "unit" and "unit_name"
                    String unit = getStringOrNumberValue(ingredientData, "unit");
                    if (unit == null || unit.trim().isEmpty()) {
                        // Try unit_name as fallback
                        unit = getStringOrNumberValue(ingredientData, "unit_name");
                        if (unit == null || unit.trim().isEmpty()) {
                            unit = "g";
                        }
                    }
                    unit = unit.trim();
                    
                    // Extract description - handle both String and Number types
                    String description = getStringOrNumberValue(ingredientData, "description");
                    if (description == null) {
                        description = "";
                    } else {
                        description = description.trim();
                    }
                    
                    // Extract image URL - support both "image_url" and "imageUrl"
                    String imageUrl = getStringOrNumberValue(ingredientData, "image_url");
                    if (imageUrl == null || imageUrl.trim().isEmpty()) {
                        imageUrl = getStringOrNumberValue(ingredientData, "imageUrl");
                    }
                    if (imageUrl != null) {
                        imageUrl = imageUrl.trim();
                        // If it's a local path (like "assets/images/..."), keep it as is
                        // If it's a full URL, use it directly
                        // System will handle it later when syncing to Firestore
                    }
                    
                    // Create FoodItem
                    FoodItem foodItem = new FoodItem();
                    foodItem.setName(name.trim());
                    foodItem.setCalories(calories);
                    foodItem.setUnit(unit);
                    foodItem.setDescription(description);
                    foodItem.setCategory(category);
                    foodItem.setAvailable(true);
                    
                    // Set image URL if available
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        foodItem.setImageUrl(imageUrl);
                    }
                    
                    // Extract nutrition values
                    foodItem.setFat(getDoubleValue(ingredientData, "fat", 0.0));
                    foodItem.setCarbs(getDoubleValue(ingredientData, "carbs", 0.0));
                    foodItem.setProtein(getDoubleValue(ingredientData, "protein", 0.0));
                    foodItem.setCholesterol(getDoubleValue(ingredientData, "cholesterol", 0.0));
                    foodItem.setSodium(getDoubleValue(ingredientData, "sodium", 0.0));
                    
                    // Extract vitamin details - support both flat format (vitaminA) and nested format (vitamins.vitamin_a)
                    Map<String, Object> vitaminsObject = null;
                    Object vitaminsRaw = ingredientData.get("vitamins");
                    if (vitaminsRaw instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> temp = (Map<String, Object>) vitaminsRaw;
                        vitaminsObject = temp;
                    }
                    
                    // Try to get vitamins from nested object first, then flat format
                    foodItem.setVitaminA(getVitaminValue(ingredientData, vitaminsObject, "vitaminA", "vitamin_a", 0.0));
                    foodItem.setVitaminB1(getVitaminValue(ingredientData, vitaminsObject, "vitaminB1", "vitamin_b1", 0.0));
                    foodItem.setVitaminB2(getVitaminValue(ingredientData, vitaminsObject, "vitaminB2", "vitamin_b2", 0.0));
                    foodItem.setVitaminB3(getVitaminValue(ingredientData, vitaminsObject, "vitaminB3", "vitamin_b3", 0.0));
                    foodItem.setVitaminB6(getVitaminValue(ingredientData, vitaminsObject, "vitaminB6", "vitamin_b6", 0.0));
                    foodItem.setVitaminB9(getVitaminValue(ingredientData, vitaminsObject, "vitaminB9", "vitamin_b9", 0.0));
                    foodItem.setVitaminB12(getVitaminValue(ingredientData, vitaminsObject, "vitaminB12", "vitamin_b12", 0.0));
                    foodItem.setVitaminC(getVitaminValue(ingredientData, vitaminsObject, "vitaminC", "vitamin_c", 0.0));
                    foodItem.setVitaminD(getVitaminValue(ingredientData, vitaminsObject, "vitaminD", "vitamin_d", 0.0));
                    foodItem.setVitaminE(getVitaminValue(ingredientData, vitaminsObject, "vitaminE", "vitamin_e", 0.0));
                    foodItem.setVitaminK(getVitaminValue(ingredientData, vitaminsObject, "vitaminK", "vitamin_k", 0.0));
                    
                    // Calculate total vitamin
                    foodItem.calculateTotalVitamin();
                    
                    // Save to database
                    FoodItem savedFood = foodItemRepository.save(foodItem);
                    
                    // Sync to Firestore
                    try {
                        if (firestoreService != null) {
                            firestoreService.saveFood(savedFood);
                            System.out.println("✅ Synced imported food to Firestore: " + savedFood.getId());
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Failed to sync imported food to Firestore: " + e.getMessage());
                        // Continue even if Firestore sync fails
                    }
                    
                    logFoodUpdate(null, savedFood, "IMPORT");
                    successCount++;
                    
                } catch (Exception e) {
                    String ingredientName = ingredientData.get("name") != null ? 
                        String.valueOf(ingredientData.get("name")) : "Dòng " + (i + 1);
                    errorMessages.add(ingredientName + ": " + e.getMessage());
                    failCount++;
                    System.err.println("❌ Error importing ingredient at line " + (i + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Prepare result message
            StringBuilder message = new StringBuilder();
            message.append("Import hoàn tất: ");
            message.append(successCount).append(" nguyên liệu thành công");
            if (failCount > 0) {
                message.append(", ").append(failCount).append(" nguyên liệu thất bại");
                if (errorMessages.size() > 0 && errorMessages.size() <= 10) {
                    message.append("\nChi tiết lỗi:\n").append(String.join("\n", errorMessages));
                }
            }
            
            if (successCount > 0) {
                redirectAttributes.addFlashAttribute("success", message.toString());
            } else {
                redirectAttributes.addFlashAttribute("error", message.toString());
            }
            
            if (failCount > 0 && errorMessages.size() > 10) {
                redirectAttributes.addFlashAttribute("warning", 
                    "Có " + errorMessages.size() + " lỗi. Vui lòng kiểm tra console để xem chi tiết.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error importing JSON: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "Lỗi khi import file JSON: " + e.getMessage());
        }
        
        return "redirect:/admin/categories/" + categoryId + "/ingredients";
    }
    
    /**
     * Helper method to safely extract Double value from Map
     */
    private Double getDoubleValue(Map<String, Object> map, String key, Double defaultValue) {
        try {
            Object value = map.get(key);
            if (value == null) {
                return defaultValue;
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                String str = ((String) value).trim();
                if (str.isEmpty()) {
                    return defaultValue;
                }
                return Double.parseDouble(str);
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper method to safely extract Double value from Map (returns null if not found)
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        try {
            Object value = map.get(key);
            if (value == null) {
                return null;
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                String str = ((String) value).trim();
                if (str.isEmpty()) {
                    return null;
                }
                return Double.parseDouble(str);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Helper method to safely extract String value from Map (handles both String and Number)
     */
    private String getStringOrNumberValue(Map<String, Object> map, String key) {
        try {
            Object value = map.get(key);
            if (value == null) {
                return null;
            }
            if (value instanceof String) {
                return (String) value;
            }
            if (value instanceof Number) {
                // Convert number to string
                Number num = (Number) value;
                // Remove trailing zeros if it's a whole number
                if (num.doubleValue() == num.longValue()) {
                    return String.valueOf(num.longValue());
                } else {
                    return String.valueOf(num.doubleValue());
                }
            }
            // For other types, convert to string
            return String.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Helper method to extract vitamin value from either flat format or nested vitamins object
     */
    private Double getVitaminValue(Map<String, Object> ingredientData, Map<String, Object> vitaminsObject, 
                                   String flatKey, String nestedKey, Double defaultValue) {
        // Try nested format first (vitamins.vitamin_a)
        if (vitaminsObject != null) {
            Double nestedValue = getDoubleValue(vitaminsObject, nestedKey);
            if (nestedValue != null) {
                return nestedValue;
            }
        }
        
        // Try flat format (vitaminA)
        Double flatValue = getDoubleValue(ingredientData, flatKey);
        if (flatValue != null) {
            return flatValue;
        }
        
        // Try nested format with flat key name (vitamins.vitaminA)
        if (vitaminsObject != null) {
            Double nestedFlatValue = getDoubleValue(vitaminsObject, flatKey);
            if (nestedFlatValue != null) {
                return nestedFlatValue;
            }
        }
        
        return defaultValue != null ? defaultValue : 0.0;
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
                if (cloudinaryService != null) {
                    try {
                        String imageUrl = cloudinaryService.uploadImage(imageFile);
                        foodItem.setImageUrl(imageUrl);
                    } catch (Exception e) {
                        System.err.println("Error uploading image to Cloudinary: " + e.getMessage());
                        // Fallback to local storage if Cloudinary fails
                        String fileName = saveImage(imageFile);
                        foodItem.setImageUrl("/uploads/" + fileName);
                    }
                } else {
                    // Fallback to local storage if CloudinaryService is not available
                    String fileName = saveImage(imageFile);
                    foodItem.setImageUrl("/uploads/" + fileName);
                }
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
                           @RequestParam(value = "returnToCategory", required = false) Long returnToCategory,
                           @RequestParam(value = "vitaminA", required = false) Double vitaminA,
                           @RequestParam(value = "vitaminB1", required = false) Double vitaminB1,
                           @RequestParam(value = "vitaminB2", required = false) Double vitaminB2,
                           @RequestParam(value = "vitaminB3", required = false) Double vitaminB3,
                           @RequestParam(value = "vitaminB6", required = false) Double vitaminB6,
                           @RequestParam(value = "vitaminB9", required = false) Double vitaminB9,
                           @RequestParam(value = "vitaminB12", required = false) Double vitaminB12,
                           @RequestParam(value = "vitaminC", required = false) Double vitaminC,
                           @RequestParam(value = "vitaminD", required = false) Double vitaminD,
                           @RequestParam(value = "vitaminE", required = false) Double vitaminE,
                           @RequestParam(value = "vitaminK", required = false) Double vitaminK,
                           RedirectAttributes redirectAttributes) {
        try {
            FoodItem existingFood = foodItemRepository.findById(id).orElse(null);
            if (existingFood != null) {
                
                // 1. Lấy Category đầy đủ từ H2
                // Ưu tiên: returnToCategory > foodItem.getCategory().getId() > existingFood.getCategory().getId()
                Long categoryIdToUse = null;
                if (returnToCategory != null) {
                    categoryIdToUse = returnToCategory;
                } else if (foodItem.getCategory() != null && foodItem.getCategory().getId() != null) {
                    categoryIdToUse = foodItem.getCategory().getId();
                } else if (existingFood.getCategory() != null && existingFood.getCategory().getId() != null) {
                    categoryIdToUse = existingFood.getCategory().getId();
                }
                
                if (categoryIdToUse == null) {
                    redirectAttributes.addFlashAttribute("error", "Không thể xác định danh mục! Vui lòng chọn danh mục.");
                    if (returnToCategory != null) {
                        return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
                    }
                    return "redirect:/admin/categories";
                }
                
                Category categoryFromDb = categoryRepository.findById(categoryIdToUse).orElse(null);
                if (categoryFromDb == null) {
                    redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
                    if (returnToCategory != null) {
                        return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
                    }
                    return "redirect:/admin/categories";
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
                existingFood.setUnit(foodItem.getUnit()); // Cập nhật đơn vị
                // Cập nhật thông tin dinh dưỡng
                existingFood.setFat(foodItem.getFat());
                existingFood.setCarbs(foodItem.getCarbs());
                existingFood.setProtein(foodItem.getProtein());
                existingFood.setCholesterol(foodItem.getCholesterol());
                existingFood.setSodium(foodItem.getSodium());
                
                // Cập nhật các giá trị vitamin chi tiết
                // Luôn cập nhật với giá trị từ form, nếu null thì dùng 0.0
                existingFood.setVitaminA(vitaminA != null ? vitaminA : 0.0);
                existingFood.setVitaminB1(vitaminB1 != null ? vitaminB1 : 0.0);
                existingFood.setVitaminB2(vitaminB2 != null ? vitaminB2 : 0.0);
                existingFood.setVitaminB3(vitaminB3 != null ? vitaminB3 : 0.0);
                existingFood.setVitaminB6(vitaminB6 != null ? vitaminB6 : 0.0);
                existingFood.setVitaminB9(vitaminB9 != null ? vitaminB9 : 0.0);
                existingFood.setVitaminB12(vitaminB12 != null ? vitaminB12 : 0.0);
                existingFood.setVitaminC(vitaminC != null ? vitaminC : 0.0);
                existingFood.setVitaminD(vitaminD != null ? vitaminD : 0.0);
                existingFood.setVitaminE(vitaminE != null ? vitaminE : 0.0);
                existingFood.setVitaminK(vitaminK != null ? vitaminK : 0.0);
                
                // Tính tổng vitamin (trung bình)
                existingFood.calculateTotalVitamin();
                
                existingFood.setUpdatedAt(LocalDateTime.now());
                
                if (imageFile != null && !imageFile.isEmpty()) {
                    // Delete old image from Cloudinary if it's a Cloudinary URL
                    if (existingFood.getImageUrl() != null && existingFood.getImageUrl().contains("cloudinary.com") && cloudinaryService != null) {
                        cloudinaryService.deleteImage(existingFood.getImageUrl());
                    }
                    
                    if (cloudinaryService != null) {
                        try {
                            String imageUrl = cloudinaryService.uploadImage(imageFile);
                            existingFood.setImageUrl(imageUrl);
                            System.out.println("✅ Image uploaded to Cloudinary successfully. URL: " + imageUrl);
                        } catch (Exception e) {
                            System.err.println("❌ Error uploading image to Cloudinary: " + e.getMessage());
                            e.printStackTrace();
                            // Fallback to local storage if Cloudinary fails
                            String fileName = saveImage(imageFile);
                            existingFood.setImageUrl("/uploads/" + fileName);
                            System.out.println("⚠️ Fallback to local storage: /uploads/" + fileName);
                        }
                    } else {
                        // Fallback to local storage if CloudinaryService is not available
                        String fileName = saveImage(imageFile);
                        existingFood.setImageUrl("/uploads/" + fileName);
                        System.out.println("⚠️ CloudinaryService not available, using local storage: /uploads/" + fileName);
                    }
                }
                
                // 3. Lưu vào H2
                foodItemRepository.save(existingFood);
                System.out.println("💾 Updated FoodItem in database. ID: " + existingFood.getId() + ", ImageURL: " + existingFood.getImageUrl());
                
                // 4. Đồng bộ lên Firestore
                try {
                    if (firestoreService != null) {
                        // "existingFood" BÂY GIỜ đã có category đầy đủ
                        firestoreService.saveFood(existingFood);
                        System.out.println("✅ Synced updated food to Firestore: " + existingFood.getId() + " with imageUrl: " + existingFood.getImageUrl());
                    }
                } catch (Exception e) {
                    System.err.println("❌ Failed to sync updated food to Firestore: " + e.getMessage());
                    e.printStackTrace();
                }
                
                logFoodUpdate(oldFood, existingFood, "UPDATE");
                redirectAttributes.addFlashAttribute("success", "Cập nhật nguyên liệu thành công!");
                if (returnToCategory != null) {
                    return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
                }
                // Nếu không có returnToCategory nhưng có category trong foodItem, dùng category đó
                if (existingFood.getCategory() != null && existingFood.getCategory().getId() != null) {
                    return "redirect:/admin/categories/" + existingFood.getCategory().getId() + "/ingredients";
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật nguyên liệu: " + e.getMessage());
            if (returnToCategory != null) {
                return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
            }
        }
        return "redirect:/admin/categories";
    }
    // === KẾT THÚC SỬA LỖI ===
    
    @PostMapping("/foods/{id}/delete")
    public String deleteFood(@PathVariable Long id, 
                           @RequestParam(value = "returnToCategory", required = false) Long returnToCategory,
                           RedirectAttributes redirectAttributes) {
        try {
            FoodItem foodItem = foodItemRepository.findById(id).orElse(null);
            if (foodItem != null) {
                // Lưu categoryId trước khi xóa để redirect
                Long categoryId = (foodItem.getCategory() != null && foodItem.getCategory().getId() != null) 
                    ? foodItem.getCategory().getId() 
                    : returnToCategory;
                
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
                redirectAttributes.addFlashAttribute("success", "Xóa nguyên liệu thành công!");
                
                // Redirect về trang category-ingredients nếu có returnToCategory hoặc categoryId
                Long finalCategoryId = (returnToCategory != null) ? returnToCategory : categoryId;
                if (finalCategoryId != null) {
                    return "redirect:/admin/categories/" + finalCategoryId + "/ingredients";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy nguyên liệu để xóa!");
                if (returnToCategory != null) {
                    return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi nghiêm trọng khi xóa nguyên liệu: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa nguyên liệu. Hãy kiểm tra log server.");
            if (returnToCategory != null) {
                return "redirect:/admin/categories/" + returnToCategory + "/ingredients";
            }
        }
        return "redirect:/admin/categories";
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
    public String userUploadedFoods(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "filter", required = false) String filter,
            Model model) {
        List<com.nutricook.dashboard.entity.UserRecipe> userRecipes = new ArrayList<>();
        
        // Debug: Kiểm tra FirestoreService
        if (firestoreService == null) {
            System.err.println("⚠️ WARNING: FirestoreService is NULL! Firebase may not be enabled or configured.");
            model.addAttribute("error", "Firestore service không khả dụng. Vui lòng kiểm tra cấu hình Firebase trong application.properties (firebase.enabled=true)");
        } else {
            System.out.println("✅ FirestoreService is available. Loading user recipes...");
            try {
                userRecipes = firestoreService.listUserRecipes();
                System.out.println("✅ Loaded " + userRecipes.size() + " user recipes from Firestore");
                
                // Debug: In ra một vài recipe để kiểm tra
                if (!userRecipes.isEmpty()) {
                    com.nutricook.dashboard.entity.UserRecipe sample = userRecipes.get(0);
                    System.out.println("📋 Sample recipe: " + sample.getRecipeName() + " by " + sample.getUserEmail());
                    System.out.println("   ImageUrls: " + (sample.getImageUrls() != null ? sample.getImageUrls().size() + " items" : "null"));
                    if (sample.getImageUrls() != null && !sample.getImageUrls().isEmpty()) {
                        System.out.println("   First image URL: " + sample.getFirstImageUrl());
                    } else {
                        System.out.println("   ⚠️ No image URLs found!");
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading user recipes: " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("error", "Lỗi khi tải dữ liệu từ Firestore: " + e.getMessage());
            }
        }
        
        // Lọc theo search query
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            userRecipes = userRecipes.stream()
                .filter(recipe -> 
                    (recipe.getRecipeName() != null && recipe.getRecipeName().toLowerCase().contains(searchLower)) ||
                    (recipe.getUserEmail() != null && recipe.getUserEmail().toLowerCase().contains(searchLower)) ||
                    (recipe.getDescription() != null && recipe.getDescription().toLowerCase().contains(searchLower))
                )
                .toList();
        }
        
        // Lọc theo filter (approved, pending, hidden)
        if (filter != null && !filter.isEmpty()) {
            switch (filter) {
                case "approved":
                    userRecipes = userRecipes.stream()
                        .filter(recipe -> recipe.getApproved() != null && recipe.getApproved())
                        .toList();
                    break;
                case "pending":
                    userRecipes = userRecipes.stream()
                        .filter(recipe -> recipe.getApproved() == null || !recipe.getApproved())
                        .toList();
                    break;
                case "hidden":
                    userRecipes = userRecipes.stream()
                        .filter(recipe -> recipe.getAvailable() != null && !recipe.getAvailable())
                        .toList();
                    break;
            }
        }
        
        model.addAttribute("recipes", userRecipes);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("filter", filter != null ? filter : "");
        model.addAttribute("title", "Món ăn người dùng upload");
        model.addAttribute("subtitle", "Quản lý các món ăn được người dùng đăng tải");
        model.addAttribute("activeTab", "userUploadedFoods");
        return "admin/user-uploaded-foods";
    }
    
    // Xóa user recipe
    @PostMapping("/user-recipes/{docId}/delete")
    public String deleteUserRecipe(
            @PathVariable String docId,
            RedirectAttributes redirectAttributes) {
        try {
            if (firestoreService != null) {
                firestoreService.deleteUserRecipe(docId);
                redirectAttributes.addFlashAttribute("success", "Xóa công thức thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Firestore service không khả dụng!");
            }
        } catch (Exception e) {
            System.err.println("Error deleting user recipe: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa công thức: " + e.getMessage());
        }
        return "redirect:/admin/user-uploaded-foods";
    }
    
    // Duyệt/không duyệt user recipe
    @PostMapping("/user-recipes/{docId}/toggle-approval")
    public String toggleUserRecipeApproval(
            @PathVariable String docId,
            RedirectAttributes redirectAttributes) {
        try {
            if (firestoreService != null) {
                // Lấy recipe hiện tại để xem trạng thái
                List<com.nutricook.dashboard.entity.UserRecipe> recipes = firestoreService.listUserRecipes();
                com.nutricook.dashboard.entity.UserRecipe recipe = recipes.stream()
                    .filter(r -> r.getDocId().equals(docId))
                    .findFirst()
                    .orElse(null);
                
                if (recipe != null) {
                    boolean newApproved = !(recipe.getApproved() != null && recipe.getApproved());
                    firestoreService.updateUserRecipeApproval(docId, newApproved);
                    redirectAttributes.addFlashAttribute("success", 
                        newApproved ? "Đã duyệt công thức!" : "Đã hủy duyệt công thức!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy công thức!");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Firestore service không khả dụng!");
            }
        } catch (Exception e) {
            System.err.println("Error toggling approval: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/user-uploaded-foods";
    }
    
    // Ẩn/hiện user recipe
    @PostMapping("/user-recipes/{docId}/toggle-availability")
    public String toggleUserRecipeAvailability(
            @PathVariable String docId,
            RedirectAttributes redirectAttributes) {
        try {
            if (firestoreService != null) {
                // Lấy recipe hiện tại để xem trạng thái
                List<com.nutricook.dashboard.entity.UserRecipe> recipes = firestoreService.listUserRecipes();
                com.nutricook.dashboard.entity.UserRecipe recipe = recipes.stream()
                    .filter(r -> r.getDocId().equals(docId))
                    .findFirst()
                    .orElse(null);
                
                if (recipe != null) {
                    boolean newAvailable = !(recipe.getAvailable() != null && recipe.getAvailable());
                    firestoreService.updateUserRecipeAvailability(docId, newAvailable);
                    redirectAttributes.addFlashAttribute("success", 
                        newAvailable ? "Đã hiển thị công thức!" : "Đã ẩn công thức!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Không tìm thấy công thức!");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Firestore service không khả dụng!");
            }
        } catch (Exception e) {
            System.err.println("Error toggling availability: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/admin/user-uploaded-foods";
    }
    
    // Debug endpoint để kiểm tra userRecipes
    @GetMapping("/admin/debug/user-recipes")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugUserRecipes() {
        Map<String, Object> result = new java.util.HashMap<>();
        
        result.put("firestoreServiceAvailable", firestoreService != null);
        
        if (firestoreService == null) {
            result.put("error", "FirestoreService is null. Check firebase.enabled in application.properties");
            return ResponseEntity.ok(result);
        }
        
        try {
            List<com.nutricook.dashboard.entity.UserRecipe> recipes = firestoreService.listUserRecipes();
            result.put("count", recipes.size());
            result.put("recipes", recipes.stream().map(r -> {
                Map<String, Object> recipeMap = new java.util.HashMap<>();
                recipeMap.put("docId", r.getDocId());
                recipeMap.put("recipeName", r.getRecipeName());
                recipeMap.put("userEmail", r.getUserEmail());
                recipeMap.put("createdAt", r.getCreatedAt());
                recipeMap.put("approved", r.getApproved());
                recipeMap.put("available", r.getAvailable());
                // Debug imageUrls
                recipeMap.put("imageUrlsCount", r.getImageUrls() != null ? r.getImageUrls().size() : 0);
                recipeMap.put("imageUrls", r.getImageUrls());
                recipeMap.put("firstImageUrl", r.getFirstImageUrl());
                return recipeMap;
            }).toList());
            result.put("success", true);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("stackTrace", java.util.Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(result);
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
            // Log để debug
            System.out.println("========== Sending Notification ==========");
            System.out.println("Title received: [" + title + "]");
            System.out.println("Message received: [" + message + "]");
            System.out.println("Target: " + target);
            System.out.println("Title length: " + (title != null ? title.length() : 0));
            System.out.println("Message length: " + (message != null ? message.length() : 0));
            
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
    
    // ==================================================================
    // SYNC METHODS - Đồng bộ dữ liệu từ Database lên Firestore
    // ==================================================================
    
    /**
     * Endpoint để đồng bộ lại tất cả FoodItems từ database lên Firestore
     * Đảm bảo imageUrl Cloudinary được cập nhật đầy đủ
     */
    @GetMapping("/admin/sync/foods")
    public String syncAllFoodsToFirestore(RedirectAttributes redirectAttributes) {
        try {
            if (firestoreService == null) {
                redirectAttributes.addFlashAttribute("error", "FirestoreService không khả dụng!");
                return "redirect:/admin/categories";
            }
            
            // Lấy tất cả FoodItems từ database
            List<FoodItem> allFoods = foodItemRepository.findAll();
            int successCount = 0;
            int failCount = 0;
            int cloudinaryCount = 0;
            int localUrlCount = 0;
            List<Long> localUrlFoodIds = new ArrayList<>();
            
            System.out.println("🔄 Bắt đầu đồng bộ " + allFoods.size() + " FoodItems lên Firestore...");
            System.out.println("==========================================");
            
            for (FoodItem food : allFoods) {
                try {
                    // Đảm bảo category được load đầy đủ
                    if (food.getCategory() != null && food.getCategory().getId() != null) {
                        Category category = categoryRepository.findById(food.getCategory().getId()).orElse(null);
                        if (category != null) {
                            food.setCategory(category);
                        }
                    }
                    
                    // Kiểm tra imageUrl từ database
                    String imageUrl = food.getImageUrl();
                    System.out.println("🔍 Checking FoodItem ID: " + food.getId() + " (" + food.getName() + ")");
                    System.out.println("   ImageURL from Database: " + imageUrl);
                    
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        if (imageUrl.contains("cloudinary.com")) {
                            cloudinaryCount++;
                            System.out.println("   ✅ Có Cloudinary URL - sẽ sync lên Firestore");
                        } else if (imageUrl.startsWith("/uploads/")) {
                            localUrlCount++;
                            localUrlFoodIds.add(food.getId());
                            System.out.println("   ⚠️ WARNING: Có local URL (" + imageUrl + ") - Cần upload lại lên Cloudinary!");
                            System.out.println("   💡 Vui lòng cập nhật hình ảnh cho FoodItem ID " + food.getId() + " (" + food.getName() + ") trong dashboard để migrate sang Cloudinary");
                        } else {
                            System.out.println("   ⚠️ FoodItem có URL không chuẩn: " + imageUrl);
                        }
                    } else {
                        System.out.println("   ⚠️ FoodItem không có imageUrl");
                    }
                    
                    // Sync lên Firestore (sync bất kể URL là gì để đảm bảo dữ liệu đồng bộ)
                    firestoreService.saveFood(food);
                    successCount++;
                    System.out.println("   ✅ Đã sync FoodItem ID: " + food.getId() + " lên Firestore với imageUrl: " + imageUrl);
                    System.out.println("   ---");
                    
                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Lỗi khi sync FoodItem ID: " + food.getId() + " (" + food.getName() + ") - " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            String message;
            if (localUrlCount > 0) {
                message = String.format(
                    "Đồng bộ hoàn tất! Thành công: %d, Thất bại: %d, Có Cloudinary URL: %d, ⚠️ CÓ LOCAL URL (cần migrate): %d",
                    successCount, failCount, cloudinaryCount, localUrlCount
                );
                redirectAttributes.addFlashAttribute("warning", 
                    message + " | Các FoodItem có local URL: " + localUrlFoodIds.toString() + " - Vui lòng cập nhật hình ảnh để migrate sang Cloudinary!");
            } else {
                message = String.format(
                    "Đồng bộ hoàn tất! Thành công: %d, Thất bại: %d, Có Cloudinary URL: %d",
                    successCount, failCount, cloudinaryCount
                );
                redirectAttributes.addFlashAttribute("success", message);
            }
            
            System.out.println("==========================================");
            System.out.println("✅ " + message);
            if (localUrlCount > 0) {
                System.out.println("⚠️ CÁC FOODITEM CÓ LOCAL URL (CẦN MIGRATE): " + localUrlFoodIds.toString());
            }
            System.out.println("==========================================");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đồng bộ: " + e.getMessage());
            System.err.println("❌ Lỗi khi đồng bộ FoodItems: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/categories";
    }
    
    /**
     * Migrate local image URLs to Cloudinary
     * This endpoint will find all FoodItems with local URLs and upload them to Cloudinary
     */
    @GetMapping("/admin/migrate/images")
    public String migrateLocalImagesToCloudinary(RedirectAttributes redirectAttributes) {
        try {
            if (cloudinaryService == null) {
                redirectAttributes.addFlashAttribute("error", "CloudinaryService không khả dụng!");
                return "redirect:/admin/categories";
            }
            
            List<FoodItem> allFoods = foodItemRepository.findAll();
            int migratedCount = 0;
            int skippedCount = 0;
            int errorCount = 0;
            List<Long> migratedIds = new ArrayList<>();
            List<Long> errorIds = new ArrayList<>();
            
            System.out.println("🔄 Bắt đầu migrate local images lên Cloudinary...");
            System.out.println("==========================================");
            
            for (FoodItem food : allFoods) {
                String imageUrl = food.getImageUrl();
                
                // Chỉ xử lý các FoodItem có local URL
                if (imageUrl != null && imageUrl.startsWith("/uploads/")) {
                    try {
                        // Tạo path đến file local
                        String fileName = imageUrl.substring("/uploads/".length());
                        Path filePath = Paths.get(UPLOAD_DIR + fileName);
                        
                        System.out.println("🔍 Checking FoodItem ID: " + food.getId() + " (" + food.getName() + ")");
                        System.out.println("   Local URL: " + imageUrl);
                        System.out.println("   File path: " + filePath.toAbsolutePath());
                        
                        // Kiểm tra file có tồn tại không
                        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                            System.out.println("   ✅ File tồn tại, đang upload lên Cloudinary...");
                            
                            // Upload lên Cloudinary
                            String cloudinaryUrl = cloudinaryService.uploadImageFromFile(filePath);
                            System.out.println("   ✅ Upload thành công! Cloudinary URL: " + cloudinaryUrl);
                            
                            // Cập nhật imageUrl trong database
                            food.setImageUrl(cloudinaryUrl);
                            
                            // Đảm bảo category được load đầy đủ
                            if (food.getCategory() != null && food.getCategory().getId() != null) {
                                Category category = categoryRepository.findById(food.getCategory().getId()).orElse(null);
                                if (category != null) {
                                    food.setCategory(category);
                                }
                            }
                            
                            // Lưu vào database
                            foodItemRepository.save(food);
                            System.out.println("   💾 Đã cập nhật database với Cloudinary URL");
                            
                            // Sync lên Firestore
                            if (firestoreService != null) {
                                firestoreService.saveFood(food);
                                System.out.println("   ✅ Đã sync lên Firestore");
                            }
                            
                            migratedCount++;
                            migratedIds.add(food.getId());
                            System.out.println("   ✅ HOÀN TẤT migrate FoodItem ID: " + food.getId());
                            
                        } else {
                            System.out.println("   ⚠️ File không tồn tại: " + filePath.toAbsolutePath());
                            skippedCount++;
                        }
                        
                        System.out.println("   ---");
                        
                    } catch (Exception e) {
                        errorCount++;
                        errorIds.add(food.getId());
                        System.err.println("❌ Lỗi khi migrate FoodItem ID: " + food.getId() + " - " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            
            String message = String.format(
                "Migrate hoàn tất! Đã migrate: %d, Bỏ qua (file không tồn tại): %d, Lỗi: %d",
                migratedCount, skippedCount, errorCount
            );
            
            if (errorCount > 0) {
                redirectAttributes.addFlashAttribute("warning", 
                    message + " | Các FoodItem lỗi: " + errorIds.toString());
            } else if (migratedCount > 0) {
                redirectAttributes.addFlashAttribute("success", 
                    message + " | Đã migrate: " + migratedIds.toString());
            } else {
                redirectAttributes.addFlashAttribute("info", message);
            }
            
            System.out.println("==========================================");
            System.out.println("✅ " + message);
            if (migratedCount > 0) {
                System.out.println("✅ Đã migrate: " + migratedIds.toString());
            }
            if (errorCount > 0) {
                System.out.println("❌ Lỗi: " + errorIds.toString());
            }
            System.out.println("==========================================");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi migrate: " + e.getMessage());
            System.err.println("❌ Lỗi khi migrate images: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/categories";
    }
}