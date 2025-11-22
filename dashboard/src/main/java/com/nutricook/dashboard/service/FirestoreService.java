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
}