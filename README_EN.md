# 💬 PingMe - Real-time Internal Chat Platform Backend

[![English](https://img.shields.io/badge/Language-English-blue.svg)](README_EN.md)
[![Tiếng Việt](https://img.shields.io/badge/Ngôn%20ngữ-Tiếng%20Việt-red.svg)](README.md)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Modulith](https://img.shields.io/badge/Spring%20Modulith-1.4-blue.svg)](https://spring.io/projects/spring-modulith)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**PingMe** is a high-performance backend system for real-time internal team/organization chat applications. The application name is dynamically configured via environment variables (`APP_NAME`). The project is designed using a **Modular Monolith** architecture with **Spring Modulith**, taking full advantage of **Java 21 Virtual Threads**, **Spring Boot 3.5**, **WebSocket STOMP**, and **Cloudflare R2**.

---

## 🎯 Purpose
This application was developed as an internal chat platform for organizations or teams, providing secure, real-time messaging, role-based member management, file sharing, and high throughput with low-latency responses.

---

## ✨ Key Features

### 🔐 1. Authentication & Security
- **JWT Authentication**: RSA Key Pair (Public/Private key) token signing and verification.
- **OAuth2 Support**: OAuth2 Client & Resource Server integration.
- **Session & Device Tracking**: Multi-device login tracking with remote logout and session revocation capabilities.
- **Email Verification**: Account activation, password reset, and device authorization via email OTP/links.
- **Role-Based Access Control (RBAC)**: Fine-grained Role and Permission management.

### ⚡ 2. Real-time Messaging & Events
- **WebSocket (STOMP Protocol)**: Real-time bi-directional message exchange via `/ws`.
- **User Presence Tracking**: Real-time status tracking (Online/Offline/Last Seen).
- **Message Reactions**: Emoji reactions on messages in real-time.
- **Direct & Group Chats**: Group creation, member management, and admin controls.

### 🚀 3. High Performance Architecture
- **Java 21 Virtual Threads**: Optimized throughput for blocking I/O operations.
- **Yitter Snowflake ID Generator**: Ultra-fast distributed unique ID generator for messages and entities.
- **Caffeine In-Memory Cache**: Low-latency caching for messages, conversations, and user presence.
- **Batch Processing & Scheduled Crons**: Automated background jobs for expired sessions, unbanning users, and verification cleanup.

### ☁️ 4. Cloud Storage & i18n
- **Cloudflare R2 / AWS S3 Integration**: Secure media and document storage with content type verification using **Apache Tika**.
- **Internationalization (i18n)**: English as default response language, with full Vietnamese support via `Accept-Language` header.
- **Swagger / OpenAPI**: Interactive API documentation.

---

## 🛠️ Tech Stack

| Technology | Description / Version |
| :--- | :--- |
| **Language** | Java 21 (Virtual Threads enabled) |
| **Core Framework** | Spring Boot 3.5.14 |
| **Architecture** | Spring Modulith 1.4.11 |
| **Security** | Spring Security 6, OAuth2 Resource Server, Nimbus JWT |
| **Database** | Microsoft SQL Server 2022, Spring Data JPA, Hibernate |
| **Caching** | Caffeine Cache |
| **Real-time** | Spring WebSocket (STOMP) |
| **File Storage** | Cloudflare R2 / AWS SDK Java v2, Apache Tika |
| **ID Generator** | Yitter ID Generator (Snowflake) |
| **Email Service** | Spring Boot Starter Mail, Thymeleaf |
| **Mapping & Utility** | MapStruct 1.6, Lombok |
| **API Documentation** | Springdoc OpenAPI UI 2.8 |
| **Containerization** | Docker, Docker Compose |

---

## 📂 Project Structure (Spring Modulith)

The project follows the **Spring Modulith** domain-driven modular structure:

```text
src/main/java/social/chat/
├── authentication/   # Login, Register, RSA JWT, Session & Device management
├── authorization/    # Role & Permission RBAC management
├── conversation/     # Direct & Group Chat management, Members
├── message/          # Real-time messages, Reactions, History
├── profile/          # User profile, Avatar, Personal information
├── user/             # Account management, Ban/Unban, Cleanup crons
├── user_presence/    # Online/Offline real-time status tracking
├── verification/     # Email OTP/Token verification & Expiry crons
└── shared/           # Global exception handler, i18n, R2 Storage, WebSocket service
```

---

## 🚀 Setup & Execution Guide

### 📋 1. Prerequisites
- **Docker & Docker Compose** (recommended for running both Database and App Server).
- **Java Development Kit (JDK 21+)** & **Maven 3.9+** (if running standalone without Docker).

---

### 🔑 2. Generate RSA Key Pair (JWT Keys)
The application requires an RSA key pair to sign and verify JWT Access Tokens. Create `privateKey.pem` and `publicKey.pem` in `src/main/resources/`:

```bash
# Generate PKCS#8 Private Key
openssl genpkey -algorithm RSA -out privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl pkcs8 -topk8 -inform PEM -outform PEM -in privateKey.pem -out src/main/resources/privateKey.pem -nocrypt

# Export Public Key
openssl rsa -in src/main/resources/privateKey.pem -pubout -out src/main/resources/publicKey.pem
```

---

### ⚙️ 3. Environment Variables Configuration (`.env`)
Create a `.env` file in the project root directory (refer to `.env.example`).

---

### 🏃 4. Launch Application

#### Method 1: Using Docker Compose (Recommended)
`Dockerfile` uses a **Multi-stage build** (automatically builds JAR with Maven and runs it on JRE Alpine). `docker-compose.yml` orchestrates 2 services: `chat-database` (SQL Server) and `chat-server` (Backend Application).

```bash
# Automatically build and launch all services (Database + App Server)
docker-compose up -d --build
```

#### Method 2: Manual Launch (Development)
If running the app directly on your local machine using Maven:

1. Launch only SQL Server Database:
```bash
docker-compose up -d database
```

2. Build and run backend application:
```bash
# Build project
./mvnw clean package -DskipTests

# Launch backend server
./mvnw spring-boot:run
```

---

## 📡 WebSocket & API Documentation

### 🔌 Real-time WebSocket Endpoints
- **Connection Endpoint**: `ws://localhost:8080/ws`
- **Application Destination Prefix**: `/app`
- **User Destination Prefix**: `/user`
- **Topic Broadcast**: `/topic`
- **Queue Private Message**: `/queue`

### 📑 Swagger API Documentation
Once the application is running, access the interactive API docs at:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 📝 License

Distributed under the **MIT License**. See `LICENSE` for details.
