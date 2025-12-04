# Câu Chuyện NutriCook - Hệ Thống Quản Lý Dinh Dưỡng

## 📖 Lời mở đầu

Chào mừng bạn đến với **NutriCook** - hệ thống quản lý dinh dưỡng toàn diện! Tài liệu này sẽ kể cho bạn nghe câu chuyện về cách hệ thống hoạt động, từ góc độ của một người dùng thực tế. Hãy cùng khám phá hành trình của một người dùng trong hệ thống này nhé! 🍳

---

## 👤 Nhân vật chính: An - Người dùng NutriCook

An là một người trẻ quan tâm đến sức khỏe và muốn xây dựng chế độ ăn uống lành mạnh. Hôm nay, An sẽ sử dụng NutriCook để quản lý dinh dưỡng hàng ngày của mình.

---

## 🎬 Chương 1: Bắt đầu hành trình - Đăng ký và Đăng nhập

### Buổi sáng, An mở ứng dụng NutriCook lần đầu tiên

**An thấy gì?**
- Màn hình chào mừng với logo NutriCook
- Nút "Đăng ký" và "Đăng nhập"

**An làm gì?**
1. An chọn "Đăng ký" và nhập thông tin:
   - Email: `an@example.com`
   - Mật khẩu: `********`
   - Tên đầy đủ: `Nguyễn Văn An`

2. Hệ thống làm gì?
   - **Firebase Authentication** tạo tài khoản mới cho An
   - Gửi email xác thực đến `an@example.com`
   - Lưu thông tin vào **Firestore** (database của Firebase)

3. An nhận email xác thực:
   - Email chứa link: `https://nutricook-fff8f.firebaseapp.com/__/auth/action?...`
   - An click vào link → Email được xác thực ✅

4. An đăng nhập lại:
   - Hệ thống kiểm tra email đã được xác thực chưa
   - Nếu chưa → Yêu cầu xác thực email
   - Nếu rồi → Cho phép đăng nhập, chuyển đến màn hình chính

**Kết quả:** An đã có tài khoản và sẵn sàng sử dụng NutriCook! 🎉

---

## 🏠 Chương 2: Khám phá màn hình chính - Home Screen

### An đăng nhập thành công, thấy màn hình Home

**An thấy gì trên màn hình Home?**

#### 1. **Thanh tìm kiếm** (Search Bar)
- An có thể tìm kiếm:
  - 🍳 **Công thức nấu ăn** (Recipes): "Gà chiên nước mắm", "Cá hấp bia"
  - 🥬 **Thực phẩm** (Food Items): "Bắp cải trắng", "Dứa", "Sầu riêng"
  - 📰 **Tin tức dinh dưỡng** (Hot News): "Cách giảm cân hiệu quả"

**Hệ thống làm gì?**
- Khi An gõ "Gà", hệ thống tìm kiếm trong **Firestore**:
  - Collection `recipes` → Tìm công thức có tên chứa "Gà"
  - Collection `foodItems` → Tìm thực phẩm có tên chứa "Gà"
  - Collection `hotNews` → Tìm tin tức có tiêu đề chứa "Gà"
- Hiển thị kết quả ngay lập tức (real-time search)

#### 2. **Danh mục thực phẩm** (Categories)
- An thấy các danh mục: "Rau củ", "Trái cây", "Thịt cá", "Đồ uống", etc.
- Click vào "Trái cây" → Xem danh sách tất cả trái cây

**Hệ thống làm gì?**
- Lấy dữ liệu từ **Firestore** collection `categories`
- Mỗi category có danh sách `foodItems` (thực phẩm)
- Hiển thị với hình ảnh, tên, và giá trị dinh dưỡng

#### 3. **Gợi ý công thức** (Recipe Suggestions)
- An thấy các công thức nổi bật: "Cơm gà", "Phở bò", "Bánh mì"
- Mỗi công thức có hình ảnh, tên, và calories

