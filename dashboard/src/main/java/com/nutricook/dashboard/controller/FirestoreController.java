package com.nutricook.dashboard.controller;

import com.nutricook.dashboard.service.FirestoreService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import com.nutricook.dashboard.entity.User;

import java.util.List;
import java.util.Map;

@RestController
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
@RequestMapping("/api/firestore")
public class FirestoreController {

    private final FirestoreService firestoreService;

    public FirestoreController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        try {
            List<Map<String, Object>> users = firestoreService.listUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/entities")
    public ResponseEntity<?> listUserEntities() {
        try {
            List<User> users = firestoreService.listUsersAsEntities();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<?> createOrUpdateUser(@RequestBody User user) {
        try {
            String id = firestoreService.saveUser(user);
            return ResponseEntity.ok(Map.of("id", id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@org.springframework.web.bind.annotation.PathVariable("id") String id) {
        try {
            User user = firestoreService.getUserByDocId(id);
            if (user == null) return ResponseEntity.status(404).body(Map.of("error", "not_found"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@org.springframework.web.bind.annotation.PathVariable("id") String id, @RequestBody User user) {
        try {
            String docId = firestoreService.saveUserWithDocId(id, user);
            return ResponseEntity.ok(Map.of("id", docId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@org.springframework.web.bind.annotation.PathVariable("id") String id) {
        try {
            boolean ok = firestoreService.deleteUserByDocId(id);
            return ResponseEntity.ok(Map.of("deleted", ok));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
