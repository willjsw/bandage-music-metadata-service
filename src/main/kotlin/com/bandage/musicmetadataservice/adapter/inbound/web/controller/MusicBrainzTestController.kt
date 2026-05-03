package com.bandage.musicmetadataservice.adapter.inbound.web.controller

import com.bandage.musicmetadataservice.application.port.outbound.MusicInfoApiClient
import com.bandage.musicmetadataservice.domain.model.dto.Artist
import com.bandage.musicmetadataservice.domain.model.dto.Recording
import com.bandage.musicmetadataservice.domain.model.dto.ReleaseGroup
import com.bandage.musicmetadataservice.domain.model.enums.SearchMode
import com.bandage.musicmetadataservice.domain.model.enums.SearchSort
import com.bandage.musicmetadataservice.domain.model.dto.UnifiedSearchHit
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * MusicBrainz 어댑터 직접 호출 검증용 컨트롤러.
 *
 * - `local` 프로필에서만 활성화
 * - MusicInfoApiClient 포트 동작 검증 목적이므로 도메인 모델 그대로 노출
 *
 * ## curl 예시
 *
 * 1) recording 검색 — exact (Lucene query 그대로)
 * ```bash
 * curl -G "http://localhost:18081/dev/musicbrainz/recording/search" \
 *   --data-urlencode "query=Doxy AND artist:Miles Davis" \
 *   --data-urlencode "limit=5"
 * ```
 *
 * 2) recording 검색 — loose (토큰별 fuzzy 자동 부착, 오탈자 허용)
 * ```bash
 * curl -G "http://localhost:18081/dev/musicbrainz/recording/search" \
 *   --data-urlencode "query=docsy mils davies" \
 *   --data-urlencode "mode=loose"
 * ```
 *
 * 3) recording MBID lookup
 * ```bash
 * curl "http://localhost:18081/dev/musicbrainz/recording/<mbid>"
 * ```
 *
 * 4) artist 검색
 * ```bash
 * curl -G "http://localhost:18081/dev/musicbrainz/artist/search" \
 *   --data-urlencode "query=Beatles" \
 *   --data-urlencode "mode=loose"
 * ```
 *
 * 5) release-group 검색 (앨범 단위)
 * ```bash
 * curl -G "http://localhost:18081/dev/musicbrainz/release-group/search" \
 *   --data-urlencode "query=Kind of Blue artist:Miles Davis" \
 *   --data-urlencode "limit=5"
 * ```
 *
 * 6) 통합 검색 — recording + artist + release-group 한 번에
 * ```bash
 * curl -G "http://localhost:18081/dev/musicbrainz/search" \
 *   --data-urlencode "query=Beatles" \
 *   --data-urlencode "limit=5" \
 *   --data-urlencode "mode=loose" \
 *   --data-urlencode "sort=score"
 * ```
 *
 * 7) 통합 검색 — release 수 desc 정렬 (인기 곡/앨범 위주)
 * ```bash
 * curl -G "http://localhost:18081/dev/musicbrainz/search" \
 *   --data-urlencode "query=Doxy" \
 *   --data-urlencode "sort=releases"
 * ```
 */
@RestController
@RequestMapping("/dev/musicbrainz")
@Profile("local")
class MusicBrainzTestController(
    private val musicInfoApiClient: MusicInfoApiClient,
) {
    @GetMapping("/recording/search")
    fun searchRecording(
        @RequestParam query: String,
        @RequestParam(defaultValue = "25") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "exact") mode: String,
    ): List<Recording> = musicInfoApiClient.searchRecording(query, limit, offset, parseMode(mode))

    @GetMapping("/recording/{mbid}")
    fun lookupRecording(
        @PathVariable mbid: String,
    ): Recording? = musicInfoApiClient.lookupRecording(mbid)

    @GetMapping("/artist/search")
    fun searchArtist(
        @RequestParam query: String,
        @RequestParam(defaultValue = "25") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "exact") mode: String,
    ): List<Artist> = musicInfoApiClient.searchArtist(query, limit, offset, parseMode(mode))

    @GetMapping("/artist/{mbid}")
    fun lookupArtist(
        @PathVariable mbid: String,
    ): Artist? = musicInfoApiClient.lookupArtist(mbid)

    @GetMapping("/release-group/search")
    fun searchReleaseGroup(
        @RequestParam query: String,
        @RequestParam(defaultValue = "25") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "exact") mode: String,
    ): List<ReleaseGroup> = musicInfoApiClient.searchReleaseGroup(query, limit, offset, parseMode(mode))

    @GetMapping("/search")
    fun searchAll(
        @RequestParam query: String,
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "exact") mode: String,
        @RequestParam(defaultValue = "score") sort: String,
    ): List<UnifiedSearchHit> = musicInfoApiClient.searchAll(query, limit, parseMode(mode), parseSort(sort))

    private fun parseMode(value: String): SearchMode =
        when (value.lowercase()) {
            "loose", "fuzzy" -> SearchMode.LOOSE
            else -> SearchMode.EXACT
        }

    private fun parseSort(value: String): SearchSort =
        when (value.lowercase()) {
            "release_count", "releasecount", "releases", "release-count" -> SearchSort.RELEASE_COUNT
            else -> SearchSort.SCORE
        }
}
