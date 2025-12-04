# Hướng Dẫn Triển Khai Firebase - NutriCook

## 📋 Tổng quan

NutriCook sử dụng **Firebase** - nền tảng backend của Google để xử lý authentication, database, storage, và push notifications. Tài liệu này giải thích chi tiết cách triển khai và làm việc với các Firebase services.

---

## 🔥 Firebase Services được sử dụng

### 1. **Firebase Authentication** (Xác thực)
- Email/Password authentication
- Email verification (Xác thực email)
- Phone authentication (OTP)
- Google Sign-In
- Facebook Login

### 2. **Cloud Firestore** (Database)
- Lưu trữ dữ liệu user
- Lưu trữ recipes, food items
- Real-time data sync

### 3. **Cloud Storage** (File Storage)
- Lưu trữ ảnh recipes
- Lưu trữ ảnh user profile

### 4. **Cloud Messaging (FCM)** (Push Notifications)
- Gửi notification từ dashboard
- Nhận notification trên mobile

---

## 🔐 Firebase Authentication

### Tổng quan

**Firebase Authentication** là service xác thực người dùng của Firebase. Nó hỗ trợ nhiều phương thức đăng nhập và xác thực.

### Các phương thức xác thực:

1. **Email/Password** - Đăng nhập bằng email và mật khẩu
2. **Email Verification** - Xác thực email qua link
3. **Phone Authentication** - Xác thực số điện thoại bằng OTP
4. **Google Sign-In** - Đăng nhập bằng Google
5. **Facebook Login** - Đăng nhập bằng Facebook

---

## 📧 Email Verification (Xác thực Email)

### Cơ chế hoạt động:

**Email Verification** là cơ chế xác thực email của user sau khi đăng ký. Firebase gửi email chứa link xác thực, user click vào link để xác thực email.

### Luồng hoạt động:

```
1. User đăng ký tài khoản
   ↓
2. Firebase tạo user và gửi email xác thực
   ↓
3. User nhận email, click vào link xác thực
   ↓
4. Firebase xác nhận email đã được verify
   ↓
5. User có thể đăng nhập (nếu app yêu cầu email verified)
```

### Code Implementation:

#### 1. Gửi email xác thực khi đăng ký:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/firebase/auth/FirebaseAuthDataSource.kt

suspend fun signUp(email: String, password: String): FirebaseUser {
    val result = auth.createUserWithEmailAndPassword(email, password).await()
    
    // Gửi email xác thực ngay sau khi đăng ký thành công
    result.user?.sendEmailVerification()?.await()
    
    return result.user ?: error("User is null after signUp")
}
```

#### 2. Gửi lại email xác thực:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/firebase/auth/FirebaseAuthDataSource.kt

suspend fun sendEmailVerification() {
    auth.currentUser?.sendEmailVerification()?.await()
}
```

#### 3. Kiểm tra email đã được xác thực chưa:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/firebase/auth/FirebaseAuthDataSource.kt

fun isEmailVerified(): Boolean {
    return auth.currentUser?.isEmailVerified == true
}

// Reload user để cập nhật trạng thái mới nhất
suspend fun reloadUser() {
    auth.currentUser?.reload()?.await()
}
```

#### 4. Kiểm tra email verified khi đăng nhập:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/viewmodel/auth/AuthViewModel.kt

private fun signInEmailPassword() = viewModelScope.launch {
    val result = loginRepo.signIn(email, pass)
    result.onSuccess {
        // Kiểm tra Email Verified ngay sau khi đăng nhập thành công
        val isVerified = verificationRepo.checkEmailVerified()
        if (isVerified) {
            // Cho phép đăng nhập
            _uiState.update { it.copy(isAuthSuccess = true) }
        } else {
            // Đăng xuất ngay nếu email chưa verified
            sessionRepo.signOut()
            _uiState.update {
                it.copy(
                    message = "Email chưa được xác thực. Vui lòng kiểm tra hộp thư!",
                    isAuthSuccess = false
                )
            }
        }
    }
}
```

