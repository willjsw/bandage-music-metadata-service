package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?,
)

/**
 * 외부 서비스(스포티파이 웹 등)별 URL 맵. 키는 보통 "spotify".
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyExternalUrls(
    val spotify: String? = null,
)

/**
 * Track 등에서 사용되는 외부 식별자.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyExternalIds(
    val isrc: String? = null,
    val ean: String? = null,
    val upc: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyFollowers(
    val href: String?,
    val total: Int,
)

/**
 * 마켓·콘텐츠 정책으로 인한 재생/제공 제한 사유.
 * reason: "market" | "product" | "explicit" | ...
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyRestrictions(
    val reason: String?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyCopyright(
    val text: String,
    val type: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyOwner(
    val id: String,
    val href: String?,
    val uri: String?,
    val type: String?,
    val displayName: String?,
    val externalUrls: SpotifyExternalUrls?,
    val followers: SpotifyFollowers? = null,
)

/**
 * Episode resume point — 사용자별 재생 이어듣기 위치.
 * 사용자 토큰일 때만 채워짐.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyResumePoint(
    val fullyPlayed: Boolean,
    val resumePositionMs: Int,
)

/**
 * Track Relinking 시 원본 트랙 정보.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SpotifyLinkedFrom(
    val id: String?,
    val href: String?,
    val uri: String?,
    val type: String?,
    val externalUrls: SpotifyExternalUrls?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyAuthor(
    val name: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SpotifyNarrator(
    val name: String,
)