**Hệ thống làm gì?**
- Lấy dữ liệu từ **Firestore** collection `recipes`
- Hiển thị công thức phổ biến hoặc mới nhất
- An click vào công thức → Xem chi tiết cách nấu

#### 4. **Thông tin dinh dưỡng hôm nay** (Today's Nutrition)
- An thấy vòng tròn hiển thị:
  - Calories đã nạp: `1200 / 2000 kcal`
  - Progress: `60% hoàn thành`

**Hệ thống làm gì?**
- Tính toán từ các bữa ăn An đã thêm trong ngày
- Lưu trong **Firestore** collection `nutrition/dailyLogs/{userId}/{date}`
- Cập nhật real-time khi An thêm/xóa món ăn

---

## 🍽️ Chương 3: Thêm bữa ăn - Theo dõi dinh dưỡng

### Trưa, An muốn ghi lại bữa trưa của mình

**An làm gì?**

#### Cách 1: Thêm từ danh sách thực phẩm
1. An vào màn hình "Thêm bữa ăn" (Add Meal)
2. Chọn "Bữa trưa"
3. Tìm kiếm "Cơm trắng" → Chọn → Nhập số lượng: `200g`
4. Tìm kiếm "Thịt gà" → Chọn → Nhập số lượng: `150g`
5. Tìm kiếm "Rau muống" → Chọn → Nhập số lượng: `100g`
6. Bấm "Lưu"

**Hệ thống làm gì?**
- Tính toán dinh dưỡng:
  ```
  Cơm trắng: 200g × 130 kcal/100g = 260 kcal
  Thịt gà: 150g × 165 kcal/100g = 247.5 kcal
  Rau muống: 100g × 23 kcal/100g = 23 kcal
  Tổng: 530.5 kcal
  ```
- Lưu vào **Firestore**:
  ```
  nutrition/dailyLogs/{userId}/2024-12-03/
    meals/
      bữa_trưa/
        - name: "Bữa trưa"
        - calories: 530
        - protein: 45g
        - fat: 12g
        - carb: 60g
        - foods: [cơm_trắng, thịt_gà, rau_muống]
  ```
- Cập nhật vòng tròn calories trên Home screen

#### Cách 2: Tính calories tự động bằng AI (Gemini)
1. An vào "Tính calories tự động"
2. Nhập: "1 quả táo"
3. Bấm icon ✨ (AutoAwesome)
4. Hệ thống tự động điền:
   - Calories: `52 kcal`
   - Protein: `0.3g`
   - Fat: `0.2g`
   - Carb: `14g`

**Hệ thống làm gì?**
- Gửi request đến **Google Gemini API**:
  ```
  Prompt: "Tính dinh dưỡng cho: 1 quả táo"
  ```
- Gemini trả về JSON:
  ```json
  {
    "calories": 52,
    "protein": 0.3,
    "fat": 0.2,
    "carb": 14
  }
  ```
- Tự động điền vào form
- An có thể chỉnh sửa nếu cần

**Kết quả:** An đã ghi lại bữa trưa và biết mình đã nạp bao nhiêu calories! 📊

---

## 🍳 Chương 4: Tạo công thức - Nấu ăn thông minh

### Chiều, An muốn tạo công thức mới "Cơm gà"

**An làm gì?**

1. An vào "Tạo công thức" (Create Recipe)
2. Nhập thông tin:
   - Tên: "Cơm gà"
   - Mô tả: "Cơm gà thơm ngon, dễ làm"
   - Số phần: `4 phần`
3. Thêm nguyên liệu:
   - "Gạo" → `500g`
   - "Thịt gà" → `300g`
   - "Hành tây" → `100g`
   - "Nước mắm" → `30ml`
4. Hệ thống tự động tính dinh dưỡng:
   ```
   Tổng calories: 1207 kcal
   Chia 4 phần: 301.75 kcal/phần
   ```
5. An upload ảnh món ăn
6. Bấm "Lưu công thức"

**Hệ thống làm gì?**

