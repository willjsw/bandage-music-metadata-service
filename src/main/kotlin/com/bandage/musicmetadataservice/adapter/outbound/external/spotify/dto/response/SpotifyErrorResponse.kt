package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Spotify Web API 공통 에러 응답.
 *
 * ```
 * { "error": { "status": 401, "message": "Invalid access token" } }
 * ```
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyErrorResponse(
    val error: SpotifyErrorBody,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SpotifyErrorBody(
        val status: Int,
        val message: String,
    )
}
