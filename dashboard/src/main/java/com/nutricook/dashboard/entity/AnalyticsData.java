package com.nutricook.dashboard.entity;

import java.util.List;

/**
 * DTO chứa dữ liệu analytics tổng quan
 */
public class AnalyticsData {
    private Long totalUsers;
    private Long activeUsers; // Users có hoạt động trong 30 ngày
    private Long totalPosts;
    private Long totalReviews;
    private Long totalFoodItems;
    private Double averageRating;
    private Long totalCaloriesTracked; // Tổng calories đã track
    private List<DailyStats> dailyStats; // Thống kê 7 ngày qua
    
    // Getters and Setters
    public Long getTotalUsers() { return totalUsers != null ? totalUsers : 0L; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
    
    public Long getActiveUsers() { return activeUsers != null ? activeUsers : 0L; }
    public void setActiveUsers(Long activeUsers) { this.activeUsers = activeUsers; }
    
    public Long getTotalPosts() { return totalPosts != null ? totalPosts : 0L; }
    public void setTotalPosts(Long totalPosts) { this.totalPosts = totalPosts; }
    
    public Long getTotalReviews() { return totalReviews != null ? totalReviews : 0L; }
    public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }
    
    public Long getTotalFoodItems() { return totalFoodItems != null ? totalFoodItems : 0L; }
    public void setTotalFoodItems(Long totalFoodItems) { this.totalFoodItems = totalFoodItems; }
    
    public Double getAverageRating() { return averageRating != null ? averageRating : 0.0; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    
    public Long getTotalCaloriesTracked() { return totalCaloriesTracked != null ? totalCaloriesTracked : 0L; }
    public void setTotalCaloriesTracked(Long totalCaloriesTracked) { this.totalCaloriesTracked = totalCaloriesTracked; }
    
    public List<DailyStats> getDailyStats() { return dailyStats != null ? dailyStats : java.util.Collections.emptyList(); }
    public void setDailyStats(List<DailyStats> dailyStats) { this.dailyStats = dailyStats; }
    
    public static class DailyStats {
        private String date;
        private Long newUsers;
        private Long newPosts;
        private Long newReviews;
        
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        
        public Long getNewUsers() { return newUsers != null ? newUsers : 0L; }
        public void setNewUsers(Long newUsers) { this.newUsers = newUsers; }
        
        public Long getNewPosts() { return newPosts != null ? newPosts : 0L; }
        public void setNewPosts(Long newPosts) { this.newPosts = newPosts; }
        
        public Long getNewReviews() { return newReviews != null ? newReviews : 0L; }
        public void setNewReviews(Long newReviews) { this.newReviews = newReviews; }
    }
}
