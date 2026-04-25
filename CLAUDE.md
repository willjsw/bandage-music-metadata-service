# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew bootRun          # Run the application (local profile, port 18081)
./gradlew build            # Build (also runs tests + kapt)
./gradlew test             # Run tests
./gradlew spotlessApply    # Format code (must pass before commit)
./gradlew kaptKotlin       # Regenerate QueryDSL Q-classes after entity changes
```

Run a single test:
```bash
./gradlew test --tests "com.bandage.musicmetadataservice.SomeTest"
./gradlew test --tests "com.bandage.musicmetadataservice.SomeTest.someMethod"
```

## Architecture

This is a **hexagonal (ports & adapters)** service that exposes music metadata sourced from Spotify. Kotlin 2.2.x + Spring Boot 4.x + JDK 21.

### Package Layout

```
com.bandage.musicmetadataservice/
├── domain/                       # Pure domain (no Spring, no JPA)
│   ├── model/
│   └── exception/
├── application/
│   ├── port/inbound/             # Use-case interfaces (driving ports)
│   ├── port/outbound/            # Interfaces the domain needs from outside (driven ports)
│   └── service/                  # Use-case implementations; depend on port/outbound only
├── adapter/
│   ├── inbound/web/              # Spring MVC controllers + DTOs; call port/inbound
│   └── outbound/
│       ├── persistence/          # JPA entities, Spring Data + QueryDSL repos
│       └── external/spotify/     # Ktor-based Spotify Web API client
├── config/                       # Spring @Configuration (Ktor client, Swagger, etc.)
└── common/                       # Cross-cutting utils (UUIDv7, etc.)
```

`inbound`/`outbound` is used instead of `in`/`out` because `in` is a Kotlin reserved keyword.

### Key Patterns

**Dependency direction** — domain inward, adapters outward. JPA entities and Spotify response DTOs must **never** appear in `domain/` or `application/`. Translate to/from domain models at the adapter boundary.

**Adding a feature**:
1. Define the use case as an interface in `application/port/inbound`.
2. Define any persistence/external contracts in `application/port/outbound`, phrased in domain terms.
3. Implement the use case in `application/service`, depending only on port interfaces.
4. Wire concrete adapters in `adapter/outbound/...` and `adapter/inbound/web/`.

**Primary keys** — use UUIDv7 via `com.github.f4b6a3:uuid-creator` (or `@UuidGenerator(style = VERSION_7)` for JPA), not `UUID.randomUUID()`.

**External calls** — Spotify integration uses Ktor 2.3.x (CIO engine), **not** Spring's `WebClient`/`RestClient`. Configure the `HttpClient` as a Spring bean in `config/`.

### Active Profiles

Default active profile is `local` (set via `PROFILE_ACTIVE` env var). Profile groups in `application.yaml`:
- `local` → includes: `local`, `default-datasource`, `swagger`, `redis`, `security`
- `dev`   → includes: `dev`, `dev-datasource`, `swagger`, `redis`, `security`

⚠️ Several profile YAMLs referenced by the groups (`application-redis.yaml`, `application-security.yaml`, `application-dev.yaml`, `application-dev-datasource.yaml`) do not yet exist in `src/main/resources/`. Anything that **requires** Redis/security config must add the corresponding YAML — Spring will silently ignore the missing file.

Required env vars (loaded from `.env` locally — not committed):
- DB: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- Spotify: `SPOTIFY_CLIENT_ID`, `SPOTIFY_CLIENT_SECRET`

JPA runs with `ddl-auto: validate` against PostgreSQL — schema mismatches fail boot, they don't migrate. The `test` profile uses in-memory H2 in PostgreSQL-compat mode with `create-drop`.

### Tech Stack

- **DB**: PostgreSQL + Spring Data JPA + QueryDSL 5.1.0 (jakarta classifier, via **kapt**)
- **External HTTP**: Ktor client 2.3.x (CIO + content negotiation + Jackson)
- **Docs**: SpringDoc OpenAPI — Swagger UI at `/swagger-ui.html`, JSON at `/api-docs`
- **Formatting**: Spotless 7.2.1
- **IDs**: UUIDv7 via `uuid-creator` 6.0.0
- **Spring plugins**: `kotlin("plugin.spring")`, `kotlin("plugin.jpa")` — JPA entities don't need explicit no-arg constructors

### CI

`.github/workflows/develop_ci_test.yml` runs `./gradlew build` on PRs to `develop` and pushes to `develop`. **`develop` is the integration branch** for feature work; PRs should typically target `develop`, not `main`.

## Pull Request Convention

When writing a PR description, always follow `.github/PULL_REQUEST_TEMPLATE.md` and write all content in valid Markdown syntax.

## Commit Convention

Always use this format for every commit message:

```
{type}: {summary}
- {detail 1}
- {detail 2}
- {detail n}

#{issue-number}
```

**Types**: `chore`, `feat`, `ai`, `test`, `refactor`, `fix`

- `{summary}` — concise description of the change
- bullet list — one line per meaningful change (omit if only one trivial change)
- `#{issue-number}` — **REQUIRED** when working on a branch tied to a GitHub issue (e.g. `feat/#12-spotify-search` → `#12`). Always place on its own line at the very end, after a blank line. Omit only if there is genuinely no related issue.

**Example:**
```
feat: Spotify 트랙 검색 API 구현
- SpotifySearchPort, SpotifySearchAdapter 추가
- SearchTrackUseCase 구현
- GET /tracks/search 엔드포인트 추가

#12
```
