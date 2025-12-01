package com.example.nutricook.data.nutrition

import com.example.nutricook.model.nutrition.DailyLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class NutritionRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun uid() = auth.currentUser?.uid ?: ""

    // Đường dẫn: users/{uid}/daily_logs
    private fun logsCol() = db.collection("users").document(uid()).collection("daily_logs")

    // --- HELPER: Lấy ID ngày hôm nay (dạng "2023-10-27") ---
    private fun getTodayDateId(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // 1. Lấy dữ liệu của CHÍNH XÁC ngày hôm nay
    suspend fun getTodayLog(): DailyLog? {
        if (auth.currentUser == null) return null
        val todayId = getTodayDateId()

        return try {
            val snap = logsCol().document(todayId).get().await()
            // Nếu tìm thấy document ngày hôm nay -> Trả về data
            // Nếu không thấy (qua ngày mới) -> Trả về null (ViewModel sẽ tự set về 0)
            snap.toObject(DailyLog::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 2. Cập nhật dinh dưỡng (Cộng dồn vào ngày hôm nay)
    suspend fun updateTodayNutrition(calories: Float, protein: Float, fat: Float, carb: Float) {
        if (auth.currentUser == null) return
        
        // Validation: Đảm bảo giá trị hợp lệ
        val validCalories = calories.coerceIn(0f, 10000f) // Giới hạn tối đa 10000 kcal
        val validProtein = protein.coerceIn(0f, 1000f)
        val validFat = fat.coerceIn(0f, 1000f)
        val validCarb = carb.coerceIn(0f, 2000f)
        
        android.util.Log.d("NutritionRepo", "Adding nutrition - Calories: $validCalories, Protein: $validProtein, Fat: $validFat, Carb: $validCarb")
        
        val todayId = getTodayDateId()
        val docRef = logsCol().document(todayId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)

            if (snapshot.exists()) {
                // Trường hợp 1: Đã có dữ liệu hôm nay -> CỘNG DỒN
                val current = snapshot.toObject(DailyLog::class.java)!!
                val newCalories = current.calories + validCalories
                val newProtein = current.protein + validProtein
                val newFat = current.fat + validFat
                val newCarb = current.carb + validCarb
                
                android.util.Log.d("NutritionRepo", "Current: Calories=${current.calories}, Protein=${current.protein}, Fat=${current.fat}, Carb=${current.carb}")
                android.util.Log.d("NutritionRepo", "New: Calories=$newCalories, Protein=$newProtein, Fat=$newFat, Carb=$newCarb")
                
                transaction.update(docRef, mapOf(
                    "calories" to newCalories,
                    "protein" to newProtein,
                    "fat" to newFat,
                    "carb" to newCarb
                ))
            } else {
                // Trường hợp 2: Chưa có (Sáng sớm ngày mới) -> TẠO MỚI
                android.util.Log.d("NutritionRepo", "Creating new log for today: $todayId")
                val newLog = DailyLog(
                    dateId = todayId,
                    calories = validCalories,
                    protein = validProtein,
                    fat = validFat,
                    carb = validCarb
                )
                transaction.set(docRef, newLog)
            }
        }.await()
        
        android.util.Log.d("NutritionRepo", "Nutrition updated successfully")
    }

    // 3. Lấy lịch sử 7 ngày gần nhất (để vẽ biểu đồ)
    suspend fun getWeeklyHistory(): List<DailyLog> {
        if (auth.currentUser == null) return emptyList()

        return try {
            val snapshot = logsCol()
                .orderBy("dateId", Query.Direction.DESCENDING) // Lấy ngày mới nhất trước
                .limit(7)
                .get()
                .await()

            // Đảo ngược lại (Cũ -> Mới) để vẽ biểu đồ từ trái sang phải
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(DailyLog::class.java)
            }.reversed()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // 4. Reset dữ liệu ngày hôm nay về 0
    suspend fun resetTodayNutrition() {
        if (auth.currentUser == null) return
        val todayId = getTodayDateId()
        val docRef = logsCol().document(todayId)
        
        android.util.Log.d("NutritionRepo", "Resetting today's nutrition data")
        
        try {
            val newLog = DailyLog(
                dateId = todayId,
                calories = 0f,
                protein = 0f,
                fat = 0f,
                carb = 0f
            )
            docRef.set(newLog).await()
            android.util.Log.d("NutritionRepo", "Today's nutrition data reset successfully")
        } catch (e: Exception) {
            android.util.Log.e("NutritionRepo", "Error resetting nutrition data: ${e.message}", e)
            throw e
        }
    }

    // (Optional) Hàm cũ giữ lại nếu cần
    suspend fun saveDailyLog(log: DailyLog) {
        logsCol().document(log.dateId).set(log, SetOptions.merge()).await()
    }
}