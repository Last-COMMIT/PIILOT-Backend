<p align="center">
  <h1 align="center">🛡️ PIILOT Backend</h1>
  <p align="center">
    <b>Personal Information Intelligent Leakage Observation Tool</b>
    <br />
    AI 기반 개인정보 유출 탐지 및 보호 플랫폼 — Backend API Server
    <br /><br />
    <a href="http://localhost:8080/swagger-ui.html">Swagger UI</a>
    ·
    <a href="#api-endpoints">API Endpoints</a>
    ·
    <a href="#getting-started">Getting Started</a>
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot_3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Gradle_8.14-02303A?style=for-the-badge&logo=gradle&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Redis-DD0031?style=for-the-badge&logo=redis&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
  <img src="https://img.shields.io/badge/AWS_S3-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white" />
</p>

---

## 📋 목차

- [프로젝트 소개](#프로젝트-소개)
- [핵심 기능](#핵심-기능)
- [시스템 아키텍처](#시스템-아키텍처)
- [기술 스택](#기술-스택)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [프로젝트 구조](#프로젝트-구조)
- [데이터베이스](#데이터베이스)
- [기술적 의사결정](#기술적-의사결정)
- [환경 변수](#환경-변수)

---

## 프로젝트 소개

**PIILOT**은 기업 내부의 데이터베이스와 파일 서버에 저장된 **개인정보(PII)**를 AI가 자동으로 탐지하고, 암호화 상태를 점검하며, 마스킹까지 처리하는 통합 관제 플랫폼입니다.

> 개인정보 보호법, GDPR 등 강화되는 규제 환경에서 기업이 보유한 데이터를 안전하게 관리할 수 있도록 돕습니다.

### 서비스 구성

| 서비스 | 기술 | 포트 | 역할 |
|--------|------|------|------|
| **Frontend** | Next.js 16 + React 19 | 3000 | 사용자 인터페이스 |
| **Backend** | Spring Boot 3.5 | 8080 | REST API, 비즈니스 로직 |
| **AI Server** | FastAPI + PyTorch | 8000 | PII 탐지, 마스킹, 챗봇 |
| **Database** | PostgreSQL 14+ | 5432 | 메인 데이터베이스 |
| **Cache** | Redis 7+ | 6379 | 마스킹 결과 캐싱 |

---

## 핵심 기능

### 🔍 DB 개인정보 탐지
- 기업 DB에 연결하여 테이블/컬럼 스키마를 자동 스캔
- AI(KoELECTRA NER + RAG)가 이름, 주민번호, 전화번호 등 **8종 PII** 자동 식별
- XGBoost 분류기로 각 컬럼의 **암호화 상태** 판별
- 비암호화 데이터 레코드 PK 목록 추출 및 이슈 자동 생성
- **증분 스캔**으로 변경된 테이블만 재분석 (재스캔 시 99.88% 성능 향상)

### 📁 파일 개인정보 탐지
- SFTP / FTP / WebDAV 파일 서버 연결 지원
- **멀티모달 PII 탐지**: 문서(PDF/DOCX), 이미지(얼굴 탐지), 음성(STT), 영상
- 비동기 스캔으로 대규모 파일 서버 처리 (TransactionSynchronization + @Async)

### 🎭 AI 마스킹
- 탐지된 PII를 AI가 자동 마스킹 (텍스트 치환, 얼굴 블러, 음성 처리)
- **"미리보기 → 저장" 2단계 플로우** + Redis 캐싱으로 저장 API 99.71% 단축
- 원본 파일 ZIP 암호화(AES) 보관 + 마스킹 파일 파일서버 업로드

### 💬 AI 챗봇 & 법규 검색
- LangGraph Self-RAG 기반 개인정보 보호 Q&A 챗봇
- pgvector + Flashrank 재랭킹으로 법규/규정 의미 검색

### 📊 대시보드
- DB/파일 스캔 현황 요약 통계
- PII 유형별 탐지 트렌드
- 최근 이슈 목록

---

## 시스템 아키텍처

```
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│  ┌──────────┐       ┌──────────────┐       ┌──────────────┐         │
│  │ Frontend │──────▶│   Backend    │──────▶│  AI Server   │         │
│  │ Next.js  │  API  │ Spring Boot  │  HTTP │   FastAPI    │         │
│  │  :3000   │       │    :8080     │       │    :8000     │         │
│  └──────────┘       └──────┬───────┘       └──────┬───────┘         │
│                            │                      │                  │
│                    ┌───────┼──────────┐     ┌─────┴──────┐          │
│                    │       │          │     │  pgvector   │          │
│                    ▼       ▼          ▼     │ (Vector DB) │          │
│              ┌─────────┐ ┌─────┐ ┌──────┐  └────────────┘          │
│              │PostgreSQL│ │Redis│ │AWS S3│                           │
│              │  :5432   │ │:6379│ │      │                           │
│              └─────────┘ └─────┘ └──────┘                           │
│                                                                      │
│              ┌─────────────────────────────┐                         │
│              │     Target Data Sources     │                         │
│              │  ┌─────┐ ┌────┐ ┌───────┐  │                         │
│              │  │MySQL│ │SFTP│ │WebDAV │  │                         │
│              │  │ DB  │ │FTP │ │  S3   │  │                         │
│              │  └─────┘ └────┘ └───────┘  │                         │
│              └─────────────────────────────┘                         │
└──────────────────────────────────────────────────────────────────────┘
```

### 요청 흐름

```
[DB 스캔 흐름]
Backend → JDBC로 대상 DB 스키마 수집 → AI 서버에 PII 식별 요청
→ AI 서버에 암호화 확인 요청 → Risk Level 계산 → 이슈 자동 생성

[파일 마스킹 흐름]
Frontend → Backend (마스킹 요청) → Redis 캐시 확인
  → [MISS] AI 서버 마스킹 → Redis 저장 (TTL 30분) → 미리보기 반환
  → [HIT] Redis에서 즉시 반환 (12ms)
Frontend → Backend (저장 요청, password만 전송)
  → Redis 조회 → ZIP 암호화 → 파일서버 업로드 → Redis 삭제
```

---

## 기술 스택

### Backend

| 기술 | 버전 | 용도 |
|------|------|------|
| **Java** | 21 | 프로그래밍 언어 |
| **Spring Boot** | 3.5.9 | REST API 프레임워크 |
| **Spring Security** | 6.x | 인증/인가 (JWT) |
| **Spring Data JPA** | 3.x | ORM |
| **QueryDSL** | 5.1.0 | 타입 안전한 동적 쿼리 |
| **Spring Data Redis** | 3.x | 캐시 (Lettuce 클라이언트) |
| **Flyway** | 10.x | 데이터베이스 마이그레이션 |
| **WebClient** | 6.x | AI 서버 비동기 HTTP 통신 |
| **AWS SDK v2** | 2.25.0 | S3 Presigned URL |

### Infrastructure

| 기술 | 버전 | 용도 |
|------|------|------|
| **PostgreSQL** | 14+ | 메인 데이터베이스 |
| **Redis** | 7+ | 마스킹 결과 캐싱 |
| **Docker** | - | 컨테이너화 |
| **Gradle** | 8.14.3 | 빌드 도구 |

### Libraries

| 라이브러리 | 용도 |
|-----------|------|
| **JJWT** 0.12.6 | JWT 토큰 생성/검증 |
| **JSch** 0.1.55 | SFTP 파일 전송 |
| **Apache Commons Net** 3.11.1 | FTP 파일 전송 |
| **Sardine** 5.10 | WebDAV 파일 전송 |
| **zip4j** 2.11.5 | ZIP AES 암호화 |
| **SpringDoc OpenAPI** 2.7.0 | Swagger API 문서화 |

### AI Server (연동)

| 기술 | 용도 |
|------|------|
| **KoELECTRA NER** | 한국어 개인정보 개체명 인식 |
| **YOLOv12n-Face** | 이미지/영상 얼굴 탐지 |
| **Whisper Large-v3** | 음성→텍스트 변환 |
| **XGBoost** | 암호화 여부 판별 |
| **LangGraph** | Self-RAG 챗봇 워크플로우 |
| **pgvector** | 법규 벡터 검색 |

---

## Getting Started

### Prerequisites

- **Java 21+**
- **PostgreSQL 14+**
- **Redis 7+**
- **Gradle 8.x** (Wrapper 포함)

### 1. 데이터베이스 설정

```bash
# PostgreSQL에 데이터베이스 생성
createdb piilot_db

# pgvector 확장 설치 (AI 서버 벡터 검색용)
psql piilot_db -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### 2. 환경 변수 설정

```bash
export DB_URL=jdbc:postgresql://localhost:5432/piilot_db
export DB_USERNAME=piilot_user
export DB_PASSWORD=piilot_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=your-256-bit-secret-key
export AES_KEY=piilot-aes-256-secret-key-32by!!
export AI_SERVER_URL=http://localhost:8000
export CORS_ORIGINS=http://localhost:3000
```

### 3. 빌드 & 실행

```bash
# 빌드
./gradlew build

# 실행 (local 프로필)
./gradlew bootRun

# 또는 JAR 실행
java -jar build/libs/piilot-*.jar --spring.profiles.active=local
```

### 4. Docker로 실행

```bash
# 테스트용 파일 서버 (WebDAV, SFTP, FTP)
docker-compose up -d

# 접속 정보: piilot / piilot123
# WebDAV: localhost:8081
# SFTP:   localhost:2222
# FTP:    localhost:21
```

### 5. 확인

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

## API Endpoints

모든 API는 `CommonResponse<T>` 래퍼로 응답합니다.

```json
{
  "success": true,
  "code": "COMMON200",
  "message": "요청이 성공했습니다.",
  "result": { },
  "timestamp": "2026-02-25T10:30:00"
}
```

### 인증 (Auth)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/auth/signup` | 회원가입 | - |
| POST | `/api/auth/login` | 로그인 (JWT 발급) | - |
| POST | `/api/auth/refresh` | 토큰 갱신 | - |

### DB 연결 관리

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/db-connections` | DB 연결 생성 |
| GET | `/api/db-connections` | 연결 목록 조회 |
| GET | `/api/db-connections/{id}` | 연결 상세 조회 |
| PUT | `/api/db-connections/{id}` | 연결 수정 |
| DELETE | `/api/db-connections/{id}` | 연결 삭제 |
| POST | `/api/db-connections/{id}/scan` | DB 스캔 시작 |
| GET | `/api/db-connections/stats` | 연결 통계 |

### DB 개인정보 관리

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/db-pii/connections` | 연결 필터 목록 |
| GET | `/api/db-pii/connections/{id}/tables` | 테이블 목록 |
| GET | `/api/db-pii/columns` | PII 컬럼 목록 (필터/페이징) |
| GET | `/api/db-pii/issues` | 이슈 목록 |
| GET | `/api/db-pii/issues/{id}` | 이슈 상세 (비암호화 데이터 포함) |
| PATCH | `/api/db-pii/issues/{id}/status` | 이슈 상태 변경 |

### 파일 연결 관리

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/file-connections` | 파일 서버 연결 생성 |
| GET | `/api/file-connections` | 연결 목록 조회 |
| GET | `/api/file-connections/{id}` | 연결 상세 조회 |
| PUT | `/api/file-connections/{id}` | 연결 수정 |
| DELETE | `/api/file-connections/{id}` | 연결 삭제 |
| POST | `/api/file-connections/{id}/scan` | 파일 스캔 시작 |
| GET | `/api/file-connections/{id}/scan/{historyId}` | 스캔 상태 조회 |
| GET | `/api/file-connections/stats` | 연결 통계 |

### 파일 개인정보 & 마스킹

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/file-pii/connections` | 연결 필터 목록 |
| GET | `/api/file-pii/files` | 파일 목록 (필터/페이징) |
| GET | `/api/file-pii/issues` | 이슈 목록 |
| PATCH | `/api/file-pii/issues/{id}/status` | 이슈 상태 변경 |
| GET | `/api/file-masking/files` | 마스킹 대상 파일 목록 |
| GET | `/api/file-masking/files/{id}/preview` | 원본 파일 미리보기 |
| POST | `/api/file-masking/files/{id}/mask` | AI 마스킹 요청 (Redis 캐싱) |
| POST | `/api/file-masking/files/{id}/save` | 마스킹 결과 ZIP 저장 |

### 대시보드 & 기타

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/dashboard/summary` | 대시보드 요약 통계 |
| GET | `/api/dashboard/trends` | 트렌드 데이터 |
| GET | `/api/notices` | 공지사항 목록 |
| POST | `/api/admin/notices` | 공지사항 생성 (관리자) |
| POST | `/api/law-search` | 법규 검색 (AI 연동) |
| POST | `/api/chatbot` | AI 챗봇 질의 |
| GET | `/api/notifications` | 알림 목록 |
| POST | `/api/documents/presigned-url` | S3 Presigned URL 생성 |
| POST | `/api/documents` | 문서 저장 + AI 임베딩 |

---

## 프로젝트 구조

```
src/main/java/com/lastcommit/piilot/
│
├── domain/                          # 비즈니스 도메인
│   ├── dbscan/                     # DB 스캔 & PII 탐지
│   │   ├── controller/            # REST API
│   │   ├── service/               # 스캔 파이프라인, PII/이슈 관리
│   │   ├── repository/            # JPA + QueryDSL
│   │   ├── entity/                # DB 연결, 테이블, PII 컬럼, 이슈
│   │   ├── dto/                   # Request/Response DTO
│   │   ├── docs/                  # Swagger 문서 인터페이스
│   │   └── exception/             # 도메인 에러 코드
│   │
│   ├── filescan/                   # 파일 스캔 & 마스킹
│   │   ├── controller/            # 파일 연결, PII, 마스킹 API
│   │   ├── service/               # 스캔, 다운로드, 업로드, 마스킹
│   │   ├── repository/            # JPA + QueryDSL
│   │   ├── entity/                # 파일 연결, 파일, PII, 이슈, 마스킹 로그
│   │   ├── dto/
│   │   ├── docs/
│   │   └── exception/
│   │
│   ├── dashboard/                  # 대시보드 분석
│   ├── document/                   # 문서 관리 (S3 + AI 임베딩)
│   ├── notice/                     # 공지사항
│   ├── notification/               # 알림
│   ├── regulation/                 # 법규 검색
│   ├── chatbot/                    # AI 챗봇
│   ├── user/                       # 사용자 엔티티
│   └── shared/                     # 공유 Enum & BaseEntity
│
├── global/                          # 전역 인프라
│   ├── auth/                       # JWT 인증 (Filter, Provider, Service)
│   ├── config/                     # Spring 설정
│   │   ├── SecurityConfig         # Spring Security + CORS
│   │   ├── RedisConfig            # Lettuce + JSON 직렬화
│   │   ├── QueryDSLConfig         # JPAQueryFactory
│   │   ├── AsyncConfig            # 비동기 스레드풀
│   │   ├── WebClientConfig        # AI 서버 HTTP 클라이언트
│   │   ├── S3Config               # AWS S3 Presigner
│   │   └── SwaggerConfig          # OpenAPI 3.0
│   ├── error/                      # 전역 예외 처리
│   │   ├── exception/             # GeneralException
│   │   ├── handler/               # @ControllerAdvice
│   │   ├── response/              # CommonResponse<T>
│   │   └── status/                # 에러/성공 코드
│   ├── util/                       # AES-256-GCM 암호화
│   └── validation/                 # @ValidPage, @ValidSize
│
src/main/resources/
├── application.yml                  # 공통 설정
├── application-local.yml            # 로컬 개발 환경
├── application-dev.yml              # 개발 서버
└── db/migration/                    # Flyway 마이그레이션 (V1~V14)
```

### 설계 원칙

| 원칙 | 적용 |
|------|------|
| **계층 분리** | Controller → Service → Repository → Entity |
| **도메인 분리** | 도메인별 독립적 패키지 (`dbscan/`, `filescan/` 등) |
| **Swagger 분리** | Controller와 Docs 인터페이스 분리하여 비즈니스 로직과 문서 분리 |
| **SRP** | 외부 연동 로직을 별도 컴포넌트로 분리 (예: `FileDownloader`, `DbConnectionTester`) |
| **Profile 기반 AI 클라이언트** | `StubAiServerClient` (local) / `RealAiServerClient` (dev, prod) |

---

## 데이터베이스

### ERD

<p align="center">
  <img src="project_docs/ERD_PIILOT_v1.5.png" alt="PIILOT ERD" width="100%" />
</p>

### 주요 테이블 (21개)

| 도메인 | 테이블 | 설명 |
|--------|--------|------|
| **User** | `users` | 사용자 (USER/ADMIN) |
| **DB Scan** | `db_server_connections` | DB 서버 연결 정보 |
| | `db_tables` | 스캔된 DB 테이블 |
| | `db_scan_history` | DB 스캔 이력 |
| | `db_pii_columns` | DB 개인정보 컬럼 |
| | `db_pii_issues` | DB 개인정보 이슈 |
| **File Scan** | `file_server_connections` | 파일 서버 연결 정보 |
| | `files` | 스캔된 파일 |
| | `file_scan_history` | 파일 스캔 이력 |
| | `file_pii` | 파일 개인정보 |
| | `file_pii_issues` | 파일 개인정보 이슈 |
| | `masking_logs` | 마스킹 처리 로그 |
| **Shared** | `pii_types` | PII 유형 8종 (이름, 주민번호, 전화번호 등) |
| | `dbms_types` | DBMS 유형 (PostgreSQL, MySQL, Oracle) |
| | `file_server_types` | 파일 서버 유형 (FTP, SFTP, WebDAV) |
| | `file_type` | 파일 유형 (문서, 사진, 음성, 영상) |
| **Other** | `notices` | 공지사항 |
| | `document` | 문서 (법규, DB 용어사전) |
| | `notifications` | 알림 |

### PII 유형 (8종)

| 코드 | 한글명 | AI 코드 | 위험 가중치 |
|------|--------|---------|------------|
| NM | 이름 | p_nm | 0.7 |
| RRN | 주민번호 | p_rrn | 1.0 |
| ADD | 주소 | p_add | 0.5 |
| IP | IP주소 | p_ip | 0.3 |
| PH | 전화번호 | p_ph | 0.7 |
| ACN | 계좌번호 | p_acn | 0.9 |
| PP | 여권번호 | p_pp | 0.8 |
| EM | 이메일 | p_em | 0.5 |

### Flyway 마이그레이션

| 버전 | 설명 |
|------|------|
| V1 | 전체 테이블 생성 + 인덱스 + 트리거 |
| V2 | 초기 데이터 (DBMS 유형, PII 유형, 파일 유형) |
| V3 | FTP/SFTP 서버 유형 추가 |
| V4 | DB/파일 연결 유니크 제약 추가 |
| V5 | files 테이블 file_size 컬럼 추가 |
| V6 | pgvector 벡터 테이블 생성 (법규, 용어사전) |
| V7 | files 테이블 이슈 관련 컬럼 추가 |
| V8 | WebDAV 서버 유형 추가 |
| V9 | 파일 스캔 is_scanning 상태 컬럼 추가 |
| V10 | ZIP 파일 타입 추가 |
| V11 | db_pii_columns PK 컬럼명 추가 |
| V12 | 알림(notifications) 테이블 생성 |
| V13 | DocumentType ENUM 수정 |
| V14 | 파일 서버 유형 업데이트 |

---

## 기술적 의사결정

### 1. Redis 캐싱 전략 (파일 마스킹)

> 상세 문서: [`project_docs/tech-decision-redis-masking-cache.md`](project_docs/tech-decision-redis-masking-cache.md)

**문제**: 마스킹 "미리보기 → 저장" 2단계 플로우에서 마스킹 결과를 어디에 보관할 것인가?

**5개 저장소 후보 검토**:

| 후보 | 결과 | 탈락 사유 |
|------|------|----------|
| HttpSession | 탈락 | JWT STATELESS 정책과 충돌 |
| 로컬 캐시 (Caffeine) | 탈락 | 스케일아웃 시 캐시 공유 불가 + GC 부담 |
| **Redis** | **채택** | 분산 공유 + TTL + 기존 인프라 활용 |
| 파일시스템 | 탈락 | 분산 불가 + 정리 로직 부담 |
| DB (BYTEA) | 탈락 | 임시 데이터에 과도한 I/O 비용 |

**성과**:

| 지표 | Before | After |
|------|--------|-------|
| 마스킹 재요청 (85MB 영상) | 214,892ms | **89ms** (99.96%) |
| 저장 API (85MB 영상) | 23,194ms | **68ms** (99.71%) |
| 저장 시 네트워크 전송량 | Base64 ~113MB | password 수 KB |

### 2. DB 스캔 증분 스캔 (Phase 2)

> 상세 문서: [`project_docs/DB_SCAN_PERFORMANCE_REPORT.md`](project_docs/DB_SCAN_PERFORMANCE_REPORT.md)

**문제**: 전체 재스캔 시 206초 소요

**해결**: 테이블별 change_signature 비교로 변경된 테이블만 AI 서버에 전송

| 시나리오 | Before | After | 개선율 |
|----------|--------|-------|--------|
| 변경 없는 재스캔 | 206초 | **0.24초** | 99.88% |
| 1개 테이블 변경 | 206초 | **14.9초** | 92.8% |

> Phase 3(비동기)도 시도했으나 AI 서버의 순차 처리로 인해 10~29% 느려져 Phase 2 유지

### 3. S3 Presigned URL (파일 업로드)

> 상세 문서: [`project_docs/feat-53-file-upload-and-presignedUrl.md`](project_docs/feat-53-file-upload-and-presignedUrl.md)

**문제**: 대용량 문서(법규 PDF) 업로드 시 백엔드 메모리/대역폭 낭비

**해결**: 백엔드가 서명된 URL만 발급 → 프론트엔드가 S3에 직접 업로드

### 4. 비동기 파일 스캔 (TransactionSynchronization)

**문제**: `@Async` 호출 시 트랜잭션 커밋 전에 다른 스레드가 데이터를 조회하면 NULL

**해결**: `TransactionSynchronization.afterCommit()`으로 커밋 후 비동기 실행 보장

---

## 환경 변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/piilot_db` |
| `DB_USERNAME` | DB 사용자명 | `piilot_user` |
| `DB_PASSWORD` | DB 비밀번호 | `piilot_password` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `JWT_SECRET` | JWT 서명 비밀키 (256bit+) | - |
| `AES_KEY` | AES-256-GCM 암호화 키 (32byte) | - |
| `AI_SERVER_URL` | AI 서버 URL | `http://localhost:8000` |
| `CORS_ORIGINS` | 허용 Origin | `http://localhost:3000` |
| `AWS_ACCESS_KEY` | AWS S3 Access Key | - |
| `AWS_SECRET_KEY` | AWS S3 Secret Key | - |
| `S3_BUCKET_NAME` | S3 버킷명 | `piilot-documents` |

### 프로필 설정

| 프로필 | 용도 |
|--------|------|
| `local` | 로컬 개발 (기본 활성화) |
| `dev` | 개발 서버 |
| `prod` | 운영 서버 |
| `stub` | AI 서버 없이 Stub 응답으로 테스트 |

---

## 팀원

| 이름 | 역할 |
|------|------|
| **김찬우** | Backend Lead |

---

<p align="center">
  <sub>Built with ❤️ by Team LastCommit (KT AIVLE School 7기 3조)</sub>
</p>
