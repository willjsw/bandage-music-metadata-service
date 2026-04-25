# Spotify Web API — Search Endpoint 명세

> 출처: <https://developer.spotify.com/documentation/web-api/reference/search>
> 정리일: 2026-04-25

---

## 1. 엔드포인트

| 항목 | 값 |
|------|----|
| Method | `GET` |
| URL | `https://api.spotify.com/v1/search` |
| Content-Type | `application/json` (응답) |

---

## 2. 인증

- **OAuth 2.0 Bearer Token** 필수
- 요청 헤더: `Authorization: Bearer {access_token}`
- Search API 자체는 별도 scope 를 요구하지 않음 (Client Credentials Flow 로도 호출 가능)
- 단, `market` 파라미터 없이 사용자 국가 정보를 활용하려면 사용자 인증 토큰(Authorization Code Flow)이 필요

---

## 3. 요청 파라미터 (Query String)

| 파라미터 | 타입 | 필수 | 기본값 | 제약 | 설명 |
|---------|------|------|--------|------|------|
| `q` | string | ✅ | — | URL 인코딩 필수 | 검색어. 필드 필터/연산자 사용 가능 |
| `type` | array&lt;string&gt; (CSV) | ✅ | — | `album`, `artist`, `playlist`, `track`, `show`, `episode`, `audiobook` | 검색할 콘텐츠 유형. 콤마로 다중 지정 가능 |
| `market` | string | ❌ | — | ISO 3166-1 alpha-2 (예: `KR`, `US`) | 해당 마켓에서 이용 가능한 콘텐츠만 반환. 사용자 토큰의 국가 정보가 우선 적용됨 |
| `limit` | integer | ❌ | `20` | `0` ~ `50` (각 type 당) | 한 type 당 반환할 최대 결과 수 |
| `offset` | integer | ❌ | `0` | `0` ~ `1000` | 페이지네이션 시작 위치. (offset + limit) ≤ 1000 |
| `include_external` | string | ❌ | — | `audio` | `audio` 지정 시 외부 호스팅 오디오도 재생 가능 항목으로 포함 |

> ⚠️ 공식 문서상 `limit` 의 표기가 위치마다 다르게 보이는 경우가 있으나, 현재 Reference 페이지 기준 **0–50, 기본 20** 입니다. 통합 검색의 경우 type 당 결과 수입니다.

---

## 4. 검색 쿼리(`q`) 문법

### 4.1 필드 필터

| 필터 | 적용 가능한 type | 예시 |
|------|----------------|------|
| `album:` | album, track | `album:"Kind of Blue"` |
| `artist:` | album, artist, track | `artist:"Miles Davis"` |
| `track:` | track | `track:Doxy` |
| `year:` | album, artist, track | `year:1959`, `year:1955-1960` |
| `genre:` | artist, track | `genre:jazz` |
| `isrc:` | track | `isrc:USSM18900468` |
| `upc:` | album | `upc:00888072443419` |
| `tag:hipster` | album | 발매 후 인기도 하위 10% (인디 트랙) |
| `tag:new` | album | 최근 2주 내 발매 |

### 4.2 연산자

| 연산자 | 의미 | 예시 |
|--------|------|------|
| `NOT` | 제외 | `roadhouse NOT blues` |
| `OR` | 합집합 | `roadhouse OR blues` |
| `AND` (암묵적, 공백) | 교집합 | `roadhouse blues` |
| `"..."` | 정확 일치 (구문 검색) | `"the beatles"` |

### 4.3 쿼리 예시

```
q=remaster%20track:Doxy%20artist:Miles%20Davis
q=year:1980-2020%20genre:rock
q=album:"Abbey Road"%20artist:"The Beatles"
q=tag:new%20genre:k-pop
q=isrc:USSM18900468
```

---

## 5. 응답 구조

요청한 `type` 별로 최상위 키가 추가되며, 각 키는 **Paging Object** 입니다.

### 5.1 공통 Paging Object

```jsonc
{
  "href": "string",        // 현재 페이지의 Web API endpoint
  "limit": 20,
  "next": "string | null", // 다음 페이지 URL
  "offset": 0,
  "previous": "string | null",
  "total": 0,
  "items": [ /* type 별 객체 배열 */ ]
}
```

### 5.2 응답 최상위 형태

