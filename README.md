# localhost-server

> **2026 워커톤 3차 대회 금상 수상작** — 팀 "상금수거반"

O2O 리워드 앱 **localhost**의 백엔드 API 서버입니다.
위치 기반 인증과 AI 이미지 분석을 결합하여 오프라인 행동을 정확하게 검증하고, 미션 달성 시 리워드를 지급합니다.

> **서버 안내**: 본 서버는 클라우드 환경에서 운영되며, 자원 제한으로 인해 **예고 없이 일시 중단되거나 종료**될 수 있습니다. 안정적인 서비스 이용을 보장하지 않으며, 데모 및 평가 목적으로 운영됩니다.

---

## Tech Stack

| 분류 | 기술 |
|------|------|
| **Framework** | Spring Boot 4.0.1 |
| **Language** | Kotlin 2.2.21 |
| **Database** | MySQL 8.4 + Spring Data JPA / Hibernate |
| **Cache** | Redis 7 |
| **Auth** | JWT (JJWT) + Spring Security |
| **AI** | Elice Cloud ML API (`gemini-2.5-flash`) |
| **Geolocation** | Kakao Geocoding API |
| **Storage** | AWS S3 (Presigned URL) |
| **Migration** | Flyway |
| **API Docs** | Swagger (springdoc-openapi 2.8) |
| **Infra** | Docker, Docker Compose |
| **Code Quality** | KtLint |
| **Testing** | JUnit 5, Testcontainers (MySQL) |

---

## Architecture

```
Client (Mobile App)
       │
       ▼
┌─────────────────────────────────────────────┐
│            Spring Boot 4.0.1                │
│                                             │
│  ┌─────────────┐     ┌──────────────────┐  │
│  │  Controllers │     │  Security        │  │
│  │  (REST API)  │◄────│  JWT Filter      │  │
│  └──────┬──────┘     └──────────────────┘  │
│         │                                   │
│  ┌──────▼──────┐     ┌──────────────────┐  │
│  │  Services    │────►│  External APIs   │  │
│  │  (비즈니스)  │     │  ├ Elice AI      │  │
│  └──────┬──────┘     │  ├ Kakao Geo     │  │
│         │            │  └ AWS S3         │  │
│  ┌──────▼──────┐     └──────────────────┘  │
│  │  JPA + Flyway│                          │
│  └──────┬──────┘                           │
└─────────┼──────────────────────────────────┘
          │
  ┌───────┴────────┐
  │                │
┌─▼──────┐  ┌─────▼───┐
│ MySQL  │  │  Redis  │
│  8.4   │  │    7    │
└────────┘  └─────────┘
```

---

## API Endpoints

### Auth (`/api/auth`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| `POST` | `/api/auth/signup` | 회원가입 (USER / OWNER) |
| `POST` | `/api/auth/login` | 로그인, JWT 토큰 발급 |

### User (`/api/users`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/users/me` | 내 프로필 조회 (포인트 포함) | O |
| `PATCH` | `/api/users/me` | 사용자 정보 수정 | O |
| `DELETE` | `/api/users/me` | 회원 탈퇴 | O |

### Store (`/api/stores`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/stores` | 전체 매장 목록 (거리순 정렬 지원) | X |
| `GET` | `/api/stores/{storeId}` | 매장 상세 조회 | X |
| `GET` | `/api/stores/my` | 내 매장 목록 | OWNER |
| `POST` | `/api/stores` | 매장 등록 (카카오 지오코딩) | OWNER |
| `DELETE` | `/api/stores/{storeId}` | 매장 삭제 | OWNER |

