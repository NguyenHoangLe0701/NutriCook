package com.example.nutricook.data.nutrition

import com.example.nutricook.model.nutrition.DailyTotals
import com.example.nutricook.model.nutrition.Meal
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat
import javax.inject.Inject

/** Dùng API cũ (java.util.*) để hỗ trợ minSdk 24 */
interface IMealRepository {
    fun observeMealsBetween(startMs: Long, endMs: Long): Flow<List<Meal>>

    suspend fun addMeal(meal: Meal)
    suspend fun deleteMeal(id: String)

    /** Mặc định theo múi giờ hệ thống (không cần API 26) */
    fun observeTodayTotals(tz: TimeZone = TimeZone.getDefault()): Flow<DailyTotals>

    /** Tổng theo từng ngày cho  n ngày gần nhất (bao gồm hôm nay) */
    fun observeLastNDaysTotals(
        n: Int,
        tz: TimeZone = TimeZone.getDefault()
    ): Flow<List<DailyTotals>>
}

class FsMealRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : IMealRepository {

    private fun uid(): String = requireNotNull(auth.currentUser?.uid) { "Must sign in" }
    private fun mealsCol() = db.collection("users").document(uid()).collection("meals")

    private fun dayFormatter(tz: TimeZone) = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = tz
    }

    override fun observeMealsBetween(startMs: Long, endMs: Long): Flow<List<Meal>> = callbackFlow {
        val reg = mealsCol()
            .whereGreaterThanOrEqualTo("ateAt", Timestamp(startMs / 1000, 0))
            .whereLessThan("ateAt", Timestamp(endMs / 1000, 0))
            .orderBy("ateAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()).isSuccess; return@addSnapshotListener }
                val list = snap?.documents?.map { d ->
                    Meal(
                        kcal = d.getLong("kcal")?.toInt() ?: 0,
                        proteinG = d.getDouble("proteinG") ?: 0.0,
                        fatG = d.getDouble("fatG") ?: 0.0,
                        carbG = d.getDouble("carbG") ?: 0.0,
                        ateAt = d.getTimestamp("ateAt"),
                        note = d.getString("note"),
                        id = d.id
                    )
                }.orEmpty()
                trySend(list).isSuccess
            }
        awaitClose { reg.remove() }
    }

    override suspend fun addMeal(meal: Meal) {
        val data = hashMapOf(
            "kcal" to meal.kcal,
            "proteinG" to meal.proteinG,
            "fatG" to meal.fatG,
            "carbG" to meal.carbG,
            "ateAt" to (meal.ateAt ?: Timestamp.now()),
            "note" to meal.note
        )
        mealsCol().add(data).await()
    }

    override suspend fun deleteMeal(id: String) {
        mealsCol().document(id).delete().await()
    }

    // ---------- Helpers (java.util.*) ----------
    private fun startOfDayMillis(tz: TimeZone, base: Date = Date()): Long {
        val cal = Calendar.getInstance(tz).apply { time = base }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun addDaysMillis(tz: TimeZone, startMs: Long, days: Int): Long {
        val cal = Calendar.getInstance(tz).apply { timeInMillis = startMs }
        cal.add(Calendar.DAY_OF_MONTH, days)
        return cal.timeInMillis
    }

    private fun dayKey(tz: TimeZone, date: Date): String =
        dayFormatter(tz).format(date)

    // ---------- Tính totals ----------
    override fun observeTodayTotals(tz: TimeZone): Flow<DailyTotals> {
        val start = startOfDayMillis(tz, Date())
        val end = addDaysMillis(tz, start, 1)
        val key = dayKey(tz, Date(start))

        return observeMealsBetween(start, end).map { meals ->
            var kc = 0; var p = 0.0; var f = 0.0; var c = 0.0
            meals.forEach {
                kc += it.kcal
                p += it.proteinG
                f += it.fatG
                c += it.carbG
            }
            DailyTotals(date = key, caloriesIn = kc, proteinG = p, fatG = f, carbG = c)
        }
    }

    override fun observeLastNDaysTotals(n: Int, tz: TimeZone): Flow<List<DailyTotals>> {
        require(n >= 1) { "n must be >= 1" }

        val endStart = startOfDayMillis(tz, Date())             // 0h hôm nay
        val startStart = addDaysMillis(tz, endStart, -(n - 1))  // 0h của (n-1) ngày trước
        val scanEnd = addDaysMillis(tz, endStart, 1)            // 0h ngày mai (exclusive)

        return observeMealsBetween(startStart, scanEnd).map { meals ->
            // Khởi tạo map đủ n ngày
            val map = LinkedHashMap<String, DailyTotals>(n)
            var curStart = startStart
            repeat(n) {
                val key = dayKey(tz, Date(curStart))
                map[key] = DailyTotals(key, 0, 0.0, 0.0, 0.0)
                curStart = addDaysMillis(tz, curStart, 1)
            }

            // Cộng dồn vào từng ngày
            val fmt = dayFormatter(tz)
            meals.forEach { m ->
                val date = m.ateAt?.toDate() ?: Date()
                val key = fmt.format(date)
                val old = map[key] ?: return@forEach
                map[key] = old.copy(
                    caloriesIn = old.caloriesIn + m.kcal,
                    proteinG = old.proteinG + m.proteinG,
                    fatG = old.fatG + m.fatG,
                    carbG = old.carbG + m.carbG
                )
            }
            map.values.toList()
        }
    }
}
