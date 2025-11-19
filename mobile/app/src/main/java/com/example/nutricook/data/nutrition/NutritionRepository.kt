package com.example.nutricook.data.nutrition

import com.example.nutricook.model.nutrition.DailyLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NutritionRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private fun uid() = auth.currentUser?.uid ?: throw Exception("Chưa đăng nhập")

    // Đường dẫn: users/{uid}/daily_logs
    private fun logsCol() = db.collection("users").document(uid()).collection("daily_logs")

    // 1. Cập nhật (hoặc Tạo mới) dữ liệu ngày hôm nay
    suspend fun saveDailyLog(log: DailyLog) {
        // Dùng dateId làm Document ID để tránh trùng lặp ngày
        // SetOptions.merge() giúp chỉ cập nhật trường thay đổi, không xóa đè
        logsCol().document(log.dateId).set(log, SetOptions.merge()).await()
    }

    // 2. Lấy dữ liệu 7 ngày gần nhất để vẽ biểu đồ
    suspend fun getWeeklyHistory(): List<DailyLog> {
        // Lấy 7 bản ghi mới nhất theo ngày
        val snapshot = logsCol()
            .orderBy("dateId", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(7)
            .get()
            .await()

        // Parse và đảo ngược lại để danh sách đi từ Quá khứ -> Hiện tại (cho biểu đồ)
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(DailyLog::class.java)
        }.reversed()
    }

    // 3. Lấy dữ liệu cụ thể 1 ngày
    suspend fun getLogByDate(dateId: String): DailyLog? {
        val snap = logsCol().document(dateId).get().await()
        return snap.toObject(DailyLog::class.java)
    }
}