#### 5. Màn hình xác thực email:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/view/auth/VerifyEmailScreen.kt

@Composable
fun VerifyEmailScreen(
    navController: NavController,
    verificationRepo: FirebaseVerificationRepository = hiltViewModel()
) {
    var isVerified by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Kiểm tra email đã verified chưa
        isVerified = verificationRepo.checkEmailVerified()
    }
    
    if (isVerified) {
        // Chuyển đến màn hình chính
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.VERIFY_EMAIL) { inclusive = true }
        }
    } else {
        // Hiển thị nút "Gửi lại email xác thực"
        Button(onClick = {
            verificationRepo.resendEmailVerification()
        }) {
            Text("Gửi lại email xác thực")
        }
    }
}
```

---

## 📱 Phone Authentication (Xác thực Số điện thoại)

### Cơ chế hoạt động:

**Phone Authentication** sử dụng OTP (One-Time Password) để xác thực số điện thoại. Firebase gửi mã OTP qua SMS, user nhập mã để xác thực.

### Code Implementation:

```kotlin
// File: mobile/app/src/main/java/com/example/nutricook/data/firebase/auth/FirebaseAuthDataSource.kt

fun sendPhoneVerification(
    activity: Activity,
    phoneNumber: String,
    callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
) {
    val options = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber)       // SĐT cần verify
        .setTimeout(60L, TimeUnit.SECONDS) // Timeout 60 giây
        .setActivity(activity)             // Activity bắt buộc (cho reCAPTCHA)
        .setCallbacks(callbacks)           // Callback trả về kết quả
        .build()
    
    PhoneAuthProvider.verifyPhoneNumber(options)
}

// Liên kết SĐT vào tài khoản hiện tại sau khi có mã OTP
suspend fun linkPhoneCredential(credential: PhoneAuthCredential): FirebaseUser {
    val user = auth.currentUser ?: error("No user logged in")
    val result = user.linkWithCredential(credential).await()
    return result.user ?: error("User is null after linking")
}
```

### Callbacks:

```kotlin
val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
        // Tự động verify (khi SMS được nhận tự động)
        linkPhoneCredential(credential)
    }
    
    override fun onVerificationFailed(e: FirebaseException) {
        // Xử lý lỗi
        Log.e("PhoneAuth", "Verification failed", e)
    }
    
    override fun onCodeSent(
        verificationId: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        // Mã OTP đã được gửi, lưu verificationId để verify sau
        savedVerificationId = verificationId
    }
}
```

---

## 🔑 Google Sign-In

### Code Implementation:

```kotlin
// File: mobile/app/build.gradle.kts
implementation("com.google.android.gms:play-services-auth:21.4.0")

// Sử dụng Google Sign-In
val gsc = GoogleSignIn.getClient(context, gso)
val signInIntent = gsc.signInIntent
startActivityForResult(signInIntent, RC_SIGN_IN)

// Xử lý kết quả
val task = GoogleSignIn.getSignedInAccountFromIntent(data)
val account = task.getResult(ApiException::class)
val credential = GoogleAuthProvider.getCredential(account.idToken, null)
auth.signInWithCredential(credential)
```

---

## 📘 Facebook Login

### Code Implementation:

```kotlin
// File: mobile/app/build.gradle.kts
implementation("com.facebook.android:facebook-login:18.1.3")

// Sử dụng Facebook Login
LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email", "public_profile"))
val token = AccessToken.getCurrentAccessToken()
val credential = FacebookAuthProvider.getCredential(token.token)
auth.signInWithCredential(credential)
```

---

## 💾 Cloud Firestore (Database)

### Tổng quan

**Cloud Firestore** là NoSQL database của Firebase, lưu trữ dữ liệu dạng document (giống MongoDB).

### Cấu trúc dữ liệu:

```
Firestore
└── users/
    └── {userId}/
        ├── email: "user@example.com"
        ├── displayName: "Nguyễn Văn A"
        ├── fcmToken: "dK3jK...xyz"
        └── createdAt: Timestamp
