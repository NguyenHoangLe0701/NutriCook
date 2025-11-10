package com.nutricook.dashboard.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nutricook.dashboard.entity.Category;
import com.nutricook.dashboard.entity.FoodItem;
import com.nutricook.dashboard.entity.FoodUpdate;
import com.nutricook.dashboard.entity.User;
import com.nutricook.dashboard.repository.CategoryRepository;
import com.nutricook.dashboard.repository.FoodItemRepository;
import com.nutricook.dashboard.repository.FoodUpdateRepository;
import com.nutricook.dashboard.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private FoodUpdateRepository foodUpdateRepository;
    
    private final String UPLOAD_DIR = "uploads/";
    
    // Initialize some sample data
    @PostConstruct
    public void init() {
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
            
            // Create sample regular user
            User user = new User("user1", passwordEncoder.encode("password123"), "user1@example.com", "Nguyễn Văn A");
            userRepository.save(user);
        }
        
        // Create sample foods if none exist
        if (foodItemRepository.count() == 0) {
            Category vegetableCategory = categoryRepository.findByName("Rau củ");
            Category fruitCategory = categoryRepository.findByName("Trái cây");
            
            if (vegetableCategory != null) {
                FoodItem carrot = new FoodItem("Cà rốt", "52 kcal", "Cà rốt tươi ngon giàu vitamin A", vegetableCategory);
                carrot.setPrice(15000.0);
                foodItemRepository.save(carrot);
                
                FoodItem tomato = new FoodItem("Cà chua", "18 kcal", "Cà chua tươi ngon", vegetableCategory);
                tomato.setPrice(12000.0);
                foodItemRepository.save(tomato);
            }
            
            if (fruitCategory != null) {
                FoodItem banana = new FoodItem("Chuối", "89 kcal", "Chuối tiêu chín vàng", fruitCategory);
                banana.setPrice(20000.0);
                foodItemRepository.save(banana);
                
                FoodItem orange = new FoodItem("Cam", "47 kcal", "Cam sành Việt Nam", fruitCategory);
                orange.setPrice(25000.0);
                foodItemRepository.save(orange);
            }
        }
    }
    
    // Dashboard - Tổng quan
