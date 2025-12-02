# 🔍 Hướng Dẫn Triển Khai Tính Năng Tìm Kiếm Nâng Cao

## 📋 Tổng Quan

Tính năng tìm kiếm nâng cao cho phép người dùng tìm kiếm đa dạng các loại nội dung trong ứng dụng NutriCook ngay trên màn hình Home:

### 🔍 Các Loại Nội Dung Có Thể Tìm Kiếm:

1. **🍳 Recipes (Công thức nấu ăn)**
   - Tìm kiếm từ collection `recipes` trong Firestore
   - Tìm theo tên công thức (ví dụ: "Gà chiên nước mắm", "Cá hấp bia")
   - Hiển thị: Tên, hình ảnh, calories (nếu có)
   - Click để xem chi tiết công thức

2. **🥬 Food Items (Thực phẩm và giá trị dinh dưỡng)**
   - Tìm kiếm từ collection `foodItems` trong Firestore
   - Tìm theo tên thực phẩm (ví dụ: "Bắp cải trắng", "Dứa", "Sầu riêng")
   - Hỗ trợ tìm kiếm đa từ: "Bắp cải trắng" sẽ tìm items chứa cả "bắp", "cải", "trắng"
   - Hiển thị: Tên, hình ảnh, calories, protein, fat, carb
   - Click để thêm vào bữa ăn

3. **📰 Hot News (Tin tức dinh dưỡng)**
   - Tìm kiếm từ `HotNewsRepository` (Firestore collection `hotNews`)
   - Tìm theo tiêu đề, nội dung, hoặc category
   - Hiển thị: Tiêu đề, hình thumbnail, category
   - Click để xem chi tiết bài viết

4. **👤 Users (Người dùng khác)**
   - Tìm kiếm từ `ProfileRepository` (Firestore collection `users`)
   - Tìm theo tên hiển thị hoặc email
   - Hiển thị: Avatar, tên, email
   - Click để xem profile người dùng

### 🔍 Các Loại Nội Dung Có Thể Mở Rộng (Chưa triển khai):

5. **📝 Posts (Bài đăng cộng đồng)**
   - Collection: `posts` trong Firestore
   - Tìm theo nội dung, caption, hashtags
   - Hiển thị: Hình ảnh, caption, tác giả, likes

6. **👨‍🍳 User Recipes (Công thức của người dùng)**
   - Collection: `userRecipes` trong Firestore
   - Tìm theo tên, mô tả, nguyên liệu
   - Hiển thị: Tên, hình ảnh, tác giả, rating

7. **📂 Categories (Danh mục)**
   - Collection: `categories` trong Firestore
   - Tìm theo tên danh mục
   - Hiển thị: Tên, icon, mô tả

8. **🧄 Ingredients (Nguyên liệu)**
   - Collection: `ingredients` trong Firestore
   - Tìm theo tên nguyên liệu
   - Hiển thị: Tên, hình ảnh, calories

9. **⭐ Reviews (Đánh giá)**
   - Collection: `reviews` trong Firestore
   - Tìm theo nội dung đánh giá, tên người dùng
   - Hiển thị: Tên người dùng, rating, nội dung

10. **💡 Cooking Tips (Mẹo nấu ăn)**
    - Collection: `cooking_tips` trong Firestore
    - Tìm theo tiêu đề, nội dung
    - Hiển thị: Tiêu đề, nội dung, category

11. **🍽️ Meal Types (Loại bữa ăn)**
    - Collection: `meal_types` trong Firestore
    - Tìm theo tên loại bữa ăn
    - Hiển thị: Tên, mô tả, icon

12. **🥗 Diet Types (Chế độ ăn)**
    - Collection: `diet_types` trong Firestore
    - Tìm theo tên chế độ ăn
    - Hiển thị: Tên, mô tả, icon

13. **📊 Calorie Info (Thông tin calories)**
    - Collection: `calorie_info` trong Firestore
    - Tìm theo tên thực phẩm, calories range
    - Hiển thị: Tên, calories, serving size

## 🛠️ Công Nghệ & Thư Viện Sử Dụng

### 1. **Jetpack Compose** 📱
**Thư viện**: `androidx.compose.*`
- `compose-ui`: UI components cơ bản
- `compose-material3`: Material Design 3 components
- `compose-foundation`: Layout và gestures
- `compose-animation`: Animation effects

