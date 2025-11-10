package com.nutricook.dashboard.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_updates")
public class FoodUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;
    
    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE
    
    private String oldData;
    private String newData;
    
    private LocalDateTime createdAt;
    
    // Constructors
    public FoodUpdate() {
        this.createdAt = LocalDateTime.now();
    }
    
    public FoodUpdate(User user, FoodItem foodItem, String action) {
        this();
        this.user = user;
        this.foodItem = foodItem;
        this.action = action;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public FoodItem getFoodItem() { return foodItem; }
    public void setFoodItem(FoodItem foodItem) { this.foodItem = foodItem; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getOldData() { return oldData; }
    public void setOldData(String oldData) { this.oldData = oldData; }
    
    public String getNewData() { return newData; }
    public void setNewData(String newData) { this.newData = newData; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}