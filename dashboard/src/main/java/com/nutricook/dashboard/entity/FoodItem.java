package com.nutricook.dashboard.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "food_items")
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String calories;
    
    private String description;
    private String imageUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    // Thêm field user để lưu người upload từ mobile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // Nullable, vì có thể admin thêm thủ công
    private User user;
    
    private Boolean available = true;
    
    // Thêm field rating và reviews cho món ăn người dùng upload
    private Double rating = 0.0; // Điểm đánh giá từ 0-5
    private Integer reviews = 0; // Số lượt đánh giá
    
    // Đơn vị đo lường mặc định cho nguyên liệu
    private String unit = "g"; // g, ml, quả, lát, tép, etc.
    
    // Thông tin dinh dưỡng (tính trên 100g)
    private Double fat = 0.0; // g
    private Double carbs = 0.0; // g
    private Double protein = 0.0; // g
    private Double cholesterol = 0.0; // mg
    private Double sodium = 0.0; // mg
    private Double vitamin = 0.0; // % daily value (tính tổng từ các vitamin chi tiết)
    
    // Chi tiết các loại vitamin (% daily value)
    private Double vitaminA = 0.0; // % DV
    private Double vitaminB1 = 0.0; // Thiamin, % DV
    private Double vitaminB2 = 0.0; // Riboflavin, % DV
    private Double vitaminB3 = 0.0; // Niacin, % DV
    private Double vitaminB6 = 0.0; // % DV
    private Double vitaminB9 = 0.0; // Folate, % DV
    private Double vitaminB12 = 0.0; // % DV
    private Double vitaminC = 0.0; // % DV
    private Double vitaminD = 0.0; // % DV
    private Double vitaminE = 0.0; // % DV
    private Double vitaminK = 0.0; // % DV
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public FoodItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public FoodItem(String name, String calories, String description, Category category) {
        this();
        this.name = name;
        this.calories = calories;
        this.description = description;
        this.category = category;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters (thêm cho user)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCalories() { return calories; }
    public void setCalories(String calories) { this.calories = calories; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public Integer getReviews() { return reviews; }
    public void setReviews(Integer reviews) { this.reviews = reviews; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Getters and Setters for nutrition fields
    public Double getFat() { return fat; }
    public void setFat(Double fat) { this.fat = fat != null ? fat : 0.0; }
    
    public Double getCarbs() { return carbs; }
    public void setCarbs(Double carbs) { this.carbs = carbs != null ? carbs : 0.0; }
    
    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein != null ? protein : 0.0; }
    
    public Double getCholesterol() { return cholesterol; }
    public void setCholesterol(Double cholesterol) { this.cholesterol = cholesterol != null ? cholesterol : 0.0; }
    
    public Double getSodium() { return sodium; }
    public void setSodium(Double sodium) { this.sodium = sodium != null ? sodium : 0.0; }
    
    public Double getVitamin() { return vitamin; }
    public void setVitamin(Double vitamin) { this.vitamin = vitamin != null ? vitamin : 0.0; }
    
    // Getter and Setter for unit
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit != null && !unit.isEmpty() ? unit : "g"; }
    
    // Getters and Setters for detailed vitamins
    public Double getVitaminA() { return vitaminA != null ? vitaminA : 0.0; }
    public void setVitaminA(Double vitaminA) { this.vitaminA = vitaminA != null ? vitaminA : 0.0; }
    
    public Double getVitaminB1() { return vitaminB1 != null ? vitaminB1 : 0.0; }
    public void setVitaminB1(Double vitaminB1) { this.vitaminB1 = vitaminB1 != null ? vitaminB1 : 0.0; }
    
    public Double getVitaminB2() { return vitaminB2 != null ? vitaminB2 : 0.0; }
    public void setVitaminB2(Double vitaminB2) { this.vitaminB2 = vitaminB2 != null ? vitaminB2 : 0.0; }
    
    public Double getVitaminB3() { return vitaminB3 != null ? vitaminB3 : 0.0; }
    public void setVitaminB3(Double vitaminB3) { this.vitaminB3 = vitaminB3 != null ? vitaminB3 : 0.0; }
    
    public Double getVitaminB6() { return vitaminB6 != null ? vitaminB6 : 0.0; }
    public void setVitaminB6(Double vitaminB6) { this.vitaminB6 = vitaminB6 != null ? vitaminB6 : 0.0; }
    
    public Double getVitaminB9() { return vitaminB9 != null ? vitaminB9 : 0.0; }
    public void setVitaminB9(Double vitaminB9) { this.vitaminB9 = vitaminB9 != null ? vitaminB9 : 0.0; }
    
    public Double getVitaminB12() { return vitaminB12 != null ? vitaminB12 : 0.0; }
    public void setVitaminB12(Double vitaminB12) { this.vitaminB12 = vitaminB12 != null ? vitaminB12 : 0.0; }
    
    public Double getVitaminC() { return vitaminC != null ? vitaminC : 0.0; }
    public void setVitaminC(Double vitaminC) { this.vitaminC = vitaminC != null ? vitaminC : 0.0; }
    
    public Double getVitaminD() { return vitaminD != null ? vitaminD : 0.0; }
    public void setVitaminD(Double vitaminD) { this.vitaminD = vitaminD != null ? vitaminD : 0.0; }
    
    public Double getVitaminE() { return vitaminE != null ? vitaminE : 0.0; }
    public void setVitaminE(Double vitaminE) { this.vitaminE = vitaminE != null ? vitaminE : 0.0; }
    
    public Double getVitaminK() { return vitaminK != null ? vitaminK : 0.0; }
    public void setVitaminK(Double vitaminK) { this.vitaminK = vitaminK != null ? vitaminK : 0.0; }
    
    // Method để tính tổng % vitamin (trung bình của tất cả các vitamin)
    public void calculateTotalVitamin() {
        double total = getVitaminA() + getVitaminB1() + getVitaminB2() + getVitaminB3() + 
                      getVitaminB6() + getVitaminB9() + getVitaminB12() + getVitaminC() + 
                      getVitaminD() + getVitaminE() + getVitaminK();
        int count = 11; // Tổng số loại vitamin
        this.vitamin = count > 0 ? total / count : 0.0; // Trung bình % DV
    }
}