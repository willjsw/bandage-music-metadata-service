package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Search 응답의 Album (simplified).
 * - albumType: "album" | "single" | "compilation"
 * - releaseDatePrecision: "year" | "month" | "day"
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyAlbum(
    val id: String,
    val name: String,
    val uri: String,
    val href: String,
    val type: String,
    val albumType: String,
    val totalTracks: Int,
    val releaseDate: String,
    val releaseDatePrecision: String,
    val availableMarkets: List<String> = emptyList(),
    val images: List<SpotifyImage> = emptyList(),
    val artists: List<SpotifyArtist> = emptyList(),
    val externalUrls: SpotifyExternalUrls? = null,
    val restrictions: SpotifyRestrictions? = null,
)