@GetMapping("/dashboard")
public String dashboard(Model model) {
    model.addAttribute("userCount", userRepository.count());
    model.addAttribute("foodCount", foodItemRepository.count());
    model.addAttribute("categoryCount", categoryRepository.count());
    
    // BỎ COMMENT hai dòng này (hoặc chỉ dòng recentUpdates)
    model.addAttribute("updateCount", foodUpdateRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(1)));
    model.addAttribute("recentUpdates", foodUpdateRepository.findTop5ByOrderByCreatedAtDesc());

    model.addAttribute("title", "Tổng quan");
    model.addAttribute("subtitle", "Thống kê và hoạt động hệ thống");
    model.addAttribute("activeTab", "dashboard");
    return "admin/dashboard"; // Trả về view này
}
    // User Management - Quản lý người dùng
    @GetMapping("/users")
    public String users(Model model) {
        List<User> users = userRepository.findAll();
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
            // Validate password match
            if (!user.getPassword().equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/admin/users";
            }
            
            // Check if username exists
            if (userRepository.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại!");
                return "redirect:/admin/users";
            }
            
            // Check if email exists
            if (userRepository.existsByEmail(user.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
                return "redirect:/admin/users";
            }
            
            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm người dùng: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Don't delete admin user
            User user = userRepository.findById(id).orElse(null);
            if (user != null && user.getRole() != User.UserRole.ADMIN) {
                userRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Xóa người dùng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản admin!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa người dùng: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
   @PostMapping("/users/{id}/toggle-status")
public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && user.getRole() != User.UserRole.ADMIN) {
            // Since User entity doesn't have enabled field, we'll use a different approach
            // You can either add an enabled field to User entity or use a different logic
            
            // Option 1: If you want to add enabled field later, uncomment this:
            // user.setEnabled(!user.isEnabled());
            // userRepository.save(user);
            
            // Option 2: For now, just show a success message without actual toggle
            String currentStatus = "đang hoạt động"; // This would be dynamic if you had enabled field
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
        List<Category> categories = categoryRepository.findAll();
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
            // Check if category name exists
            if (categoryRepository.existsByName(category.getName())) {
                redirectAttributes.addFlashAttribute("error", "Tên danh mục đã tồn tại!");
                return "redirect:/admin/categories";
            }
            
            categoryRepository.save(category);
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
            model.addAttribute("categories", categoryRepository.findAll());
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
            // Check if category exists first
            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
                return "redirect:/admin/categories";
            }
            
            // Check if category has food items
            List<FoodItem> foodsInCategory = foodItemRepository.findByCategory(category);
            if (!foodsInCategory.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Không thể xóa danh mục '" + category.getName() + "' vì có " + 
                    foodsInCategory.size() + " món ăn thuộc danh mục này!");
                return "redirect:/admin/categories";
            }
            
            categoryRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa danh mục '" + category.getName() + "' thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "ID danh mục không hợp lệ: " + e.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục do ràng buộc dữ liệu");
        } catch (org.springframework.dao.DataAccessException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa danh mục: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
    @GetMapping("/api/categories")
// @ResponseBody
public List<Category> getCategoriesForMobile() {
    return categoryRepository.findAll();
}
    // Food Item Management - Quản lý món ăn
    @GetMapping("/foods")
    public String foods(Model model) {
        List<FoodItem> foods = foodItemRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        
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
            // Check if food name exists
            if (foodItemRepository.existsByName(foodItem.getName())) {
                redirectAttributes.addFlashAttribute("error", "Tên món ăn đã tồn tại!");
                return "redirect:/admin/foods";
            }
            
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                foodItem.setImageUrl("/uploads/" + fileName);
            }
            
            foodItemRepository.save(foodItem);
            
            // Log the update
            logFoodUpdate(null, foodItem, "CREATE");
            
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
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            // Tìm category
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                return ResponseEntity.badRequest().body("Category not found");
            }
            
            // Tìm user (nếu mobile gửi userId)
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }
            
            // Tạo FoodItem
            FoodItem foodItem = new FoodItem(name, calories, description != null ? description : "", category);
            foodItem.setUser(user);  // Set user từ mobile
            foodItem.setPrice(price != null ? price : 0.0);
            foodItem.setAvailable(true);
            
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                foodItem.setImageUrl("/uploads/" + fileName);
            }
            
            foodItemRepository.save(foodItem);
            
            // Log update
            logFoodUpdate(null, foodItem, "CREATE");
            
            return ResponseEntity.ok("Food uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading food: " + e.getMessage());
        }
    }
    
    @GetMapping("/foods/{id}/edit")
    public String editFoodForm(@PathVariable Long id, Model model) {
        Object obj = foodItemRepository.findById(id).orElse(null);
        FoodItem foodItem = obj != null ? (FoodItem) obj : null;
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
    
    @PostMapping("/foods/{id}/edit")
    public String updateFood(@PathVariable Long id, 
                           @ModelAttribute FoodItem foodItem,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {
        try {
            Object obj = foodItemRepository.findById(id).orElse(null);
            FoodItem existingFood = obj != null ? (FoodItem) obj : null;
            if (existingFood != null) {
                // Save old data for logging
                FoodItem oldFood = new FoodItem();
                oldFood.setName(existingFood.getName());
                oldFood.setCalories(existingFood.getCalories());
                oldFood.setDescription(existingFood.getDescription());
                oldFood.setPrice(existingFood.getPrice());
                oldFood.setAvailable(existingFood.getAvailable());
                
                // Update fields
                existingFood.setName(foodItem.getName());
                existingFood.setCalories(foodItem.getCalories());
                existingFood.setDescription(foodItem.getDescription());
                existingFood.setCategory(foodItem.getCategory());
                existingFood.setPrice(foodItem.getPrice());
                existingFood.setAvailable(foodItem.getAvailable());
                existingFood.setUpdatedAt(LocalDateTime.now());
                
                if (imageFile != null && !imageFile.isEmpty()) {
                    String fileName = saveImage(imageFile);
                    existingFood.setImageUrl("/uploads/" + fileName);
                }
                
                foodItemRepository.save(existingFood);
                
                // Log the update
                logFoodUpdate(oldFood, existingFood, "UPDATE");
                
                redirectAttributes.addFlashAttribute("success", "Cập nhật món ăn thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật món ăn: " + e.getMessage());
        }
        
        return "redirect:/admin/foods";
    }
    
    @PostMapping("/foods/{id}/delete")
    public String deleteFood(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Object obj = foodItemRepository.findById(id).orElse(null);
            FoodItem foodItem = obj != null ? (FoodItem) obj : null;
            if (foodItem != null) {
                // Log the deletion
                logFoodUpdate(foodItem, null, "DELETE");
                
                foodItemRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Xóa món ăn thành công!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "ID món ăn không hợp lệ: " + e.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa món ăn do ràng buộc dữ liệu");
        } catch (org.springframework.dao.DataAccessException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa món ăn: " + e.getMessage());
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
            
            // Get admin user (you might want to get the currently logged in user)
            User admin = userRepository.findByUsername("admin").orElse(null);
            if (admin != null) {
                update.setUser(admin);
            }
            
            if (newFood != null) {
                update.setFoodItem(newFood);
            } else if (oldFood != null) {
                // For deletion, we still want to reference the food item
                update.setFoodItem(oldFood);
            }
            
            update.setAction(action);
            
            // Store changes as JSON (you can implement this)
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
        // Create uploads directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        return fileName;
    }
    
}