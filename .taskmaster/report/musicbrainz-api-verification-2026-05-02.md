# MusicBrainz API 어댑터 실서버 검증 리포트

## 메타

- 작성일: 2026-05-02
- 작성자: Claude (BD-8 작업)
- 대상: `music-metadata-service` 의 MusicBrainz 어댑터 (Spotify → MusicBrainz 전환)
- 검증 도구: `curl` (HTTP 1.1, port 18081)
- 범위: `/dev/musicbrainz/*` 4개 엔드포인트 + 어댑터 동작 (rate limit, 404, User-Agent)
- 상태: **실서버 검증 대기 중** (로컬 PostgreSQL 미기동으로 `./gradlew bootRun` 부팅 실패)

## 자동화된 검증 (완료)

- `./gradlew spotlessApply` — 통과
- `./gradlew compileKotlin` — 통과
- `./gradlew test` (`MusicMetadataServiceApplicationTests.contextLoads`) — 통과
  - 테스트 프로필에 `spotify` / `musicbrainz` / `external.music.provider=musicbrainz` 더미 설정 추가
- `./gradlew build` — 통과

## 실서버 검증 — 대기 중인 이유

`./gradlew bootRun` 실행 시 다음 오류로 부팅 실패:

```
HibernateException: Unable to determine Dialect without JDBC metadata
(please set 'jakarta.persistence.jdbc.url' for common cases ...)
```

원인: `local` 프로필은 외부 PostgreSQL 을 요구하며 (`ddl-auto: validate`), 다음 환경변수가 필요함:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`

현재 로컬 셸에 DB 환경변수 미설정 + Postgres 미기동 → 부팅 불가.

## 검증해야 할 항목 (DB 기동 후 수행)

### 사전 조건

```bash
# 1) PostgreSQL 기동 (예시)
docker run -d --name bandage-pg \
  -e POSTGRES_USER=bandage -e POSTGRES_PASSWORD=bandage \
  -e POSTGRES_DB=music_metadata -p 5432:5432 postgres:16

# 2) .env 작성 (gitignore 됨)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=music_metadata
DB_USERNAME=bandage
DB_PASSWORD=bandage
EXTERNAL_MUSIC_PROVIDER=musicbrainz
MUSICBRAINZ_USER_AGENT=BandageMusicMetadata/0.1.0
MUSICBRAINZ_CONTACT=contact@bandage.com

# 3) 부팅
PROFILE_ACTIVE=local ./gradlew bootRun
```

### 테스트 케이스

#### 1. recording 검색 (성공)

```bash
curl -G "http://localhost:18081/dev/musicbrainz/recording/search" \
  --data-urlencode "query=Doxy AND artist:Miles Davis" \
  --data-urlencode "limit=3"
```

기대: 200 + `[{ id: "...mbid...", title: "Doxy", lengthMs: ..., artists: [...], isrcs: [...] }, ...]`

#### 2. recording MBID lookup (성공)

```bash
# Miles Davis 의 So What 녹음 (1959 Kind of Blue)
curl "http://localhost:18081/dev/musicbrainz/recording/2db4eb7c-cb18-4abc-92f8-f3b18e8d6738"
```

기대: 200 + 도메인 객체 단일

#### 3. recording lookup 404 → null

```bash
curl -i "http://localhost:18081/dev/musicbrainz/recording/00000000-0000-0000-0000-000000000000"
```

기대: 200 (Spring 이 null 직렬화) — 어댑터에서 404 → null 반환

#### 4. artist 검색 (성공)

```bash
curl -G "http://localhost:18081/dev/musicbrainz/artist/search" \
  --data-urlencode "query=Beatles" \
  --data-urlencode "limit=3"
```

기대: 200 + Artist 도메인 리스트

#### 5. artist MBID lookup

```bash
# The Beatles
curl "http://localhost:18081/dev/musicbrainz/artist/b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d"
```

기대: 200 + `{ id, name: "The Beatles", sortName: "Beatles, The", country: "GB", type: "Group" }`

#### 6. Rate limit throttle (어댑터 내부)

연속 5회 호출 → 첫 호출 외 4회는 ~1초씩 지연되어야 함:

```bash
for i in 1 2 3 4 5; do
  /usr/bin/time -p curl -s -o /dev/null \
    "http://localhost:18081/dev/musicbrainz/artist/b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d" 2>&1 | grep real
done
```

기대: 첫 호출만 <1s, 나머지는 약 1.0s.

#### 7. User-Agent 헤더 부착

서버 access log 또는 MusicBrainz 측 로그에서 `User-Agent: BandageMusicMetadata/0.1.0 ( contact@bandage.com )` 확인.
또는 로컬 mock proxy 로 헤더 확인.

#### 8. Spotify 어댑터 비활성 확인

```bash
curl -i "http://localhost:18081/dev/spotify/token"
```

기대: 404 (컨트롤러 빈 미등록 — `external.music.provider=musicbrainz` 기본값)

`external.music.provider=spotify` 로 재기동 시 200 (재활성화 검증).

## 권장 조치 (실서버 검증 후 수행)

- 실패 케이스 발견 시 P0/P1/P2 분류 및 수정 PR
- 정상 동작 시 본 리포트의 status 를 "완료" 로 갱신하고 PR 본문에 링크

## 프론트 관련 구현 지점

(해당 없음 — 백엔드 단독 작업, FE 호출 지점 변경 없음)

## 재현용 페이로드 위치

본 리포트 내 curl 스니펫.
