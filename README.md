# Zoopick Server

Zoopick은 유실물 관리 및 매칭 서비스를 위한 백엔드 서버입니다. AI 이미지 분석, 스마트 사물함 연동, 실시간 채팅 등의 기능을 통해 유실물을 빠르고 안전하게 찾을 수 있도록 돕습니다.

---

## 주요 기능

- **AI 유실물 매칭**: FastAPI 기반의 이미지 분석을 통해 유실물의 카테고리, 색상을 분류하고 벡터 임베딩을 활용한 정교한 매칭 시스템을 제공합니다.
- **IOT 사물함 (IOT Locker)**: 비대면 유실물 전달을 위한 사물함 제어 및 상태 관리 시스템을 지원합니다.
- **실시간 채팅**: 습득자와 분실자 간의 원활한 소통을 위한 WebSocket 기반 실시간 채팅 기능을 제공합니다.
- **CCTV 및 위치 분석**: 유실물 발생 지역의 CCTV 영상 분석 요청 및 사용자 시간표 기반의 이동 동선 매칭 기능을 포함합니다.
- **알림 서비스**: Firebase(FCM)를 통한 실시간 푸시 알림 및 이메일 인증 서비스를 제공합니다.

---

## Tech Stack

- **Framework**: Spring Boot 4.0.5 (Java 17)
- **Database**: PostgreSQL (with Hibernate Vector for similarity search)
- **Caching/Queue**: Redis (Email Auth, Session etc.)
- **Security**: Spring Security, JWT (JSON Web Token)
- **Messaging**: WebSocket, Firebase Cloud Messaging (FCM)
- **API Documentation**: Springdoc OpenAPI (Swagger UI)
- **AI Integration**: FastAPI (External Image Analysis Service)

---

## 사전 요구 사항

- **Java 17** 이상
- **PostgreSQL 18.3** (또는 호환 버전)
- **Redis 3.0.504** 이상
- **Firebase Admin SDK** 서비스 계정 키 (.json)

---

## 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하거나 환경 변수를 설정해 주세요.

```bash
# 필수 설정 (Mandatory)
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_MAIL_USERNAME=example@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
FIREBASE_ACCOUNT_KEY_PATH=/path/to/firebase-adminsdk.json -> Firebase Admin SDK 서비스 계정 키 필수 
JWT_SECRET=your_secret_key_at_least_32_bytes

# 선택 설정 (Optional)
# default: jdbc:postgresql://localhost:5432/zoopick
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/zoopick
# default: false
SPRING_JPA_SHOW_SQL=false
```

---

## 데이터베이스 세팅 및 복원

### 1. DB 초기 복원 (Restore)
`zoopick_dump.sql` 파일을 이용하여 스키마와 초기 시드 데이터를 세팅할 수 있습니다.

```bash
# 1. zoopick 데이터베이스 생성
createdb -U postgres zoopick 

# 2. 덤프 파일을 이용하여 데이터 복원
psql -U postgres -d zoopick -f zoopick_dump.sql
```

> **주의**: 인코딩 오류 방지를 위해 UTF-8 환경에서 수행하는 것을 권장합니다.

### 2. Redis 서버 실행
이메일 인증 및 휘발성 데이터 처리를 위해 Redis가 실행 중이어야 합니다. (기본 포트: 6379)
```bash
redis-cli ping  # PONG 응답 확인
```

---

## 빌드 및 실행

### 빌드
```bash
./mvnw clean package
```

### 실행
```bash
cd target
java -jar zoopick-server-0.0.1.jar
```

---

## API 문서 (Swagger)

서버 실행 후 아래 주소에서 API 명세를 확인할 수 있습니다.
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`




---

## 📁 프로젝트 구조

```text
src/main/java/com/zoopick/server/
├── config/          # 설정 클래스 (Security, Redis, Swagger 등)
├── controller/      # API 컨트롤러 계층
├── dto/             # 데이터 전송 객체
├── entity/          # JPA 엔티티 계층
├── repository/      # 데이터 저장소 계층 (Spring Data JPA)
├── service/         # 비즈니스 로직 계층
└── websocket/       # 실시간 채팅 및 WebSocket 처리
```
