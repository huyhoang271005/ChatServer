# 💬 SocialChat - Real-time Social & Chat Platform Backend

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Modulith](https://img.shields.io/badge/Spring%20Modulith-1.4-blue.svg)](https://spring.io/projects/spring-modulith)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**SocialChat** là hệ thống Backend cho ứng dụng nhắn tin và mạng xã hội thời gian thực (Real-time Chat & Social Platform). Dự án được thiết kế theo kiến trúc **Modular Monolith** với **Spring Modulith**, tận dụng tối đa sức mạnh của **Java 21 Virtual Threads**, **Spring Boot 3.5**, **WebSocket STOMP**, và **Cloudflare R2**.

---

## ✨ Tính năng nổi bật

### 🔐 1. Xác thực & Bảo mật (Authentication & Authorization)
- **JWT Authentication**: Mã hóa và xác thực Token bằng cặp khóa RSA (Public/Private key).
- **OAuth2 Support**: Hỗ trợ OAuth2 Client & Resource Server.
- **Session & Device Tracking**: Quản lý phiên đăng nhập theo từng thiết bị (Device management), hỗ trợ đăng xuất từ xa hoặc hủy phiên.
- **Xác thực Email (Verification)**: Gửi mã OTP / Link xác thực tài khoản qua email bằng Spring Mail & Thymeleaf.
- **Phân quyền dựa trên Vai trò (RBAC)**: Quản lý Role và Permission chặt chẽ.

### ⚡ 2. Nhắn tin thời gian thực & Sự kiện (Real-time Messaging & Events)
- **WebSocket (STOMP Protocol)**: Hỗ trợ trao đổi tin nhắn thời gian thực qua kênh kết nối `/ws`.
- **Trạng thái Hoạt động (User Presence)**: Theo dõi và cập nhật trạng thái Online/Offline/Last Seen của người dùng.
- **Thả cảm xúc & Tương tác tin nhắn (Reactions)**: Hỗ trợ thả icon cảm xúc vào tin nhắn thời gian thực.
- **Trò chuyện Nhóm & Trực tiếp (Group & Direct Chat)**: Quản lý cuộc trò chuyện, thành viên, quyền điều hành nhóm.

### 🚀 3. Hiệu năng cao & Kiến trúc hiện đại
- **Java 21 Virtual Threads**: Tối ưu hóa throughput cho các tác vụ I/O blocking.
- **Yitter Snowflake ID Generator**: Tạo ID duy nhất (Distributed Unique ID Generator) tốc độ cao cho tin nhắn và thực thể.
- **Caffeine In-Memory Cache**: Cache bộ nhớ trong cho Tin nhắn, Cuộc trò chuyện, Trạng thái người dùng để phản hồi siêu tốc.
- **Batch Processing & Scheduled Tasks**: Tự động dọn dẹp phiên hết hạn, khôi phục tài khoản, thu hồi mã xác thực theo lịch (Cron jobs).

### ☁️ 4. Lưu trữ đám mây & Đa ngôn ngữ (Cloud Storage & i18n)
- **Cloudflare R2 / AWS S3 Integration**: Lưu trữ tệp tin, hình ảnh, video với kiểm tra định dạng an toàn qua **Apache Tika**.
- **Đa ngôn ngữ (i18n)**: Phản hồi thông báo lỗi và kết quả API theo ngôn ngữ người dùng (Ví dụ: Tiếng Việt, Tiếng Anh).
- **Swagger / Open API Document**: Tự động sinh tài liệu API và giao diện thử nghiệm tương tác.

---

## 🛠️ Công nghệ sử dụng

| Công nghệ | Mô tả / Phiên bản |
| :--- | :--- |
| **Ngôn ngữ** | Java 21 (Virtual Threads enabled) |
| **Framework chính** | Spring Boot 3.5.14 |
| **Kiến trúc** | Spring Modulith 1.4.11 |
| **Bảo mật** | Spring Security 6, OAuth2 Resource Server, Nimbus JWT |
| **Cơ sở dữ liệu** | Microsoft SQL Server 2022, Spring Data JPA, Hibernate |
| **Bộ nhớ tạm (Cache)** | Caffeine Cache |
| **Real-time** | Spring WebSocket (STOMP) |
| **Lưu trữ tệp** | Cloudflare R2 / AWS SDK Java v2, Apache Tika |
| **Tạo ID** | Yitter ID Generator (Snowflake) |
| **Gửi mail** | Spring Boot Starter Mail, Thymeleaf |
| **Mapping & Utility** | MapStruct 1.6, Lombok |
| **Tài liệu API** | Springdoc OpenAPI UI 2.8 |
| **Containerization** | Docker, Docker Compose |

---

## 📂 Cấu trúc dự án (Spring Modulith)

Dự án tuân theo mô hình **Spring Modulith**, chia nhỏ theo từng miền nghiệp vụ độc lập:

```text
src/main/java/social/chat/
├── authentication/   # Đăng nhập, đăng ký, JWT RSA, Quản lý Session & Thiết bị
├── authorization/    # Phân quyền Role & Permission
├── conversation/     # Quản lý Cuộc trò chuyện (Direct & Group Chat)
├── message/          # Tin nhắn, Cảm xúc (Reactions), Lịch sử nhắn tin
├── profile/          # Hồ sơ người dùng, Avatar, Thông tin cá nhân
├── user/             # Quản lý tài khoản, Ban/Unban, Dọn dẹp tài khoản xóa
├── user_presence/    # Trạng thái Online/Offline thời gian thực
├── verification/     # Xác thực Email (OTP/Token) & Cron dọn dẹp
└── shared/           # Xử lý ngoại lệ toàn cục, i18n, Storage (Cloudflare R2), WebSocket Service
```

---

## 🚀 Hướng dẫn Cài đặt & Chạy Dự án

### 📋 1. Yêu cầu hệ thống
- **Java Development Kit (JDK)**: Phiên bản 21 trở lên.
- **Maven**: Phiên bản 3.9+ (hoặc dùng `mvnw` đi kèm dự án).
- **Docker & Docker Compose** (để chạy database SQL Server).

---

### 🔑 2. Tạo Cặp khóa RSA (JWT Keys)
Ứng dụng sử dụng RSA key pair để ký và xác thực JWT Access Token. Bạn cần tạo 2 file `privateKey.pem` và `publicKey.pem` đặt trong thư mục `src/main/resources/`:

```bash
# Tạo Private Key (PKCS#8)
openssl genpkey -algorithm RSA -out privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -in privateKey.pem -out src/main/resources/privateKey.pem -nocrypt

# Tạo Public Key từ Private Key
openssl rsa -in src/main/resources/privateKey.pem -pubout -out src/main/resources/publicKey.pem
```

---

### ⚙️ 3. Cấu hình Biến môi trường (`.env`)
Tạo file `.env` tại thư mục gốc của dự án và điền các thông tin cấu hình cần thiết:

```env
APP_NAME=SocialChat
FRONTEND_URL=http://localhost:3000
BACKEND_URL=http://localhost:8080
UNKNOW_USER_URL=http://localhost:3000/default-avatar.png
APP_ICON=favicon.ico

# Database Configuration (MSSQL)
DATABASE_URL=jdbc:sqlserver://localhost:1433;databaseName=SocialChatDB;encrypt=true;trustServerCertificate=true;
DATABASE_USERNAME=sa
DATABASE_PASSWORD=YourStrongPassword123!
DEFAULT_BATCH_FETCH_SIZE=20
BATCH_SIZE=30

# JWT Expiry Configuration (Seconds)
JWT_ACCESS_TOKEN_EXPIRE=900
JWT_REFRESH_TOKEN_EXPIRE=604800

# Mail Configuration
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM_NAME=SocialChat Support

# Cloudflare R2 Storage Configuration
CLOUDFLARE_R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
CLOUDFLARE_R2_ACCESS_KEY=<your-access-key>
CLOUDFLARE_R2_SECRET_KEY=<your-secret-key>
CLOUDFLARE_R2_BUCKET_NAME=social-chat-bucket
CLOUDFLARE_R2_BUCKET_URL=https://pub-<id>.r2.dev

# Cache Expiration (Seconds)
MESSAGE_EXPIRE_AFTER_ACCESS=3600
CONVERSATION_EXPIRE_AFTER_ACCESS=3600
REACTOR_EXPIRE_AFTER_ACCESS=3600
USER_PRESENCE_EXPIRE_AFTER_ACCESS=300

# Cron Schedule Configurations
CLEANUP_USER_CRON=0 0 2 * * ?
DAYS_TO_KEEP_DELETED_USER=30
UNBANNED_USER_CRON=0 0/15 * * * ?
CLEANUP_ROLE_CRON=0 0 3 * * ?
DAYS_TO_KEEP_DELETED_ROLE=30
EXPIRED_VERIFICATION_CRON=0 0/10 * * * ?
CLEANUP_VERIFICATION_CRON=0 0 4 * * ?
VERIFICATION_TO_KEEP=7
CLEANUP_DEVICE_CRON=0 0 1 * * ?
REVOKED_SESSION_EXPIRED_CRON=0 0/30 * * * ?
CLEANUP_SESSION_CRON=0 0 5 * * ?
DAYS_TO_KEEP_SESSION_REVOKED=7
CONVERSATIONS_BATCH_SIZE=50
MESSAGES_BATCH_SIZE=100
REACTORS_BATCH_SIZE=100
```

---

### 🐳 4. Khởi chạy Database với Docker Compose
Chạy SQL Server 2022 bằng Docker:

```bash
docker-compose up -d
```

---

### 🏃 5. Khởi chạy Ứng dụng Backend

#### Sử dụng Maven Wrapper:
```bash
# Build dự án
./mvnw clean package -DskipTests

# Khởi chạy
./mvnw spring-boot:run
```

#### Hoặc Chạy bằng Docker:
```bash
# Build Docker image
docker build -t social-chat-backend .

# Chạy Docker container
docker run -p 8080:8080 --env-file .env social-chat-backend
```

---

## 📡 WebSocket & API Documentation

### 🔌 Real-time WebSocket Endpoints
- **Endpoint kết nối**: `ws://localhost:8080/ws`
- **Application Destination Prefix**: `/app`
- **User Destination Prefix**: `/user`
- **Topic Broadcast**: `/topic`
- **Queue Private Message**: `/queue`

### 📑 Swagger API Documentation
Sau khi khởi chạy ứng dụng thành công, truy cập tài liệu API trực quan tại:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🤝 Đóng góp (Contributing)

Mọi đóng góp nhằm hoàn thiện dự án đều được hoan nghênh:
1. **Fork** dự án.
2. Tạo nhánh tính năng mới (`git checkout -b feature/AmazingFeature`).
3. Commit các thay đổi (`git commit -m 'Add some AmazingFeature'`).
4. Push lên nhánh (`git push origin feature/AmazingFeature`).
5. Mở một **Pull Request**.

---

## 📝 Giấy phép (License)

Dự án được phân phối dưới giấy phép **MIT License**. Xem thêm chi tiết tại file `LICENSE`.
