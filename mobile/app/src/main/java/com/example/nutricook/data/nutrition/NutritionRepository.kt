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
        val todayId = getTodayDateId()
        val docRef = logsCol().document(todayId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)

            if (snapshot.exists()) {
                // Trường hợp 1: Đã có dữ liệu hôm nay -> CỘNG DỒN
                val current = snapshot.toObject(DailyLog::class.java)!!
                transaction.update(docRef, mapOf(
                    "calories" to current.calories + calories,
                    "protein" to current.protein + protein,
                    "fat" to current.fat + fat,
                    "carb" to current.carb + carb
                ))
            } else {
                // Trường hợp 2: Chưa có (Sáng sớm ngày mới) -> TẠO MỚI
                val newLog = DailyLog(
                    dateId = todayId,
                    calories = calories,
                    protein = protein,
                    fat = fat,
                    carb = carb
                    // ĐÃ XÓA dòng createdAt gây lỗi
                )
                transaction.set(docRef, newLog)
            }
        }.await()
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

    // (Optional) Hàm cũ giữ lại nếu cần
    suspend fun saveDailyLog(log: DailyLog) {
        logsCol().document(log.dateId).set(log, SetOptions.merge()).await()
    }
}