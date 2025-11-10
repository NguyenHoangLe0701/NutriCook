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
import com.example.nutricook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavController) {
    var comment by remember { mutableStateOf(TextFieldValue("")) }

    // Fake data
    val reviews = remember {
        listOf(
            ReviewItem(
                userName = "Allrecipes Member",
                date = "07/09/2022",
                text = "I would recommend adding the vegetables in until the last three hours of cooking time. That way they won’t turn to mush.",
                liked = true,
                rating = 4,
                likes = 10
            ),
            ReviewItem(
                userName = "seany42o1",
                date = "06/12/2022",
                text = "The corned beef needs to be on low and then 9 hours works. Great recipe, I made it without the beer.",
                liked = false,
                rating = 4,
                likes = 5
            ),
            ReviewItem(
                userName = "vicki936",
                date = "03/22/2022",
                text = "I bought a corned beef brisket at Costco and didn’t want to toil with stove top directions so I found this crock pot recipe.",
                liked = false,
                rating = 4,
                likes = 5
            ),
            ReviewItem(
                userName = "John Shifflett",
                date = "04/02/2022",
                text = "Perfect recipe, made this numerous times and it always comes out delicious.",
                liked = false,
                rating = 4,
                likes = 5
            )
        )
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
                    onClick = { /* handle send */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Send", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ======= Review list =======
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(reviews) { review ->
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
