package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * GET /v1/search 응답 최상위 객체.
 * 요청한 type 별로 해당 키만 채워져서 반환된다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifySearchResponse(
    val tracks: SpotifyPage<SpotifyTrack>? = null,
    val artists: SpotifyPage<SpotifyArtist>? = null,
    val albums: SpotifyPage<SpotifyAlbum>? = null,
    val playlists: SpotifyPage<SpotifyPlaylist>? = null,
    val shows: SpotifyPage<SpotifyShow>? = null,
    val episodes: SpotifyPage<SpotifyEpisode>? = null,
    val audiobooks: SpotifyPage<SpotifyAudiobook>? = null,
)

/**
 * Spotify 공통 Paging Object.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyPage<T>(
    val href: String,
    val limit: Int,
    val offset: Int,
    val total: Int,
    val next: String?,
    val previous: String?,
    val items: List<T>,
)
