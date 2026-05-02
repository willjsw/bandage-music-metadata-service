package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz

/**
 * MusicBrainz Web Service 호출이 2xx 가 아닐 때 던지는 어댑터 레벨 예외.
 *
 * - statusCode: HTTP status (400, 404, 429, 503 등)
 * - retryAfterSeconds: 429 / 503 응답일 때 Retry-After 헤더 값(초)
 * - rawBody: 응답 본문 그대로 (디버깅용)
 *
 * MusicBrainz 는 익명 요청에 대해 1 req/sec rate limit 을 적용하며,
 * 초과 시 503 + Retry-After 로 응답한다.
 */
class MusicBrainzApiException(
    val statusCode: Int,
    val rawBody: String,
    val retryAfterSeconds: Long? = null,
) : RuntimeException("MusicBrainz API error [$statusCode]: $rawBody")
