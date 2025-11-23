package com.nutricook.dashboard.entity;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho Review/Đánh giá món ăn từ mobile app
 */
public class Review {
    private String id; // Firestore document ID
    private String foodItemId;
    private String foodItemName;
    private String userId;
    private String userName;
    private String userEmail;
    private Integer rating; // 1-5
    private String comment;
    private Long createdAt;
    private Boolean isDeleted;
    
    // Constructors
    public Review() {
        this.rating = 0;
        this.isDeleted = false;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getFoodItemId() { return foodItemId; }
    public void setFoodItemId(String foodItemId) { this.foodItemId = foodItemId; }
    
    public String getFoodItemName() { return foodItemName; }
    public void setFoodItemName(String foodItemName) { this.foodItemName = foodItemName; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public Integer getRating() { return rating != null ? rating : 0; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getComment() { return comment != null ? comment : ""; }
    public void setComment(String comment) { this.comment = comment; }
    
    public Long getCreatedAt() { return createdAt != null ? createdAt : 0L; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    
    public Boolean getIsDeleted() { return isDeleted != null ? isDeleted : false; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    // Helper methods
    public LocalDateTime getCreatedDateTime() {
        if (createdAt == null) return null;
        return LocalDateTime.ofEpochSecond(createdAt / 1000, 0, 
            java.time.ZoneOffset.systemDefault().getRules().getOffset(java.time.Instant.now()));
    }
    
    public String getRatingStars() {
        int rating = getRating();
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
}
