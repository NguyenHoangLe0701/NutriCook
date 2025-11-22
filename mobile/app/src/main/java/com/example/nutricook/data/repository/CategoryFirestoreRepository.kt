package com.example.nutricook.data.repository

import androidx.compose.ui.graphics.Color
import com.example.nutricook.viewmodel.CategoryUI
import com.example.nutricook.viewmodel.FoodItemUI
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// !! QUAN TRỌNG: Thay IP này bằng IP Wi-Fi THẬT của máy tính bạn
// (Đây là IP của server Spring Boot để lấy hình ảnh)
private const val BASE_URL = "http://192.168.88.164:8080/" // <-- THAY IP CỦA BẠN VÀO ĐÂY

@Singleton
class CategoryFirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Lấy danh sách Category từ collection "categories" trên Firestore.
     */
    suspend fun getCategories(): List<CategoryUI> {
        // Đọc collection "categories" BẠN VỪA TẠO
        val snapshot = firestore.collection("categories").get().await()
        return snapshot.documents.mapNotNull { doc ->
            try {
                val colorString = doc.getString("color")
                val safeColorString = if (colorString.isNullOrEmpty()) {
                    "#808080" // Màu xám mặc định
                } else {
                    colorString
                }

                CategoryUI(
                    id = doc.getLong("id") ?: 0L, // Lấy trường id (ví dụ: 1)
                    name = doc.getString("name") ?: "", // Lấy trường name (ví dụ: "Rau củ")
                    icon = doc.getString("icon") ?: "❓",
                    color = Color(android.graphics.Color.parseColor(safeColorString))
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Lấy danh sách FoodItem từ collection "foodItems" dựa trên categoryId.
     */
    suspend fun getFoods(categoryId: Long): List<FoodItemUI> { // Nhận categoryId (ví dụ: 1)
        val snapshot = firestore.collection("foodItems")
            // Tìm món ăn có categoryId khớp
            .whereEqualTo("categoryId", categoryId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                val imageUrl = doc.getString("imageUrl")
                // Kiểm tra imageUrl không null và không rỗng, sau đó tạo full URL
                val fullImageUrl = if (!imageUrl.isNullOrBlank()) {
                    // Nếu imageUrl đã là full URL (bắt đầu bằng http), dùng trực tiếp
                    // Nếu không, ghép với BASE_URL
                    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        imageUrl
                    } else {
                        BASE_URL.dropLast(1) + imageUrl
                    }
                } else {
                    "" // Trả về chuỗi rỗng nếu không có imageUrl
                }
                
                FoodItemUI(
                    id = doc.getLong("id") ?: 0L,
                    name = doc.getString("name") ?: "",
                    calories = doc.getString("calories") ?: "0 kcal",
                    imageUrl = fullImageUrl
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}