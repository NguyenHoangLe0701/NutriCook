package com.nutricook.dashboard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nutricook.dashboard.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Find users by role
    List<User> findByRole(User.UserRole role);
    
    // Find users by name containing (case insensitive)
    List<User> findByFullNameContainingIgnoreCase(String name);
    
    // Find users by username containing (case insensitive)
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    // Count users by role
    long countByRole(User.UserRole role);
    
    // Check if email exists excluding current user (for updates)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    // Check if username exists excluding current user (for updates)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);
}