└── recipes/
    └── {recipeId}/
        ├── name: "Cơm gà"
        ├── ingredients: [...]
        └── nutrition: {...}
```

### Code Implementation:

```kotlin
// File: mobile/app/build.gradle.kts
implementation("com.google.firebase:firebase-firestore-ktx")

// Lấy dữ liệu
val db = FirebaseFirestore.getInstance()
val userRef = db.collection("users").document(userId)
userRef.get()
    .addOnSuccessListener { document ->
        val user = document.toObject(User::class.java)
    }

// Lưu dữ liệu
userRef.set(user)
    .addOnSuccessListener { /* Success */ }

// Cập nhật dữ liệu
userRef.update("displayName", "New Name")
    .addOnSuccessListener { /* Success */ }

// Real-time listener
userRef.addSnapshotListener { snapshot, error ->
    val user = snapshot?.toObject(User::class.java)
}
```

---

## 📦 Cloud Storage (File Storage)

### Tổng quan

**Cloud Storage** là dịch vụ lưu trữ file (ảnh, video, etc.) của Firebase.

### Code Implementation:

```kotlin
// File: mobile/app/build.gradle.kts
implementation("com.google.firebase:firebase-storage-ktx")

// Upload ảnh
val storage = FirebaseStorage.getInstance()
val storageRef = storage.reference
val imageRef = storageRef.child("recipes/${recipeId}.jpg")

imageRef.putFile(imageUri)
    .addOnSuccessListener { /* Success */ }
    .addOnFailureListener { /* Error */ }

// Download ảnh
imageRef.downloadUrl
    .addOnSuccessListener { uri ->
        // Sử dụng URI để load ảnh
    }
```

---

## 📲 Cloud Messaging (FCM) - Push Notifications

### Tổng quan

**Firebase Cloud Messaging (FCM)** là dịch vụ gửi push notification từ server đến mobile app.

### Code Implementation:

```kotlin
// File: mobile/app/build.gradle.kts
implementation("com.google.firebase:firebase-messaging-ktx")

// Nhận FCM token
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    val token = task.result
    // Lưu token vào Firestore
}

// Nhận notification
class NutriCookMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Xử lý notification
        sendNotification(remoteMessage.notification?.title, remoteMessage.notification?.body)
    }
}
```

**Xem chi tiết:** `dashboard/docs/NOTIFICATION_SYSTEM_IMPLEMENTATION.md`

---

## 🔧 Cấu hình Firebase

### 1. **Android App**

#### File: `mobile/app/google-services.json`

```json
{
  "project_info": {
    "project_id": "nutricook-fff8f",
    "project_number": "697610921161"
  },
  "client": [
    {
      "client_info": {
        "android_client_info": {
          "package_name": "com.example.nutricook"
        }
      }
    }
  ]
}
```

#### File: `mobile/app/build.gradle.kts`

```kotlin
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    // Firebase BoM (Bill of Materials) - Quản lý version tự động
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    
    // Firebase services
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
}
```

### 2. **Dashboard (Spring Boot)**

#### File: `dashboard/src/main/resources/serviceAccountKey.json`

```json
{
  "type": "service_account",
  "project_id": "nutricook-fff8f",
  "private_key_id": "...",
  "private_key": "...",
  "client_email": "...",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token"
}
```

#### File: `dashboard/src/main/resources/application.properties`

```properties
firebase.enabled=true
```

---

## 📁 Cấu trúc Code

### Mobile App:

```
mobile/app/src/main/java/com/example/nutricook/
├── data/
│   └── firebase/
│       └── auth/
│           ├── FirebaseAuthDataSource.kt      # Data source cho Auth
│           └── FirebaseVerificationRepository.kt # Repository cho verification
├── viewmodel/
│   └── auth/
│       └── AuthViewModel.kt                   # ViewModel xử lý auth
└── view/
    └── auth/
        ├── LoginScreen.kt                     # Màn hình đăng nhập
        ├── SignUpScreen.kt                    # Màn hình đăng ký
        └── VerifyEmailScreen.kt               # Màn hình xác thực email
