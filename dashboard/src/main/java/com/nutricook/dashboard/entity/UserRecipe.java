package com.nutricook.dashboard.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Entity đại diện cho recipe do người dùng tạo từ mobile app
 * Lưu trong Firestore collection "userRecipes"
 */
public class UserRecipe {
    private String docId; // Document ID trong Firestore
    private String recipeName;
    private String description;
    private String estimatedTime;
    private String servings;
    private List<String> imageUrls; // Danh sách URL ảnh từ Cloudinary
    private List<Map<String, Object>> ingredients; // Danh sách nguyên liệu
    private List<Map<String, Object>> cookingSteps; // Các bước nấu
    private String notes;
    private String tips;
    private Map<String, Object> nutritionData; // Thông tin dinh dưỡng
    private String userId; // Firebase Auth UID
    private String userEmail; // Email người upload
    private Double rating = 0.0;
    private Integer reviewCount = 0;
    private Boolean approved = true; // Trạng thái duyệt (mặc định true)
    private Boolean available = true; // Trạng thái hiển thị
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructor
    public UserRecipe() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
    
    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }
    
    public String getServings() { return servings; }
    public void setServings(String servings) { this.servings = servings; }
    
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    
    public List<Map<String, Object>> getIngredients() { return ingredients; }
    public void setIngredients(List<Map<String, Object>> ingredients) { this.ingredients = ingredients; }
    
    public List<Map<String, Object>> getCookingSteps() { return cookingSteps; }
    public void setCookingSteps(List<Map<String, Object>> cookingSteps) { this.cookingSteps = cookingSteps; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }
    
    public Map<String, Object> getNutritionData() { return nutritionData; }
    public void setNutritionData(Map<String, Object> nutritionData) { this.nutritionData = nutritionData; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }
    
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                if (url != null && !url.trim().isEmpty()) {
                    String trimmedUrl = url.trim();
                    // Đảm bảo URL là HTTPS (Cloudinary URLs thường là HTTPS)
                    if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
                        // Chuyển đổi format HEIC sang JPG nếu cần (Cloudinary transformation)
                        String convertedUrl = convertHeicToJpg(trimmedUrl);
                        System.out.println("✅ getFirstImageUrl() returning: " + convertedUrl);
                        return convertedUrl;
                    } else {
                        System.out.println("⚠️ getFirstImageUrl() - URL doesn't start with http:// or https://: " + trimmedUrl);
                    }
                }
            }
            System.out.println("⚠️ getFirstImageUrl() - No valid URL found in imageUrls list");
        } else {
            System.out.println("⚠️ getFirstImageUrl() - imageUrls is null or empty");
        }
        return null;
    }
    
    /**
     * Chuyển đổi URL Cloudinary từ HEIC sang JPG bằng cách thêm transformation parameter
     * Cloudinary URL format: https://res.cloudinary.com/{cloud_name}/image/upload/{version}/{public_id}.{format}
     * Với transformation: https://res.cloudinary.com/{cloud_name}/image/upload/{transformation}/{version}/{public_id}.{format}
     */
    private String convertHeicToJpg(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return cloudinaryUrl;
        }
        
        // Kiểm tra nếu URL chứa .heic hoặc .HEIC
        String lowerUrl = cloudinaryUrl.toLowerCase();
        if (lowerUrl.contains(".heic") || lowerUrl.contains(".heif")) {
            // Cloudinary URL format: https://res.cloudinary.com/{cloud_name}/image/upload/{version}/{public_id}.{format}
            if (cloudinaryUrl.contains("/image/upload/")) {
                // Tìm vị trí /upload/
                int uploadIndex = cloudinaryUrl.indexOf("/image/upload/") + "/image/upload/".length();
                String beforeUpload = cloudinaryUrl.substring(0, uploadIndex);
                String afterUpload = cloudinaryUrl.substring(uploadIndex);
                
                // Thêm transformation để chuyển sang JPG
                // f_jpg: format to JPG, q_auto: auto quality, fl_progressive: progressive JPEG
                String transformation = "f_jpg,q_auto,fl_progressive/";
                
                // Thay thế extension .heic/.heif bằng .jpg
                String convertedAfterUpload = afterUpload
                    .replace(".heic", ".jpg")
                    .replace(".HEIC", ".jpg")
                    .replace(".heif", ".jpg")
                    .replace(".HEIF", ".jpg");
                
                return beforeUpload + transformation + convertedAfterUpload;
            }
        }
        
        return cloudinaryUrl;
    }
    
    public Double getCalories() {
        if (nutritionData != null && nutritionData.get("calories") != null) {
            Object cal = nutritionData.get("calories");
            if (cal instanceof Number) {
                return ((Number) cal).doubleValue();
            }
        }
        return 0.0;
    }
    
    // Helper methods để lấy các giá trị dinh dưỡng
    public Double getFat() {
        if (nutritionData != null && nutritionData.get("fat") != null) {
            Object fat = nutritionData.get("fat");
            if (fat instanceof Number) {
                return ((Number) fat).doubleValue();
            }
        }
        return 0.0;
    }
    
    public Double getCarbs() {
        if (nutritionData != null && nutritionData.get("carbs") != null) {
            Object carbs = nutritionData.get("carbs");
            if (carbs instanceof Number) {
                return ((Number) carbs).doubleValue();
            }
        }
        return 0.0;
    }
    
    public Double getProtein() {
        if (nutritionData != null && nutritionData.get("protein") != null) {
            Object protein = nutritionData.get("protein");
            if (protein instanceof Number) {
                return ((Number) protein).doubleValue();
            }
        }
        return 0.0;
    }
    
    public Double getCholesterol() {
        if (nutritionData != null && nutritionData.get("cholesterol") != null) {
            Object cholesterol = nutritionData.get("cholesterol");
            if (cholesterol instanceof Number) {
                return ((Number) cholesterol).doubleValue();
            }
        }
        return 0.0;
    }
    
    public Double getSodium() {
        if (nutritionData != null && nutritionData.get("sodium") != null) {
            Object sodium = nutritionData.get("sodium");
            if (sodium instanceof Number) {
                return ((Number) sodium).doubleValue();
            }
        }
        return 0.0;
    }
    
    public Double getVitamin() {
        if (nutritionData != null && nutritionData.get("vitamin") != null) {
            Object vitamin = nutritionData.get("vitamin");
            if (vitamin instanceof Number) {
                return ((Number) vitamin).doubleValue();
            }
        }
        return 0.0;
    }
}

