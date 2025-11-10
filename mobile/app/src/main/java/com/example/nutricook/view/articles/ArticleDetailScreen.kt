package com.example.nutricook.view.articles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.nutricook.R

@Composable
fun ArticleDetailScreen(navController: NavController) {
    val otherPosts = listOf(
        ArticleItem(
            title = "You Can Now Try the Hottest Sauce From the...",
            category = "Food News and Trends",
            author = "Courtney Kassel",
            date = "Sept 13, 2022",
            image = R.drawable.news_fastfood
        ),
        ArticleItem(
            title = "8 5-Ingredient Breakfast Sandwiches for Easy...",
            category = "Kitchen Tips",
            author = "Sara Tane",
            date = "Sept 18, 2022",
            image = R.drawable.news_hambuger
        ),
        ArticleItem(
            title = "How to Make Pizza Chips — TikTok's New It Snack",
            category = "Food News and Trends",
            author = "Alice Knisley",
            date = "Sept 22, 2022",
            image = R.drawable.news_pizza
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ==== Header ====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = Color(0xFF9CA3AF)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ==== Category ====
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .background(Color(0xFFDEF7F6), shape = RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Food News And Trends",
                fontSize = 13.sp,
                color = Color(0xFF00AFA2),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ==== Title ====
        Text(
            text = "Don't Have Muffin Liners? This TikTok Hack Will Save the Day",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFF1E1E1E)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ==== Author ====
        Text(
            text = "By Courtney Kassel",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ==== Image ====
        Image(
            painter = painterResource(id = R.drawable.sample_muffin),
            contentDescription = "Muffin Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Photo: Mari Carmen Martinez/ Getty Images",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ==== Body ====
        Text(
            text = "Have you ever been baking, about to scoop your batter into the muffin tin, and realize you have no paper liners? You can always grease the pan, but doing each individual divot takes time and if you don’t have nonstick spray, this process can get very tedious very fast. Plus, those bakery muffins look so appealing with their little paper wrappers.",
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = Color(0xFF1E1E1E),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))) {
                    append("How to Make Baking Pan Liners Out of Parchment Paper\n")
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF00AFA2))) {
                    append("To Line Muffin Tins\n")
                }
                append("To start, simply cut a piece of parchment paper (not wax paper!) into about 4- to 5-inch squares for regular-sized muffin cups...")
            },
            fontSize = 15.sp,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==== Link Share ====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF8F8F8), shape = RoundedCornerShape(10.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "https://bom.so/28UhW8",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Button(
                onClick = { /* Copy link */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3AC7BF)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Copy", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==== Other Posts ====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Other Posts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                text = "View all",
                color = Color(0xFF00AFA2),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .padding(horizontal = 16.dp)
        ) {
            items(otherPosts) { post ->
                OtherPostItem(
                    post = post,
                    navController = navController
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun OtherPostItem(post: ArticleItem,navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("article_detail") }
            .background(Color.White, shape = RoundedCornerShape(12.dp))
    ) {
        Image(
            painter = painterResource(id = post.image),
            contentDescription = "Post image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFDEF7F6), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    post.category,
                    color = Color(0xFF00AFA2),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                post.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF1E1E1E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_sample),
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(post.author, fontSize = 12.sp, color = Color.Gray)
                }
                Text(post.date, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

data class ArticleItem(
    val title: String,
    val category: String,
    val author: String,
    val date: String,
    val image: Int
)