#### Bước 1: Tính dinh dưỡng từ nguyên liệu
- Lấy giá trị dinh dưỡng từ database cho mỗi nguyên liệu
- Tính theo số lượng:
  ```
  Gạo: 500g × 130 kcal/100g = 650 kcal
  Thịt gà: 300g × 165 kcal/100g = 495 kcal
  Hành tây: 100g × 40 kcal/100g = 40 kcal
  Nước mắm: 30ml × 22 kcal/100ml = 6.6 kcal
  Tổng: 1191.6 kcal
  ```
- Chia theo số phần: `1191.6 / 4 = 297.9 kcal/phần`

#### Bước 2: Upload ảnh
- Ảnh được upload lên **Cloudinary** (image hosting service)
- Lấy URL ảnh: `https://res.cloudinary.com/.../com_ga.jpg`

#### Bước 3: Lưu công thức
- Lưu vào **Firestore** collection `recipes`:
  ```
  recipes/
    {recipeId}/
      - name: "Cơm gà"
      - description: "Cơm gà thơm ngon, dễ làm"
      - servings: 4
      - calories: 297.9
      - imageUrl: "https://res.cloudinary.com/..."
      - ingredients: [
          {name: "Gạo", quantity: "500g"},
          {name: "Thịt gà", quantity: "300g"},
          ...
        ]
      - steps: ["Bước 1: Nấu cơm", "Bước 2: Luộc gà", ...]
  ```

**Kết quả:** An đã tạo công thức mới và có thể chia sẻ với người khác! 👨‍🍳

---

## 🏃 Chương 5: Tập thể dục - Đốt calories

### Tối, An muốn tập thể dục để đốt calories

**An làm gì?**

1. An vào "Hoạt động thể thao" (Exercise)
2. Chọn "Đạp xe" (Cycling)
3. Thấy màn hình:
   - Mục tiêu: `15 phút` = `100 kcal`
   - Vòng tròn progress: `00:00 / 15:00`
4. Bấm "Tiếp tục" (▶️)

**Hệ thống làm gì?**

#### Bước 1: Bắt đầu Exercise Service
- **Foreground Service** (chạy nền) được khởi động
- Timer bắt đầu đếm: `00:01`, `00:02`, `00:03`...
- Tính calories đốt cháy theo thời gian:
  ```
  Calories = (100 kcal / 900 giây) × số giây đã tập
  ```
- Ví dụ: Sau 5 phút (300 giây):
  ```
  Calories = (100 / 900) × 300 = 33.33 kcal
  ```

#### Bước 2: Hiển thị notification
- Notification hiển thị trên màn hình khóa:
  ```
  Đạp xe • ▶️ Đang chạy...
  05:00 / 15:00 • 33/100 kcal
  [⏸ Tạm dừng] [⏹ Dừng]
  ```

#### Bước 3: An tạm dừng
- An bấm "Tạm dừng" (⏸️) → Timer dừng lại
- An có thể:
  - Bấm "Tiếp tục" (▶️) → Timer tiếp tục từ vị trí dừng
  - Bấm "Reset" → Về 0:00
  - Bấm "Dừng" (⏹️) → Kết thúc exercise

#### Bước 4: Hoàn thành
- Khi đạt 15 phút → Exercise hoàn thành
- Calories đốt cháy: `100 kcal`
- Hệ thống cập nhật vào **Firestore**:
  ```
  users/{userId}/
    exercises/
      {date}/
        - exerciseName: "Đạp xe"
        - duration: 900 giây
        - caloriesBurned: 100
  ```

**Kết quả:** An đã tập thể dục và đốt được 100 kcal! 💪

---

## 📊 Chương 6: Xem thống kê - Theo dõi tiến độ

### Cuối ngày, An muốn xem tổng kết dinh dưỡng

**An làm gì?**

1. An vào "Hồ sơ" (Profile)
2. Thấy các thông tin:

#### 1. **Vòng tròn Calories hôm nay**
- Đã nạp: `1200 kcal`
- Mục tiêu: `2000 kcal`
- Còn thiếu: `800 kcal`
- Progress: `60%`