**Components sử dụng**:
- `TextField`: Search input với Material3
- `LazyColumn`: Hiển thị danh sách kết quả tìm kiếm
- `LazyRow`: Hiển thị filter chips ngang
- `AnimatedVisibility`: Animation khi show/hide kết quả
- `Card`: Container cho mỗi search result item
- `FilterChip`: Chip để filter theo type
- `CircularProgressIndicator`: Loading indicator

**Dependencies** (trong `build.gradle.kts`):
```kotlin
implementation("androidx.compose.ui:ui:$compose_version")
implementation("androidx.compose.material3:material3:$material3_version")
implementation("androidx.compose.foundation:foundation:$compose_version")
implementation("androidx.compose.animation:animation:$compose_version")
```

### 2. **Architecture Components** 🏗️
**Thư viện**: `androidx.lifecycle.*`, `androidx.hilt.*`
- **ViewModel**: `androidx.lifecycle:lifecycle-viewmodel-compose`
- **StateFlow**: Quản lý UI state reactive
- **Hilt**: Dependency injection
  - `com.google.dagger:hilt-android`
  - `androidx.hilt:hilt-navigation-compose`

**Dependencies**:
```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
implementation("com.google.dagger:hilt-android:$hilt_version")
kapt("com.google.dagger:hilt-compiler:$hilt_version")
implementation("androidx.hilt:hilt-navigation-compose:$hilt_navigation_version")
```

### 3. **Repository Pattern** 📦
**Thư viện**: Custom implementation
- `SearchRepository`: Tập trung logic tìm kiếm
- Tích hợp với:
  - `ProfileRepository` (Firebase Firestore)
  - `HotNewsRepository` (Firebase Firestore)
  - `UserRecipeRepository` (Firebase Firestore)
  - `PostRepository` (Firebase Firestore)

### 4. **Coroutines & Flow** ⚡
**Thư viện**: `kotlinx.coroutines.*`
- **Debouncing**: `kotlinx.coroutines.delay(500)`
- **Flow**: `kotlinx.coroutines.flow.*`
- **CoroutineScope**: `viewModelScope`, `rememberCoroutineScope`

**Dependencies**:
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines_version")
```

### 5. **Navigation Component** 🧭
**Thư viện**: `androidx.navigation.*`
- Navigate đến màn hình chi tiết
- Deep linking cho search results

**Dependencies**:
```kotlin
implementation("androidx.navigation:navigation-compose:$nav_version")
implementation("androidx.hilt:hilt-navigation-compose:$hilt_navigation_version")
```

### 6. **Firebase Firestore** 🔥
**Thư viện**: `com.google.firebase:firebase-firestore-ktx`
- Tìm kiếm từ nhiều collections
- Real-time updates
- Query với filters

**Dependencies**:
```kotlin
implementation(platform("com.google.firebase:firebase-bom:$firebase_bom_version"))
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
```

### 7. **Image Loading** 🖼️
**Thư viện**: `io.coil-kt:coil-compose`
- Load images từ URL
- Caching tự động
- Placeholder và error handling

**Dependencies**:
```kotlin
implementation("io.coil-kt:coil-compose:$coil_version")
```

### 8. **Local Storage** 💾
**Thư viện**: `androidx.datastore:datastore-preferences` hoặc `SharedPreferences`
- Lưu recent searches
- Cache search history

**Dependencies**:
```kotlin
implementation("androidx.datastore:datastore-preferences:$datastore_version")
// Hoặc sử dụng SharedPreferences có sẵn trong Android
```

### 9. **Text Search & Filtering** 🔍
**Thư viện**: Kotlin Standard Library
- String matching với `contains()`, `startsWith()`
- Case-insensitive search với `lowercase()`
- Multi-word search với `split()` và `all {}`
- Relevance sorting với custom comparator

### 10. **Optional: External APIs** 🌐
**Spoonacular API** (nếu cần):
- Tìm kiếm recipes từ external source
- Cần API key trong `secrets.properties`

**Retrofit** (nếu dùng external API):
```kotlin
implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")
```

## 📐 Kiến Trúc

```
HomeScreen
    └── SearchBar (TextField)
        ├── SearchViewModel (HiltViewModel)
        │   ├── SearchRepository
        │   │   ├── Firestore (recipes collection)
        │   │   ├── Firestore (foodItems collection)
        │   │   ├── HotNewsRepository (hotNews collection)
        │   │   └── ProfileRepository (users collection)
        │   └── StateFlow<SearchUiState>
        │
        └── UI Components
            ├── SearchTextField (trên HomeScreen)
            ├── FilterChips (Recipes, Foods, News, Users)
            ├── SearchResultsList (hiển thị ngay trên HomeScreen)
            └── RecentSearches (khi query rỗng)
