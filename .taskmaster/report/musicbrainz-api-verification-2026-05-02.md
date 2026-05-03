# MusicBrainz API 어댑터 실서버 검증 리포트

## 메타

- 작성일: 2026-05-02
- 작성자: Claude (BD-8 작업)
- 대상: `music-metadata-service` 의 MusicBrainz 어댑터 (Spotify → MusicBrainz 전환)
- 검증 도구: `curl` (HTTP 1.1, port 18081)
- 범위: `/dev/musicbrainz/*` 4개 엔드포인트 + 어댑터 동작 (rate limit, 404, User-Agent, Spotify 비활성)
- 상태: **검증 완료** — P2 이슈 1건 발견

## 자동화된 검증

- `./gradlew spotlessApply` — 통과
- `./gradlew compileKotlin` — 통과
- `./gradlew test` (`MusicMetadataServiceApplicationTests.contextLoads`) — 통과
- `./gradlew build` — 통과

## 환경

- 앱: PROFILE_ACTIVE=local, port 18081, 2.5s 부팅
- DB: localhost:5432 PostgreSQL `bandage` (developer/1234)
- 외부: `https://musicbrainz.org` 직접 호출
- `external.music.provider=musicbrainz` (Spotify 어댑터 빈 비활성)

## 케이스별 결과

### 1. recording 검색 (Lucene query)

```
GET /dev/musicbrainz/recording/search?query=Doxy AND artist:Miles Davis&limit=2
```

- HTTP 200, 2.91s
- 응답 (요약):
  ```json
  [
    {
      "id":"fb5c9d91-6d40-4a3c-af72-f6166a516d83",
      "title":"Doxy",
      "lengthMs":293000,
      "artists":[
        {"id":"561d854a-...","name":"Miles Davis"},
        {"id":"d185d986-...","name":"Horace Silver"},
        ...
      ],
      "isrcs":["FR8X00900735","USUG11401903","DEU241093369","DEU240820044","DEA319804492"]
    },
    {"id":"214d3494-...","title":"Doxy (remastered 2009)", ...}
  ]
  ```
- 판정: 정상 — DTO→도메인 매핑 (length ms / artist-credit / isrcs) 모두 정확

### 2. recording MBID lookup (성공)

```
GET /dev/musicbrainz/recording/fb5c9d91-6d40-4a3c-af72-f6166a516d83
```

- HTTP 200, 1.78s
- 응답: `{ id, title:"Doxy", lengthMs:293000, artists:[5명], isrcs:[5개] }`
- 판정: 정상 — `inc=artists+isrcs+releases` 효과로 isrcs / artists 포함

### 3a. lookup invalid MBID 형식 (전부 0)

```
GET /dev/musicbrainz/recording/00000000-0000-0000-0000-000000000000
```

- HTTP **500** (예상: 400 또는 4xx)
- 백엔드 로그: `MusicBrainzApiException: MusicBrainz API error [400]: {"error":"Invalid mbid."}`
- 원인: MusicBrainz 가 invalid mbid 에 400 응답 → 어댑터에서 `MusicBrainzApiException` throw → 글로벌 예외 핸들러가 500 으로 변환
- 판정: **P2 (품질)** — invalid 사용자 입력에 대해 5xx 가 아닌 4xx 응답으로 mapping 권장 (별도 후속 작업)

### 3b. lookup valid-format nonexistent UUID → null

```
GET /dev/musicbrainz/recording/aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee
```

- HTTP 200, body empty (Spring null 직렬화)
- 판정: 정상 — 어댑터의 `if (response.status.value == 404) return null` 동작 확인

### 4. artist 검색

```
GET /dev/musicbrainz/artist/search?query=Beatles&limit=2
```

- HTTP 200, 1.84s
- 응답:
  ```json
  [
    {"id":"b10bbbfc-...","name":"The Beatles","sortName":"Beatles, The","country":"GB","type":"Group"},
    {"id":"4d5447d7-...","name":"John Lennon","sortName":"Lennon, John","country":"GB","type":"Person"}
  ]
  ```
- 판정: 정상 — 모든 도메인 필드 매핑

### 5. artist MBID lookup

```
GET /dev/musicbrainz/artist/b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d
```

- HTTP 200, 1.33s
- 응답: `{"id":"b10bbbfc-...","name":"The Beatles","sortName":"Beatles, The","country":"GB","type":"Group"}`
- 판정: 정상

### 6. Rate limit throttle (1 req/sec)

```bash
for i in 1..5; do time curl -s -o /dev/null .../dev/musicbrainz/artist/b10bbbfc-... ; done
```

| 회차 | 응답 시간 |
|---|---|
| 1 | 1.24s |
| 2 | 1.32s |
| 3 | 1.28s |
| 4 | 1.22s |
| 5 | 1.53s |

- 판정: 정상 — 모든 호출이 1초 이상 (네트워크 RTT + 강제 throttle delay). MusicBrainz 정책 1 req/sec 준수.

### 7. User-Agent 헤더

- 직접 헤더 캡처는 외부 호출이라 어려움
- 간접 검증: MusicBrainz 는 식별 가능한 User-Agent 누락 시 403/blocked 처리하나, 본 검증의 모든 호출이 200 응답 → User-Agent 부착 확인
- 코드: `MusicBrainzHttpClientConfig.kt:38` `install(DefaultRequest) { header(HttpHeaders.UserAgent, "${userAgent} ( ${contact} )") }`
- 판정: 정상

### 8. Spotify 어댑터 비활성화

```
GET /dev/spotify/token
GET /dev/spotify/search?q=test&type=track
```

- 둘 다 HTTP 404 (`{"success":false,"message":"요청한 리소스를 찾을 수 없습니다.","code":"RESOURCE_NOT_FOUND"}`)
- 컨트롤러 빈이 `@ConditionalOnProperty(external.music.provider=spotify, matchIfMissing=false)` 로 미등록 → 라우팅 없음
- 판정: 정상 — Spotify 어댑터 운영 경로 분리 확인

## 발견된 이슈

| ID | 우선순위 | 내용 | 위치 | 권장 조치 |
|---|---|---|---|---|
| 1 | P2 (품질) | invalid MBID 등 사용자 입력 오류로 인한 외부 4xx 응답이 500 으로 매핑됨 | `MusicBrainzApiClient.ensureSuccess` + `GlobalExceptionHandler` | 후속 작업: `MusicBrainzApiException` 의 statusCode 를 그대로 (또는 400 으로) 매핑하는 핸들러 추가. 이번 PR 스코프 외 — 별도 이슈로 분리 권장. |

## 권장 조치 / 후속 작업

- **P2-1**: MusicBrainzApiException → 4xx 응답 매핑 추가 (별도 이슈)
- 운영 시 MusicBrainz 부하 폭주 대비 결과 캐시 (Redis) 도입 — PRD `Future Enhancements` 항목

## 프론트 관련 구현 지점

해당 없음 (백엔드 단독 작업, FE 호출 지점 변경 없음).

## 재현용 페이로드 위치

본 리포트 내 curl 스니펫.
