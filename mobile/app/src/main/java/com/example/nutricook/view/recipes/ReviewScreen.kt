package com.example.nutricook.view.recipes

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavController, queryVM: QueryViewModel = hiltViewModel()) {
    var comment by remember { mutableStateOf(TextFieldValue("")) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    // Load reviews from Firestore
    val firebaseReviews by queryVM.reviews
    val isLoading by queryVM.isLoading
    val error by queryVM.error

    // Fetch reviews on first load
    LaunchedEffect(Unit) {
        scope.launch {
            queryVM.loadReviews()
        }
    }

    // Convert Map to ReviewItem for display, fallback to sample data if empty
    val displayReviews = if (firebaseReviews.isNotEmpty()) {
        firebaseReviews.mapNotNull { map ->
            try {
                ReviewItem(
                    userName = map["userName"] as? String ?: "Anonymous",
                    date = map["date"] as? String ?: "",
                    text = map["text"] as? String ?: "",
                    liked = false,
                    rating = (map["rating"] as? Number)?.toInt() ?: 0,
                    likes = (map["likes"] as? Number)?.toInt() ?: 0
                )
            } catch (e: Exception) {
                null
            }
        }
    } else {
        com.example.nutricook.data.SampleData.reviews
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reviews (1,522)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
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
                onValueChange = { comment = it },
                placeholder = { Text("What did you think about this recipe?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

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
                        scope.launch {
                            try {
                                val userName = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Anonymous"
                                val data = mapOf(
                                    "userName" to userName,
                                    "text" to comment.text,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                // write to collection 'reviews'
                                db.collection("reviews").add(data).await()
                                // clear text on success
                                comment = TextFieldValue("")
                            } catch (e: Exception) {
                                // optionally handle error (log / show snackbar)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Send", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ======= Review list =======
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $error", color = Color.Red)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(displayReviews) { review ->
                        ReviewCard(review)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Button(
                            onClick = { /* Load more */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Load more review", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ======= Single review item =======
@Composable
fun ReviewCard(review: ReviewItem) {
    var liked by remember { mutableStateOf(review.liked) }

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
                    Image(
                        painter = painterResource(id = R.drawable.avatar_sample),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(review.userName, fontWeight = FontWeight.Bold)
                        Text(
                            review.date,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Icon(
                    imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (liked) Color(0xFFFF5A5F) else Color(0xFFBDBDBD),
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { liked = !liked }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                review.text,
                color = Color(0xFF1E1E1E),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("😊 (${review.likes})", color = Color(0xFF38427A), fontSize = 13.sp)
                Row {
                    repeat(5) { index ->
                        Icon(
                            painter = painterResource(id = R.drawable.star13),
                            contentDescription = "Star",
                            tint = if (index < review.rating) Color(0xFFFFB800) else Color(0xFFD3D3D3),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ======= Data model =======
data class ReviewItem(
    val userName: String,
    val date: String,
    val text: String,
    val liked: Boolean,
    val rating: Int,
    val likes: Int
)