### Mission Definition (`/api/stores/{storeId}/missions`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/stores/{storeId}/missions` | 매장별 미션 목록 | X |
| `GET` | `/api/stores/{storeId}/missions/{missionId}` | 미션 상세 조회 | X |
| `POST` | `/api/stores/{storeId}/missions` | 미션 생성 | OWNER |
| `PUT` | `/api/stores/{storeId}/missions/{missionId}` | 미션 수정 | OWNER |
| `DELETE` | `/api/stores/{storeId}/missions/{missionId}` | 미션 삭제 (soft delete) | OWNER |
| `POST` | `.../inventory/presigned-url` | 인벤토리 이미지 업로드 URL 발급 | OWNER |
| `POST` | `.../{missionId}/image/presigned-url` | 정답 이미지 업로드 URL 발급 | OWNER |
| `PUT` | `.../{missionId}/image/confirm` | 이미지 업로드 확인 | OWNER |

### Mission Attempt (`/api/missions`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/missions` | 활성 미션 목록 (storeId, type 필터) | X |
| `POST` | `/api/missions/{missionId}/attempts` | 미션 시도 (M1/M5: JSON, M3/M4: multipart 이미지) | O |
| `GET` | `/api/missions/{missionId}/attempts/me` | 내 시도 이력 조회 | O |
| `POST` | `/api/missions/{missionId}/attempts/checkin` | M2 체류 미션 체크인 | O |
| `POST` | `/api/missions/{missionId}/attempts/checkout` | M2 체류 미션 체크아웃 | O |

### Reward (`/api/rewards`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/rewards/my` | 내 리워드 내역 조회 | O |

---

## Mission Types

| 코드 | 미션 유형 | 검증 방식 | 설명 |
|------|----------|----------|------|
| **M1** | TIME_WINDOW | 서버 시간 비교 | 특정 요일·시간대 방문 인증 |
| **M2** | DWELL | 체크인/체크아웃 시간차 | 매장 내 일정 시간 체류 인증 |
| **M3** | RECEIPT | AI Vision (영수증 OCR) | 영수증 촬영 → 특정 상품 구매 확인 |
| **M4** | INVENTORY | AI Vision (이미지 비교) | 상품 사진 촬영 → 기준 이미지와 동일 상품 판별 |
| **M5** | STAMP | 일별 방문 카운트 | N회 반복 방문 달성 시 리워드 |

---

## Database Schema

```
users ──────────< mission_attempts
  │                     │
  │                     │
  └──────────< reward_ledger >──────── mission_definitions >──────── stores
```

### ERD

| 테이블 | 주요 컬럼 | 설명 |
|--------|----------|------|
| **users** | `id`, `username`, `password`, `point`, `role` | 사용자 (USER / OWNER) |
| **stores** | `id`, `name`, `address`, `lat`, `lng`, `owner_id`, `icon_emoji` | 매장 정보 + 좌표 |
| **mission_definitions** | `id`, `store_id`, `type`, `config_json`, `reward_amount`, `is_active` | 미션 정의 및 설정 |
| **mission_attempts** | `id`, `user_id`, `mission_id`, `status`, `attempt_date`, `ai_result_json` | 미션 시도 기록 |
| **reward_ledger** | `id`, `user_id`, `mission_id`, `amount` | 리워드 지급 이력 (유저당 미션 1회 제한) |

- **DB 마이그레이션**: Flyway V1~V8, 서버 시작 시 자동 실행
- **인덱스**: `(user_id, mission_id)`, `(user_id, mission_id, attempt_date)` 로 조회 최적화
- **제약조건**: `reward_ledger`에 `UNIQUE(user_id, mission_id)` → 미션당 중복 보상 방지

---

## AI Integration

영수증 판독(M3)과 상품 이미지 비교(M4)에 **AI Vision**을 활용합니다.