```jsonc
{
  "tracks":     { /* Paging<Track> */ },
  "artists":    { /* Paging<Artist> */ },
  "albums":     { /* Paging<AlbumSimplified> */ },
  "playlists":  { /* Paging<PlaylistSimplified> */ },
  "shows":      { /* Paging<ShowSimplified> */ },
  "episodes":   { /* Paging<EpisodeSimplified> */ },
  "audiobooks": { /* Paging<AudiobookSimplified> */ }
}
```

### 5.3 Track 객체 주요 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | string | Spotify ID |
| `name` | string | 트랙명 |
| `uri` | string | Spotify URI (`spotify:track:...`) |
| `href` | string | Web API endpoint |
| `external_urls` | object | 외부 URL 맵 (`spotify` 등) |
| `external_ids` | object | `isrc`, `ean`, `upc` |
| `album` | AlbumSimplified | 수록 앨범 |
| `artists` | array&lt;ArtistSimplified&gt; | 아티스트 목록 |
| `available_markets` | array&lt;string&gt; | ISO 3166-1 alpha-2 |
| `disc_number` | integer | 디스크 번호 |
| `track_number` | integer | 트랙 번호 |
| `duration_ms` | integer | 재생 시간(ms) |
| `explicit` | boolean | 19금 여부 |
| `popularity` | integer | 0~100 |
| `preview_url` | string \| null | 30초 미리듣기 URL |
| `is_playable` | boolean | 마켓 내 재생 가능 여부 |
| `is_local` | boolean | 로컬 트랙 여부 |
| `linked_from` | object | Track Relinking 원본 |
| `restrictions` | object | 제한 정보 (`reason`) |
| `type` | string | `"track"` |

### 5.4 Artist 객체 주요 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id`, `name`, `uri`, `href`, `type` | — | 공통 |
| `external_urls` | object | — |
| `followers` | object | `{ "href": null, "total": int }` |
| `genres` | array&lt;string&gt; | 장르 |
| `images` | array&lt;Image&gt; | 프로필 이미지 |
| `popularity` | integer | 0~100 |

### 5.5 Album (Simplified) 주요 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id`, `name`, `uri`, `href`, `type` | — | 공통 |
| `album_type` | string | `album`, `single`, `compilation` |
| `total_tracks` | integer | 총 트랙 수 |
| `available_markets` | array&lt;string&gt; | — |
| `images` | array&lt;Image&gt; | 커버 |
| `release_date` | string | `YYYY-MM-DD` 등 |
| `release_date_precision` | string | `year` \| `month` \| `day` |
| `restrictions` | object | — |
| `artists` | array&lt;ArtistSimplified&gt; | — |
| `external_urls` | object | — |

### 5.6 Playlist (Simplified) 주요 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id`, `name`, `uri`, `href`, `type` | — | 공통 |
| `collaborative` | boolean | 공동 편집 여부 |
| `description` | string \| null | — |
| `images` | array&lt;Image&gt; | — |
| `owner` | object | `{ id, display_name, ... }` |
| `public` | boolean \| null | 공개 여부 |
| `snapshot_id` | string | 변경 추적용 ID |
| `tracks` | object | `{ href, total }` (요약) |

### 5.7 Show (Simplified) 주요 필드

`available_markets`, `copyrights`, `description`, `html_description`, `explicit`, `external_urls`, `href`, `id`, `images`, `is_externally_hosted`, `languages`, `media_type`, `name`, `publisher`, `type`, `uri`, `total_episodes`

### 5.8 Episode (Simplified) 주요 필드

`audio_preview_url`, `description`, `html_description`, `duration_ms`, `explicit`, `external_urls`, `href`, `id`, `images`, `is_externally_hosted`, `is_playable`, `language`, `languages`, `name`, `release_date`, `release_date_precision`, `resume_point`, `type`, `uri`, `restrictions`

### 5.9 Audiobook (Simplified) 주요 필드

`authors`, `available_markets`, `copyrights`, `description`, `html_description`, `edition`, `explicit`, `external_urls`, `href`, `id`, `images`, `languages`, `media_type`, `name`, `narrators`, `publisher`, `type`, `uri`, `total_chapters`

### 5.10 Image 객체

```jsonc
{
  "url": "string",
  "height": 640,   // null 가능
  "width": 640     // null 가능
}
```

---

## 6. 응답 상태 코드

| 코드 | 의미 | 비고 |
|------|------|------|
| `200 OK` | 성공 | 본문에 결과 포함 |
| `401 Unauthorized` | 토큰 없음/만료 | 토큰 재발급 필요 |
| `403 Forbidden` | 권한 부족 / 제한된 콘텐츠 | — |
| `429 Too Many Requests` | 레이트 리밋 초과 | `Retry-After` 헤더(초) 확인 |

