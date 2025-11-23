package com.nutricook.dashboard.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho Post từ mobile app
 * Tương ứng với Post trong mobile app
 */
public class Post {
    private String id; // Firestore document ID
    private String content;
    private List<String> images;
    private String authorId;
    private String authorName;
    private String authorEmail;
    private Long createdAt;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isDeleted;
    
    // Constructors
    public Post() {
        this.likeCount = 0;
        this.commentCount = 0;
        this.isDeleted = false;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getContent() { return content != null ? content : ""; }
    public void setContent(String content) { this.content = content; }
    
    public List<String> getImages() { return images != null ? images : java.util.Collections.emptyList(); }
    public void setImages(List<String> images) { this.images = images; }
    
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    
    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }
    
    public Long getCreatedAt() { return createdAt != null ? createdAt : 0L; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    
    public Integer getLikeCount() { return likeCount != null ? likeCount : 0; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    
    public Integer getCommentCount() { return commentCount != null ? commentCount : 0; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    
    public Boolean getIsDeleted() { return isDeleted != null ? isDeleted : false; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    // Helper methods
    public LocalDateTime getCreatedDateTime() {
        if (createdAt == null) return null;
        return LocalDateTime.ofEpochSecond(createdAt / 1000, 0, 
            java.time.ZoneOffset.systemDefault().getRules().getOffset(java.time.Instant.now()));
    }
    
    public String getFirstImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }
    
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }
}
