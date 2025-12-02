# Hướng dẫn Deploy lên Render

## Cấu trúc hiện tại
Dự án của bạn là một Spring Boot application nằm trong folder `dashboard`. Bạn **CÓ THỂ** deploy trực tiếp từ folder này lên Render.

## Các bước deploy

### 1. Chuẩn bị Repository
Đảm bảo code đã được push lên GitHub/GitLab/Bitbucket.

### 2. Tạo Service trên Render Dashboard

#### Cách 1: Sử dụng render.yaml (Khuyến nghị)
1. Đăng nhập vào [Render Dashboard](https://dashboard.render.com)
2. Chọn **New** > **Blueprint**
3. Kết nối repository của bạn
4. Render sẽ tự động detect file `render.yaml` trong folder `dashboard`
5. Chọn **Apply** để deploy

#### Cách 2: Tạo Web Service thủ công
1. Đăng nhập vào [Render Dashboard](https://dashboard.render.com)
2. Chọn **New** > **Web Service**
3. Kết nối repository của bạn
4. Cấu hình như sau:
   - **Name**: `nutricook-dashboard`
   - **Root Directory**: `dashboard` (quan trọng!)
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/dashboard-0.0.1-SNAPSHOT.jar`
   - **Plan**: Free (hoặc Starter nếu cần)

### 3. Cấu hình Environment Variables

Trên Render Dashboard, thêm các biến môi trường sau:

```
DB_HOST=<your-mysql-host>
DB_NAME=<your-database-name>
DB_USER=<your-database-user>
DB_PASS=<your-database-password>
JAVA_VERSION=17
SPRING_PROFILES_ACTIVE=production
```

**Lưu ý**: 
- Nếu bạn chưa có MySQL database, có thể tạo MySQL database trên Render:
  - Chọn **New** > **PostgreSQL** (hoặc MySQL nếu có)
  - Sau đó lấy connection string và điền vào các biến môi trường

### 4. Cấu hình Database

Nếu bạn sử dụng Render MySQL:
1. Tạo MySQL database trên Render
2. Lấy thông tin connection:
   - Host: `xxxxx.mysql.database.azure.com` (hoặc tương tự)
   - Database name
   - Username
   - Password
3. Điền vào các biến môi trường

### 5. Firebase Configuration

Nếu bạn sử dụng Firebase:
1. Upload file `serviceAccountKey.json` lên Render
2. Hoặc sử dụng Environment Variables để lưu Firebase credentials
3. Cập nhật code để đọc từ environment variables thay vì file

### 6. Build và Deploy

Sau khi cấu hình xong:
1. Render sẽ tự động build và deploy
2. Kiểm tra logs để đảm bảo không có lỗi
3. Ứng dụng sẽ chạy tại URL: `https://nutricook-dashboard.onrender.com` (hoặc tên bạn đặt)

## Lưu ý quan trọng

### Root Directory
- **QUAN TRỌNG**: Phải set **Root Directory** = `dashboard` trong Render settings
- Điều này báo cho Render biết code nằm trong folder `dashboard`, không phải root

### Port Configuration
- Render tự động set biến môi trường `PORT`
- Ứng dụng Spring Boot của bạn đang set port 8080 trong `application.properties`
- Cần cập nhật để đọc từ `PORT` environment variable:

```properties
server.port=${PORT:8080}
```

### Build Time
- Lần đầu build có thể mất 5-10 phút
- Các lần sau sẽ nhanh hơn nhờ cache

### Free Plan Limitations
- Ứng dụng sẽ sleep sau 15 phút không có traffic
- Lần đầu wake up có thể mất 30-60 giây
- Nên upgrade lên Starter plan ($7/tháng) để tránh sleep

## Troubleshooting

### Lỗi: "Cannot find pom.xml"
- Kiểm tra Root Directory đã set đúng là `dashboard` chưa

### Lỗi: "Port already in use"
- Cập nhật `application.properties` để dùng `${PORT}`

### Lỗi: "Database connection failed"
- Kiểm tra các biến môi trường DB_* đã đúng chưa
- Kiểm tra MySQL database đã được tạo và running chưa
- Kiểm tra firewall/network settings của database

### Lỗi: "Build failed"
- Xem logs chi tiết trên Render Dashboard
- Kiểm tra Java version (cần Java 17)
- Kiểm tra Maven dependencies có đầy đủ không

## Tối ưu hóa

1. **Build Cache**: Render tự động cache Maven dependencies
2. **Health Check**: Đã cấu hình health check path trong render.yaml
3. **Environment Variables**: Sử dụng Render's environment variables thay vì .env file

