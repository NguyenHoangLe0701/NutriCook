package com.nutricook.dashboard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nutricook.dashboard.entity.Category;
import com.nutricook.dashboard.entity.FoodItem;
import com.nutricook.dashboard.entity.User;


@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    
    // Basic CRUD methods are provided by JpaRepository
    
    // Find foods by category - ĐẢM BẢO Category entity có field 'id'
    List<FoodItem> findByCategory(Category category);
    
    // Find foods by category ID using query
    @Query("SELECT f FROM FoodItem f WHERE f.category.id = :categoryId")
    List<FoodItem> findByCategoryId(@Param("categoryId") Long categoryId);
    
    // Find available foods
    List<FoodItem> findByAvailableTrue();
    
    // Find foods by name containing
    List<FoodItem> findByNameContainingIgnoreCase(String name);
    
    // Find all foods ordered by name
    List<FoodItem> findAllByOrderByNameAsc();
    
    // Find foods by category ordered by name
    List<FoodItem> findByCategoryOrderByNameAsc(Category category);
    
    // Check if food name exists
    boolean existsByName(String name);

   List<FoodItem> findByUser(User user);
List<FoodItem> findByUserOrderByCreatedAtDesc(User user);

}