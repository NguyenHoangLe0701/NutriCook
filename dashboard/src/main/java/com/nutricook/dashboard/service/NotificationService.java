package com.nutricook.dashboard.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.api.core.ApiFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@Service
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final Firestore firestore;

    public NotificationService(Firestore firestore) {
        this.firestore = firestore;
        this.firebaseMessaging = FirebaseMessaging.getInstance();
    }

    /**
     * Gửi thông báo đến tất cả người dùng
     */
    public int sendNotificationToAll(String title, String message) throws FirebaseMessagingException, ExecutionException, InterruptedException {
        List<String> fcmTokens = getAllFcmTokens();
        return sendNotificationToTokens(fcmTokens, title, message);
    }

    /**
     * Gửi thông báo đến người dùng hoạt động (có FCM token)
     */
    public int sendNotificationToActive(String title, String message) throws FirebaseMessagingException, ExecutionException, InterruptedException {
        List<String> fcmTokens = getActiveFcmTokens();
        return sendNotificationToTokens(fcmTokens, title, message);
    }

    /**
     * Gửi thông báo đến người dùng mới (đăng ký trong 30 ngày gần đây)
     */
    public int sendNotificationToNew(String title, String message) throws FirebaseMessagingException, ExecutionException, InterruptedException {
        List<String> fcmTokens = getNewUsersFcmTokens();
        return sendNotificationToTokens(fcmTokens, title, message);
    }

    /**
     * Gửi thông báo đến danh sách FCM tokens
     */
    private int sendNotificationToTokens(List<String> fcmTokens, String title, String message) throws FirebaseMessagingException {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        List<String> invalidTokens = new ArrayList<>();

        for (String token : fcmTokens) {
            if (token == null || token.isEmpty()) {
                continue;
            }

            try {
                // Tạo Android notification config để hiển thị trên lock screen
                AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH) // High priority để hiển thị trên lock screen
                    .setNotification(AndroidNotification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .setSound("default")
                        .setChannelId("nutricook_notifications") // Channel ID cho Android
                        .setVisibility(AndroidNotification.Visibility.PUBLIC) // Hiển thị trên lock screen
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .build())
                    .build();

                // Tạo message
                Message fcmMessage = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(message)
                        .build())
                    .setAndroidConfig(androidConfig)
                    .putData("title", title)
                    .putData("message", message)
                    .putData("type", "admin_notification")
                    .build();

                // Gửi message
                String response = firebaseMessaging.send(fcmMessage);
                successCount++;
                System.out.println("✅ Successfully sent message to token: " + token.substring(0, Math.min(20, token.length())) + "...");

            } catch (FirebaseMessagingException e) {
                System.err.println("❌ Error sending message to token: " + e.getMessage());
                if (e.getErrorCode().equals("messaging/invalid-registration-token") || 
                    e.getErrorCode().equals("messaging/registration-token-not-registered")) {
                    invalidTokens.add(token);
                }
            } catch (Exception e) {
                System.err.println("❌ Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Xóa invalid tokens khỏi Firestore (optional)
        if (!invalidTokens.isEmpty()) {
            removeInvalidTokens(invalidTokens);
        }

        return successCount;
    }

    /**
     * Lấy tất cả FCM tokens từ Firestore
     */
    private List<String> getAllFcmTokens() throws ExecutionException, InterruptedException {
        List<String> tokens = new ArrayList<>();
        if (firestore == null) {
            return tokens;
        }

        try {
            CollectionReference users = firestore.collection("users");
            QuerySnapshot snapshot = users.get().get();

            snapshot.getDocuments().forEach(doc -> {
                Map<String, Object> data = doc.getData();
                if (data != null && data.containsKey("fcmToken")) {
                    Object token = data.get("fcmToken");
                    if (token != null && !token.toString().isEmpty()) {
                        tokens.add(token.toString());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error getting FCM tokens: " + e.getMessage());
            e.printStackTrace();
        }

        return tokens;
    }

    /**
     * Lấy FCM tokens của người dùng hoạt động (có token)
     */
    private List<String> getActiveFcmTokens() throws ExecutionException, InterruptedException {
        // Tạm thời trả về tất cả tokens, có thể filter theo lastActiveDate sau
        return getAllFcmTokens();
    }

    /**
     * Lấy FCM tokens của người dùng mới (đăng ký trong 30 ngày)
     */
    private List<String> getNewUsersFcmTokens() throws ExecutionException, InterruptedException {
        List<String> tokens = new ArrayList<>();
        if (firestore == null) {
            return tokens;
        }

        try {
            long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            CollectionReference users = firestore.collection("users");
            QuerySnapshot snapshot = users.get().get();

            snapshot.getDocuments().forEach(doc -> {
                Map<String, Object> data = doc.getData();
                if (data != null && data.containsKey("fcmToken")) {
                    // Kiểm tra ngày đăng ký (nếu có)
                    Object createdAt = data.get("createdAt");
                    if (createdAt != null) {
                        long createdTime = 0;
                        if (createdAt instanceof com.google.cloud.Timestamp) {
                            createdTime = ((com.google.cloud.Timestamp) createdAt).toDate().getTime();
                        } else if (createdAt instanceof Long) {
                            createdTime = (Long) createdAt;
                        }

                        if (createdTime >= thirtyDaysAgo) {
                            Object token = data.get("fcmToken");
                            if (token != null && !token.toString().isEmpty()) {
                                tokens.add(token.toString());
                            }
                        }
                    } else {
                        // Nếu không có createdAt, thêm vào (có thể là user mới)
                        Object token = data.get("fcmToken");
                        if (token != null && !token.toString().isEmpty()) {
                            tokens.add(token.toString());
                        }
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error getting new users FCM tokens: " + e.getMessage());
            e.printStackTrace();
        }

        return tokens;
    }

    /**
     * Xóa invalid tokens khỏi Firestore
     */
    private void removeInvalidTokens(List<String> invalidTokens) {
        if (firestore == null || invalidTokens.isEmpty()) {
            return;
        }

        try {
            CollectionReference users = firestore.collection("users");
            QuerySnapshot snapshot = users.get().get();

            snapshot.getDocuments().forEach(doc -> {
                Map<String, Object> data = doc.getData();
                if (data != null && data.containsKey("fcmToken")) {
                    String token = data.get("fcmToken").toString();
                    if (invalidTokens.contains(token)) {
                        // Xóa fcmToken field
                        doc.getReference().update("fcmToken", null);
                        System.out.println("🗑️ Removed invalid token for user: " + doc.getId());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error removing invalid tokens: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

