package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Search 응답의 Artist.
 * - simplified 컨텍스트(Track.artists 등)에서는 followers/genres/images/popularity 가 비어 있을 수 있음.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyArtist(
    val id: String,
    val name: String,
    val uri: String,
    val href: String,
    val type: String,
    val popularity: Int? = null,
    val genres: List<String> = emptyList(),
    val images: List<SpotifyImage> = emptyList(),
    val externalUrls: SpotifyExternalUrls? = null,
    val followers: SpotifyFollowers? = null,
)
