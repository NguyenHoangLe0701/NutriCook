package com.example.nutricook.view.hotnews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nutricook.viewmodel.hotnews.HotNewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotNewsDetailScreen(
    articleId: String,
    navController: NavController,
    viewModel: HotNewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val article = remember(uiState.articles, articleId) {
        uiState.articles.find { it.id == articleId }
    }
    
    LaunchedEffect(Unit) {
        if (uiState.articles.isEmpty()) {
            viewModel.loadHotNews()
        }
    }
    
    if (article == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết bài đăng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Favorite */ }) {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Category tag
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color(0xFFDEF7F6), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = article.getTranslatedCategory(),
                    fontSize = 13.sp,
                    color = Color(0xFF20B2AA),
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Title
            Text(
                text = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                color = Color(0xFF1E1E1E)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Author and date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Author avatar
                AsyncImage(
                    model = article.author.avatarUrl ?: "",
                    contentDescription = "Author",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.res.painterResource(com.example.nutricook.R.drawable.news_fastfood)
                )
                
                Text(
                    text = article.author.displayName ?: article.author.email ?: "Anonymous",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                
                Text(
                    text = "•",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                
                Text(
                    text = article.getFormattedDate(),
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Thumbnail image
            if (!article.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = article.thumbnailUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp),
                    error = androidx.compose.ui.res.painterResource(com.example.nutricook.R.drawable.news_fastfood)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Content
            Text(
                text = article.content,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = Color(0xFF1E1E1E),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Link section (if exists)
            article.link?.let { link ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = "Link",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = link,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        TextButton(onClick = { /* TODO: Open link */ }) {
                            Text("Mở", fontWeight = FontWeight.Medium)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

