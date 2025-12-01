package com.nutricook.dashboard.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; // Cần import Category
import org.springframework.stereotype.Service; // Cần import FoodItem

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.nutricook.dashboard.entity.AnalyticsData;
import com.nutricook.dashboard.entity.Category;
import com.nutricook.dashboard.entity.DailyLog;
import com.nutricook.dashboard.entity.FoodItem;
import com.nutricook.dashboard.entity.NutritionStats;
import com.nutricook.dashboard.entity.Post;
import com.nutricook.dashboard.entity.Review;
import com.nutricook.dashboard.entity.User;
import com.nutricook.dashboard.entity.UserRecipe;

@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirestoreService {

    private final Firestore firestore;

    public FirestoreService(Firestore firestore) {
        this.firestore = firestore;
    }

    // ==========================================================
    // USER METHODS (Giữ nguyên)
    // ==========================================================
    
    public List<Map<String, Object>> listUsers() throws Exception {
        // ... (Code của bạn giữ nguyên)
        if (firestore == null) {
            throw new IllegalStateException("Firestore is not initialized");
        }
        CollectionReference users = firestore.collection("users");
        QuerySnapshot snap = users.get().get();
        List<Map<String, Object>> out = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data != null) {
                data.put("_id", doc.getId());
                out.add(data);
            }
        }
        return out;
    }

    public List<User> listUsersAsEntities() throws Exception {
        // ... (Code của bạn giữ nguyên)
        CollectionReference users = firestore.collection("users");
        QuerySnapshot snap = users.get().get();
        List<User> out = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            User u = new User();
            Object idField = data.get("id");
            if (idField instanceof Number) {
                u.setId(((Number) idField).longValue());
            } else if (idField instanceof String) {
                try { u.setId(Long.parseLong((String) idField)); } catch (Exception ignored) {}
            }
            u.setUsername((String) data.get("username"));
            u.setPassword((String) data.get("password"));
            u.setEmail((String) data.get("email"));
            u.setFullName((String) data.get("fullName"));
            u.setAvatar((String) data.get("avatar"));
            Object roleObj = data.get("role");
            if (roleObj instanceof String) {
                try {
                    u.setRole(User.UserRole.valueOf((String) roleObj));
                } catch (Exception ignored) {}
            }
            Object created = data.get("createdAt");
            if (created instanceof Timestamp) {
                Timestamp ts = (Timestamp) created;
                u.setCreatedAt(ts.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (created instanceof Date) {
                Date d = (Date) created;
                u.setCreatedAt(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            Object updated = data.get("updatedAt");
            if (updated instanceof Timestamp) {
                Timestamp ts = (Timestamp) updated;
                u.setUpdatedAt(ts.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (updated instanceof Date) {
                Date d = (Date) updated;
                u.setUpdatedAt(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            out.add(u);
        }
        return out;
    }

    public String saveUser(User user) throws Exception {
        // ... (Code của bạn giữ nguyên)
        CollectionReference users = firestore.collection("users");
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", user.getUsername());
        payload.put("email", user.getEmail());
        payload.put("fullName", user.getFullName());
        payload.put("avatar", user.getAvatar());
        payload.put("role", user.getRole() != null ? user.getRole().name() : null);
        if (user.getCreatedAt() != null) {
            payload.put("createdAt", Date.from(user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            payload.put("createdAt", new Date());
        }
        payload.put("updatedAt", new Date());
        String docId = null;
        if (user.getId() != null) {
            docId = String.valueOf(user.getId());
            users.document(docId).set(payload).get();
        } else {
            com.google.cloud.firestore.DocumentReference ref = users.document();
            ref.set(payload).get();
            docId = ref.getId();
        }
        return docId;
    }

    public User getUserByDocId(String docId) throws Exception {
        // ... (Code của bạn giữ nguyên)
        CollectionReference users = firestore.collection("users");
        com.google.cloud.firestore.DocumentSnapshot doc = users.document(docId).get().get();
        if (doc == null || !doc.exists()) return null;
        Map<String, Object> data = doc.getData();
        if (data == null) return null;
        User u = new User();
        Object idField = data.get("id");
        if (idField instanceof Number) {
            u.setId(((Number) idField).longValue());
        } else if (idField instanceof String) {
            try { u.setId(Long.parseLong((String) idField)); } catch (Exception ignored) {}
        }
        u.setUsername((String) data.get("username"));
        u.setPassword((String) data.get("password"));
        u.setEmail((String) data.get("email"));
        u.setFullName((String) data.get("fullName"));
        u.setAvatar((String) data.get("avatar"));
        Object roleObj = data.get("role");
        if (roleObj instanceof String) {
            try { u.setRole(User.UserRole.valueOf((String) roleObj)); } catch (Exception ignored) {}
        }
        Object created = data.get("createdAt");
        if (created instanceof Timestamp) {
            Timestamp ts = (Timestamp) created;
            u.setCreatedAt(ts.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } else if (created instanceof Date) {
            Date d = (Date) created;
            u.setCreatedAt(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        Object updated = data.get("updatedAt");
        if (updated instanceof Timestamp) {
            Timestamp ts = (Timestamp) updated;
            u.setUpdatedAt(ts.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } else if (updated instanceof Date) {
            Date d = (Date) updated;
            u.setUpdatedAt(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        return u;
    }

    public boolean deleteUserByDocId(String docId) throws Exception {
        // ... (Code của bạn giữ nguyên)
        CollectionReference users = firestore.collection("users");
        users.document(docId).delete().get();
        return true;
    }

    public boolean deleteUserCascade(String docId) throws Exception {
        // ... (Code của bạn giữ nguyên)
        CollectionReference users = firestore.collection("users");
        users.document(docId).delete().get();
        CollectionReference foodItems = firestore.collection("foodItems");
        QuerySnapshot fiSnap = foodItems.whereEqualTo("userId", docId).get().get();
        for (DocumentSnapshot d : fiSnap.getDocuments()) {
            d.getReference().delete().get();
        }
        CollectionReference updates = firestore.collection("foodUpdates");
        QuerySnapshot upSnap = updates.whereEqualTo("userId", docId).get().get();
        for (DocumentSnapshot d : upSnap.getDocuments()) {
            d.getReference().delete().get();
        }
        return true;
    }

    public String saveUserWithDocId(String docId, User user) throws Exception {
        // ... (Code của bạn giữ nguyên)
        CollectionReference users = firestore.collection("users");
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", user.getUsername());
        payload.put("email", user.getEmail());
        payload.put("fullName", user.getFullName());
        payload.put("avatar", user.getAvatar());
        payload.put("role", user.getRole() != null ? user.getRole().name() : null);
        if (user.getCreatedAt() != null) {
            payload.put("createdAt", Date.from(user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            payload.put("createdAt", new Date());
        }
        payload.put("updatedAt", new Date());
        users.document(docId).set(payload).get();
        return docId;
    }

    // ==========================================================
    // FOODITEM METHODS (Đã xóa 'price')
    // ==========================================================

    /**
     * Lấy danh sách FoodItem từ collection 'foodItems'.
     */
    public List<FoodItem> listFoodsAsEntities() throws Exception {
        CollectionReference cols = firestore.collection("foodItems");
        QuerySnapshot snap = cols.get().get();
        List<FoodItem> out = new ArrayList<>();
        
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            
            FoodItem f = new FoodItem();
            
            Object idField = data.get("id");
            if (idField instanceof Number) {
                f.setId(((Number) idField).longValue());
            } else if (idField instanceof String) {
                try { f.setId(Long.parseLong((String) idField)); } catch (Exception ignored) {}
            }

            f.setName((String) data.get("name"));
            f.setCalories((String) data.get("calories")); 
            f.setDescription((String) data.get("description"));
            f.setImageUrl((String) data.get("imageUrl"));

            Object availableObj = data.get("available");
            if (availableObj instanceof Boolean) {
                f.setAvailable((Boolean) availableObj);
            } else {
                f.setAvailable(true); 
            }
            
            // Đọc rating và reviews
            Object ratingObj = data.get("rating");
            if (ratingObj instanceof Number) {
                f.setRating(((Number) ratingObj).doubleValue());
            } else {
                f.setRating(0.0);
            }
            
            Object reviewsObj = data.get("reviews");
            if (reviewsObj instanceof Number) {
                f.setReviews(((Number) reviewsObj).intValue());
            } else {
                f.setReviews(0);
            }

            Object categoryIdObj = data.get("categoryId");
            if (categoryIdObj != null) {
                Category cat = new Category();
                try {
                    long categoryId = Long.parseLong(String.valueOf(categoryIdObj));
                    cat.setId(categoryId);
                    
                    if (data.get("categoryName") != null) {
                        cat.setName((String) data.get("categoryName"));
                    }
                } catch (Exception ignored) {}
                f.setCategory(cat);
            }
            
            // Đọc thông tin dinh dưỡng
            Object fatObj = data.get("fat");
            if (fatObj instanceof Number) {
                f.setFat(((Number) fatObj).doubleValue());
            } else {
                f.setFat(0.0);
            }
            
            Object carbsObj = data.get("carbs");
            if (carbsObj instanceof Number) {
                f.setCarbs(((Number) carbsObj).doubleValue());
            } else {
                f.setCarbs(0.0);
            }
            
            Object proteinObj = data.get("protein");
            if (proteinObj instanceof Number) {
                f.setProtein(((Number) proteinObj).doubleValue());
            } else {
                f.setProtein(0.0);
            }
            
            Object cholesterolObj = data.get("cholesterol");
            if (cholesterolObj instanceof Number) {
                f.setCholesterol(((Number) cholesterolObj).doubleValue());
            } else {
                f.setCholesterol(0.0);
            }
            
            Object sodiumObj = data.get("sodium");
            if (sodiumObj instanceof Number) {
                f.setSodium(((Number) sodiumObj).doubleValue());
            } else {
                f.setSodium(0.0);
            }
            
            Object vitaminObj = data.get("vitamin");
            if (vitaminObj instanceof Number) {
                f.setVitamin(((Number) vitaminObj).doubleValue());
            } else {
                f.setVitamin(0.0);
            }
            
            // Đọc đơn vị đo lường
            Object unitObj = data.get("unit");
            if (unitObj != null) {
                f.setUnit(String.valueOf(unitObj));
            } else {
                f.setUnit("g"); // Mặc định là gram
            }
            
            out.add(f);
        }
        return out;
    }

    /**
     * Lưu hoặc cập nhật một FoodItem vào Firestore.
     */
    public String saveFood(FoodItem food) throws Exception {
        if (food.getId() == null) {
            throw new IllegalArgumentException("FoodItem ID must not be null to save to Firestore");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", food.getId()); 
        data.put("name", food.getName());
        data.put("calories", food.getCalories());
        data.put("description", food.getDescription());
        data.put("available", food.getAvailable());
        String imageUrl = food.getImageUrl();
        // Đảm bảo imageUrl không null khi lưu vào Firestore
        // Nếu là URL Cloudinary, đảm bảo nó là full URL
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Validate Cloudinary URL format
            if (imageUrl.contains("cloudinary.com")) {
                if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                    System.err.println("⚠️ Warning: Cloudinary URL is not a full URL: " + imageUrl);
                } else {
                    System.out.println("✅ Saving Cloudinary URL to Firestore: " + imageUrl);
                }
            }
            data.put("imageUrl", imageUrl);
        } else {
            data.put("imageUrl", "");
            System.out.println("⚠️ Warning: FoodItem ID " + food.getId() + " has empty imageUrl");
        }
        System.out.println("🖼️ Saving FoodItem to Firestore - ID: " + food.getId() + ", Name: " + food.getName() + ", ImageURL: " + imageUrl);
        data.put("rating", food.getRating() != null ? food.getRating() : 0.0);
        data.put("reviews", food.getReviews() != null ? food.getReviews() : 0);
        // Đơn vị đo lường
        data.put("unit", food.getUnit() != null && !food.getUnit().isEmpty() ? food.getUnit() : "g");
        // Thông tin dinh dưỡng
        data.put("fat", food.getFat() != null ? food.getFat() : 0.0);
        data.put("carbs", food.getCarbs() != null ? food.getCarbs() : 0.0);
        data.put("protein", food.getProtein() != null ? food.getProtein() : 0.0);
        data.put("cholesterol", food.getCholesterol() != null ? food.getCholesterol() : 0.0);
        data.put("sodium", food.getSodium() != null ? food.getSodium() : 0.0);
        data.put("vitamin", food.getVitamin() != null ? food.getVitamin() : 0.0);
        
        // Lưu chi tiết các loại vitamin
        data.put("vitaminA", food.getVitaminA() != null ? food.getVitaminA() : 0.0);
        data.put("vitaminB1", food.getVitaminB1() != null ? food.getVitaminB1() : 0.0);
        data.put("vitaminB2", food.getVitaminB2() != null ? food.getVitaminB2() : 0.0);
        data.put("vitaminB3", food.getVitaminB3() != null ? food.getVitaminB3() : 0.0);
        data.put("vitaminB6", food.getVitaminB6() != null ? food.getVitaminB6() : 0.0);
        data.put("vitaminB9", food.getVitaminB9() != null ? food.getVitaminB9() : 0.0);
        data.put("vitaminB12", food.getVitaminB12() != null ? food.getVitaminB12() : 0.0);
        data.put("vitaminC", food.getVitaminC() != null ? food.getVitaminC() : 0.0);
        data.put("vitaminD", food.getVitaminD() != null ? food.getVitaminD() : 0.0);
        data.put("vitaminE", food.getVitaminE() != null ? food.getVitaminE() : 0.0);
        data.put("vitaminK", food.getVitaminK() != null ? food.getVitaminK() : 0.0);
        
        if (food.getCategory() != null) {
            data.put("categoryId", food.getCategory().getId());
            data.put("categoryName", food.getCategory().getName()); 
        }
        
        // Lưu thông tin người upload nếu có
        if (food.getUser() != null) {
            data.put("userId", food.getUser().getId());
            data.put("userName", food.getUser().getFullName());
            data.put("userEmail", food.getUser().getEmail());
        }
        
        // Lưu ngày upload
        if (food.getCreatedAt() != null) {
            data.put("createdAt", Date.from(food.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        String docId = String.valueOf(food.getId());
        ApiFuture<WriteResult> future = firestore.collection("foodItems")
                                                .document(docId)
                                                .set(data);
        
        future.get(); // Đợi hoàn thành
        return docId;
    }

    /**
     * Xóa một FoodItem khỏi Firestore bằng ID (từ H2).
     */
    public boolean deleteFood(Long id) throws Exception {
        if (id == null) return false;
        
        String docId = String.valueOf(id);
        ApiFuture<WriteResult> future = firestore.collection("foodItems")
                                                .document(docId)
                                                .delete();
        
        future.get(); // Đợi hoàn thành
        return true;
    }
    
    // ==========================================================
    // CATEGORY METHODS (Code mới thêm)
    // ==========================================================

    /**
     * Lấy danh sách Category từ collection 'categories'.
     */
    public List<Category> listCategoriesAsEntities() throws Exception {
        CollectionReference cols = firestore.collection("categories");
        QuerySnapshot snap = cols.orderBy("id").get().get(); // Sắp xếp theo ID
        List<Category> out = new ArrayList<>();
        
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            
            Category c = new Category();
            // Lấy ID từ trường 'id' (dạng Number)
            if(data.get("id") instanceof Number) {
                c.setId(((Number) data.get("id")).longValue());
            }
            c.setName(doc.getString("name"));
            c.setDescription(doc.getString("description"));
            c.setIcon(doc.getString("icon"));
            c.setColor(doc.getString("color"));

            // Gán createdAt/updatedAt nếu có, sử dụng logic của User
            Object created = data.get("createdAt");
            if (created instanceof Timestamp) {
                c.setCreatedAt(((Timestamp) created).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (created instanceof Date) {
                c.setCreatedAt(((Date) created).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            Object updated = data.get("updatedAt");
            if (updated instanceof Timestamp) {
                c.setUpdatedAt(((Timestamp) updated).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (updated instanceof Date) {
                c.setUpdatedAt(((Date) updated).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }

            out.add(c);
        }
        return out;
    }

    /**
     * Lưu hoặc cập nhật một Category vào Firestore.
     * Sử dụng ID của H2 (c.getId()) làm Document ID trên Firestore.
     */
    public String saveCategory(Category category) throws Exception {
        if (category.getId() == null) {
            throw new IllegalArgumentException("Category ID must not be null to save to Firestore");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("id", category.getId()); // Lưu ID H2
        data.put("name", category.getName());
        data.put("description", category.getDescription());
        data.put("icon", category.getIcon());
        data.put("color", category.getColor());
        
        if (category.getCreatedAt() != null) {
            data.put("createdAt", Date.from(category.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        } else {
             data.put("createdAt", new Date()); // Đặt ngày tạo nếu là mới
        }
        data.put("updatedAt", new Date()); // Luôn cập nhật ngày

        // Dùng ID (dạng Số) của H2 làm Document ID (dạng String)
        String docId = String.valueOf(category.getId());
        ApiFuture<WriteResult> future = firestore.collection("categories")
                                                .document(docId)
                                                .set(data);
        
        future.get(); // Đợi hoàn thành
        return docId;
    }

    /**
     * Xóa một Category khỏi Firestore bằng ID (từ H2).
     */
    public boolean deleteCategory(Long id) throws Exception {
        if (id == null) return false;
        
        String docId = String.valueOf(id);
        ApiFuture<WriteResult> future = firestore.collection("categories")
                                                .document(docId)
                                                .delete();
        
        future.get(); // Đợi hoàn thành
        return true;
    }
    
    // ==========================================================
    // NUTRITION METHODS (Quản lý Calories người dùng)
    // ==========================================================
    
    /**
     * Lấy danh sách DailyLog của một user từ Firestore.
     * Path: users/{userId}/daily_logs
     */
    public List<DailyLog> getUserDailyLogs(String userId, int limit) throws Exception {
        if (userId == null || userId.isEmpty()) {
            return new ArrayList<>();
        }
        
        CollectionReference logsCol = firestore.collection("users")
                                               .document(userId)
                                               .collection("daily_logs");
        
        // Lấy tất cả rồi sort trong code để tránh lỗi index
        QuerySnapshot snap = logsCol.get().get();
        
        List<DailyLog> logs = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            
            DailyLog log = new DailyLog();
            // Document ID chính là dateId, hoặc lấy từ field dateId
            String dateId = (String) data.getOrDefault("dateId", doc.getId());
            log.setDateId(dateId);
            
            // Parse calories, protein, fat, carb
            Object calObj = data.get("calories");
            if (calObj instanceof Number) {
                log.setCalories(((Number) calObj).floatValue());
            } else if (calObj instanceof String) {
                try {
                    log.setCalories(Float.parseFloat((String) calObj));
                } catch (Exception ignored) {}
            }
            
            Object proObj = data.get("protein");
            if (proObj instanceof Number) {
                log.setProtein(((Number) proObj).floatValue());
            } else if (proObj instanceof String) {
                try {
                    log.setProtein(Float.parseFloat((String) proObj));
                } catch (Exception ignored) {}
            }
            
            Object fatObj = data.get("fat");
            if (fatObj instanceof Number) {
                log.setFat(((Number) fatObj).floatValue());
            } else if (fatObj instanceof String) {
                try {
                    log.setFat(Float.parseFloat((String) fatObj));
                } catch (Exception ignored) {}
            }
            
            Object carbObj = data.get("carb");
            if (carbObj instanceof Number) {
                log.setCarb(((Number) carbObj).floatValue());
            } else if (carbObj instanceof String) {
                try {
                    log.setCarb(Float.parseFloat((String) carbObj));
                } catch (Exception ignored) {}
            }
            
            Object updatedObj = data.get("updatedAt");
            if (updatedObj instanceof Timestamp) {
                log.setUpdatedAt(((Timestamp) updatedObj).toDate().getTime());
            } else if (updatedObj instanceof Number) {
                log.setUpdatedAt(((Number) updatedObj).longValue());
            }
            
            logs.add(log);
        }
        
        // Sort theo dateId (document ID) giảm dần (mới nhất trước)
        logs.sort((a, b) -> {
            if (a.getDateId() == null || b.getDateId() == null) return 0;
            return b.getDateId().compareTo(a.getDateId()); // DESC
        });
        
        // Lấy limit phần tử đầu tiên (mới nhất)
        if (logs.size() > limit) {
            logs = logs.subList(0, limit);
        }
        
        // Đảo ngược để từ cũ đến mới (cho biểu đồ)
        java.util.Collections.reverse(logs);
        return logs;
    }
    
    /**
     * Lấy tất cả DailyLogs của một user (không giới hạn)
     */
    public List<DailyLog> getAllUserDailyLogs(String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            return new ArrayList<>();
        }
        
        CollectionReference logsCol = firestore.collection("users")
                                               .document(userId)
                                               .collection("daily_logs");
        
        // Lấy tất cả rồi sort trong code để tránh lỗi index
        QuerySnapshot snap = logsCol.get().get();
        
        List<DailyLog> logs = new ArrayList<>();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            
            DailyLog log = new DailyLog();
            String dateId = (String) data.getOrDefault("dateId", doc.getId());
            log.setDateId(dateId);
            
            // Parse calories, protein, fat, carb
            Object calObj = data.get("calories");
            if (calObj instanceof Number) {
                log.setCalories(((Number) calObj).floatValue());
            } else if (calObj instanceof String) {
                try {
                    log.setCalories(Float.parseFloat((String) calObj));
                } catch (Exception ignored) {}
            }
            
            Object proObj = data.get("protein");
            if (proObj instanceof Number) {
                log.setProtein(((Number) proObj).floatValue());
            } else if (proObj instanceof String) {
                try {
                    log.setProtein(Float.parseFloat((String) proObj));
                } catch (Exception ignored) {}
            }
            
            Object fatObj = data.get("fat");
            if (fatObj instanceof Number) {
                log.setFat(((Number) fatObj).floatValue());
            } else if (fatObj instanceof String) {
                try {
                    log.setFat(Float.parseFloat((String) fatObj));
                } catch (Exception ignored) {}
            }
            
            Object carbObj = data.get("carb");
            if (carbObj instanceof Number) {
                log.setCarb(((Number) carbObj).floatValue());
            } else if (carbObj instanceof String) {
                try {
                    log.setCarb(Float.parseFloat((String) carbObj));
                } catch (Exception ignored) {}
            }
            
            Object updatedObj = data.get("updatedAt");
            if (updatedObj instanceof Timestamp) {
                log.setUpdatedAt(((Timestamp) updatedObj).toDate().getTime());
            } else if (updatedObj instanceof Number) {
                log.setUpdatedAt(((Number) updatedObj).longValue());
            }
            
            logs.add(log);
        }
        
        // Sort theo dateId (document ID) tăng dần (cũ nhất trước)
        logs.sort((a, b) -> {
            if (a.getDateId() == null || b.getDateId() == null) return 0;
            return a.getDateId().compareTo(b.getDateId()); // ASC
        });
        
        return logs;
    }
    
    /**
     * Lấy calories target từ profile của user
     * Path: users/{userId} -> field "nutrition.caloriesTarget"
     */
    public Float getUserCaloriesTarget(String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            return 2000f; // Default
        }
        
        DocumentSnapshot userDoc = firestore.collection("users")
                                           .document(userId)
                                           .get()
                                           .get();
        
        if (!userDoc.exists()) {
            return 2000f;
        }
        
        Map<String, Object> data = userDoc.getData();
        if (data == null) {
            return 2000f;
        }
        
        // Tìm nutrition.caloriesTarget
        Object nutritionObj = data.get("nutrition");
        if (nutritionObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nutrition = (Map<String, Object>) nutritionObj;
            Object targetObj = nutrition.get("caloriesTarget");
            if (targetObj instanceof Number) {
                return ((Number) targetObj).floatValue();
            }
        }
        
        return 2000f; // Default
    }
    
    /**
     * Tính toán thống kê nutrition cho một user
     * userId là Firestore document ID
     */
    public NutritionStats calculateNutritionStats(String userId) throws Exception {
        NutritionStats stats = new NutritionStats();
        stats.setUserId(userId);
        
        // Lấy thông tin user từ Firestore document
        try {
            DocumentSnapshot userDoc = firestore.collection("users")
                                               .document(userId)
                                               .get()
                                               .get();
            
            if (userDoc.exists()) {
                Map<String, Object> userData = userDoc.getData();
                if (userData != null) {
                    String fullName = (String) userData.get("fullName");
                    String username = (String) userData.get("username");
                    String email = (String) userData.get("email");
                    
                    stats.setUserName(fullName != null && !fullName.isEmpty() ? fullName : username);
                    stats.setUserEmail(email != null ? email : "");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user info for " + userId + ": " + e.getMessage());
        }
        
        // Lấy calories target
        Float target = getUserCaloriesTarget(userId);
        stats.setCaloriesTarget(target);
        
        // Lấy tất cả logs
        List<DailyLog> allLogs = getAllUserDailyLogs(userId);
        
        if (allLogs.isEmpty()) {
            return stats;
        }
        
        // Lấy 7 ngày gần nhất
        int limit = Math.min(7, allLogs.size());
        List<DailyLog> weeklyLogs = allLogs.subList(Math.max(0, allLogs.size() - limit), allLogs.size());
        stats.setWeeklyLogs(weeklyLogs);
        
        // Tính toán trung bình
        float totalCal = 0f, totalPro = 0f, totalFat = 0f, totalCarb = 0f;
        int daysReached = 0;
        int count = 0;
        
        for (DailyLog log : allLogs) {
            if (log.getCalories() > 0) {
                totalCal += log.getCalories();
                totalPro += log.getProtein();
                totalFat += log.getFat();
                totalCarb += log.getCarb();
                count++;
                
                // Kiểm tra đạt mục tiêu (cho phép sai số 5%)
                if (log.getCalories() >= target * 0.95f) {
                    daysReached++;
                }
            }
        }
        
        if (count > 0) {
            stats.setAverageCalories(totalCal / count);
            stats.setAverageProtein(totalPro / count);
            stats.setAverageFat(totalFat / count);
            stats.setAverageCarb(totalCarb / count);
            stats.setDaysTracked(count);
            stats.setDaysReachedGoal(daysReached);
            stats.setGoalAchievementRate((daysReached * 100f) / count);
        }
        
        return stats;
    }
    
    /**
     * Lấy danh sách NutritionStats cho tất cả users có track calories
     */
    public List<NutritionStats> getAllUsersNutritionStats() {
        List<NutritionStats> statsList = new ArrayList<>();
        
        try {
            // Lấy tất cả users từ Firestore collection "users"
            CollectionReference usersCol = firestore.collection("users");
            QuerySnapshot usersSnap = usersCol.get().get();
            
            for (DocumentSnapshot userDoc : usersSnap.getDocuments()) {
                String userId = userDoc.getId(); // Document ID trong Firestore
                
                try {
                    // Kiểm tra xem user có daily_logs không
                    CollectionReference logsCol = firestore.collection("users")
                                                           .document(userId)
                                                           .collection("daily_logs");
                    QuerySnapshot snap = logsCol.limit(1).get().get();
                    
                    // Chỉ thêm user có ít nhất 1 log
                    if (!snap.isEmpty()) {
                        NutritionStats stats = calculateNutritionStats(userId);
                        if (stats != null) {
                            // Lấy thông tin user từ document
                            Map<String, Object> userData = userDoc.getData();
                            if (userData != null) {
                                if (stats.getUserName() == null || stats.getUserName().isEmpty()) {
                                    String fullName = (String) userData.get("fullName");
                                    String username = (String) userData.get("username");
                                    stats.setUserName(fullName != null && !fullName.isEmpty() ? fullName : username);
                                }
                                if (stats.getUserEmail() == null || stats.getUserEmail().isEmpty()) {
                                    String email = (String) userData.get("email");
                                    stats.setUserEmail(email != null ? email : "");
                                }
                            }
                            statsList.add(stats);
                        }
                    }
                } catch (Exception e) {
                    // Skip user nếu có lỗi
                    System.err.println("Error loading nutrition for user " + userId + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in getAllUsersNutritionStats: " + e.getMessage());
            e.printStackTrace();
            // Không throw, trả về danh sách rỗng
        }
        
        return statsList;
    }
    
    // ==========================================================
    // POST METHODS (Quản lý Posts)
    // ==========================================================
    
    /**
     * Lấy danh sách tất cả Posts từ Firestore
     */
    public List<Post> getAllPosts() {
        List<Post> posts = new ArrayList<>();
        try {
            CollectionReference postsCol = firestore.collection("posts");
            QuerySnapshot snap = postsCol.get().get();
            
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data == null) continue;
                
                Post post = new Post();
                post.setId(doc.getId());
                post.setContent((String) data.get("content"));
                
                // Parse images
                Object imagesObj = data.get("images");
                if (imagesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> images = (List<String>) imagesObj;
                    post.setImages(images);
                }
                
                // Parse author
                Object authorObj = data.get("author");
                if (authorObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> author = (Map<String, Object>) authorObj;
                    post.setAuthorId((String) author.get("id"));
                    post.setAuthorEmail((String) author.get("email"));
                    post.setAuthorName((String) author.get("displayName"));
                }
                
                // Parse timestamps
                Object createdAtObj = data.get("createdAt");
                if (createdAtObj instanceof Timestamp) {
                    post.setCreatedAt(((Timestamp) createdAtObj).toDate().getTime());
                } else if (createdAtObj instanceof Number) {
                    post.setCreatedAt(((Number) createdAtObj).longValue());
                }
                
                Object likeCountObj = data.get("likeCount");
                if (likeCountObj instanceof Number) {
                    post.setLikeCount(((Number) likeCountObj).intValue());
                }
                
                Object commentCountObj = data.get("commentCount");
                if (commentCountObj instanceof Number) {
                    post.setCommentCount(((Number) commentCountObj).intValue());
                }
                
                posts.add(post);
            }
            
            // Sort theo createdAt giảm dần
            posts.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        } catch (Exception e) {
            System.err.println("Error loading posts: " + e.getMessage());
            e.printStackTrace();
        }
        return posts;
    }
    
    /**
     * Xóa một Post (soft delete)
     */
    public boolean deletePost(String postId) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("isDeleted", true);
            firestore.collection("posts").document(postId).update(updates).get();
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting post: " + e.getMessage());
            return false;
        }
    }
    
    // ==========================================================
    // REVIEW METHODS (Quản lý Reviews)
    // ==========================================================
    
    /**
     * Lấy danh sách tất cả Reviews từ Firestore
     */
    public List<Review> getAllReviews() {
        List<Review> reviews = new ArrayList<>();
        try {
            CollectionReference reviewsCol = firestore.collection("reviews");
            QuerySnapshot snap = reviewsCol.get().get();
            
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data == null) continue;
                
                Review review = new Review();
                review.setId(doc.getId());
                review.setFoodItemId((String) data.get("foodItemId"));
                review.setFoodItemName((String) data.get("foodItemName"));
                review.setComment((String) data.get("comment"));
                
                Object ratingObj = data.get("rating");
                if (ratingObj instanceof Number) {
                    review.setRating(((Number) ratingObj).intValue());
                }
                
                // Parse user info
                Object userIdObj = data.get("userId");
                if (userIdObj != null) {
                    review.setUserId(userIdObj.toString());
                }
                
                Object userNameObj = data.get("userName");
                if (userNameObj != null) {
                    review.setUserName(userNameObj.toString());
                }
                
                Object userEmailObj = data.get("userEmail");
                if (userEmailObj != null) {
                    review.setUserEmail(userEmailObj.toString());
                }
                
                // Parse timestamp
                Object createdAtObj = data.get("createdAt");
                if (createdAtObj instanceof Timestamp) {
                    review.setCreatedAt(((Timestamp) createdAtObj).toDate().getTime());
                } else if (createdAtObj instanceof Number) {
                    review.setCreatedAt(((Number) createdAtObj).longValue());
                }
                
                reviews.add(review);
            }
            
            // Sort theo createdAt giảm dần
            reviews.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        } catch (Exception e) {
            System.err.println("Error loading reviews: " + e.getMessage());
            e.printStackTrace();
        }
        return reviews;
    }
    
    /**
     * Xóa một Review (soft delete)
     */
    public boolean deleteReview(String reviewId) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("isDeleted", true);
            firestore.collection("reviews").document(reviewId).update(updates).get();
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting review: " + e.getMessage());
            return false;
        }
    }
    
    // ==========================================================
    // ANALYTICS METHODS (Thống kê)
    // ==========================================================
    
    /**
     * Lấy thống kê 7 ngày qua
     */
    public List<AnalyticsData.DailyStats> getDailyStats(int days) {
        List<AnalyticsData.DailyStats> stats = new ArrayList<>();
        
        try {
            long now = System.currentTimeMillis();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM");
            
            // Khởi tạo stats cho mỗi ngày
            for (int i = days - 1; i >= 0; i--) {
                long dayStart = now - (i * 24L * 60 * 60 * 1000);
                
                AnalyticsData.DailyStats dailyStat = new AnalyticsData.DailyStats();
                dailyStat.setDate(dateFormat.format(new Date(dayStart)));
                dailyStat.setNewUsers(0L);
                dailyStat.setNewPosts(0L);
                dailyStat.setNewReviews(0L);
                stats.add(dailyStat);
            }
            
            // Đếm users mới
            CollectionReference usersCol = firestore.collection("users");
            QuerySnapshot usersSnap = usersCol.get().get();
            for (DocumentSnapshot doc : usersSnap.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data == null) continue;
                
                Object createdAtObj = data.get("createdAt");
                long createdAt = 0;
                if (createdAtObj instanceof Timestamp) {
                    createdAt = ((Timestamp) createdAtObj).toDate().getTime();
                } else if (createdAtObj instanceof Number) {
                    createdAt = ((Number) createdAtObj).longValue();
                }
                
                if (createdAt >= now - (days * 24L * 60 * 60 * 1000) && createdAt < now) {
                    int dayIndex = (int) ((now - createdAt) / (24L * 60 * 60 * 1000));
                    if (dayIndex >= 0 && dayIndex < stats.size()) {
                        stats.get(dayIndex).setNewUsers(stats.get(dayIndex).getNewUsers() + 1);
                    }
                }
            }
            
            // Đếm posts mới
            CollectionReference postsCol = firestore.collection("posts");
            QuerySnapshot postsSnap = postsCol.get().get();
            for (DocumentSnapshot doc : postsSnap.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data == null) continue;
                
                Object createdAtObj = data.get("createdAt");
                long createdAt = 0;
                if (createdAtObj instanceof Timestamp) {
                    createdAt = ((Timestamp) createdAtObj).toDate().getTime();
                } else if (createdAtObj instanceof Number) {
                    createdAt = ((Number) createdAtObj).longValue();
                }
                
                if (createdAt >= now - (days * 24L * 60 * 60 * 1000) && createdAt < now) {
                    int dayIndex = (int) ((now - createdAt) / (24L * 60 * 60 * 1000));
                    if (dayIndex >= 0 && dayIndex < stats.size()) {
                        stats.get(dayIndex).setNewPosts(stats.get(dayIndex).getNewPosts() + 1);
                    }
                }
            }
            
            // Đếm reviews mới
            CollectionReference reviewsCol = firestore.collection("reviews");
            QuerySnapshot reviewsSnap = reviewsCol.get().get();
            for (DocumentSnapshot doc : reviewsSnap.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data == null) continue;
                
                Object createdAtObj = data.get("createdAt");
                long createdAt = 0;
                if (createdAtObj instanceof Timestamp) {
                    createdAt = ((Timestamp) createdAtObj).toDate().getTime();
                } else if (createdAtObj instanceof Number) {
                    createdAt = ((Number) createdAtObj).longValue();
                }
                
                if (createdAt >= now - (days * 24L * 60 * 60 * 1000) && createdAt < now) {
                    int dayIndex = (int) ((now - createdAt) / (24L * 60 * 60 * 1000));
                    if (dayIndex >= 0 && dayIndex < stats.size()) {
                        stats.get(dayIndex).setNewReviews(stats.get(dayIndex).getNewReviews() + 1);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error getting daily stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Tính tổng calories đã track từ tất cả daily logs
     */
    public Long getTotalCaloriesTracked() {
        try {
            CollectionReference dailyLogsCol = firestore.collection("dailyLogs");
            QuerySnapshot snap = dailyLogsCol.get().get();
            
            long totalCalories = 0;
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data == null) continue;
                
                Object caloriesObj = data.get("totalCalories");
                if (caloriesObj instanceof Number) {
                    totalCalories += ((Number) caloriesObj).longValue();
                }
            }
            
            return totalCalories;
        } catch (Exception e) {
            System.err.println("Error calculating total calories: " + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Đếm số người dùng hoạt động (có hoạt động trong 30 ngày)
     */
    public Long getActiveUsersCount() {
        try {
            long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            CollectionReference usersCol = firestore.collection("users");
            QuerySnapshot snap = usersCol.get().get();
            
            long activeCount = 0;
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data == null) continue;
                
                // Kiểm tra lastActiveAt hoặc createdAt
                Object lastActiveObj = data.get("lastActiveAt");
                long lastActive = 0;
                
                if (lastActiveObj instanceof Timestamp) {
                    lastActive = ((Timestamp) lastActiveObj).toDate().getTime();
                } else if (lastActiveObj instanceof Number) {
                    lastActive = ((Number) lastActiveObj).longValue();
                } else {
                    // Fallback to createdAt
                    Object createdAtObj = data.get("createdAt");
                    if (createdAtObj instanceof Timestamp) {
                        lastActive = ((Timestamp) createdAtObj).toDate().getTime();
                    } else if (createdAtObj instanceof Number) {
                        lastActive = ((Number) createdAtObj).longValue();
                    }
                }
                
                if (lastActive >= thirtyDaysAgo) {
                    activeCount++;
                }
            }
            
            return activeCount;
        } catch (Exception e) {
            System.err.println("Error counting active users: " + e.getMessage());
            return 0L;
        }
    }
    
    // ==========================================================
    // USER RECIPE METHODS (Quản lý recipes từ mobile app)
    // ==========================================================
    
    /**
     * Lấy danh sách UserRecipe từ collection "userRecipes" trong Firestore
     */
    public List<UserRecipe> listUserRecipes() throws Exception {
        System.out.println("🔍 FirestoreService.listUserRecipes() - Starting...");
        CollectionReference userRecipesCol = firestore.collection("userRecipes");
        System.out.println("🔍 Querying collection: userRecipes");
        
        QuerySnapshot snap;
        try {
            snap = userRecipesCol.orderBy("createdAt", com.google.cloud.firestore.Query.Direction.DESCENDING).get().get();
            System.out.println("✅ Found " + snap.size() + " documents in userRecipes collection");
        } catch (Exception e) {
            System.err.println("⚠️ Error ordering by createdAt, trying without orderBy: " + e.getMessage());
            // Thử không orderBy nếu có lỗi index
            snap = userRecipesCol.get().get();
            System.out.println("✅ Found " + snap.size() + " documents (without orderBy)");
        }
        
        List<UserRecipe> out = new ArrayList<>();
        
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Map<String, Object> data = doc.getData();
            if (data == null) continue;
            
            UserRecipe recipe = new UserRecipe();
            recipe.setDocId(doc.getId());
            recipe.setRecipeName((String) data.get("recipeName"));
            recipe.setDescription((String) data.get("description"));
            recipe.setEstimatedTime((String) data.get("estimatedTime"));
            recipe.setServings((String) data.get("servings"));
            recipe.setNotes((String) data.get("notes"));
            recipe.setTips((String) data.get("tips"));
            recipe.setUserId((String) data.get("userId"));
            recipe.setUserEmail((String) data.get("userEmail"));
            
            // Parse imageUrls
            Object imageUrlsObj = data.get("imageUrls");
            System.out.println("🔍 Parsing imageUrls for recipe: " + recipe.getRecipeName());
            System.out.println("   imageUrlsObj type: " + (imageUrlsObj != null ? imageUrlsObj.getClass().getName() : "null"));
            System.out.println("   imageUrlsObj value: " + imageUrlsObj);
            
            if (imageUrlsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> imageUrlsRaw = (List<Object>) imageUrlsObj;
                List<String> imageUrls = new ArrayList<>();
                
                System.out.println("   📦 List size: " + imageUrlsRaw.size());
                
                for (int i = 0; i < imageUrlsRaw.size(); i++) {
                    Object item = imageUrlsRaw.get(i);
                    System.out.println("   Item[" + i + "] type: " + (item != null ? item.getClass().getName() : "null"));
                    System.out.println("   Item[" + i + "] value: " + item);
                    
                    if (item instanceof String) {
                        String url = (String) item;
                        if (url != null && !url.trim().isEmpty()) {
                            imageUrls.add(url.trim());
                            System.out.println("   ✅ Added image URL[" + i + "]: " + url.trim());
                        } else {
                            System.out.println("   ⚠️ URL[" + i + "] is empty or null");
                        }
                    } else if (item != null) {
                        // Convert to string if not null
                        String url = item.toString().trim();
                        if (!url.isEmpty()) {
                            imageUrls.add(url);
                            System.out.println("   ✅ Converted and added image URL[" + i + "]: " + url);
                        } else {
                            System.out.println("   ⚠️ Converted URL[" + i + "] is empty");
                        }
                    } else {
                        System.out.println("   ⚠️ Item[" + i + "] is null, skipping");
                    }
                }
                
                recipe.setImageUrls(imageUrls);
                System.out.println("   ✅ Total image URLs parsed: " + imageUrls.size());
                if (!imageUrls.isEmpty()) {
                    System.out.println("   🖼️ First image URL: " + imageUrls.get(0));
                }
            } else if (imageUrlsObj != null) {
                System.out.println("   ⚠️ imageUrls is not a List, it's: " + imageUrlsObj.getClass().getName());
                System.out.println("   ⚠️ Trying to convert to string: " + imageUrlsObj.toString());
            } else {
                System.out.println("   ⚠️ imageUrls is null or missing");
            }
            
            // Parse ingredients
            Object ingredientsObj = data.get("ingredients");
            if (ingredientsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> ingredients = (List<Map<String, Object>>) ingredientsObj;
                recipe.setIngredients(ingredients);
            }
            
            // Parse cookingSteps
            Object cookingStepsObj = data.get("cookingSteps");
            if (cookingStepsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> cookingSteps = (List<Map<String, Object>>) cookingStepsObj;
                recipe.setCookingSteps(cookingSteps);
            }
            
            // Parse nutritionData
            Object nutritionDataObj = data.get("nutritionData");
            if (nutritionDataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nutritionData = (Map<String, Object>) nutritionDataObj;
                recipe.setNutritionData(nutritionData);
            }
            
            // Parse rating và reviewCount
            Object ratingObj = data.get("rating");
            if (ratingObj instanceof Number) {
                recipe.setRating(((Number) ratingObj).doubleValue());
            } else {
                recipe.setRating(0.0);
            }
            
            Object reviewCountObj = data.get("reviewCount");
            if (reviewCountObj instanceof Number) {
                recipe.setReviewCount(((Number) reviewCountObj).intValue());
            } else {
                recipe.setReviewCount(0);
            }
            
            // Parse approved và available (mặc định true nếu không có)
            Object approvedObj = data.get("approved");
            if (approvedObj instanceof Boolean) {
                recipe.setApproved((Boolean) approvedObj);
            } else {
                recipe.setApproved(true);
            }
            
            Object availableObj = data.get("available");
            if (availableObj instanceof Boolean) {
                recipe.setAvailable((Boolean) availableObj);
            } else {
                recipe.setAvailable(true);
            }
            
            // Parse timestamps
            Object createdAtObj = data.get("createdAt");
            if (createdAtObj instanceof Timestamp) {
                Timestamp ts = (Timestamp) createdAtObj;
                recipe.setCreatedAt(ts.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (createdAtObj instanceof Date) {
                Date d = (Date) createdAtObj;
                recipe.setCreatedAt(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            
            Object updatedAtObj = data.get("updatedAt");
            if (updatedAtObj instanceof Timestamp) {
                Timestamp ts = (Timestamp) updatedAtObj;
                recipe.setUpdatedAt(ts.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (updatedAtObj instanceof Date) {
                Date d = (Date) updatedAtObj;
                recipe.setUpdatedAt(d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            
            out.add(recipe);
        }
        
        System.out.println("✅ Successfully parsed " + out.size() + " UserRecipe objects");
        return out;
    }
    
    /**
     * Xóa một UserRecipe khỏi Firestore
     */
    public boolean deleteUserRecipe(String docId) throws Exception {
        if (docId == null || docId.isEmpty()) return false;
        firestore.collection("userRecipes").document(docId).delete().get();
        return true;
    }
    
    /**
     * Cập nhật trạng thái approved của UserRecipe
     */
    public boolean updateUserRecipeApproval(String docId, boolean approved) throws Exception {
        if (docId == null || docId.isEmpty()) return false;
        Map<String, Object> updates = new HashMap<>();
        updates.put("approved", approved);
        updates.put("updatedAt", new Date());
        firestore.collection("userRecipes").document(docId).update(updates).get();
        return true;
    }
    
    /**
     * Cập nhật trạng thái available của UserRecipe
     */
    public boolean updateUserRecipeAvailability(String docId, boolean available) throws Exception {
        if (docId == null || docId.isEmpty()) return false;
        Map<String, Object> updates = new HashMap<>();
        updates.put("available", available);
        updates.put("updatedAt", new Date());
        firestore.collection("userRecipes").document(docId).update(updates).get();
        return true;
    }
}