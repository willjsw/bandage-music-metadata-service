package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyEpisode(
    val id: String,
    val name: String,
    val uri: String,
    val href: String,
    val type: String,
    val description: String,
    val htmlDescription: String,
    val durationMs: Int,
    val explicit: Boolean,
    val isExternallyHosted: Boolean,
    val isPlayable: Boolean,
    val language: String? = null,
    val languages: List<String> = emptyList(),
    val releaseDate: String,
    val releaseDatePrecision: String,
    val audioPreviewUrl: String?,
    val images: List<SpotifyImage> = emptyList(),
    val externalUrls: SpotifyExternalUrls? = null,
    val resumePoint: SpotifyResumePoint? = null,
    val restrictions: SpotifyRestrictions? = null,
)
