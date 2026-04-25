package com.bandage.musicmetadataservice.adapter.outbound.external.spotify

/**
 * Spotify Web API 호출이 2xx 가 아닐 때 던지는 어댑터 레벨 예외.
 *
 * - statusCode: HTTP status (401, 403, 429, 5xx 등)
 * - retryAfterSeconds: 429 응답일 때 Retry-After 헤더 값(초)
 * - rawBody: 응답 본문 그대로 (디버깅용)
 */
class SpotifyApiException(
    val statusCode: Int,
    val rawBody: String,
    val retryAfterSeconds: Long? = null,
) : RuntimeException("Spotify API error [$statusCode]: $rawBody")
