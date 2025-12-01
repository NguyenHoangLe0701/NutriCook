package com.example.nutricook.view.recipes

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Reply
import androidx.compose.material.icons.outlined.StarBorder
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nutricook.R
import com.example.nutricook.viewmodel.QueryViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    navController: NavController,
    recipeId: String,
    queryVM: QueryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var comment by remember { mutableStateOf(TextFieldValue("")) }
    var selectedRating by remember { mutableStateOf(0) } // 0 = chưa chọn, 1-5 = số sao
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    
    var reviews by remember { mutableStateOf<List<ReviewItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var reviewCount by remember { mutableStateOf(0) }
    var reloadTrigger by remember { mutableStateOf(0) } // Trigger để reload reviews
    
    // Load reviews for this recipe
    LaunchedEffect(recipeId, reloadTrigger) {
        try {
            isLoading = true
            // Thử query có orderBy trước (cần index)
            val reviewsSnapshot = try {
                db.collection("reviews")
                    .whereEqualTo("recipeId", recipeId)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (e: Exception) {
                // Nếu lỗi index, thử query không orderBy và sort trong memory
                if (e.message?.contains("index") == true || e.message?.contains("FAILED_PRECONDITION") == true) {
                    android.util.Log.w("ReviewScreen", "Index not found, loading without orderBy and sorting in memory")
                    db.collection("reviews")
                        .whereEqualTo("recipeId", recipeId)
                        .get()
                        .await()
                } else {
                    throw e
                }
            }
            
            // Parse reviews
            val reviewsList = reviewsSnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                try {
                    val createdAt = data["createdAt"] as? com.google.firebase.Timestamp
                    val dateStr = if (createdAt != null) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        sdf.format(Date(createdAt.seconds * 1000))
                    } else {
                        data["date"] as? String ?: ""
                    }
                    
                    val likedBy = data["likedBy"] as? List<*> ?: emptyList<Any>()
                    val currentUserId = auth.currentUser?.uid ?: ""
                    val isLiked = currentUserId.isNotEmpty() && likedBy.contains(currentUserId)
                    
                    ReviewItem(
                        id = doc.id,
                        userName = data["userName"] as? String ?: "Người dùng",
                        date = dateStr,
                        text = data["text"] as? String ?: "",
                        liked = isLiked,
                        rating = (data["rating"] as? Number)?.toInt() ?: 0,
                        likes = (data["likes"] as? Number)?.toInt() ?: 0,
                        userId = data["userId"] as? String ?: "",
                        createdAt = createdAt, // Thêm field để sort
                        avatarUrl = data["userAvatarUrl"] as? String // Lấy avatarUrl từ review
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ReviewScreen", "Error parsing review: ${e.message}", e)
                    null
                }
            }
            
            // Sort by createdAt descending nếu không có orderBy từ query
            val sortedReviews = if (reviewsList.isNotEmpty() && reviewsList.firstOrNull()?.createdAt != null) {
                reviewsList.sortedByDescending { it.createdAt?.seconds ?: 0L }
            } else {
                reviewsList
            }
            
            reviewCount = sortedReviews.size
            reviews = sortedReviews
        } catch (e: Exception) {
            android.util.Log.e("ReviewScreen", "Error loading reviews: ${e.message}", e)
            val errorMessage = if (e.message?.contains("index") == true || e.message?.contains("FAILED_PRECONDITION") == true) {
                "Lỗi: Cần tạo index trong Firebase Console. Ứng dụng sẽ hoạt động nhưng có thể chậm hơn."
            } else {
                "Lỗi tải đánh giá: ${e.message}"
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Đánh giá ($reviewCount)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    // Avatar của user hiện tại
                    val currentUser = auth.currentUser
                    val avatarUrl = currentUser?.photoUrl?.toString()
                    val userInitial = currentUser?.displayName?.firstOrNull()?.uppercase() 
                        ?: currentUser?.email?.firstOrNull()?.uppercase() ?: "U"
                    
                    IconButton(onClick = { /* Profile */ }) {
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00BFA5)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInitial,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            // ======= Comment box =======
            OutlinedTextField(
                value = comment,
                onValueChange = { 
                    if (it.text.length <= 900) {
                        comment = it
                    }
                },
                placeholder = { Text("Bạn nghĩ gì về công thức này?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            // Star rating selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đánh giá: ",
                    fontSize = 14.sp,
                    color = Color(0xFF1E1E1E),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                repeat(5) { index ->
                    IconButton(
                        onClick = { selectedRating = index + 1 },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (index < selectedRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Sao ${index + 1}",
                            tint = if (index < selectedRating) Color(0xFFFFB800) else Color(0xFFD3D3D3),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${comment.text.length}/900",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Button(
                    onClick = {
                            if (selectedRating == 0) {
                                Toast.makeText(context, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (comment.text.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập đánh giá", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            scope.launch {
                                try {
                                    val currentUser = auth.currentUser
                                    if (currentUser == null) {
                                        Toast.makeText(context, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show()
                                        return@launch
                                    }
                                    
                                    val userName = currentUser.displayName ?: currentUser.email?.split("@")?.get(0) ?: "Người dùng"
                                    val userAvatarUrl = currentUser.photoUrl?.toString() ?: ""
                                    val data = hashMapOf(
                                        "recipeId" to recipeId,
                                        "userId" to currentUser.uid,
                                        "userName" to userName,
                                        "userEmail" to (currentUser.email ?: ""),
                                        "userAvatarUrl" to userAvatarUrl, // Lưu avatarUrl
                                        "text" to comment.text,
                                        "rating" to selectedRating,
                                        "likes" to 0,
                                        "likedBy" to emptyList<String>(), // Khởi tạo mảng likedBy rỗng
                                        "createdAt" to FieldValue.serverTimestamp()
                                    )
                                    
                                    // Lưu review vào Firestore
                                    db.collection("reviews").add(data).await()
                                    
                                    // Tính toán lại rating trung bình và reviewCount
                                    val allReviewsSnapshot = db.collection("reviews")
                                        .whereEqualTo("recipeId", recipeId)
                                        .get()
                                        .await()
                                    
                                    val newReviewCount = allReviewsSnapshot.size()
                                    
                                    // Tính tổng rating từ tất cả reviews
                                    val totalRating = allReviewsSnapshot.documents.sumOf { doc ->
                                        val data = doc.data
                                        val ratingValue = data?.get("rating")
                                        when (ratingValue) {
                                            is Number -> ratingValue.toDouble() // Giữ nguyên Double để tính chính xác
                                            is Int -> ratingValue.toDouble()
                                            is Long -> ratingValue.toDouble()
                                            is Double -> ratingValue
                                            is Float -> ratingValue.toDouble()
                                            else -> 0.0
                                        }
                                    }
                                    
                                    // Tính rating trung bình và làm tròn đến 1 chữ số thập phân
                                    val averageRating = if (newReviewCount > 0 && totalRating > 0) {
                                        val rawAverage = totalRating / newReviewCount
                                        // Làm tròn đến 1 chữ số thập phân
                                        kotlin.math.round(rawAverage * 10.0) / 10.0
                                    } else {
                                        0.0
                                    }
                                    
                                    android.util.Log.d("ReviewScreen", "Rating calculation: totalRating=$totalRating, reviewCount=$newReviewCount, averageRating=$averageRating")
                                    
                                    // Cập nhật rating và reviewCount trong recipe document
                                    db.collection("userRecipes").document(recipeId)
                                        .update(
                                            mapOf(
                                                "rating" to averageRating,
                                                "reviewCount" to newReviewCount
                                            )
                                        )
                                        .await()
                                    
                                    // Clear form
                                    comment = TextFieldValue("")
                                    selectedRating = 0
                                    
                                    // Reload reviews
                                    val updatedReviewsSnapshot = try {
                                        db.collection("reviews")
                                            .whereEqualTo("recipeId", recipeId)
                                            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                            .get()
                                            .await()
                                    } catch (e: Exception) {
                                        // Nếu lỗi index, thử query không orderBy
                                        if (e.message?.contains("index") == true || e.message?.contains("FAILED_PRECONDITION") == true) {
                                            db.collection("reviews")
                                                .whereEqualTo("recipeId", recipeId)
                                                .get()
                                                .await()
                                        } else {
                                            throw e
                                        }
                                    }
                                    
                                    val updatedReviewsList = updatedReviewsSnapshot.documents.mapNotNull { doc ->
                                        val reviewData = doc.data ?: return@mapNotNull null
                                        try {
                                            val createdAt = reviewData["createdAt"] as? com.google.firebase.Timestamp
                                            val dateStr = if (createdAt != null) {
                                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                                sdf.format(Date(createdAt.seconds * 1000))
                                            } else {
                                                reviewData["date"] as? String ?: ""
                                            }
                                            
                                            val likedBy = reviewData["likedBy"] as? List<*> ?: emptyList<Any>()
                                            val currentUserId = auth.currentUser?.uid ?: ""
                                            val isLiked = currentUserId.isNotEmpty() && likedBy.contains(currentUserId)
                                            
                                            ReviewItem(
                                                id = doc.id,
                                                userName = reviewData["userName"] as? String ?: "Người dùng",
                                                date = dateStr,
                                                text = reviewData["text"] as? String ?: "",
                                                liked = isLiked,
                                                rating = (reviewData["rating"] as? Number)?.toInt() ?: 0,
                                                likes = (reviewData["likes"] as? Number)?.toInt() ?: 0,
                                                userId = reviewData["userId"] as? String ?: "",
                                                createdAt = createdAt,
                                                avatarUrl = reviewData["userAvatarUrl"] as? String
                                            )
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                    
                                    // Sort by createdAt descending nếu không có orderBy từ query
                                    val sortedUpdatedReviews = if (updatedReviewsList.isNotEmpty() && updatedReviewsList.firstOrNull()?.createdAt != null) {
                                        updatedReviewsList.sortedByDescending { it.createdAt?.seconds ?: 0L }
                                    } else {
                                        updatedReviewsList
                                    }
                                    
                                    reviewCount = sortedUpdatedReviews.size
                                    reviews = sortedUpdatedReviews
                                    reloadTrigger++ // Trigger reload để cập nhật UI
                                    
                                    Toast.makeText(context, "Đánh giá đã được gửi!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    android.util.Log.e("ReviewScreen", "Error submitting review: ${e.message}", e)
                                    val errorMessage = if (e.message?.contains("index") == true || e.message?.contains("FAILED_PRECONDITION") == true) {
                                        "Vui lòng tạo index trong Firebase Console. Xem log để biết thêm chi tiết."
                                    } else {
                                        "Lỗi gửi đánh giá: ${e.message}"
                                    }
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Gửi", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ======= Review list =======
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (reviews.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có đánh giá nào",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(reviews) { review ->
                        ReviewCard(
                            review = review, 
                            recipeId = recipeId,
                            onReviewDeleted = { reloadTrigger++ } // Reload khi xóa review
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Button(
                            onClick = { 
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Trở về trang chủ", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ======= Single review item =======
@Composable
fun ReviewCard(
    review: ReviewItem, 
    recipeId: String,
    onReviewDeleted: () -> Unit = {} // Callback khi xóa review
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val currentUser = auth.currentUser
    val currentUserId = currentUser?.uid ?: ""
    
    var liked by remember { mutableStateOf(review.liked) }
    var likesCount by remember { mutableStateOf(review.likes) }
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf(TextFieldValue("")) }
    var isOwner by remember { mutableStateOf(review.userId == currentUserId) }
    var replies by remember { mutableStateOf<List<ReplyItem>>(emptyList()) }
    var isLoadingReplies by remember { mutableStateOf(false) }
    
    // Load likedBy array từ Firestore để check trạng thái like
    LaunchedEffect(review.id, currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                val reviewDoc = db.collection("reviews").document(review.id).get().await()
                val likedBy = reviewDoc.data?.get("likedBy") as? List<*> ?: emptyList<Any>()
                liked = likedBy.contains(currentUserId)
                likesCount = (reviewDoc.data?.get("likes") as? Number)?.toInt() ?: 0
            } catch (e: Exception) {
                android.util.Log.e("ReviewCard", "Error loading like status: ${e.message}", e)
            }
        }
    }
    
    // Load replies cho review này
    LaunchedEffect(review.id) {
        try {
            isLoadingReplies = true
            val repliesSnapshot = try {
                db.collection("reviews")
                    .document(review.id)
                    .collection("replies")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (e: Exception) {
                // Nếu lỗi index, thử query không orderBy và sort trong memory
                if (e.message?.contains("index") == true || e.message?.contains("FAILED_PRECONDITION") == true) {
                    db.collection("reviews")
                        .document(review.id)
                        .collection("replies")
                        .get()
                        .await()
                } else {
                    throw e
                }
            }
            
            val repliesList = repliesSnapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                try {
                    val createdAt = data["createdAt"] as? com.google.firebase.Timestamp
                    ReplyItem(
                        id = doc.id,
                        userId = data["userId"] as? String ?: "",
                        userName = data["userName"] as? String ?: "Người dùng",
                        userAvatarUrl = data["userAvatarUrl"] as? String,
                        text = data["text"] as? String ?: "",
                        createdAt = createdAt
                    )
                } catch (e: Exception) {
                    android.util.Log.e("ReviewCard", "Error parsing reply: ${e.message}", e)
                    null
                }
            }
            
            // Sort by createdAt descending nếu không có orderBy từ query
            replies = if (repliesList.isNotEmpty() && repliesList.firstOrNull()?.createdAt != null) {
                repliesList.sortedByDescending { it.createdAt?.seconds ?: 0L }
            } else {
                repliesList
            }
        } catch (e: Exception) {
            android.util.Log.e("ReviewCard", "Error loading replies: ${e.message}", e)
        } finally {
            isLoadingReplies = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar từ review hoặc placeholder
                    val reviewerAvatarUrl = review.avatarUrl
                    val reviewerInitial = review.userName.firstOrNull()?.uppercase() ?: "U"
                    
                    if (!reviewerAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(reviewerAvatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00BFA5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = reviewerInitial,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(review.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            review.date,
                            color = Color(0xFF00BFA5),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nút xóa (chỉ hiển thị cho người tạo review)
                    if (isOwner) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        // Xóa review
                                        db.collection("reviews").document(review.id).delete().await()
                                        
                                        // Tính lại rating và reviewCount
                                        val allReviewsSnapshot = db.collection("reviews")
                                            .whereEqualTo("recipeId", recipeId)
                                            .get()
                                            .await()
                                        
                                        val newReviewCount = allReviewsSnapshot.size()
                                        val totalRating = allReviewsSnapshot.documents.sumOf { doc ->
                                            val data = doc.data
                                            val ratingValue = data?.get("rating")
                                            when (ratingValue) {
                                                is Number -> ratingValue.toDouble()
                                                is Int -> ratingValue.toDouble()
                                                is Long -> ratingValue.toDouble()
                                                is Double -> ratingValue
                                                is Float -> ratingValue.toDouble()
                                                else -> 0.0
                                            }
                                        }
                                        
                                        val averageRating = if (newReviewCount > 0 && totalRating > 0) {
                                            kotlin.math.round((totalRating / newReviewCount) * 10.0) / 10.0
                                        } else {
                                            0.0
                                        }
                                        
                                        db.collection("userRecipes").document(recipeId)
                                            .update(
                                                mapOf(
                                                    "rating" to averageRating,
                                                    "reviewCount" to newReviewCount
                                                )
                                            )
                                            .await()
                                        
                                        Toast.makeText(context, "Đã xóa đánh giá", Toast.LENGTH_SHORT).show()
                                        onReviewDeleted() // Trigger reload
                                    } catch (e: Exception) {
                                        android.util.Log.e("ReviewCard", "Error deleting review: ${e.message}", e)
                                        Toast.makeText(context, "Lỗi xóa đánh giá: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Xóa",
                                tint = Color(0xFFFF5A5F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Thích",
                        tint = if (liked) Color(0xFFFF5A5F) else Color(0xFFBDBDBD),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable {
                                if (currentUser == null) {
                                    Toast.makeText(context, "Vui lòng đăng nhập để thích đánh giá", Toast.LENGTH_SHORT).show()
                                    return@clickable
                                }
                                
                                val wasLiked = liked // Lưu trạng thái trước khi toggle
                                
                                scope.launch {
                                    try {
                                        val reviewRef = db.collection("reviews").document(review.id)
                                        val reviewDoc = reviewRef.get().await()
                                        val likedBy = (reviewDoc.data?.get("likedBy") as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf<String>()
                                        
                                        if (wasLiked) {
                                            // Unlike: remove userId from likedBy array
                                            likedBy.remove(currentUserId)
                                            reviewRef.update(
                                                mapOf(
                                                    "likes" to FieldValue.increment(-1),
                                                    "likedBy" to likedBy
                                                )
                                            ).await()
                                            liked = false
                                            likesCount--
                                        } else {
                                            // Like: add userId to likedBy array (only if not already liked)
                                            if (!likedBy.contains(currentUserId)) {
                                                likedBy.add(currentUserId)
                                                reviewRef.update(
                                                    mapOf(
                                                        "likes" to FieldValue.increment(1),
                                                        "likedBy" to likedBy
                                                    )
                                                ).await()
                                                liked = true
                                                likesCount++
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ReviewCard", "Error updating likes: ${e.message}", e)
                                        Toast.makeText(context, "Lỗi cập nhật lượt thích", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                review.text,
                color = Color(0xFF1E1E1E),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("😊 ($likesCount)", color = Color(0xFF38427A), fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    // Nút phản hồi
                    if (currentUser != null) {
                        TextButton(
                            onClick = { showReplyInput = !showReplyInput },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Reply,
                                contentDescription = "Phản hồi",
                                tint = Color(0xFF00BFA5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Phản hồi",
                                color = Color(0xFF00BFA5),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Sao",
                            tint = if (index < review.rating) Color(0xFFFFB800) else Color(0xFFD3D3D3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Reply input section
            if (showReplyInput && currentUser != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar người phản hồi
                    val currentUserAvatarUrl = currentUser.photoUrl?.toString()
                    val currentUserInitial = currentUser.displayName?.firstOrNull()?.uppercase() 
                        ?: currentUser.email?.firstOrNull()?.uppercase() ?: "U"
                    
                    if (!currentUserAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentUserAvatarUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00BFA5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUserInitial,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = {
                            if (it.text.length <= 500) {
                                replyText = it
                            }
                        },
                        placeholder = { Text("Viết phản hồi...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 60.dp, max = 120.dp),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 4,
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00BFA5),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (replyText.text.isNotBlank()) {
                                scope.launch {
                                    try {
                                        val replyData = hashMapOf(
                                            "reviewId" to review.id,
                                            "userId" to currentUserId,
                                            "userName" to (currentUser.displayName ?: currentUser.email?.split("@")?.get(0) ?: "Người dùng"),
                                            "userAvatarUrl" to (currentUser.photoUrl?.toString() ?: ""),
                                            "text" to replyText.text,
                                            "createdAt" to FieldValue.serverTimestamp()
                                        )
                                        
                                        // Lưu reply vào subcollection "replies" của review
                                        val replyDoc = db.collection("reviews").document(review.id)
                                            .collection("replies")
                                            .add(replyData)
                                            .await()
                                        
                                        // Thêm reply vào danh sách local
                                        val newReply = ReplyItem(
                                            id = replyDoc.id,
                                            userId = currentUserId,
                                            userName = currentUser.displayName ?: currentUser.email?.split("@")?.get(0) ?: "Người dùng",
                                            userAvatarUrl = currentUser.photoUrl?.toString(),
                                            text = replyText.text,
                                            createdAt = com.google.firebase.Timestamp.now()
                                        )
                                        replies = replies + newReply
                                        
                                        replyText = TextFieldValue("")
                                        showReplyInput = false
                                        Toast.makeText(context, "Đã gửi phản hồi", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("ReviewCard", "Error sending reply: ${e.message}", e)
                                        Toast.makeText(context, "Lỗi gửi phản hồi: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Gửi",
                            tint = Color(0xFF00BFA5),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Hiển thị danh sách replies
            if (replies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))
                
                Column {
                    Text(
                        text = "Phản hồi (${replies.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    replies.forEach { reply ->
                        ReplyItem(
                            reply = reply,
                            reviewId = review.id,
                            currentUserId = currentUserId,
                            onReplyDeleted = {
                                // Reload replies sau khi xóa
                                scope.launch {
                                    try {
                                        val repliesSnapshot = try {
                                            db.collection("reviews")
                                                .document(review.id)
                                                .collection("replies")
                                                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                                                .get()
                                                .await()
                                        } catch (e: Exception) {
                                            if (e.message?.contains("index") == true || e.message?.contains("FAILED_PRECONDITION") == true) {
                                                db.collection("reviews")
                                                    .document(review.id)
                                                    .collection("replies")
                                                    .get()
                                                    .await()
                                            } else {
                                                throw e
                                            }
                                        }
                                        
                                        val repliesList = repliesSnapshot.documents.mapNotNull { doc ->
                                            val data = doc.data ?: return@mapNotNull null
                                            try {
                                                val createdAt = data["createdAt"] as? com.google.firebase.Timestamp
                                                ReplyItem(
                                                    id = doc.id,
                                                    userId = data["userId"] as? String ?: "",
                                                    userName = data["userName"] as? String ?: "Người dùng",
                                                    userAvatarUrl = data["userAvatarUrl"] as? String,
                                                    text = data["text"] as? String ?: "",
                                                    createdAt = createdAt
                                                )
                                            } catch (e: Exception) {
                                                android.util.Log.e("ReviewCard", "Error parsing reply: ${e.message}", e)
                                                null
                                            }
                                        }
                                        
                                        replies = if (repliesList.isNotEmpty() && repliesList.firstOrNull()?.createdAt != null) {
                                            repliesList.sortedByDescending { it.createdAt?.seconds ?: 0L }
                                        } else {
                                            repliesList
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ReviewCard", "Error reloading replies: ${e.message}", e)
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// ======= Reply Item =======
@Composable
fun ReplyItem(
    reply: ReplyItem,
    reviewId: String,
    currentUserId: String,
    onReplyDeleted: () -> Unit
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()
    val isReplyOwner = reply.userId == currentUserId
    
    val replyDate = reply.createdAt?.let {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.format(Date(it.seconds * 1000))
    } ?: ""
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar người phản hồi
        val replyAvatarUrl = reply.userAvatarUrl
        val replyInitial = reply.userName.firstOrNull()?.uppercase() ?: "U"
        
        if (!replyAvatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(replyAvatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00BFA5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = replyInitial,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reply.userName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E1E1E)
                    )
                    if (replyDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = replyDate,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                // Nút xóa (chỉ hiển thị cho người tạo reply)
                if (isReplyOwner && currentUserId.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    db.collection("reviews")
                                        .document(reviewId)
                                        .collection("replies")
                                        .document(reply.id)
                                        .delete()
                                        .await()
                                    
                                    Toast.makeText(context, "Đã xóa phản hồi", Toast.LENGTH_SHORT).show()
                                    onReplyDeleted()
                                } catch (e: Exception) {
                                    android.util.Log.e("ReplyItem", "Error deleting reply: ${e.message}", e)
                                    Toast.makeText(context, "Lỗi xóa phản hồi: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Xóa",
                            tint = Color(0xFFFF5A5F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reply.text,
                fontSize = 13.sp,
                color = Color(0xFF1E1E1E),
                lineHeight = 18.sp
            )
        }
    }
}

// ======= Reply Data Model =======
data class ReplyItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String? = null,
    val text: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)

// ======= Data model =======
data class ReviewItem(
    val id: String = "",
    val userName: String,
    val date: String,
    val text: String,
    val liked: Boolean,
    val rating: Int,
    val likes: Int,
    val userId: String = "",
    val createdAt: com.google.firebase.Timestamp? = null, // Thêm field để sort khi không có index
    val avatarUrl: String? = null // Avatar URL của người đánh giá
)
