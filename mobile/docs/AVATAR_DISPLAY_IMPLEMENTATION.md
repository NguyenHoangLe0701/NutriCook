# Avatar Display Implementation Guide

## Tổng quan
Tài liệu này mô tả cách thức hiển thị avatars của người dùng đã xem các phương pháp nấu ăn (Xào, Chiên, Hấp, Nướng) trong ứng dụng NutriCook.

## Kiến trúc

### 1. Data Model
```kotlin
data class MethodGroupViewer(
    val userId: String,
    val userName: String,
    val avatarUrl: String?,
    val viewedAt: com.google.firebase.Timestamp
)
```

### 2. Firestore Structure
```
methodGroupViews/
  {methodName}/  // "Xào", "Chiên", "Hấp", "Nướng"
    viewers/
      {userId}/
        - userId: String
        - userName: String
        - avatarUrl: String?
        - viewedAt: Timestamp
```

## Cách thức hoạt động

### Bước 1: Lưu view của user hiện tại
Khi user vào màn hình `RecipeDetailScreen` với một phương pháp nấu cụ thể:

1. **Thêm user hiện tại vào viewers list ngay lập tức** (không cần đợi Firestore):
   - Lấy thông tin từ `FirebaseAuth.currentUser`
   - Tạo `MethodGroupViewer` object
   - Thêm vào `viewers` state ngay lập tức để UI hiển thị ngay

2. **Lưu vào Firestore** (async, không block UI):
   - Tạo document trong `methodGroupViews/{methodName}/viewers/{userId}`
   - Lưu thông tin: userId, userName, avatarUrl, viewedAt

### Bước 2: Load danh sách viewers từ Firestore
Sau khi lưu view của user hiện tại:

1. Query Firestore để lấy tất cả viewers:
   - Collection: `methodGroupViews/{methodName}/viewers`
   - Order by: `viewedAt` DESC
   - Limit: 50 viewers

2. Map documents thành `MethodGroupViewer` objects

3. Cập nhật `viewers` state:
   - Loại bỏ duplicate bằng `distinctBy { it.userId }`
   - Giữ lại user hiện tại nếu load từ Firestore thất bại (offline mode)

### Bước 3: Hiển thị avatars
Sử dụng composable `MethodGroupViewersRow`:

```kotlin
MethodGroupViewersRow(
    viewers = viewers.take(3),  // Chỉ lấy 3 avatars đầu tiên
    additionalCount = (viewers.size - 3).coerceAtLeast(0)  // Số người còn lại
)
```

## UI Specifications

### MethodGroupViewersRow Component

**Thông số kỹ thuật:**
- **Số lượng avatars hiển thị:** Tối đa 3 avatars
- **Kích thước avatar:** 32dp x 32dp
- **Overlap offset:** -10dp cho mỗi avatar sau avatar đầu tiên
- **Border:** 2dp màu trắng để tách biệt các avatars
- **Z-index:** Avatar đầu tiên có z-index cao nhất (3), giảm dần

**Layout:**
```
[Avatar1] [Avatar2 offset -10dp] [Avatar3 offset -20dp] [+X others]
```

**Text "+X others":**
- Hiển thị khi có hơn 3 viewers
- Format: "+{số lượng} others"
- Font size: 13sp
- Color: #374151 (dark gray)
- Font weight: Medium

**Avatar fallback:**
- Nếu không có `avatarUrl`: Hiển thị chữ cái đầu của `userName`
- Background: #E5E7EB (light gray)
- Text color: #6B7280 (gray)
- Font size: 13sp
- Font weight: Bold

## Các màn hình sử dụng

### 1. RecipeDetailScreen
- **File:** `RecipeDetailScreen.kt`
- **Vị trí:** Dưới phần mô tả ngắn trong header section
- **Component:** `MethodGroupViewersRow`
- **Số avatars:** 3

### 2. RecipeDiscoveryScreen
- **File:** `RecipeDiscoveryScreen.kt`
- **Vị trí:** Trong các method group cards
- **Component:** `MethodGroupViewersRowCompact` (compact version)
- **Số avatars:** 3

## Xử lý lỗi và edge cases

### 1. Firestore Offline
- User hiện tại vẫn được hiển thị ngay lập tức
- Khi Firestore online, load thêm viewers từ server
- Không block UI khi Firestore offline

### 2. Coroutine Cancellation
- Kiểm tra `isActive` trước khi gọi `await()`
- Bỏ qua `CancellationException` trong catch blocks
- Không log cancellation errors

### 3. Empty Viewers List
- Nếu không có viewers và `additionalCount = 0`: Không hiển thị gì
- Nếu có user hiện tại: Luôn hiển thị ít nhất 1 avatar

### 4. Duplicate Viewers
- Sử dụng `distinctBy { it.userId }` để loại bỏ duplicate
- Ưu tiên viewer mới nhất nếu có duplicate

## Code Example

### Load và hiển thị viewers trong RecipeDetailScreen

```kotlin
@Composable
fun RecipeDetailScreen(navController: NavController, methodName: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val viewers = remember { mutableStateOf<List<MethodGroupViewer>>(emptyList()) }
    
    LaunchedEffect(methodName) {
        val currentUser = auth.currentUser
        
        // 1. Thêm user hiện tại ngay lập tức
        if (currentUser != null) {
            val currentViewer = MethodGroupViewer(
                userId = currentUser.uid,
                userName = currentUser.displayName ?: currentUser.email?.split("@")?.firstOrNull() ?: "User",
                avatarUrl = currentUser.photoUrl?.toString(),
                viewedAt = com.google.firebase.Timestamp.now()
            )
            viewers.value = listOf(currentViewer)
        }
        
        // 2. Lưu vào Firestore (async)
        // ... save to Firestore ...
        
        // 3. Load từ Firestore (async)
        // ... load from Firestore and update viewers.value ...
    }
    
    // 4. Hiển thị trong UI
    MethodGroupViewersRow(
        viewers = viewers.take(3),
        additionalCount = (viewers.size - 3).coerceAtLeast(0)
    )
}
```

## Best Practices

1. **Luôn hiển thị user hiện tại ngay lập tức**: Không đợi Firestore response
2. **Giới hạn số lượng avatars**: Chỉ hiển thị 3 avatars để tránh UI quá tải
3. **Xử lý offline gracefully**: App vẫn hoạt động khi Firestore offline
4. **Loại bỏ duplicate**: Sử dụng `distinctBy` để tránh hiển thị cùng một user nhiều lần
5. **Error handling**: Bỏ qua cancellation exceptions, log các lỗi khác

## Debugging

### Logs
- `RecipeDetail`: Log khi load/save viewers
- `MethodGroupViewersRow`: Log số lượng viewers và additionalCount

### Kiểm tra
1. Xem logcat để kiểm tra số lượng viewers được load
2. Kiểm tra Firestore console để xem data có được lưu đúng không
3. Test offline mode để đảm bảo user hiện tại vẫn hiển thị

## Tương lai cải thiện

1. **Real-time updates**: Sử dụng Firestore snapshot listener để cập nhật real-time
2. **Caching**: Cache viewers list để giảm số lần query Firestore
3. **Pagination**: Load thêm viewers khi scroll
4. **Animation**: Thêm animation khi avatars xuất hiện

