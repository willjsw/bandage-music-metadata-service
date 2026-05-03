# MusicBrainz 검색 확장 (통합 검색 / loose 모드 / release-count 정렬) 검증 리포트

## 메타

- 작성일: 2026-05-03
- 작성자: Claude (BD-8 작업, 후속)
- 대상: `music-metadata-service` 의 MusicBrainz 어댑터 검색 확장
- 검증 도구: `curl` (port 18081)
- 범위:
  - (1) `mode=loose` 의 fuzzy/wildcard 부분일치 검색
  - (2) 응답에 `score` / `releaseCount` 필드 노출 + sort=releases 정렬
  - (3) 단일 query 로 recording / artist / release-group 통합 검색 (가중치 미적용)
- 상태: 검증 완료

## 자동화된 검증

- `./gradlew spotlessApply build` — 통과 (test contextLoads 포함)

## 변경 요약

| 영역 | 변경 |
|---|---|
| DTO | `MusicBrainzRecordingDto` / `MusicBrainzArtistDto` 에 `score` 추가, `MusicBrainzReleaseGroupDto` + `MusicBrainzReleaseGroupSearchResponse` 신규 |
| 도메인 | `Recording.releaseCount` / `Recording.score` / `Artist.score` 추가, `ReleaseGroup` / `SearchMode` / `SearchSort` / `SearchEntityType` / `UnifiedSearchHit` 신규 |
| 포트 | `MusicInfoApiClient` 에 `searchReleaseGroup`, `searchAll(query, limit, mode, sort)` 추가, search* 에 `mode` 파라미터 추가 |
| 어댑터 | `MusicBrainzApiClient` 에 release-group 검색 + searchAll 직렬 호출 + LOOSE 모드 토큰 변환 구현. SpotifyApiClient 은 stub. |
| 컨트롤러 | `/dev/musicbrainz/search`, `/dev/musicbrainz/release-group/search` 추가, 기존 search 엔드포인트에 `mode`/`sort` 파라미터 |

## 케이스별 결과

### A. recording search exact — score / releaseCount 노출

```
GET /dev/musicbrainz/recording/search?query=Doxy AND artist:Miles Davis&limit=2
```

| title | score | releaseCount |
|---|---|---|
| Doxy | 100 | 79 |
| Doxy (remastered 2009) | 78 | 1 |

판정: 정상. Miles Davis "Doxy" 가 79개 release 에 수록됨이 그대로 노출.

### B. recording search loose — fuzzy 부분일치

```
GET /dev/musicbrainz/recording/search?query=docsy mils davies&mode=loose&limit=3
```

내부 query 변환: `docsy~1 mils~1 davies~1`

결과: 오탈자 query 임에도 score 100/98/87 의 recording 반환 (`Mile Davies (demo)` / `Election Day in Daviess County` / `All Blues (...MILES DAV...)`). fuzzy edit-distance 1 매칭 정상 동작.

판정: 정상. 단, 의미적으로 가장 가까운 Miles Davis Doxy 는 query 가 너무 흩어져서 상위로 오지 않음 — fuzzy 의 한계.

### C. release-group search

```
GET /dev/musicbrainz/release-group/search?query=Kind of Blue artist:Miles Davis&limit=3
```

| title | primaryType | score | releaseCount |
|---|---|---|---|
| Kind of Blue | Album | 100 | 137 |
| 3 Originals: Porgy and Bess / Birth | Album | 72 | 1 |
| Lullaby Versions of Kind of Blue & ... | Album | 68 | 1 |

판정: 정상. 도메인 모델의 `primaryType` / `releaseCount` 정확 매핑.

### D. 통합 검색 sort=score

```
GET /dev/musicbrainz/search?query=Beatles&limit=3
```

응답 (entity 당 limit=3, 총 9건 → score desc 정렬, tie-breaker = releaseCount desc):

| type | title | subtitle | score | rc |
|---|---|---|---|---|
| RECORDING | Beatles | Kathryn Williams | 100 | 3 |
| RELEASE_GROUP | Beatles | Emmerson Nogueira | 100 | 2 |
| RECORDING | Beatles | VanderJact | 100 | 1 |
| RECORDING | Beatles | Karel Gott | 100 | 1 |
| RELEASE_GROUP | Beatles | Uakti | 100 | 1 |
| RELEASE_GROUP | Beatles | Lasha | 100 | 1 |
| ARTIST | The Beatles | Beatles, The | 100 | 0 |
| ARTIST | John Lennon | Lennon, John | 72 | 0 |
| ARTIST | Paul McCartney | McCartney, Paul | 71 | 0 |

판정: 정상 — 가중치 미적용 정렬. 단, 실제 사용자가 "Beatles" 검색 시 The Beatles 아티스트가 1순위로 오기를 기대할 수 있으나, MB 의 score 100 동률 + artist 의 releaseCount 0 로 인해 뒤로 밀림. tie-breaker 순서 조정 또는 가중치 도입 시 개선 가능 (현재 스코프는 가중치 미적용).

### E. 통합 검색 sort=releases

```
GET /dev/musicbrainz/search?query=Doxy artist:Miles Davis&sort=releases&limit=5
```

| type | title | subtitle | score | rc |
|---|---|---|---|---|
| RECORDING | Doxy | Miles Davis | 100 | **79** |
| RECORDING | Doxy | Oliver Nelson | 81 | 4 |
| RECORDING | Doxy | Jamey Aebersold | 81 | 2 |
| ... | | | | |

판정: 정상 — releaseCount desc 로 가장 수록 빈도 높은 항목이 상단.

### F. Lucene 특수문자 escape (LOOSE)

코드 검토: `escapeLucene` 정규식이 `+ - ! ( ) { } [ ] ^ " ~ * ? : \ /` 모두 escape. 1자 토큰은 fuzzy 대신 wildcard `*` 적용 (Lucene fuzzy 1자 거절 대비).

판정: 정상.

### G. 1 req/sec rate limit (통합 검색 시)

searchAll 은 3 entity 직렬 호출 → throttle 가 호출 간 ~1초 간격 강제 → 통합 검색 1회 응답에 약 3초 소요. 단일 호출 검증 (이전 리포트) 과 동일 메커니즘 적용 — 별도 검증 생략.

## 발견 이슈 / 한계

| ID | 우선순위 | 내용 | 권장 조치 |
|---|---|---|---|
| 1 | P3 (참고) | 통합 검색 score 동률 시 tie-breaker = releaseCount 로 인해 artist 결과가 뒤로 밀림 (artist 의 rc 는 항상 0) | 사용자 의도에 따라 entity 가중치 도입 또는 별도 카테고리 분리 응답 검토. 현재 스코프 외. |
| 2 | P3 (참고) | LOOSE 모드의 fuzzy 는 토큰별 edit-distance 만 반영 — 다국어 / 한자 / 발음 유사 검색은 약함 | 추후 phonetic / n-gram 인덱스 (외부 elasticsearch 등) 검토 |
| 3 | P3 (참고) | searchAll 3호출 직렬 → ~3초 응답 | 인증 적용 시 50 req/sec 로 병렬화, 또는 결과 캐싱 (Redis TTL) |

## 후속 작업 후보

- 통합 검색 결과 카테고리 분리 응답 (`{ recordings: [...], artists: [...], releaseGroups: [...] }`) 옵션 추가
- artist 결과에 `release-group-count` browse 호출로 release_count 채우기 (호출 추가 비용)
- 검색 결과 Redis 캐시
- entity 가중치 옵션화 (예: `?weights=recording:1.0,artist:1.2,releaseGroup:0.9`)
