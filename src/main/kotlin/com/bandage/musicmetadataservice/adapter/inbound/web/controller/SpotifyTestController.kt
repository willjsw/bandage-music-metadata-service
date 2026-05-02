package com.bandage.musicmetadataservice.adapter.inbound.web.controller

import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.SpotifyApiClient
import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.request.IncludeExternal
import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.request.SpotifySearchRequest
import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.request.SpotifySearchType
import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.SpotifyTokenResponse
import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search.SpotifySearchResponse
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Spotify Web API 직접 호출 검증용 컨트롤러.
 *
 * - `local` 프로필에서만 활성화
 * - SpotifyApiClient 정상 동작 여부 확인 목적이므로 raw DTO 를 그대로 노출
 * - 실제 도메인 use case 가 아니므로 application/port/inbound 를 거치지 않음
 *
 * ## curl 예시
 *
 * 1) Client Credentials Flow 로 access token 발급
 * ```bash
 * curl -X GET "http://localhost:18081/dev/spotify/token"
 * ```
 *
 * 2) 트랙 단일 검색 (필드 필터 + market + 페이지네이션)
 * ```bash
 * curl -G "http://localhost:18081/dev/spotify/search" \
 *   --data-urlencode "q=remaster track:Doxy artist:Miles Davis" \
 *   --data-urlencode "type=track" \
 *   --data-urlencode "market=KR" \
 *   --data-urlencode "limit=10" \
 *   --data-urlencode "offset=0"
 * ```
 *
 * 3) 다중 type 통합 검색
 * ```bash
 * curl -G "http://localhost:18081/dev/spotify/search" \
 *   --data-urlencode "q=Beatles" \
 *   --data-urlencode "type=track,album,artist" \
 *   --data-urlencode "market=KR" \
 *   --data-urlencode "limit=5"
 * ```
 *
 * 4) tag:new 로 최근 발매 앨범 검색
 * ```bash
 * curl -G "http://localhost:18081/dev/spotify/search" \
 *   --data-urlencode "q=tag:new genre:k-pop" \
 *   --data-urlencode "type=album" \
 *   --data-urlencode "market=KR"
 * ```
 *
 * 5) ISRC 로 트랙 정확 검색
 * ```bash
 * curl -G "http://localhost:18081/dev/spotify/search" \
 *   --data-urlencode "q=isrc:USSM18900468" \
 *   --data-urlencode "type=track"
 * ```
 *
 * 6) 외부 호스팅 audio 포함 (팟캐스트 에피소드)
 * ```bash
 * curl -G "http://localhost:18081/dev/spotify/search" \
 *   --data-urlencode "q=podcast" \
 *   --data-urlencode "type=episode" \
 *   --data-urlencode "include_external=audio"
 * ```
 *
 * 7) 연도 범위 + 장르 필터
 * ```bash
 * curl -G "http://localhost:18081/dev/spotify/search" \
 *   --data-urlencode "q=year:1980-1989 genre:rock" \
 *   --data-urlencode "type=track" \
 *   --data-urlencode "limit=20"
 * ```
 */
@RestController
@RequestMapping("/dev/spotify")
@Profile("local")
class SpotifyTestController(
    private val spotifyApiClient: SpotifyApiClient,
) {
    @GetMapping("/token")
    fun token(): SpotifyTokenResponse = spotifyApiClient.issueAccessToken()

    @GetMapping("/search")
    fun search(
        @RequestParam q: String,
        @RequestParam type: String,
        @RequestParam(required = false) market: String?,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(name = "include_external", required = false) includeExternal: String?,
    ): SpotifySearchResponse {
        val types =
            type.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map { SpotifySearchType.valueOf(it.uppercase()) }
        val request =
            SpotifySearchRequest(
                q = q,
                type = types,
                market = market,
                limit = limit,
                offset = offset,
                includeExternal = includeExternal?.let { IncludeExternal.valueOf(it.uppercase()) },
            )
        return spotifyApiClient.search(request)
    }

}