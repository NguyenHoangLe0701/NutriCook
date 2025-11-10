package com.nutricook.dashboard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nutricook.dashboard.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByNameAsc();
    boolean existsByName(String name);
    Category findByName(String name);
    
    // Find category by name ignoring case
    Optional<Category> findByNameIgnoreCase(String name);
    
    // Check if category name exists excluding current category (for updates)
    boolean existsByNameAndIdNot(String name, Long id);
}