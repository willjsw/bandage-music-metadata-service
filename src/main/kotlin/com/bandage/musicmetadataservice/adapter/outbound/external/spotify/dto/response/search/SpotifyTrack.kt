package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyTrack(
    val id: String,
    val name: String,
    val uri: String,
    val href: String,
    val type: String,
    val durationMs: Int,
    val explicit: Boolean,
    val popularity: Int,
    val discNumber: Int,
    val trackNumber: Int,
    val isLocal: Boolean,
    val previewUrl: String?,
    val isPlayable: Boolean? = null,
    val availableMarkets: List<String> = emptyList(),
    val externalUrls: SpotifyExternalUrls? = null,
    val externalIds: SpotifyExternalIds? = null,
    val artists: List<SpotifyArtist> = emptyList(),
    val album: SpotifyAlbum? = null,
    val linkedFrom: SpotifyLinkedFrom? = null,
    val restrictions: SpotifyRestrictions? = null,
)
