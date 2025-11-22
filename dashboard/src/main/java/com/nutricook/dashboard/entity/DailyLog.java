package com.nutricook.dashboard.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho dữ liệu calories của người dùng mỗi ngày
 * Tương ứng với DailyLog trong mobile app
 */
public class DailyLog {
    private String dateId; // Format: "yyyy-MM-dd" (ví dụ: "2025-01-20")
    private Float calories;
    private Float protein;
    private Float fat;
    private Float carb;
    private Long updatedAt;
    
    // Constructors
    public DailyLog() {}
    
    public DailyLog(String dateId, Float calories, Float protein, Float fat, Float carb, Long updatedAt) {
        this.dateId = dateId;
        this.calories = calories != null ? calories : 0f;
        this.protein = protein != null ? protein : 0f;
        this.fat = fat != null ? fat : 0f;
        this.carb = carb != null ? carb : 0f;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public String getDateId() { return dateId; }
    public void setDateId(String dateId) { this.dateId = dateId; }
    
    public Float getCalories() { return calories != null ? calories : 0f; }
    public void setCalories(Float calories) { this.calories = calories; }
    
    public Float getProtein() { return protein != null ? protein : 0f; }
    public void setProtein(Float protein) { this.protein = protein; }
    
    public Float getFat() { return fat != null ? fat : 0f; }
    public void setFat(Float fat) { this.fat = fat; }
    
    public Float getCarb() { return carb != null ? carb : 0f; }
    public void setCarb(Float carb) { this.carb = carb; }
    
    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public LocalDate getDate() {
        if (dateId == null || dateId.isEmpty()) return null;
        try {
            return LocalDate.parse(dateId);
        } catch (Exception e) {
            return null;
        }
    }
    
    public LocalDateTime getUpdatedDateTime() {
        if (updatedAt == null) return null;
        return LocalDateTime.ofEpochSecond(updatedAt / 1000, 0, 
            java.time.ZoneOffset.systemDefault().getRules().getOffset(java.time.Instant.now()));
    }
}