- **모델**: `gemini-2.5-flash` (Elice Cloud ML API 경유)
- **선택 이유**: 빠른 응답 속도와 충분한 정확도를 위해 `gemini-2.5-flash`를 채택하였습니다. 실시간 사용자 경험을 해치지 않으면서도 영수증 OCR 및 이미지 비교에 필요한 멀티모달 성능을 제공합니다.
- **호출 방식**: Spring Boot에서 `RestClient`로 OpenAI 호환 API를 직접 호출 (별도 AI 서버 없음)
- **영수증 분석 (M3)**: 이미지에서 문자를 직접 읽어 대상 상품명과 글자 형태 유사도로 매칭, 공백 차이 무시
- **인벤토리 비교 (M4)**: 사용자 촬영 사진과 기준 상품 이미지를 비교, 브랜드·이름·패키징 기준 동일 상품 판별
- **응답 형식**: `{ match, confidence, retryHint, reason }` JSON → confidence threshold 초과 시 성공 처리
- **에러 처리**: AI 응답 파싱 실패 시 안전하게 `match=false` 반환, 네트워크 타임아웃 60초

---

## Tech Points

### 1. AI Vision 기반 실시간 미션 검증
- `gemini-2.5-flash` 모델을 통한 빠른 멀티모달 분석으로 사용자 대기 시간 최소화
- 영수증 OCR에서 공백/줄바꿈을 무시하는 유연한 문자열 매칭 프롬프트 설계
- AI 응답 실패 시에도 서비스가 중단되지 않는 방어적 에러 핸들링

### 2. Stateless JWT 인증 + 역할 기반 접근 제어
- Spring Security 필터 체인에 JWT 인증 필터를 통합한 무상태 아키텍처
- `USER` / `OWNER` 역할 분리로 매장 관리와 미션 참여를 분리

### 3. Flyway 기반 DB 마이그레이션
- V1~V8의 순차적 스키마 마이그레이션으로 안전한 DB 스키마 변경 관리
- 서버 시작 시 자동 실행되어 배포 파이프라인 단순화

### 4. 거리 기반 매장 정렬 (Haversine Formula)
- 사용자 좌표 기반 매장 거리 계산 및 가까운 순 정렬
- Kakao Geocoding API 연동으로 주소 → 좌표 자동 변환

### 5. S3 Presigned URL을 활용한 이미지 업로드
- 서버 부하 없이 클라이언트에서 S3에 직접 업로드
- 10분 만료 URL로 보안 확보

### 6. Soft Delete & 중복 보상 방지
- 미션 삭제 시 `is_active` 플래그를 사용한 논리적 삭제로 데이터 보존
- `reward_ledger` UNIQUE 제약조건으로 미션당 1회만 보상 지급

---

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose

### 1. 인프라 실행 (MySQL + Redis)

```bash
docker-compose up -d
```

### 2. 환경변수 설정

```bash
export INVENTORY_API_URL=<Elice API URL>
export INVENTORY_API_KEY=<Elice API Key>
export KAKAO_API_KEY=<Kakao REST API Key>
export JWT_SECRET=<최소 32바이트 시크릿 키>
```

### 3. 서버 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. API 문서 확인

서버 실행 후 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)에서 Swagger UI를 통해 전체 API 명세를 확인할 수 있습니다.

### Test

```bash
./gradlew test
```

---

## Known Issues

### INVENTORY(M4) 미션 등록 시 DB 미저장 문제

OWNER가 인벤토리(M4) 미션을 등록할 때, 정답 이미지를 S3에 업로드하고 Presigned URL을 발급받아 미션을 생성하는 플로우에서 **API 응답은 200으로 정상 반환되지만 실제 DB에 해당 미션이 저장되지 않는 문제**가 확인되었습니다.

- **재현 경로**: Presigned URL 발급 → S3 이미지 업로드 → 미션 생성 API 호출 → 200 응답 → DB에 미션 레코드 없음
- **상태**: 미해결

---

## Related Repositories

| Repository | 설명 |
|------------|------|
| [localhost-Mobile](https://github.com/yabsed/localhost-Mobile) | React Native 모바일 클라이언트 |
| [localhost_PC](https://github.com/user983740/localhost_PC) | 관리자 웹 클라이언트 |
