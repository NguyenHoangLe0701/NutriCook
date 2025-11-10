package com.example.nutricook.view.profile


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nutricook.viewmodel.profile.PostViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPostsScreen(
    onBack: () -> Unit,
    vm: PostViewModel
) {
    val st by vm.state.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Posts") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        if (st.loading && st.items.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(st.items) { post ->
                    PostItem(post)
                }
            }
        }
    }
}