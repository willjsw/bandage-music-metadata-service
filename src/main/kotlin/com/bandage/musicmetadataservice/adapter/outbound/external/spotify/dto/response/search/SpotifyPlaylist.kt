package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyPlaylist(
    val id: String,
    val name: String,
    val uri: String,
    val href: String,
    val type: String,
    val collaborative: Boolean,
    val description: String?,
    val snapshotId: String,
    val public: Boolean? = null,
    val images: List<SpotifyImage> = emptyList(),
    val owner: SpotifyOwner? = null,
    val externalUrls: SpotifyExternalUrls? = null,
    val tracks: SpotifyPlaylistTrackRef? = null,
)

/**
 * Playlist 검색 응답에서는 track 항목 전체가 아닌 요약만 반환된다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyPlaylistTrackRef(
    val href: String,
    val total: Int,
)