```

## 🔎 Chi Tiết Tìm Kiếm

### Tìm Kiếm Foods (Thực phẩm)

**Collection**: `foodItems` trong Firestore

**Cách hoạt động**:
- Lấy tất cả items từ collection `foodItems`
- Filter trong memory với logic:
  - Tìm kiếm case-insensitive
  - Hỗ trợ tìm kiếm đa từ: "Bắp cải trắng" → tìm items chứa cả "bắp", "cải", "trắng"
  - Hoặc tìm kiếm chứa toàn bộ query: "bắp cải trắng"
- Sort theo relevance:
  1. Exact match (ưu tiên cao nhất)
  2. Starts with query
  3. Contains query

**Ví dụ tìm kiếm**:
- "Bắp cải" → Tìm thấy: "Bắp cải trắng", "Bắp cải tím", "Bắp cải xanh"
- "Bắp cải trắng" → Tìm thấy: "Bắp cải trắng" (exact match)
- "Dứa" → Tìm thấy: "Dứa/Thơm"
- "Sầu riêng" → Tìm thấy: "Sầu riêng"

**Dữ liệu hiển thị**:
- `name`: Tên thực phẩm
- `calories`: Calories (có thể là String "48 kcal" hoặc Number)
- `protein`, `fat`, `carbs`: Giá trị dinh dưỡng (g)
- `imageUrl`: URL hình ảnh

### Tìm Kiếm Recipes (Công thức)

**Collection**: `recipes` trong Firestore

**Cách hoạt động**:
- Tìm kiếm theo tên công thức
- Sử dụng Firestore query với range: `whereGreaterThanOrEqualTo` và `whereLessThanOrEqualTo`
- Filter thêm với `contains` check

**Ví dụ tìm kiếm**:
- "Gà" → Tìm thấy: "Gà chiên nước mắm", "Gà nướng", etc.
- "Cá" → Tìm thấy: "Cá hấp bia", "Cá kho", etc.

**Dữ liệu hiển thị**:
- `name`: Tên công thức
- `calories`: Calories (nếu có)
- `imageUrl`: URL hình ảnh

### Tìm Kiếm Hot News

**Collection**: `hotNews` trong Firestore

**Cách hoạt động**:
- Lấy tất cả articles từ `HotNewsRepository`
- Filter theo:
  - Tiêu đề chứa query
  - Nội dung chứa query
  - Category chứa query

**Ví dụ tìm kiếm**:
- "Dinh dưỡng" → Tìm thấy các bài viết về dinh dưỡng
- "Giảm cân" → Tìm thấy các bài viết về giảm cân

**Dữ liệu hiển thị**:
- `title`: Tiêu đề bài viết
- `thumbnailUrl`: URL hình thumbnail
- `category`: Danh mục bài viết

### Tìm Kiếm Users

**Collection**: `users` trong Firestore

**Cách hoạt động**:
- Sử dụng `ProfileRepository.searchProfiles()`
- Tìm theo displayName hoặc email

**Ví dụ tìm kiếm**:
- "Nguyễn" → Tìm thấy users có tên chứa "Nguyễn"
- "example@email.com" → Tìm thấy user với email đó

**Dữ liệu hiển thị**:
- `displayName`: Tên hiển thị
- `email`: Email
- `avatarUrl`: URL avatar

## 🎯 Các Bước Triển Khai

### Bước 1: Tạo Data Models

**File**: `mobile/app/src/main/java/com/example/nutricook/model/search/SearchResult.kt`

```kotlin
sealed class SearchResult {
    data class RecipeResult(
        val id: String,
        val title: String,
        val imageUrl: String?,
        val calories: Int?,
        val source: String = "local" // "local" hoặc "spoonacular"
    ) : SearchResult()
    
    data class FoodResult(
        val id: String,
        val name: String,
        val calories: Float,
        val protein: Float,
        val fat: Float,
        val carb: Float
    ) : SearchResult()
    
    data class NewsResult(
        val id: String,
        val title: String,
        val thumbnailUrl: String?,
        val category: String
    ) : SearchResult()
    