**Hệ thống tính toán:**
- Lấy tất cả bữa ăn trong ngày từ **Firestore**
- Tổng hợp calories:
  ```
  Bữa sáng: 300 kcal
  Bữa trưa: 530 kcal
  Bữa tối: 370 kcal
  Tổng: 1200 kcal
  ```
- Trừ đi calories đốt cháy:
  ```
  1200 - 100 (tập thể dục) = 1100 kcal thực tế
  ```

#### 2. **Biểu đồ dinh dưỡng**
- Protein: `80g / 100g` (80%)
- Fat: `45g / 65g` (69%)
- Carb: `150g / 250g` (60%)

**Hệ thống tính toán:**
- Tổng hợp từ tất cả món ăn trong ngày
- So sánh với mục tiêu (dựa trên cân nặng, chiều cao, mục tiêu)

#### 3. **Lịch sử 7 ngày**
- Biểu đồ đường hiển thị calories mỗi ngày
- An có thể xem xu hướng tăng/giảm

**Kết quả:** An biết mình đã ăn đủ chưa và cần điều chỉnh gì! 📈

---

## 🔔 Chương 7: Nhận thông báo - Nhắc nhở thông minh

### Sáng hôm sau, An nhận thông báo

**An nhận được gì?**

#### 1. **Thông báo định kỳ** (Scheduled Notifications)
- **7h sáng:** "Buổi sáng rồi! Hãy ăn sáng để có năng lượng bắt đầu ngày mới ☀️"
- **12h trưa:** "Giờ trưa đến rồi! Ghi lại bữa ăn của bạn nhé 🍚"
- **19h tối:** "Buổi tối đến rồi! Cùng xem hôm nay bạn đã đạt được mục tiêu chưa 🌙"

**Hệ thống làm gì?**
- Sử dụng **AlarmManager** để đặt lịch
- Mỗi ngày tự động gửi thông báo vào 3 giờ cố định
- Không cần app mở, thông báo vẫn hiển thị

#### 2. **Thông báo từ Admin** (Push Notifications)
- Admin gửi thông báo: "Chương trình giảm giá 50% cho Premium!"
- An nhận được ngay lập tức

**Hệ thống làm gì?**
- Admin vào Dashboard → Nhập tiêu đề và nội dung
- **NotificationService** lấy tất cả FCM tokens từ **Firestore**
- Gửi qua **Firebase Cloud Messaging (FCM)**
- Tất cả user nhận được notification

**Kết quả:** An luôn được nhắc nhở và cập nhật thông tin mới nhất! 📱

---

## 👨‍💼 Chương 8: Góc nhìn Admin - Quản lý hệ thống

### Trong khi An sử dụng app, Admin quản lý hệ thống

**Admin làm gì trên Dashboard?**

#### 1. **Quản lý người dùng** (User Management)
- Xem danh sách tất cả users
- Xem thông tin: Email, tên, ngày đăng ký
- Xem thống kê: Số bữa ăn, calories trung bình

**Hệ thống làm gì?**
- Lấy dữ liệu từ **Firestore** collection `users`
- Hiển thị trong bảng với pagination
- Có thể tìm kiếm, lọc theo ngày

#### 2. **Quản lý thực phẩm** (Food Management)
- Thêm/sửa/xóa thực phẩm
- Upload ảnh thực phẩm
- Nhập giá trị dinh dưỡng: Calories, Protein, Fat, Carb

**Hệ thống làm gì?**
- Lưu vào **MySQL database** (local database)
- Đồng bộ với **Firestore** (để mobile app dùng)
- Ảnh upload lên **Cloudinary**

#### 3. **Quản lý công thức** (Recipe Management)
- Xem tất cả công thức users tạo
- Phê duyệt/từ chối công thức
- Chỉnh sửa công thức nếu cần

**Hệ thống làm gì?**
- Lấy từ **Firestore** collection `recipes`
- Admin có thể update/delete recipes

#### 4. **Gửi thông báo** (Send Notifications)
- Admin nhập tiêu đề: "Chương trình khuyến mãi"
- Nhập nội dung: "Giảm 50% cho Premium!"
- Chọn đối tượng: "Tất cả người dùng"
- Bấm "Gửi thông báo"

