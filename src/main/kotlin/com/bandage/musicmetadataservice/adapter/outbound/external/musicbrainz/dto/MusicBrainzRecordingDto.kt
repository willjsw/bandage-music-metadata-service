package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * MusicBrainz recording 응답.
 *
 * `GET /ws/2/recording/{mbid}?inc=artists+isrcs+releases&fmt=json`
 * 또는 search 응답의 `recordings[]` 항목.
 *
 * - length 는 milliseconds 단위
 * - isrcs / artist-credit / releases 는 inc 파라미터에 따라 누락될 수 있음
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MusicBrainzRecordingDto(
    val id: String,
    val title: String,
    val length: Long? = null,
    val disambiguation: String? = null,
    val isrcs: List<String>? = null,
    @JsonProperty("artist-credit") val artistCredit: List<ArtistCreditDto>? = null,
    val releases: List<MusicBrainzReleaseDto>? = null,
)
