package com.bandage.musicmetadataservice.global.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * application-musicbrainz.yaml 매핑.
 *
 * ```
 * musicbrainz:
 *   base-url: https://musicbrainz.org
 *   user-agent: BandageMusicMetadata/0.1.0
 *   contact: contact@bandage.com
 * ```
 *
 * MusicBrainz 는 인증이 필요 없는 공개 API 이며, 대신 식별 가능한 User-Agent 가 필수다.
 * 익명 호출은 1 req/sec 의 rate limit 이 적용된다.
 */
@ConfigurationProperties(prefix = "musicbrainz")
data class MusicBrainzApiProperties(
    val baseUrl: String = "https://musicbrainz.org",
    val userAgent: String,
    val contact: String,
)
