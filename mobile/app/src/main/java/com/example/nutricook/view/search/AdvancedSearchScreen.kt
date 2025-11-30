package com.example.nutricook.view.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nutricook.model.search.SearchResult
import com.example.nutricook.model.search.SearchType
import com.example.nutricook.viewmodel.search.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tìm kiếm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.Black
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
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBarSection(
                query = uiState.query,
                onQueryChange = viewModel::onQueryChange,
                onFilterClick = viewModel::toggleFilters,
                showFilters = uiState.showFilters
            )
            
            // Filter Chips
            if (uiState.showFilters) {
                FilterSection(
                    selectedTypes = uiState.selectedTypes,
                    onTypeToggle = viewModel::toggleSearchType
                )
            }
            
            // Recent Searches (when query is empty)
            if (uiState.query.isBlank() && uiState.recentSearches.isNotEmpty()) {
                RecentSearchesSection(
                    recentSearches = uiState.recentSearches,
                    onSearchClick = viewModel::selectRecentSearch,
                    onClearClick = viewModel::clearRecentSearches
                )
            }
            
            // Results
            if (uiState.query.isNotBlank()) {
                SearchResultsSection(
                    results = uiState.results,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onResultClick = { result ->
                        // Navigate based on result type
                        when (result) {
                            is SearchResult.RecipeResult -> {
                                navController.navigate("recipe_detail/${result.title}")
                            }
                            is SearchResult.FoodResult -> {
                                // Navigate to food detail or add meal
                                navController.navigate("add_meal")
                            }
                            is SearchResult.NewsResult -> {
                                navController.navigate("article_detail")
                            }
                            is SearchResult.UserResult -> {
                                // Navigate to user profile
                                navController.navigate("profile")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SearchBarSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    showFilters: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Tìm kiếm",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Tìm kiếm công thức, thực phẩm, tin tức...",
                        color = Color.Gray
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Xóa",
                        tint = Color.Gray
                    )
                }
            }
            
            IconButton(
                onClick = onFilterClick,
                modifier = Modifier.background(
                    if (showFilters) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    CircleShape
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Lọc",
                    tint = if (showFilters) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedTypes: Set<SearchType>,
    onTypeToggle: (SearchType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Loại tìm kiếm",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedTypes.contains(SearchType.RECIPES),
                onClick = { onTypeToggle(SearchType.RECIPES) },
                label = { Text("Công thức") },
                leadingIcon = if (selectedTypes.contains(SearchType.RECIPES)) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            
            FilterChip(
                selected = selectedTypes.contains(SearchType.FOODS),
                onClick = { onTypeToggle(SearchType.FOODS) },
                label = { Text("Thực phẩm") },
                leadingIcon = if (selectedTypes.contains(SearchType.FOODS)) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            
            FilterChip(
                selected = selectedTypes.contains(SearchType.NEWS),
                onClick = { onTypeToggle(SearchType.NEWS) },
                label = { Text("Tin tức") },
                leadingIcon = if (selectedTypes.contains(SearchType.NEWS)) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            
            FilterChip(
                selected = selectedTypes.contains(SearchType.USERS),
                onClick = { onTypeToggle(SearchType.USERS) },
                label = { Text("Người dùng") },
                leadingIcon = if (selectedTypes.contains(SearchType.USERS)) {
                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
fun RecentSearchesSection(
    recentSearches: List<String>,
    onSearchClick: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tìm kiếm gần đây",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            
            TextButton(onClick = onClearClick) {
                Text("Xóa tất cả", fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            recentSearches.take(5).forEach { search ->
                FilterChip(
                    selected = false,
                    onClick = { onSearchClick(search) },
                    label = { Text(search) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.History,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SearchResultsSection(
    results: Map<SearchType, List<SearchResult>>,
    isLoading: Boolean,
    error: String?,
    onResultClick: (SearchResult) -> Unit
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (error != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color.Gray
                )
            }
        }
        return
    }
    
    val allResultsEmpty = results.values.all { it.isEmpty() }
    
    if (allResultsEmpty) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Không tìm thấy kết quả",
                    color = Color.Gray
                )
            }
        }
        return
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Recipes
        if (results[SearchType.RECIPES]?.isNotEmpty() == true) {
            item {
                SectionHeader("Công thức nấu ăn")
            }
            items(results[SearchType.RECIPES]!!) { result ->
                if (result is SearchResult.RecipeResult) {
                    RecipeResultItem(result, onClick = { onResultClick(result) })
                }
            }
        }
        
        // Foods
        if (results[SearchType.FOODS]?.isNotEmpty() == true) {
            item {
                SectionHeader("Thực phẩm")
            }
            items(results[SearchType.FOODS]!!) { result ->
                if (result is SearchResult.FoodResult) {
                    FoodResultItem(result, onClick = { onResultClick(result) })
                }
            }
        }
        
        // News
        if (results[SearchType.NEWS]?.isNotEmpty() == true) {
            item {
                SectionHeader("Tin tức")
            }
            items(results[SearchType.NEWS]!!) { result ->
                if (result is SearchResult.NewsResult) {
                    NewsResultItem(result, onClick = { onResultClick(result) })
                }
            }
        }
        
        // Users
        if (results[SearchType.USERS]?.isNotEmpty() == true) {
            item {
                SectionHeader("Người dùng")
            }
            items(results[SearchType.USERS]!!) { result ->
                if (result is SearchResult.UserResult) {
                    UserResultItem(result, onClick = { onResultClick(result) })
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

