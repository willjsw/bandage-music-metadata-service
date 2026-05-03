package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * `GET /ws/2/recording/?query=...&fmt=json` 검색 응답.
 *
 * - count: 전체 매칭 수
 * - offset: 요청 offset
 * - recordings: 결과 목록 (각 항목에 score 포함 가능 — DTO 에는 미반영)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MusicBrainzRecordingSearchResponse(
    val created: String? = null,
    val count: Int = 0,
    val offset: Int = 0,
    val recordings: List<MusicBrainzRecordingDto> = emptyList(),
)

/**
 * `GET /ws/2/artist/?query=...&fmt=json` 검색 응답.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MusicBrainzArtistSearchResponse(
    val created: String? = null,
    val count: Int = 0,
    val offset: Int = 0,
    val artists: List<MusicBrainzArtistDto> = emptyList(),
)

/**
 * `GET /ws/2/release-group/?query=...&fmt=json` 검색 응답.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MusicBrainzReleaseGroupSearchResponse(
    val created: String? = null,
    val count: Int = 0,
    val offset: Int = 0,
    @com.fasterxml.jackson.annotation.JsonProperty("release-groups") val releaseGroups: List<MusicBrainzReleaseGroupDto> = emptyList(),
)
