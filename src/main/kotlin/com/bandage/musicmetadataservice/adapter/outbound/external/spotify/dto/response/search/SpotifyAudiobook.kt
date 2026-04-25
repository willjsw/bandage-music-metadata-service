package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyAudiobook(
    val id: String,
    val name: String,
    val uri: String,
    val href: String,
    val type: String,
    val description: String,
    val htmlDescription: String,
    val edition: String? = null,
    val explicit: Boolean,
    val mediaType: String,
    val publisher: String,
    val totalChapters: Int,
    val languages: List<String> = emptyList(),
    val availableMarkets: List<String> = emptyList(),
    val images: List<SpotifyImage> = emptyList(),
    val authors: List<SpotifyAuthor> = emptyList(),
    val narrators: List<SpotifyNarrator> = emptyList(),
    val copyrights: List<SpotifyCopyright> = emptyList(),
    val externalUrls: SpotifyExternalUrls? = null,
)
