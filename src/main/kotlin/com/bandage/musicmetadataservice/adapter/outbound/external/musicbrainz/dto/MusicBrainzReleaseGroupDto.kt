package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * MusicBrainz release-group 응답 (앨범 단위 추상).
 *
 * - primaryType: "Album" / "Single" / "EP" / "Broadcast" / "Other"
 * - secondaryTypes: "Compilation" / "Soundtrack" / "Live" / "Remix" / "Demo" 등
 * - firstReleaseDate: 최초 발매일 (ISO 부분 문자열 가능)
 * - releases: 검색/lookup 시 inc=releases 로 동봉되는 실제 발매본 목록 (release count 산출에 사용)
 *
 * search 응답에만 score 존재.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MusicBrainzReleaseGroupDto(
    val id: String,
    val title: String,
    val disambiguation: String? = null,
    @JsonProperty("primary-type") val primaryType: String? = null,
    @JsonProperty("secondary-types") val secondaryTypes: List<String>? = null,
    @JsonProperty("first-release-date") val firstReleaseDate: String? = null,
    @JsonProperty("artist-credit") val artistCredit: List<ArtistCreditDto>? = null,
    val releases: List<MusicBrainzReleaseDto>? = null,
    val score: Int? = null,
)
