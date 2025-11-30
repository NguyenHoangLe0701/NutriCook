package com.example.nutricook.view.hotnews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import com.example.nutricook.model.hotnews.HotNewsArticle
import com.example.nutricook.viewmodel.hotnews.HotNewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllHotNewsScreen(
    navController: NavController,
    viewModel: HotNewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Reload when screen is first shown
    LaunchedEffect(Unit) {
        viewModel.loadHotNews()
    }
    
    // Reload when coming back from create screen
    // Check savedStateHandle for reload flag
    val backStackEntry = navController.currentBackStackEntry
    val shouldReload = remember(backStackEntry?.id) {
        backStackEntry?.savedStateHandle?.get<Boolean>("shouldReload") ?: false
    }
    
    LaunchedEffect(shouldReload) {
        if (shouldReload) {
            viewModel.loadHotNews()
            backStackEntry?.savedStateHandle?.remove<Boolean>("shouldReload")
        }
    }
    
    LaunchedEffect(searchQuery) {
        viewModel.search(searchQuery)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hot News", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    IconButton(onClick = { navController.navigate("create_hot_news") }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tạo bài đăng"
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
                .background(Color(0xFFF8F9FA))
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm kiếm bài đăng...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            
            // Filter chips
            if (uiState.selectedCategory != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.filterByCategory(null) },
                        label = { Text(uiState.selectedCategory ?: "") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove filter"
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Articles list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredArticles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotBlank() || uiState.selectedCategory != null) {
                            "Không tìm thấy bài đăng nào"
                        } else {
                            "Chưa có bài đăng nào"
                        },
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredArticles) { article ->
                        HotNewsCard(
                            article = article,
                            onClick = {
                                navController.navigate("hot_news_detail/${article.id}")
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        FilterDialog(
            availableCategories = viewModel.getAvailableCategories(),
            selectedCategory = uiState.selectedCategory,
            onDismiss = { showFilterDialog = false },
            onCategorySelected = { category ->
                viewModel.filterByCategory(category)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun HotNewsCard(
    article: HotNewsArticle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            AsyncImage(
                model = article.thumbnailUrl ?: "",
                contentDescription = article.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
                error = androidx.compose.ui.res.painterResource(com.example.nutricook.R.drawable.news_fastfood)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                // Category tag
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDEF7F6), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = article.category,
                        color = Color(0xFF20B2AA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Title
                Text(
                    text = article.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1B1B),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Author and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Author info (avatar + name)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Author avatar
                        AsyncImage(
                            model = article.author.avatarUrl ?: "",
                            contentDescription = "Author",
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = androidx.compose.ui.res.painterResource(com.example.nutricook.R.drawable.news_fastfood)
                        )
                        
                        Text(
                            text = article.author.displayName ?: article.author.email ?: "Anonymous",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    
                    // Date
                    Text(
                        text = article.getFormattedDate(),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDialog(
    availableCategories: List<String>,
    selectedCategory: String?,
    onDismiss: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lọc theo danh mục") },
        text = {
            Column {
                // Option: All categories
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("Tất cả") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Category options
                availableCategories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