### 6.1 에러 응답 포맷

```jsonc
{
  "error": {
    "status": 401,
    "message": "Invalid access token"
  }
}
```

---

## 7. 레이트 리밋

- Spotify Web API 는 **롤링 30초 윈도우** 기반의 레이트 리밋을 적용 (앱 단위 산정)
- 한도 초과 시 `429` + `Retry-After` 헤더 반환 → 해당 초 만큼 대기 후 재시도
- 정확한 한도는 비공개. 트래픽이 많은 앱은 Extended Quota Mode 신청 필요
- 본 Search Reference 페이지에 별도 수치는 명시되어 있지 않음

---

## 8. `market` 파라미터 동작 상세

1. 유효한 ISO 3166-1 alpha-2 코드 지정 시, 해당 국가에서 이용 가능한 콘텐츠만 반환
2. 사용자 토큰(Authorization Code Flow)이 있고 사용자 국가 정보가 등록되어 있으면, 사용자 국가가 **우선** 적용
3. `market` 도 없고 사용자 국가도 없으면 콘텐츠가 "이용 불가"로 간주되어 누락될 수 있음
4. Track Relinking: 마켓 제한으로 원곡이 재생 불가일 때 동등한 트랙으로 자동 매핑되며 `linked_from` 에 원본이 담김

---

## 9. 페이지네이션

- `limit` × `offset` 으로 제어
- `(offset + limit)` 합이 **1000 을 초과할 수 없음**
- 응답의 `next`, `previous` 를 그대로 사용해 다음/이전 페이지 호출 가능
- 통합 검색 시 limit 은 **type 별로 동일하게** 적용됨

---

## 10. 요청/응답 예시

### 10.1 요청

```http
GET /v1/search?q=remaster%20track:Doxy%20artist:Miles%20Davis&type=track,album&market=KR&limit=10&offset=0 HTTP/1.1
Host: api.spotify.com
Authorization: Bearer BQDk...AcCQ
```

### 10.2 응답 (요약)

```jsonc
{
  "tracks": {
    "href": "https://api.spotify.com/v1/search?...",
    "limit": 10,
    "offset": 0,
    "total": 124,
    "next": "https://api.spotify.com/v1/search?...&offset=10",
    "previous": null,
    "items": [
      {
        "id": "5W3cjX2J3tjhG8zb6u0qHn",
        "name": "Doxy - Remastered",
        "uri": "spotify:track:5W3cjX2J3tjhG8zb6u0qHn",
        "duration_ms": 295000,
        "explicit": false,
        "popularity": 42,
        "preview_url": "https://p.scdn.co/mp3-preview/...",
        "external_ids": { "isrc": "USSM18900468" },
        "artists": [{ "id": "...", "name": "Miles Davis" }],
        "album": { "id": "...", "name": "Bags' Groove" }
      }
      // ...
    ]
  },
  "albums": { /* ... */ }
}
```

---

## 11. 사용 시 주의사항 / 정책

- **AI/ML 학습 금지**: Spotify 콘텐츠(메타데이터 포함)는 머신러닝/AI 모델 학습에 사용할 수 없음
- **미리듣기 단독 서비스 금지**: `preview_url` 30초 클립을 독립된 음악 서비스 형태로 제공할 수 없음
- **속성 표시 의무**: Spotify 로고/링크/저작권 표기 가이드라인 준수
- **캐싱**: Spotify Developer Terms 에 따라 일부 데이터는 캐싱 제한이 있음 (최대 24시간 등)
- **`q` 의 한글/특수문자**: 반드시 UTF-8 + URL 인코딩 (공백은 `%20`, 콜론은 그대로 사용 가능)

---

## 12. 본 프로젝트 적용 시 체크리스트

- [ ] `SpotifyClient` (Ktor) 에서 토큰 만료 시 자동 재발급 처리 (Client Credentials)
- [ ] `429` 응답 시 `Retry-After` 기반 백오프
- [ ] `market` 기본값을 호출부에서 명시적으로 주입할지 여부 결정 (KR 고정 vs 사용자 입력)
- [ ] `limit`, `offset` 의 범위 검증 (`0 ≤ limit ≤ 50`, `offset + limit ≤ 1000`)
- [ ] 검색 결과 DTO → 도메인 모델 변환 시 nullable 필드(`preview_url`, `images.height/width` 등) 처리
- [ ] `external_ids.isrc` 등 식별자를 도메인 트랙의 키로 활용할지 정책 정의
