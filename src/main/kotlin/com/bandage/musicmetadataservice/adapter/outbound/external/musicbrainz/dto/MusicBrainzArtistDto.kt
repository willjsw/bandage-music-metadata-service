package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * MusicBrainz 단일 artist 응답.
 *
 * `GET /ws/2/artist/{mbid}?fmt=json` 또는 search 응답의 `artists[]` 항목에 대응.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MusicBrainzArtistDto(
    val id: String,
    val name: String,
    @JsonProperty("sort-name") val sortName: String? = null,
    val country: String? = null,
    val type: String? = null,
    /** search 응답에만 존재 (0..100). lookup 응답에는 없음 */
    val score: Int? = null,
)

/**
 * recording / release 응답의 `artist-credit[]` 항목.
 *
 * `name` 은 표시용 표기(featured 표기 등 포함), `artist.name` 은 정식 명칭.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ArtistCreditDto(
    val name: String? = null,
    val artist: ArtistRefDto,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArtistRefDto(
    val id: String,
    val name: String,
    @JsonProperty("sort-name") val sortName: String? = null,
)
