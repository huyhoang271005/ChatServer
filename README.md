# 💬 PingMe - Backend Ứng dụng Nhắn tin Nội bộ Real-time

[![Tiếng Việt](https://img.shields.io/badge/Ngôn%20ngữ-Tiếng%20Việt-red.svg)](README.md)
[![English](https://img.shields.io/badge/Language-English-blue.svg)](README_EN.md)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Modulith](https://img.shields.io/badge/Spring%20Modulith-1.4-blue.svg)](https://spring.io/projects/spring-modulith)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**PingMe** là hệ thống Backend cho **ứng dụng nhắn tin nội bộ thời gian thực** (Internal Real-time Chat Application). Tên ứng dụng được cấu hình linh hoạt thông qua biến môi trường (`APP_NAME`). Dự án được thiết kế theo kiến trúc **Modular Monolith** với **Spring Modulith**, tận dụng tối đa sức mạnh của **Java 21 Virtual Threads**, **Spring Boot 3.5**, **WebSocket STOMP**, và **Cloudflare R2**.

---

## 🎯 Mục đích dự án
Hệ thống được phát triển với mục đích cung cấp giải pháp trao đổi thông tin, giao tiếp thời gian thực cho tổ chức/nhóm làm việc nội bộ, đảm bảo tính bảo mật cao, khả năng quản lý phân quyền thành viên chặt chẽ, chia sẻ tệp tin an toàn và hiệu năng phản hồi tức thì.

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
- **Đa ngôn ngữ (i18n)**: Mặc định phản hồi bằng **Tiếng Anh**, hỗ trợ **Tiếng Việt** đầy đủ khi client yêu cầu qua header `Accept-Language`.
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
- **Docker & Docker Compose** (khuyên dùng để chạy cả Database và App Server).
- **Java Development Kit (JDK 21+)** & **Maven 3.9+** (nếu chọn chạy trực tiếp không qua Docker).

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
Tạo file `.env` tại thư mục gốc dự án (tham khảo mẫu `.env.example`).

---

### 🏃 4. Khởi chạy Ứng dụng

#### Cách 1: Khởi chạy toàn bộ hệ thống bằng Docker Compose (Khuyên dùng)
File `Dockerfile` đã được thiết lập **Multi-stage build** (tự động build JAR với Maven và chạy trên JRE Alpine). `docker-compose.yml` quản lý 2 container: `chat-database` (SQL Server) và `chat-server` (Backend Application).

```bash
# Build tự động và khởi chạy toàn bộ dịch vụ (Database + App Server)
docker-compose up -d --build
```

#### Cách 2: Khởi chạy thủ công (Development)
Nếu bạn muốn chạy trực tiếp ứng dụng bằng Maven trên máy:

1. Khởi chạy riêng SQL Server Database:
```bash
docker-compose up -d database
```

2. Build và khởi chạy ứng dụng:
```bash
# Build dự án
./mvnw clean package -DskipTests

# Khởi chạy Backend Server
./mvnw spring-boot:run
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

## 📝 Giấy phép (License)

Dự án được phân phối dưới giấy phép **MIT License**. Xem thêm chi tiết tại file `LICENSE`.
