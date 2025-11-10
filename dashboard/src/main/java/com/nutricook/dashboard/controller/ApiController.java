package com.nutricook.dashboard.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nutricook.dashboard.entity.Category;
import com.nutricook.dashboard.entity.FoodItem;
import com.nutricook.dashboard.repository.CategoryRepository;
import com.nutricook.dashboard.repository.FoodItemRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow Android app to connect
public class ApiController {
    
    @Autowired
    private FoodItemRepository foodItemRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @GetMapping("/foods")
    public List<FoodItem> getAllFoods() {
        return foodItemRepository.findAllByOrderByNameAsc().stream()
                .map(obj -> (FoodItem) obj)
                .toList();
    }
    
    @GetMapping("/foods/category/{categoryId}")
    public List<FoodItem> getFoodsByCategory(@PathVariable Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null) {
            return foodItemRepository.findByCategory(category).stream()
                    .map(obj -> (FoodItem) obj)
                    .toList();
        }
        return List.of();
    }
    
    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }
    
    @GetMapping("/foods/{id}")
    public FoodItem getFoodById(@PathVariable Long id) {
        Object obj = foodItemRepository.findById(id).orElse(null);
        return obj != null ? (FoodItem) obj : null;
    }
}