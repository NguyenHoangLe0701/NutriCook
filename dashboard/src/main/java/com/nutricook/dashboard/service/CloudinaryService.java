package com.nutricook.dashboard.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    @Autowired(required = false)
    private Cloudinary cloudinary;
    
    /**
     * Check if Cloudinary is properly configured
     */
    public boolean isConfigured() {
        return cloudinary != null;
    }

    /**
     * Upload image to Cloudinary
     * @param file MultipartFile to upload
     * @return Cloudinary URL of the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file) throws IOException {
        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary is not configured! Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET environment variables.");
        }
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Generate unique public_id for the image
        String publicId = "nutricook/foods/" + UUID.randomUUID().toString();

        // Upload options
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadOptions = ObjectUtils.asMap(
            "public_id", publicId,
            "folder", "nutricook/foods",
            "resource_type", "image",
            "overwrite", false,
            "invalidate", true
        );

        // Upload to Cloudinary
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            uploadOptions
        );

        // Return secure URL (always HTTPS)
        String secureUrl = (String) uploadResult.get("secure_url");
        if (secureUrl == null || secureUrl.isEmpty()) {
            throw new IOException("Cloudinary upload failed: secure_url is null or empty");
        }
        
        // Ensure URL is a full HTTPS URL
        if (!secureUrl.startsWith("http://") && !secureUrl.startsWith("https://")) {
            throw new IOException("Invalid Cloudinary URL format: " + secureUrl);
        }
        
        System.out.println("✅ Cloudinary upload successful. URL: " + secureUrl);
        return secureUrl;
    }

    /**
     * Upload image to Cloudinary from a local file path
     * @param filePath Path to the local image file
     * @return Cloudinary URL of the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadImageFromFile(Path filePath) throws IOException {
        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary is not configured! Please set CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET environment variables.");
        }
        
        if (filePath == null || !Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        File file = filePath.toFile();
        if (!file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Cannot read file: " + filePath);
        }

        // Generate unique public_id for the image
        String publicId = "nutricook/foods/" + UUID.randomUUID().toString();

        // Upload options
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadOptions = ObjectUtils.asMap(
            "public_id", publicId,
            "folder", "nutricook/foods",
            "resource_type", "image",
            "overwrite", false,
            "invalidate", true
        );

        // Upload to Cloudinary
        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
            Files.readAllBytes(filePath),
            uploadOptions
        );

        // Return secure URL (always HTTPS)
        String secureUrl = (String) uploadResult.get("secure_url");
        if (secureUrl == null || secureUrl.isEmpty()) {
            throw new IOException("Cloudinary upload failed: secure_url is null or empty");
        }
        
        // Ensure URL is a full HTTPS URL
        if (!secureUrl.startsWith("http://") && !secureUrl.startsWith("https://")) {
            throw new IOException("Invalid Cloudinary URL format: " + secureUrl);
        }
        
        System.out.println("✅ Cloudinary upload from file successful. URL: " + secureUrl);
        return secureUrl;
    }

    /**
     * Delete image from Cloudinary
     * @param imageUrl Cloudinary URL of the image to delete
     * @return true if deletion is successful
     */
    public boolean deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }

        try {
            // Extract public_id from Cloudinary URL
            String publicId = extractPublicId(imageUrl);
            if (publicId == null) {
                return false;
            }

            // Delete from Cloudinary
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting image from Cloudinary: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extract public_id from Cloudinary URL
     * Format: https://res.cloudinary.com/{cloud_name}/image/upload/{version}/{public_id}.{format}
     * or: https://res.cloudinary.com/{cloud_name}/image/upload/{transformation}/{version}/{public_id}.{format}
     */
    private String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            // Find the upload path
            int uploadIndex = imageUrl.indexOf("/image/upload/");
            if (uploadIndex == -1) {
                return null;
            }

            String afterUpload = imageUrl.substring(uploadIndex + "/image/upload/".length());
            
            // Skip transformation and version if present
            // Format after /image/upload/ could be:
            // - v1234567890/public_id.ext (with version)
            // - t_transformation/v1234567890/public_id.ext (with transformation and version)
            // - public_id.ext (without version/transformation)
            
            String[] parts = afterUpload.split("/");
            String lastPart = parts[parts.length - 1];
            
            // Remove file extension
            if (lastPart.contains(".")) {
                lastPart = lastPart.substring(0, lastPart.lastIndexOf("."));
            }
            
            // Reconstruct full public_id path if there are folders
            if (parts.length > 1) {
                StringBuilder publicIdBuilder = new StringBuilder();
                // Check if first part is a transformation (starts with 't_' or similar)
                int startIndex = 0;
                for (int i = 0; i < parts.length - 1; i++) {
                    if (parts[i].startsWith("v") && parts[i].matches("v\\d+")) {
                        // This is a version, skip it
                        startIndex = i + 1;
                        break;
                    } else if (!parts[i].matches("v\\d+")) {
                        startIndex = i;
                        break;
                    }
                }
                
                for (int i = startIndex; i < parts.length - 1; i++) {
                    if (!parts[i].matches("v\\d+") && !parts[i].startsWith("t_")) {
                        if (publicIdBuilder.length() > 0) {
                            publicIdBuilder.append("/");
                        }
                        publicIdBuilder.append(parts[i]);
                    }
                }
                
                if (publicIdBuilder.length() > 0) {
                    return publicIdBuilder.toString() + "/" + lastPart;
                }
            }
            
            return lastPart;
        } catch (Exception e) {
            System.err.println("Error extracting public_id from URL: " + imageUrl);
            return null;
        }
    }
}