**Hệ thống làm gì?**
- **NotificationService** lấy FCM tokens từ **Firestore**
- Gửi notification qua **FCM** đến tất cả users
- Users nhận được ngay lập tức

#### 5. **Xem thống kê** (Analytics)
- Tổng số users: `1,234`
- Tổng số recipes: `5,678`
- Calories trung bình/ngày: `1,850 kcal`

**Hệ thống làm gì?**
- Tính toán từ **Firestore** và **MySQL**
- Hiển thị biểu đồ (Chart.js)
- Export ra Excel nếu cần

**Kết quả:** Admin quản lý hệ thống hiệu quả và dễ dàng! 📊

---

## 🔄 Chương 9: Đồng bộ dữ liệu - Real-time Sync

### An sử dụng app trên nhiều thiết bị

**Tình huống:**
- An đăng nhập trên điện thoại → Thêm bữa trưa
- An đăng nhập trên máy tính → Thấy bữa trưa ngay lập tức

**Hệ thống làm gì?**

#### Firestore Real-time Listeners
- Khi An thêm bữa ăn trên điện thoại:
  ```
  Firestore: nutrition/dailyLogs/{userId}/2024-12-03/meals/bữa_trưa
  → Thêm document mới
  ```
- Máy tính đang lắng nghe (listener) → Tự động cập nhật
- Không cần refresh, dữ liệu sync real-time

**Kết quả:** Dữ liệu luôn đồng bộ trên mọi thiết bị! 🔄

---

## 🎯 Chương 10: Tổng kết - Hệ thống hoạt động như thế nào?

### Tóm tắt các thành phần chính:

#### 1. **Mobile App (Android)**
- **Ngôn ngữ:** Kotlin
- **UI Framework:** Jetpack Compose
- **Database:** Firestore (cloud), Room (local)
- **Authentication:** Firebase Authentication
- **Storage:** Cloudinary (ảnh)
- **Notifications:** FCM (Firebase Cloud Messaging)

#### 2. **Dashboard (Web)**
- **Framework:** Spring Boot
- **UI:** Thymeleaf + Tailwind CSS
- **Database:** MySQL (local), Firestore (sync)
- **Authentication:** Session-based
- **Notifications:** FCM (gửi đến mobile)

#### 3. **Backend Services**
- **Firebase:**
  - Authentication (Email, Phone, Google, Facebook)
  - Firestore (Database)
  - Storage (Files)
  - Cloud Messaging (Push Notifications)
- **Cloudinary:** Image hosting
- **Gemini API:** AI tính calories tự động

---

## 📚 Các chức năng chính của hệ thống

### 1. **Quản lý người dùng**
- ✅ Đăng ký/Đăng nhập (Email, Google, Facebook)
- ✅ Xác thực email
- ✅ Quên mật khẩu
- ✅ Quản lý profile

### 2. **Theo dõi dinh dưỡng**
- ✅ Thêm bữa ăn (Sáng, Trưa, Tối, Phụ)
- ✅ Tính calories tự động (AI Gemini)
- ✅ Tính calories từ nguyên liệu
- ✅ Xem thống kê dinh dưỡng (Calories, Protein, Fat, Carb)
- ✅ Biểu đồ tiến độ 7 ngày

### 3. **Quản lý công thức**
- ✅ Tạo công thức mới
- ✅ Thêm nguyên liệu và bước nấu
- ✅ Tính dinh dưỡng tự động
- ✅ Upload ảnh món ăn
- ✅ Xem công thức của người khác

### 4. **Tập thể dục**
- ✅ Chọn bài tập (Đạp xe, Đi bộ, Yoga, etc.)
- ✅ Timer đếm thời gian
- ✅ Tính calories đốt cháy
- ✅ Tạm dừng/Tiếp tục
- ✅ Notification hiển thị tiến trình

