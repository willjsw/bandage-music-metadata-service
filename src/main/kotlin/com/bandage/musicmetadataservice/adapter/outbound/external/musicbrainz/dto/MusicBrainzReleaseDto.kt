package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * MusicBrainz release 응답.
 *
 * recording lookup 시 `inc=releases` 로 함께 내려오거나, release 단일 조회 응답에 사용.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class MusicBrainzReleaseDto(
    val id: String,
    val title: String,
    val date: String? = null,
    val country: String? = null,
    @JsonProperty("artist-credit") val artistCredit: List<ArtistCreditDto>? = null,
)
