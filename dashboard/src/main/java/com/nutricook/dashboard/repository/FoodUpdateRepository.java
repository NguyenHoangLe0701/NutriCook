package com.nutricook.dashboard.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutricook.dashboard.entity.FoodUpdate;
import com.nutricook.dashboard.entity.FoodItem; // <-- Đảm bảo bạn có import này

@Repository
public interface FoodUpdateRepository extends JpaRepository<FoodUpdate, Long> {
    List<FoodUpdate> findAllByOrderByCreatedAtDesc();
    
    Long countByCreatedAtAfter(LocalDateTime createdAt); 
    
    List<FoodUpdate> findTop5ByOrderByCreatedAtDesc();

    // === Đảm bảo bạn có dòng này ===
    List<FoodUpdate> findByFoodItem(FoodItem foodItem);
}