## 🎵 Music Metadata Service

### 📝 모듈 개요 (Overview)

Bandage 생태계에서 **음악 메타데이터(트랙 / 앨범 / 아티스트)를 책임지는 단일 출처(Single Source of Truth)** 마이크로서비스.
외부 음원 플랫폼(Spotify) 연동을 캡슐화하여, 합주곡 선정·셋리스트 구성 등 도메인 서비스(`v1`)가 외부 API의 변경·실패·레이트리밋에 직접 노출되지 않도록 격리한다.

The Bandage ecosystem's single source of truth for music metadata (tracks, albums, artists).
It encapsulates external music platform integrations (Spotify) so that domain services (e.g. `v1`) — practice song selection, setlist composition — are insulated from external API changes, failures, and rate limits.

### 🎯 책임 범위 (Responsibilities)

- **외부 음원 플랫폼 연동** — Spotify Web API에서 트랙·앨범·아티스트 메타데이터를 조회·정규화한다.
- **메타데이터 영속화** — 조회 결과를 PostgreSQL에 캐시/적재하여 반복 호출 비용과 외부 API 의존도를 낮춘다.
- **도메인 중립적 모델 노출** — 내부 클라이언트(`v1` 등)에는 Spotify 응답이 아닌 자체 도메인 모델만 노출한다. 추후 다른 음원 제공자(YouTube Music, Apple Music 등)로 교체·확장이 가능하도록 설계한다.

**Out of scope** — 사용자 인증, 합주/공연 일정, 셋리스트 관리 등은 본 서비스의 책임이 아니다. 해당 로직은 `v1` (메인 모놀리스)에 위치한다.

### 🏗 아키텍처 (Architecture)

**헥사고날 아키텍처 (Ports & Adapters)**
- 도메인(`domain`)과 유스케이스(`application`)는 Spring·JPA·Spotify SDK에 의존하지 않는다.
- 외부 의존(JPA, Spotify HTTP)은 모두 `adapter/outbound`의 어댑터로 격리되며, `application/port/outbound` 인터페이스를 구현한다.
- 음원 제공자 교체 / 영속 계층 변경 시 도메인 코드는 수정하지 않는다.

**비동기 외부 호출 (Async Outbound Calls)**
- Spotify 호출은 Ktor Client(CIO 엔진) + Kotlin Coroutines 기반 논블로킹으로 수행한다. Spring MVC 스레드를 외부 I/O 대기로 점유하지 않는다.

자세한 패키지 구조 및 개발 규칙은 [`CLAUDE.md`](./CLAUDE.md) 참조.

### 🛠 기술 스택 (Tech Stack)

- **Language / Runtime**: Kotlin 2.2 / JDK 21
- **Framework**: Spring Boot 4.x (Web MVC, Data JPA, Validation, Actuator)
- **Persistence**: PostgreSQL + QueryDSL 5.1 (kapt)
- **External HTTP**: Ktor Client 2.3 (CIO + Jackson)
- **API Docs**: SpringDoc OpenAPI (Swagger UI)
- **IDs**: UUIDv7 (`uuid-creator`)
- **Format**: Spotless

### 🚀 시작하기 (Getting Started)

**Prerequisites**
- JDK 21
- PostgreSQL 15+
- Spotify Developer 앱 (Client ID / Secret)

**Environment**
`.env` 또는 환경 변수로 다음을 설정한다.
```
DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD
SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET
PROFILE_ACTIVE=local   # 기본값
```

**Run**
```bash
./gradlew bootRun       # http://localhost:18081
```

- Swagger UI: `http://localhost:18081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:18081/api-docs`
