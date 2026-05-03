package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyShow(
    val id: String,
    val name: String,
    val uri: String,
    val href: String,
    val type: String,
    val description: String,
    val htmlDescription: String,
    val publisher: String,
    val mediaType: String,
    val explicit: Boolean,
    val isExternallyHosted: Boolean,
    val totalEpisodes: Int,
    val languages: List<String> = emptyList(),
    val availableMarkets: List<String> = emptyList(),
    val images: List<SpotifyImage> = emptyList(),
    val copyrights: List<SpotifyCopyright> = emptyList(),
    val externalUrls: SpotifyExternalUrls? = null,
)
