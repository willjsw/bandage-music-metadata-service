package com.bandage.musicmetadataservice.adapter.inbound.web.controller

import com.bandage.musicmetadataservice.application.port.outbound.MusicInfoApiClient
import com.bandage.musicmetadataservice.domain.model.Artist
import com.bandage.musicmetadataservice.domain.model.Recording
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
 * - 실제 비즈니스 use case 가 아니므로 application/port/inbound 를 거치지 않음
 *
 * ## curl 예시
 *
 * 1) recording 검색 (Lucene query 문법)
 * ```bash
 * curl -G "http://localhost:18081/dev/musicbrainz/recording/search" \
 *   --data-urlencode "query=Doxy AND artist:Miles Davis" \
 *   --data-urlencode "limit=5"
 * ```
 *
 * 2) recording MBID lookup
 * ```bash
 * curl "http://localhost:18081/dev/musicbrainz/recording/<mbid>"
 * ```
 *
 * 3) artist 검색
 * ```bash
 * curl -G "http://localhost:18081/dev/musicbrainz/artist/search" \
 *   --data-urlencode "query=Beatles" \
 *   --data-urlencode "limit=5"
 * ```
 *
 * 4) artist MBID lookup
 * ```bash
 * curl "http://localhost:18081/dev/musicbrainz/artist/<mbid>"
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
    ): List<Recording> = musicInfoApiClient.searchRecording(query, limit, offset)

    @GetMapping("/recording/{mbid}")
    fun lookupRecording(
        @PathVariable mbid: String,
    ): Recording? = musicInfoApiClient.lookupRecording(mbid)

    @GetMapping("/artist/search")
    fun searchArtist(
        @RequestParam query: String,
        @RequestParam(defaultValue = "25") limit: Int,
        @RequestParam(defaultValue = "0") offset: Int,
    ): List<Artist> = musicInfoApiClient.searchArtist(query, limit, offset)

    @GetMapping("/artist/{mbid}")
    fun lookupArtist(
        @PathVariable mbid: String,
    ): Artist? = musicInfoApiClient.lookupArtist(mbid)
}
