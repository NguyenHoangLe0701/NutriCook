package com.nutricook.dashboard.entity;

import java.util.List;

/**
 * DTO chứa thống kê calories của một người dùng
 */
public class NutritionStats {
    private String userId;
    private String userName;
    private String userEmail;
    private Float caloriesTarget;
    private List<DailyLog> weeklyLogs; // 7 ngày gần nhất
    private Float averageCalories; // Trung bình calories/ngày
    private Float averageProtein;
    private Float averageFat;
    private Float averageCarb;
    private Integer daysTracked; // Số ngày đã track
    private Integer daysReachedGoal; // Số ngày đạt mục tiêu
    private Float goalAchievementRate; // Tỉ lệ đạt mục tiêu (%)
    
    // Constructors
    public NutritionStats() {}
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public Float getCaloriesTarget() { return caloriesTarget != null ? caloriesTarget : 2000f; }
    public void setCaloriesTarget(Float caloriesTarget) { this.caloriesTarget = caloriesTarget; }
    
    public List<DailyLog> getWeeklyLogs() { return weeklyLogs; }
    public void setWeeklyLogs(List<DailyLog> weeklyLogs) { this.weeklyLogs = weeklyLogs; }
    
    public Float getAverageCalories() { return averageCalories != null ? averageCalories : 0f; }
    public void setAverageCalories(Float averageCalories) { this.averageCalories = averageCalories; }
    
    public Float getAverageProtein() { return averageProtein != null ? averageProtein : 0f; }
    public void setAverageProtein(Float averageProtein) { this.averageProtein = averageProtein; }
    
    public Float getAverageFat() { return averageFat != null ? averageFat : 0f; }
    public void setAverageFat(Float averageFat) { this.averageFat = averageFat; }
    
    public Float getAverageCarb() { return averageCarb != null ? averageCarb : 0f; }
    public void setAverageCarb(Float averageCarb) { this.averageCarb = averageCarb; }
    
    public Integer getDaysTracked() { return daysTracked != null ? daysTracked : 0; }
    public void setDaysTracked(Integer daysTracked) { this.daysTracked = daysTracked; }
    
    public Integer getDaysReachedGoal() { return daysReachedGoal != null ? daysReachedGoal : 0; }
    public void setDaysReachedGoal(Integer daysReachedGoal) { this.daysReachedGoal = daysReachedGoal; }
    
    public Float getGoalAchievementRate() { return goalAchievementRate != null ? goalAchievementRate : 0f; }
    public void setGoalAchievementRate(Float goalAchievementRate) { this.goalAchievementRate = goalAchievementRate; }
    
    // Helper methods
    public String getStatus() {
        Float avgCal = getAverageCalories();
        Float target = getCaloriesTarget();
        if (avgCal == 0f) return "Chưa có dữ liệu";
        float ratio = avgCal / target;
        if (ratio >= 1.0f) return "Vượt mục tiêu";
        if (ratio >= 0.9f) return "Gần mục tiêu";
        if (ratio >= 0.7f) return "Trung bình";
        return "Thiếu mục tiêu";
    }
    
    public String getStatusColor() {
        Float avgCal = getAverageCalories();
        Float target = getCaloriesTarget();
        if (avgCal == 0f) return "gray";
        float ratio = avgCal / target;
        if (ratio >= 1.0f) return "red";
        if (ratio >= 0.9f) return "orange";
        if (ratio >= 0.7f) return "yellow";
        return "green";
    }
}