    data class UserResult(
        val id: String,
        val displayName: String,
        val avatarUrl: String?,
        val email: String
    ) : SearchResult()
}
```

### Bước 2: Tạo SearchRepository

**File**: `mobile/app/src/main/java/com/example/nutricook/data/search/SearchRepository.kt`

- Combine kết quả từ nhiều repository
- Xử lý debouncing và caching
- Filter và sort results

### Bước 3: Tạo SearchViewModel

**File**: `mobile/app/src/main/java/com/example/nutricook/viewmodel/search/SearchViewModel.kt`

- Quản lý search query
- Debounce input (500ms)
- Combine results từ nhiều nguồn
- Filter theo category/type
- Recent searches (SharedPreferences)

### Bước 4: Tích Hợp Vào HomeScreen

**File**: `mobile/app/src/main/java/com/example/nutricook/view/home/HomeScreen.kt`

- Search bar được tích hợp trực tiếp vào HomeScreen
- TextField thay vì clickable Card
- Hiển thị kết quả ngay trên HomeScreen khi có query
- Ẩn nội dung bình thường (banner, categories) khi đang search
- Filter chips hiển thị khi click vào filter icon

### Bước 5: Tạo SearchResultItem Composables

**File**: `mobile/app/src/main/java/com/example/nutricook/view/search/SearchResultItems.kt`

- `RecipeResultItem`
- `FoodResultItem`
- `NewsResultItem`
- `UserResultItem`

### Bước 6: Tích Hợp SearchViewModel Vào HomeScreen

- Inject `SearchViewModel` vào `HomeScreen`
- Kết nối TextField với `searchViewModel::onQueryChange`
- Hiển thị kết quả từ `searchState.results`
- Xử lý loading và error states

### Bước 7: Tạo Hilt Module

**File**: `mobile/app/src/main/java/com/example/nutricook/di/SearchModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object SearchModule {
    @Provides
    @Singleton
    fun provideSearchRepository(
        firestore: FirebaseFirestore,
        profileRepository: ProfileRepository,
        hotNewsRepository: HotNewsRepository
    ): SearchRepository {
        return SearchRepository(firestore, profileRepository, hotNewsRepository)
    }
}
```

### Bước 8: Mở Rộng Tìm Kiếm - Thêm Posts

**File**: `mobile/app/src/main/java/com/example/nutricook/data/search/SearchRepository.kt`

1. **Thêm method searchPosts**:
```kotlin
private suspend fun searchPosts(query: String): List<SearchResult.PostResult> = withContext(Dispatchers.IO) {
    try {
        val snapshot = firestore.collection("posts")
            .whereGreaterThanOrEqualTo("caption", query)
            .whereLessThanOrEqualTo("caption", query + "\uf8ff")
            .limit(20)
            .get()
            .await()
        
        snapshot.documents.mapNotNull { doc ->
            val data = doc.data
            SearchResult.PostResult(
                id = doc.id,
                title = data["caption"] as? String ?: "",
                imageUrl = (data["imageUrls"] as? List<*>)?.firstOrNull() as? String,
                authorId = data["authorId"] as? String ?: "",
                likesCount = (data["likesCount"] as? Long)?.toInt() ?: 0
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}
```

2. **Thêm PostResult vào SearchResult.kt**:
```kotlin
data class PostResult(
    override val id: String,
    override val title: String,
    override val imageUrl: String?,
    val authorId: String,
    val likesCount: Int
) : SearchResult()
```

3. **Thêm SearchType.POSTS**:
```kotlin
enum class SearchType {
    RECIPES,
    FOODS,
    NEWS,
    USERS,
    POSTS  // Thêm mới
}
```

### Bước 9: Mở Rộng Tìm Kiếm - Thêm User Recipes

1. **Thêm method searchUserRecipes**:
```kotlin
private suspend fun searchUserRecipes(query: String): List<SearchResult.UserRecipeResult> = withContext(Dispatchers.IO) {
    try {
        val snapshot = firestore.collection("userRecipes")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .limit(20)
            .get()
            .await()
        
        snapshot.documents.mapNotNull { doc ->
            val data = doc.data
            SearchResult.UserRecipeResult(
                id = doc.id,
                title = data["name"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String,
                authorId = data["authorId"] as? String ?: "",
                rating = (data["rating"] as? Double)?.toFloat() ?: 0f
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}
```

2. **Thêm UserRecipeResult vào SearchResult.kt**:
```kotlin
data class UserRecipeResult(
    override val id: String,
    override val title: String,
    override val imageUrl: String?,
    val authorId: String,
    val rating: Float
) : SearchResult()
```

### Bước 10: Mở Rộng Tìm Kiếm - Thêm Ingredients

1. **Thêm method searchIngredients**:
```kotlin
private suspend fun searchIngredients(query: String): List<SearchResult.IngredientResult> = withContext(Dispatchers.IO) {
    try {
        val snapshot = firestore.collection("ingredients")
            .get()
            .await()
        
        val queryLower = query.lowercase()
        snapshot.documents
            .mapNotNull { doc ->
                val data = doc.data
                val name = data["name"] as? String ?: ""
                if (name.lowercase().contains(queryLower)) {
                    SearchResult.IngredientResult(
                        id = doc.id,
                        title = name,
                        imageUrl = data["imageUrl"] as? String,
                        calories = (data["calories"] as? Long)?.toFloat() ?: 0f
                    )
                } else null
            }
            .sortedByDescending { it.title.lowercase().startsWith(queryLower) }
            .take(20)
    } catch (e: Exception) {
        emptyList()
    }
}
```

2. **Thêm IngredientResult vào SearchResult.kt**:
```kotlin
data class IngredientResult(
    override val id: String,
    override val title: String,
    override val imageUrl: String?,
    val calories: Float
) : SearchResult()
```

### Bước 11: Mở Rộng Tìm Kiếm - Thêm Categories

1. **Thêm method searchCategories**:
```kotlin
private suspend fun searchCategories(query: String): List<SearchResult.CategoryResult> = withContext(Dispatchers.IO) {
    try {
        val snapshot = firestore.collection("categories")
            .get()
            .await()
        
        val queryLower = query.lowercase()
        snapshot.documents
            .mapNotNull { doc ->
                val data = doc.data
                val name = data["name"] as? String ?: ""
                if (name.lowercase().contains(queryLower)) {
                    SearchResult.CategoryResult(
                        id = doc.id,
                        title = name,
                        imageUrl = data["iconUrl"] as? String,
                        description = data["description"] as? String
                    )
                } else null
            }
            .take(10)
    } catch (e: Exception) {
        emptyList()
    }
}
```

2. **Thêm CategoryResult vào SearchResult.kt**:
```kotlin
data class CategoryResult(
    override val id: String,
    override val title: String,
    override val imageUrl: String?,
    val description: String?
) : SearchResult()
```

### Bước 12: Cập Nhật UI - Thêm Result Items Mới

**File**: `mobile/app/src/main/java/com/example/nutricook/view/search/SearchResultItems.kt`

Thêm các composable mới:
- `PostResultItem`
- `UserRecipeResultItem`
- `IngredientResultItem`
- `CategoryResultItem`

### Bước 13: Cập Nhật HomeScreen - Hiển Thị Kết Quả Mới

**File**: `mobile/app/src/main/java/com/example/nutricook/view/home/HomeScreen.kt`

1. Thêm filter chips cho các type mới
2. Hiển thị kết quả từ các type mới trong LazyColumn
3. Xử lý navigation khi click vào các result items mới

### Bước 14: Tối Ưu Performance

1. **Caching**: Cache kết quả tìm kiếm trong memory
2. **Pagination**: Load thêm kết quả khi scroll
3. **Debouncing**: Tăng delay lên 300-500ms
4. **Lazy Loading**: Load images khi cần thiết với Coil
5. **Background Processing**: Xử lý search trong background thread

## 🎨 UI/UX Features

### 1. **Search Bar**
- Placeholder: "Tìm kiếm nguyên liệu..."
- Filter icon để mở filter dialog
- Clear button khi có text
- Auto-focus khi mở màn hình search

### 2. **Filter Options**
- **Type**: Recipes, Foods, News, Users, Posts, UserRecipes, Ingredients, Categories
- **Category**: Rau củ, Trái cây, Thịt, etc.
- **Calories Range**: Slider từ 0-1000 kcal
- **Sort**: Mới nhất, Phổ biến, Calories, Relevance
- **Multi-select**: Có thể chọn nhiều types cùng lúc

### 3. **Search Results**
- Group theo type (Recipes, Foods, News, Users)
- Mỗi item có:
  - Image/Icon
  - Title/Name
  - Metadata (calories, category, etc.)
  - Click để navigate đến detail

### 4. **Recent Searches**
- Lưu 10 searches gần nhất
- Quick access chips
- Clear all button

### 5. **Empty State**
- Icon và message khi không có kết quả
- Suggest related searches

### 6. **Loading State**
- Progress indicator
- Skeleton loaders

## 📊 State Management

```kotlin
data class SearchUiState(
    val query: String = "",
    val selectedTypes: Set<SearchType> = emptySet(),
    val selectedCategory: String? = null,
    val caloriesRange: ClosedFloatingPointRange<Float> = 0f..1000f,
    val sortBy: SortOption = SortOption.RELEVANCE,
    val results: Map<SearchType, List<SearchResult>> = emptyMap(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showFilters: Boolean = false
)
```

## 🔄 Flow Diagram

```
User types query
    ↓
Debounce 500ms
    ↓
SearchViewModel.onQueryChange()
    ↓
SearchRepository.search()
    ↓
Parallel searches:
    ├── RecipeRepository.search()
    ├── ProfileRepository.search()
    ├── HotNewsRepository.search()
    └── NutritionRepository.search()
    ↓
Combine results
    ↓
Apply filters
    ↓
Sort results
    ↓
Update UIState
    ↓
Recompose UI
```

## 🧪 Testing

### Unit Tests
- `SearchViewModelTest`: Test search logic, debouncing, filtering
- `SearchRepositoryTest`: Test data combination

### UI Tests
- Test search bar interaction
- Test filter dialog
- Test navigation to detail screens

## 📝 Notes

1. **Performance**: 
   - Debounce để tránh quá nhiều API calls
   - Cache recent searches
   - Lazy loading cho images

2. **Error Handling**:
   - Show error message nếu API fail
   - Fallback to local data nếu có

3. **Accessibility**:
   - Content descriptions cho icons
   - Keyboard navigation support

4. **Localization**:
   - Tất cả strings trong `strings.xml`

## 🚀 Next Steps & Tính Năng Mở Rộng

### Đã Triển Khai ✅
1. ✅ Tìm kiếm Recipes, Foods, News, Users
2. ✅ Debouncing và filtering
3. ✅ Tích hợp vào HomeScreen
4. ✅ Recent searches

### Cần Triển Khai 🔄
1. **Tìm kiếm Posts** (Bài đăng cộng đồng)
   - Collection: `posts`
   - Tìm theo caption, hashtags
   - Hiển thị author, likes, comments

2. **Tìm kiếm User Recipes** (Công thức của người dùng)
   - Collection: `userRecipes`
   - Tìm theo tên, mô tả, nguyên liệu
   - Hiển thị rating, author

3. **Tìm kiếm Ingredients** (Nguyên liệu)
   - Collection: `ingredients`
   - Tìm theo tên nguyên liệu
   - Hiển thị calories, serving size

4. **Tìm kiếm Categories** (Danh mục)
   - Collection: `categories`
   - Tìm theo tên danh mục
   - Hiển thị icon, mô tả

5. **Tìm kiếm Reviews** (Đánh giá)
   - Collection: `reviews`
   - Tìm theo nội dung, tên người dùng
   - Hiển thị rating, date

6. **Tìm kiếm Cooking Tips** (Mẹo nấu ăn)
   - Collection: `cooking_tips`
   - Tìm theo tiêu đề, nội dung
   - Hiển thị category, author

### Tính Năng Nâng Cao 🚀
1. **Gemini AI Integration** (Smart Suggestions)
   - Sử dụng Gemini API để gợi ý tìm kiếm thông minh
   - Auto-complete với AI suggestions
   - Thư viện: `com.google.ai.client.generativeai`

2. **Voice Search** (Tìm kiếm bằng giọng nói)
   - Sử dụng Speech-to-Text API
   - Thư viện: `androidx.speech:speech-recognition`

3. **Image Search** (Tìm kiếm bằng hình ảnh)
   - Upload ảnh để tìm món ăn tương tự
   - Sử dụng ML Kit hoặc custom vision API
   - Thư viện: `com.google.mlkit:image-labeling`

4. **Search History Analytics**
   - Track popular searches
   - Suggest trending searches
   - Personalize based on user history

5. **Advanced Filters**
   - Filter by calories range
   - Filter by cooking time
   - Filter by difficulty level
   - Filter by dietary restrictions (vegan, gluten-free, etc.)

6. **Search Suggestions**
   - Auto-complete khi typing
   - Related searches
   - Popular searches

7. **Search Result Ranking**
   - Boost results based on user preferences
   - Personalize ranking
   - Consider user's past interactions

