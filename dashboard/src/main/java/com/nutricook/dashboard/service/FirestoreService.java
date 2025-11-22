package com.nutricook.dashboard.service;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.nutricook.dashboard.entity.Category; // Cần import Category
import com.nutricook.dashboard.entity.FoodItem; // Cần import FoodItem
import com.nutricook.dashboard.entity.User;
import com.nutricook.dashboard.entity.DailyLog;
import com.nutricook.dashboard.entity.NutritionStats;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.google.cloud.firestore.WriteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
        data.put("imageUrl", food.getImageUrl());
        data.put("rating", food.getRating() != null ? food.getRating() : 0.0);
        data.put("reviews", food.getReviews() != null ? food.getReviews() : 0);
        
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
}