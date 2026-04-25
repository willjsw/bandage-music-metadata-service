package com.bandage.musicmetadataservice.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * application-spotify.yaml 매핑.
 *
 * ```
 * spotify:
 *   client-id: ...
 *   client-secret: ...
 *   api-base-url: https://api.spotify.com
 *   accounts-base-url: https://accounts.spotify.com
 * ```
 */
@ConfigurationProperties(prefix = "spotify")
data class SpotifyApiProperties(
    val clientId: String,
    val clientSecret: String,
    val apiBaseUrl: String = "https://api.spotify.com",
    val accountsBaseUrl: String = "https://accounts.spotify.com",
)
