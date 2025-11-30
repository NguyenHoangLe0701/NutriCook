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

## 🛠️ Công Nghệ Sử Dụng

### 1. **Jetpack Compose**
- `SearchBar` composable với Material3
- `LazyColumn` để hiển thị kết quả tìm kiếm
- `AnimatedVisibility` cho animation khi mở/đóng search
- `TextField` với `Icons.Default.Search` và `Icons.Default.FilterList`

### 2. **Architecture Components**
- **ViewModel**: `SearchViewModel` để quản lý state và logic tìm kiếm
- **StateFlow**: Quản lý UI state (query, results, loading, error)
- **Hilt**: Dependency injection cho ViewModel và Repository

### 3. **Repository Pattern**
- `SearchRepository`: Tập trung logic tìm kiếm từ nhiều nguồn
- Tích hợp với:
  - `RecipeRepository` (Spoonacular API)
  - `ProfileRepository` (Firebase Firestore)
  - `HotNewsRepository` (Firebase Firestore)
  - `NutritionRepository` (Local database)

### 4. **Coroutines & Flow**
- **Debouncing**: Delay 500ms sau khi người dùng ngừng gõ
- **Flow**: Combine nhiều nguồn dữ liệu
- **CoroutineScope**: Xử lý async operations

### 5. **Navigation Component**
- Navigate đến màn hình chi tiết khi click vào kết quả
- Deep linking cho search results

### 6. **Firebase Firestore**
- Tìm kiếm recipes từ Firestore
- Tìm kiếm hot news articles
- Tìm kiếm users

### 7. **Spoonacular API** (Optional)
- Tìm kiếm recipes từ external API
- Cần API key trong `secrets.properties`

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

### Bước 7: Tạo Hilt Module (nếu cần)

**File**: `mobile/app/src/main/java/com/example/nutricook/di/SearchModule.kt`

- Provide `SearchRepository`

## 🎨 UI/UX Features

### 1. **Search Bar**
- Placeholder: "Tìm kiếm công thức, thực phẩm, tin tức..."
- Filter icon để mở filter dialog
- Clear button khi có text

### 2. **Filter Options**
- **Type**: Recipes, Foods, News, Users
- **Category**: Rau củ, Trái cây, Thịt, etc.
- **Calories Range**: Slider từ 0-1000 kcal
- **Sort**: Mới nhất, Phổ biến, Calories

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

## 🚀 Next Steps

1. Implement search với Gemini AI (smart suggestions)
2. Voice search integration
3. Image search (search by photo)
4. Search history analytics