### 5. **Tìm kiếm**
- ✅ Tìm công thức nấu ăn
- ✅ Tìm thực phẩm
- ✅ Tìm tin tức dinh dưỡng
- ✅ Tìm kiếm đa từ (multi-word search)

### 6. **Thông báo**
- ✅ Thông báo định kỳ (7h, 12h, 19h)
- ✅ Push notification từ Admin
- ✅ Notification khi tập thể dục

### 7. **Admin Dashboard**
- ✅ Quản lý users
- ✅ Quản lý thực phẩm
- ✅ Quản lý công thức
- ✅ Gửi thông báo
- ✅ Xem thống kê và analytics
- ✅ Export dữ liệu ra Excel

---

## 🔧 Công nghệ sử dụng

### Mobile App:
- **Kotlin** - Ngôn ngữ lập trình
- **Jetpack Compose** - UI framework
- **Firebase** - Backend services
- **Room** - Local database
- **Hilt** - Dependency injection
- **Coil** - Image loading

### Dashboard:
- **Java** - Ngôn ngữ lập trình
- **Spring Boot** - Web framework
- **Thymeleaf** - Template engine
- **Tailwind CSS** - Styling
- **MySQL** - Database
- **Firebase Admin SDK** - Kết nối Firebase

### Services:
- **Firebase Authentication** - Xác thực
- **Cloud Firestore** - Database
- **Cloud Storage** - File storage
- **FCM** - Push notifications
- **Cloudinary** - Image hosting
- **Gemini API** - AI tính calories

---

## 🎓 Bài học rút ra

### Cho người mới bắt đầu:

1. **Hệ thống có 2 phần chính:**
   - **Mobile App:** Cho người dùng (An)
   - **Dashboard:** Cho admin quản lý

2. **Dữ liệu được lưu ở đâu?**
   - **Firestore:** Dữ liệu cloud (users, recipes, nutrition logs)
   - **MySQL:** Dữ liệu local của dashboard (foods, categories)
   - **Room:** Dữ liệu local của mobile app (cache)

3. **Làm sao mobile và dashboard giao tiếp?**
   - Cùng dùng **Firestore** → Dữ liệu sync real-time
   - Dashboard gửi notification qua **FCM**

4. **Tính calories như thế nào?**
   - **Từ database:** Lấy giá trị dinh dưỡng của thực phẩm
   - **Từ AI:** Gửi tên món đến Gemini API, nhận kết quả
   - **Từ nguyên liệu:** Tính tổng dinh dưỡng của tất cả nguyên liệu

5. **Notification hoạt động ra sao?**
   - **Định kỳ:** AlarmManager đặt lịch mỗi ngày
   - **Từ Admin:** FCM gửi đến tất cả users
   - **Khi tập thể dục:** Foreground Service hiển thị notification

---

## 🎉 Kết thúc câu chuyện

An đã sử dụng NutriCook thành công trong một ngày:
- ✅ Đăng ký và xác thực email
- ✅ Ghi lại 3 bữa ăn (Sáng, Trưa, Tối)
- ✅ Tạo công thức mới "Cơm gà"
- ✅ Tập thể dục đốt 100 kcal
- ✅ Xem thống kê dinh dưỡng
- ✅ Nhận thông báo nhắc nhở

**NutriCook** đã giúp An quản lý dinh dưỡng một cách khoa học và hiệu quả! 🎊

---

## 📖 Tài liệu tham khảo

Nếu bạn muốn tìm hiểu sâu hơn về từng phần:

- **Kiến trúc hệ thống:** `ARCHITECTURE.md`
- **Firebase:** `FIREBASE_IMPLEMENTATION_GUIDE.md`
- **Tính calories:** `CALORIE_CALCULATION_SYSTEM.md`
- **Đốt calories:** `CALORIE_BURNING_SYSTEM.md`
- **Thông báo:** `NOTIFICATION_SYSTEM_IMPLEMENTATION.md`
- **UI Libraries:** `UI_LIBRARIES_GUIDE.md`

---

**Chúc bạn học tập vui vẻ! 🚀**

