package com.example.nutricook.view.recipes

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.nutricook.R
import com.example.nutricook.viewmodel.CategoriesViewModel
import com.example.nutricook.viewmodel.CreateRecipeViewModel
import com.example.nutricook.utils.IngredientUnit

data class IngredientItem(
    val name: String = "",
    val quantity: String = "", // Số lượng
    val unit: com.example.nutricook.utils.IngredientUnit = com.example.nutricook.utils.IngredientUnit.GRAMS, // Đơn vị
    val foodItemId: Long? = null, // ID của foodItem từ database
    val categoryId: Long? = null // ID của category
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeStep1Screen(
    navController: NavController,
    createRecipeViewModel: CreateRecipeViewModel,
    categoriesViewModel: CategoriesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Lấy dữ liệu từ ViewModel
    val recipeState by createRecipeViewModel.state.collectAsState()
    
    // State variables - khôi phục từ ViewModel nếu có
    var recipeName by remember { mutableStateOf(recipeState.recipeName) }
    var estimatedTime by remember { mutableStateOf(recipeState.estimatedTime) }
    var servings by remember { mutableStateOf(recipeState.servings) }
    var selectedImageUris by remember { mutableStateOf(recipeState.selectedImageUris) }
    var ingredients by remember { mutableStateOf(
        if (recipeState.ingredients.isNotEmpty()) recipeState.ingredients else listOf(IngredientItem())
    ) }
    
    // Khôi phục dữ liệu từ ViewModel khi màn hình được tạo
    LaunchedEffect(Unit) {
        if (recipeState.recipeName.isNotEmpty() || recipeState.estimatedTime.isNotEmpty() || 
            recipeState.servings.isNotEmpty() || recipeState.selectedImageUris.isNotEmpty() || 
            recipeState.ingredients.isNotEmpty()) {
            recipeName = recipeState.recipeName
            estimatedTime = recipeState.estimatedTime
            servings = recipeState.servings
            selectedImageUris = recipeState.selectedImageUris
            ingredients = if (recipeState.ingredients.isNotEmpty()) recipeState.ingredients else listOf(IngredientItem())
        }
    }
    
    // Lưu dữ liệu tự động vào ViewModel khi có thay đổi (debounce để tránh lưu quá nhiều)
    LaunchedEffect(recipeName, estimatedTime, servings, selectedImageUris, ingredients) {
        // Chỉ lưu nếu có ít nhất một trường đã được nhập
        if (recipeName.isNotBlank() || estimatedTime.isNotBlank() || servings.isNotBlank() || 
            selectedImageUris.isNotEmpty() || ingredients.any { it.name.isNotBlank() || it.quantity.isNotBlank() }) {
            createRecipeViewModel.setStep1Data(
                recipeName = recipeName,
                estimatedTime = estimatedTime,
                servings = servings,
                selectedImageUris = selectedImageUris,
                ingredients = ingredients
            )
        }
    }
    
    // Categories và FoodItems từ ViewModel
    val categories by categoriesViewModel.categories.collectAsState()
    val foodItems by categoriesViewModel.foodItems.collectAsState()
    
    // Map để tìm foodItem theo ID
    val foodItemsMap = remember(foodItems) {
        foodItems.associateBy { it.id }
    }
    
    // Multiple image picker launcher (Android 13+)
    val multipleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedImageUris = selectedImageUris + uris
        }
    }
    
    // Single image picker launcher (fallback for older Android - có thể gọi nhiều lần)
    val singleImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUris = selectedImageUris + it
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        /** 🔹 Header */
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color(0xFF00BFA5).copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                modifier = Modifier.size(24.dp),
                                tint = Color(0xFF00BFA5)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bước 1: Thông tin cơ bản",
                            fontSize = 16.sp, // Thu nhỏ từ 20.sp xuống 16.sp
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1C1E),
                            maxLines = 1, // Không cho xuống dòng
                            overflow = TextOverflow.Ellipsis // Cắt bớt nếu quá dài
                        )
                        Text(
                            text = "Nhập thông tin về món ăn",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1, // Không cho xuống dòng
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Step indicator
                    Surface(
                        color = Color(0xFF00BFA5),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "1/4",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        /** 🔹 Tên món ăn */
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Color(0xFF00BFA5),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tên món ăn *",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1C1E)
                        )
                    }
                    OutlinedTextField(
                        value = recipeName,
                        onValueChange = { recipeName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nhập tên món ăn", fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00BFA5),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }
            }
        }
        
        /** 🔹 Thời gian dự kiến & Số phần ăn */
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF00BFA5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Thời gian *",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                        OutlinedTextField(
                            value = estimatedTime,
                            onValueChange = { newTime ->
                                // Không cho phép nhập số âm
                                val filteredTime = if (newTime.startsWith("-")) {
                                    estimatedTime // Giữ nguyên giá trị cũ nếu nhập dấu trừ
                                } else {
                                    newTime
                                }
                                estimatedTime = filteredTime
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("VD: 30 phút", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00BFA5),
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 1.dp,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = Color(0xFF00BFA5),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Số phần *",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                        OutlinedTextField(
                            value = servings,
                            onValueChange = { newServings ->
                                // Không cho phép nhập số âm
                                val filteredServings = if (newServings.startsWith("-")) {
                                    servings // Giữ nguyên giá trị cũ nếu nhập dấu trừ
                                } else {
                                    newServings
                                }
                                servings = filteredServings
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("VD: 4 người", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00BFA5),
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )
                    }
                }
            }
        }
        
        /** 🔹 Ảnh món ăn (Multiple) */
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color(0xFF00BFA5),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ảnh món ăn",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                        if (selectedImageUris.isNotEmpty()) {
                            Surface(
                                color = Color(0xFF00BFA5).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "${selectedImageUris.size} ảnh",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF00BFA5),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    // Button chọn ảnh
                    Button(
                        onClick = {
                            // Try multiple picker first (Android 13+)
                            try {
                                multipleImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } catch (e: Exception) {
                                // Fallback to single picker (có thể chọn nhiều lần)
                                singleImagePickerLauncher.launch("image/*")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00BFA5),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoLibrary,
                            contentDescription = "Chọn ảnh",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chọn ảnh từ bộ sưu tập",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Grid hiển thị ảnh đã chọn
                if (selectedImageUris.isNotEmpty()) {
                    val rows = (selectedImageUris.size + 2) / 3 // Làm tròn lên
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height((rows * 120).dp)
                    ) {
                        itemsIndexed(selectedImageUris) { index, uri ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Ảnh ${index + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Delete button
                                IconButton(
                                    onClick = {
                                        selectedImageUris = selectedImageUris.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            RoundedCornerShape(bottomStart = 12.dp)
                                        )
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Placeholder khi chưa có ảnh
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8F9FA))
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFF9CA3AF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Chưa có ảnh",
                                color = Color(0xFF6B7280),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                }
            }
        }
        
        /** 🔹 Nguyên liệu */
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color(0xFF00BFA5),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nguyên liệu *",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1C1C1E)
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                ingredients = ingredients + IngredientItem()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF00BFA5)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00BFA5))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Thêm",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thêm", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    // Danh sách nguyên liệu
                    ingredients.forEachIndexed { index, ingredient ->
                        var expandedFoodItem by remember { mutableStateOf(false) }
                        var expandedUnit by remember { mutableStateOf(false) }
                        var expandedCategory by remember { mutableStateOf(false) }
                        
                        // Lấy danh sách foodItems theo category đã chọn của ingredient này
                        val availableFoodItems = remember(ingredient.categoryId, foodItems) {
                            foodItems // foodItems đã được lọc theo category trong ViewModel
                        }
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            color = Color(0xFFF8F9FA),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Dropdown chọn danh mục
                                    ExposedDropdownMenuBox(
                                        expanded = expandedCategory,
                                        onExpandedChange = { expandedCategory = !expandedCategory },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = categories.find { it.id == ingredient.categoryId }?.name ?: "Chọn danh mục",
                                            onValueChange = {},
                                            readOnly = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(),
                                            placeholder = { Text("Danh mục", fontSize = 13.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF00BFA5),
                                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedCategory,
                                            onDismissRequest = { expandedCategory = false }
                                        ) {
                                            categories.forEach { category ->
                                                DropdownMenuItem(
                                                    text = { Text("${category.icon} ${category.name}") },
                                                    onClick = {
                                                        categoriesViewModel.selectCategory(category.id)
                                                        ingredients = ingredients.mapIndexed { i, item ->
                                                            if (i == index) {
                                                                item.copy(
                                                                    categoryId = category.id,
                                                                    foodItemId = null, // Reset foodItem khi đổi category
                                                                    name = ""
                                                                )
                                                            } else item
                                                        }
                                                        expandedCategory = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Nút xóa
                                    IconButton(
                                        onClick = {
                                            if (ingredients.size > 1) {
                                                ingredients = ingredients.filterIndexed { i, _ -> i != index }
                                            } else {
                                                ingredients = listOf(IngredientItem())
                                            }
                                        },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                Color(0xFFFEE2E2),
                                                RoundedCornerShape(10.dp)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Xóa",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                
                                // TextField với autocomplete để chọn hoặc nhập nguyên liệu
                                var searchQuery by remember(ingredient.name) { mutableStateOf(ingredient.name) }
                                
                                // Đồng bộ searchQuery khi ingredient.name thay đổi từ bên ngoài
                                LaunchedEffect(ingredient.name) {
                                    if (searchQuery != ingredient.name) {
                                        searchQuery = ingredient.name
                                    }
                                }
                                
                                // Lọc danh sách foodItems dựa trên search query
                                val filteredFoodItems = remember(searchQuery, foodItems) {
                                    if (searchQuery.isBlank()) {
                                        foodItems
                                    } else {
                                        foodItems.filter { 
                                            it.name.contains(searchQuery, ignoreCase = true) 
                                        }
                                    }
                                }
                                
                                ExposedDropdownMenuBox(
                                    expanded = expandedFoodItem && filteredFoodItems.isNotEmpty(),
                                    onExpandedChange = { expandedFoodItem = it },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { newValue ->
                                            searchQuery = newValue
                                            expandedFoodItem = newValue.isNotBlank() && filteredFoodItems.isNotEmpty()
                                            // Cập nhật ingredient name khi gõ
                                            val matchedFoodItem = filteredFoodItems.firstOrNull { it.name.equals(newValue, ignoreCase = true) }
                                            val suggestedUnit = if (newValue.isNotBlank()) {
                                                IngredientUnit.getDefaultUnit(newValue)
                                            } else {
                                                ingredient.unit
                                            }
                                            ingredients = ingredients.mapIndexed { i, item ->
                                                if (i == index) {
                                                    item.copy(
                                                        name = newValue,
                                                        foodItemId = matchedFoodItem?.id,
                                                        unit = if (matchedFoodItem != null) suggestedUnit else item.unit
                                                    )
                                                } else item
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        placeholder = { Text("Tên nguyên liệu (gõ để tìm kiếm)", fontSize = 13.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BFA5),
                                            unfocusedBorderColor = Color(0xFFE5E7EB),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        ),
                                        enabled = ingredient.categoryId != null,
                                        trailingIcon = {
                                            if (searchQuery.isNotBlank()) {
                                                IconButton(
                                                    onClick = {
                                                        searchQuery = ""
                                                        ingredients = ingredients.mapIndexed { i, item ->
                                                            if (i == index) item.copy(name = "", foodItemId = null) else item
                                                        }
                                                        expandedFoodItem = false
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Xóa",
                                                        modifier = Modifier.size(18.dp),
                                                        tint = Color(0xFF6B7280)
                                                    )
                                                }
                                            } else {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFoodItem)
                                            }
                                        }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedFoodItem && filteredFoodItems.isNotEmpty(),
                                        onDismissRequest = { expandedFoodItem = false }
                                    ) {
                                        if (ingredient.categoryId != null) {
                                            // Hiển thị tối đa 5 kết quả đề xuất
                                            filteredFoodItems.take(5).forEach { foodItem ->
                                                DropdownMenuItem(
                                                    text = { Text(foodItem.name) },
                                                    onClick = {
                                                        searchQuery = foodItem.name
                                                        // Sử dụng unit từ foodItem, nếu không có thì đề xuất dựa trên tên
                                                        val unitFromFood = try {
                                                            IngredientUnit.entries.firstOrNull { 
                                                                it.abbreviation.equals(foodItem.unit, ignoreCase = true) 
                                                            } ?: IngredientUnit.getDefaultUnit(foodItem.name)
                                                        } catch (e: Exception) {
                                                            IngredientUnit.getDefaultUnit(foodItem.name)
                                                        }
                                                        ingredients = ingredients.mapIndexed { i, item ->
                                                            if (i == index) {
                                                                item.copy(
                                                                    name = foodItem.name,
                                                                    foodItemId = foodItem.id,
                                                                    categoryId = foodItem.categoryId,
                                                                    unit = unitFromFood
                                                                )
                                                            } else item
                                                        }
                                                        expandedFoodItem = false
                                                    }
                                                )
                                            }
                                            // Nếu có nhiều hơn 5 kết quả, hiển thị thông báo
                                            if (filteredFoodItems.size > 5) {
                                                DropdownMenuItem(
                                                    text = { 
                                                        Text(
                                                            "... và ${filteredFoodItems.size - 5} kết quả khác. Gõ thêm để lọc",
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF6B7280)
                                                        ) 
                                                    },
                                                    onClick = { },
                                                    enabled = false
                                                )
                                            }
                                        } else {
                                            DropdownMenuItem(
                                                text = { Text("Vui lòng chọn danh mục trước", fontSize = 12.sp) },
                                                onClick = { expandedFoodItem = false },
                                                enabled = false
                                            )
                                        }
                                    }
                                }
                                
                                // Hàng 1: Số lượng và Đơn vị
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Số lượng
                                    OutlinedTextField(
                                        value = ingredient.quantity,
                                        onValueChange = { newQuantity ->
                                            // Không cho phép nhập số âm
                                            val filteredQuantity = if (newQuantity.startsWith("-")) {
                                                ingredient.quantity // Giữ nguyên giá trị cũ nếu nhập dấu trừ
                                            } else {
                                                newQuantity
                                            }
                                            ingredients = ingredients.mapIndexed { i, item ->
                                                if (i == index) item.copy(quantity = filteredQuantity) else item
                                            }
                                        },
                                        modifier = Modifier.weight(1.2f),
                                        placeholder = { Text("VD: 100, 1/2, 2/3", fontSize = 13.sp) },
                                        supportingText = {
                                            if (ingredient.quantity.isBlank()) {
                                                Text(
                                                    text = "Có thể nhập số hoặc phân số",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF6B7280)
                                                )
                                            }
                                        },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00BFA5),
                                            unfocusedBorderColor = Color(0xFFE5E7EB),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                    
                                    // Autocomplete chọn đơn vị
                                    var unitSearchQuery by remember(ingredient.unit.abbreviation) { mutableStateOf(ingredient.unit.abbreviation) }
                                    
                                    // Đồng bộ unitSearchQuery khi ingredient.unit thay đổi
                                    LaunchedEffect(ingredient.unit.abbreviation) {
                                        if (unitSearchQuery != ingredient.unit.abbreviation) {
                                            unitSearchQuery = ingredient.unit.abbreviation
                                        }
                                    }
                                    
                                    val filteredUnits = remember(unitSearchQuery) {
                                        if (unitSearchQuery.isBlank()) {
                                            IngredientUnit.entries
                                        } else {
                                            IngredientUnit.entries.filter { 
                                                it.abbreviation.contains(unitSearchQuery, ignoreCase = true) ||
                                                it.displayName.contains(unitSearchQuery, ignoreCase = true)
                                            }
                                        }
                                    }
                                    
                                    ExposedDropdownMenuBox(
                                        expanded = expandedUnit && filteredUnits.isNotEmpty(),
                                        onExpandedChange = { expandedUnit = it },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        OutlinedTextField(
                                            value = unitSearchQuery,
                                            onValueChange = { newValue ->
                                                unitSearchQuery = newValue
                                                expandedUnit = newValue.isNotBlank() && filteredUnits.isNotEmpty()
                                                // Tìm unit khớp
                                                val matchedUnit = filteredUnits.firstOrNull { 
                                                    it.abbreviation.equals(newValue, ignoreCase = true) ||
                                                    it.displayName.equals(newValue, ignoreCase = true)
                                                }
                                                if (matchedUnit != null) {
                                                    ingredients = ingredients.mapIndexed { i, item ->
                                                        if (i == index) item.copy(unit = matchedUnit) else item
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(),
                                            placeholder = { Text("Đơn vị (gõ để tìm)", fontSize = 13.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF00BFA5),
                                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            ),
                                            trailingIcon = {
                                                if (unitSearchQuery.isNotBlank()) {
                                                    IconButton(
                                                        onClick = {
                                                            unitSearchQuery = ingredient.unit.abbreviation
                                                            expandedUnit = false
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Xóa",
                                                            modifier = Modifier.size(18.dp),
                                                            tint = Color(0xFF6B7280)
                                                        )
                                                    }
                                                } else {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit)
                                                }
                                            }
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expandedUnit && filteredUnits.isNotEmpty(),
                                            onDismissRequest = { expandedUnit = false }
                                        ) {
                                            filteredUnits.take(5).forEach { unit ->
                                                DropdownMenuItem(
                                                    text = { Text("${unit.abbreviation} - ${unit.displayName}") },
                                                    onClick = {
                                                        unitSearchQuery = unit.abbreviation
                                                        ingredients = ingredients.mapIndexed { i, item ->
                                                            if (i == index) item.copy(unit = unit) else item
                                                        }
                                                        expandedUnit = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        /** 🔹 Nút Tiếp theo */
        item {
            Button(
                onClick = {
                    // Validation
                    if (recipeName.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập tên món ăn", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (estimatedTime.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập thời gian dự kiến", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (servings.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập số phần ăn", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    // Filter ingredients: chỉ lưu những ingredient có đầy đủ thông tin (name, quantity, foodItemId)
                    val validIngredients = ingredients.filter { 
                        it.name.isNotBlank() && 
                        it.quantity.isNotBlank() && 
                        it.foodItemId != null 
                    }
                    if (validIngredients.isEmpty()) {
                        Toast.makeText(context, "Vui lòng nhập ít nhất một nguyên liệu hợp lệ (có tên, số lượng và chọn từ danh sách)", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    // Log để debug
                    android.util.Log.d("Step1", "Saving ${validIngredients.size} valid ingredients:")
                    validIngredients.forEachIndexed { index, ing ->
                        android.util.Log.d("Step1", "[$index] name='${ing.name}', quantity='${ing.quantity}', foodItemId=${ing.foodItemId}, unit=${ing.unit}")
                    }
                    
                    // Lưu dữ liệu vào ViewModel trước khi chuyển bước
                    createRecipeViewModel.setStep1Data(
                        recipeName = recipeName,
                        estimatedTime = estimatedTime,
                        servings = servings,
                        selectedImageUris = selectedImageUris,
                        ingredients = validIngredients
                    )
                    
                    // Chuyển sang bước 2
                    navController.navigate("create_recipe_step2")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BFA5),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp
                )
            ) {
                Text(
                    text = "Tiếp theo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Tiếp theo",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