```

### Dashboard:

```
dashboard/src/main/java/com/nutricook/dashboard/
├── service/
│   ├── FirestoreService.java                 # Service làm việc với Firestore
│   └── NotificationService.java              # Service gửi FCM notification
└── controller/
    └── AdminController.java                  # Controller xử lý requests
```

---

## 🔄 Luồng Xác Thực Email

### Chi tiết:

```
1. User đăng ký
   ↓
2. FirebaseAuthDataSource.signUp()
   - Tạo user với email/password
   - Gửi email verification tự động
   ↓
3. User nhận email từ Firebase
   Email chứa link: https://nutricook-fff8f.firebaseapp.com/__/auth/action?mode=verifyEmail&oobCode=...
   ↓
4. User click vào link
   - Nếu mở trên mobile: Deep link mở app
   - Nếu mở trên web: Redirect về app
   ↓
5. App xử lý deep link
   - MainActivity nhận intent với action code
   - Xác thực email thành công
   ↓
6. User reload để cập nhật trạng thái
   - FirebaseAuthDataSource.reloadUser()
   - isEmailVerified = true
   ↓
7. User có thể đăng nhập
```

### Deep Link Configuration:

```xml
<!-- File: mobile/app/src/main/AndroidManifest.xml -->
<activity
    android:name=".MainActivity"
    android:launchMode="singleTop">
    
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        
        <data android:scheme="https" />
        <data android:host="nutricook-fff8f.firebaseapp.com" />
        <data android:pathPrefix="/__/auth/action" />
    </intent-filter>
</activity>
```

---

## 🎯 Các Firebase Services và Mục đích

| Service | Mục đích | Code Location |
|---------|----------|---------------|
| **Authentication** | Xác thực user (Email, Phone, Google, Facebook) | `FirebaseAuthDataSource.kt` |
| **Email Verification** | Xác thực email qua link | `FirebaseAuthDataSource.sendEmailVerification()` |
| **Phone Auth** | Xác thực SĐT qua OTP | `FirebaseAuthDataSource.sendPhoneVerification()` |
| **Firestore** | Database lưu trữ dữ liệu | `FirestoreService.java` (Dashboard) |
| **Storage** | Lưu trữ file (ảnh) | Sử dụng trong upload recipes |
| **FCM** | Push notifications | `NotificationService.java` (Dashboard) |

---

## ✅ Checklist Triển Khai

### Mobile:
- [x] ✅ Firebase configuration (google-services.json)
- [x] ✅ Firebase dependencies (build.gradle.kts)
- [x] ✅ FirebaseAuthDataSource.kt - Xử lý authentication
- [x] ✅ Email verification implementation
- [x] ✅ Phone authentication implementation
- [x] ✅ Deep link configuration (AndroidManifest.xml)
- [x] ✅ VerifyEmailScreen.kt - Màn hình xác thực email

### Dashboard:
- [x] ✅ Firebase Admin SDK configuration
- [x] ✅ Service account key (serviceAccountKey.json)
- [x] ✅ FirestoreService.java - Làm việc với Firestore
- [x] ✅ NotificationService.java - Gửi FCM notifications

---

## 🎉 Kết Luận

Firebase trong NutriCook được sử dụng cho:

1. **Authentication** - Xác thực user (Email, Phone, Google, Facebook)
2. **Email Verification** - Xác thực email qua link (cơ chế: `sendEmailVerification()`)
3. **Firestore** - Database lưu trữ dữ liệu
4. **Storage** - Lưu trữ file (ảnh)
5. **FCM** - Push notifications

**Email Verification** là một tính năng của **Firebase Authentication**, không phải service riêng biệt. Nó sử dụng method `sendEmailVerification()` để gửi email chứa link xác thực đến user.

Tất cả các file đã được triển khai và sẵn sàng sử dụng! 🚀

