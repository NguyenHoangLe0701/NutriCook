package com.nutricook.dashboard.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutricook.dashboard.entity.FoodUpdate; // Cần import LocalDateTime

@Repository
public interface FoodUpdateRepository extends JpaRepository<FoodUpdate, Long> {
    List<FoodUpdate> findAllByOrderByCreatedAtDesc();
    
    // Thêm phương thức để đếm số bản ghi sau một thời điểm (cho updateCount)
    Long countByCreatedAtAfter(LocalDateTime createdAt); 
    
    // Thêm phương thức để lấy 5 bản ghi gần nhất
    List<FoodUpdate> findTop5ByOrderByCreatedAtDesc();
}