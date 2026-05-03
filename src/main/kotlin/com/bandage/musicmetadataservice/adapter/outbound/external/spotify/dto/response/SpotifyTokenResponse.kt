package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * POST https://accounts.spotify.com/api/token (Client Credentials Flow) 응답.
 *
 * 요청 본문(form-urlencoded): grant_type=client_credentials
 * 요청 헤더: Authorization: Basic Base64(client_id:client_secret)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
)
