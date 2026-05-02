package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz

import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzArtistDto
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzArtistSearchResponse
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzRecordingDto
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzRecordingSearchResponse
import com.bandage.musicmetadataservice.application.port.outbound.MusicInfoApiClient
import com.bandage.musicmetadataservice.domain.model.Artist
import com.bandage.musicmetadataservice.domain.model.Recording
import com.bandage.musicmetadataservice.global.properties.MusicBrainzApiProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * MusicBrainz Web Service v2 호출 어댑터.
 *
 * - 인증 불필요 (User-Agent 헤더만 필수 — HttpClient DefaultRequest 에서 부착)
 * - Rate limit: 익명 요청은 1 req/sec — 인스턴스 내 throttle 로 강제
 * - 404 응답은 lookup 시 null 반환, 그 외 비-2xx 는 [MusicBrainzApiException] throw
 *
 * 주의: 다중 인스턴스 / 멀티 프로세스 환경에서는 본 throttle 이 전역 보장을 못한다.
 * 추후 Redis 기반 token bucket 등으로 확장 가능.
 */
@Component
@Primary
class MusicBrainzApiClient(
    private val musicBrainzHttpClient: HttpClient,
    private val properties: MusicBrainzApiProperties,
) : MusicInfoApiClient {

    private val throttleMutex = Mutex()

    @Volatile
    private var lastCallTime: Instant = Instant.EPOCH

    override fun searchRecording(query: String, limit: Int, offset: Int): List<Recording> =
        runBlocking {
            val response = throttledGet("/ws/2/recording/") {
                parameter("query", query)
                parameter("limit", limit)
                parameter("offset", offset)
                parameter("fmt", "json")
            }
            ensureSuccess(response)
            val dto: MusicBrainzRecordingSearchResponse = response.body()
            dto.recordings.map { it.toDomain() }
        }

    override fun lookupRecording(id: String): Recording? =
        runBlocking {
            val response = throttledGet("/ws/2/recording/$id") {
                parameter("inc", "artists+isrcs+releases")
                parameter("fmt", "json")
            }
            if (response.status.value == 404) return@runBlocking null
            ensureSuccess(response)
            val dto: MusicBrainzRecordingDto = response.body()
            dto.toDomain()
        }

    override fun searchArtist(query: String, limit: Int, offset: Int): List<Artist> =
        runBlocking {
            val response = throttledGet("/ws/2/artist/") {
                parameter("query", query)
                parameter("limit", limit)
                parameter("offset", offset)
                parameter("fmt", "json")
            }
            ensureSuccess(response)
            val dto: MusicBrainzArtistSearchResponse = response.body()
            dto.artists.map { it.toDomain() }
        }

    override fun lookupArtist(id: String): Artist? =
        runBlocking {
            val response = throttledGet("/ws/2/artist/$id") {
                parameter("fmt", "json")
            }
            if (response.status.value == 404) return@runBlocking null
            ensureSuccess(response)
            val dto: MusicBrainzArtistDto = response.body()
            dto.toDomain()
        }

    private suspend fun throttledGet(
        path: String,
        block: io.ktor.client.request.HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        throttle()
        return musicBrainzHttpClient.get("${properties.baseUrl}$path", block)
    }

    /**
     * 마지막 호출로부터 1초 이상 지나지 않았다면 부족분만큼 대기.
     */
    private suspend fun throttle() {
        throttleMutex.withLock {
            val elapsed = Duration.between(lastCallTime, Instant.now())
            val minInterval = Duration.ofSeconds(1)
            if (elapsed < minInterval) {
                delay(minInterval.minus(elapsed).toMillis())
            }
            lastCallTime = Instant.now()
        }
    }

    private suspend fun ensureSuccess(response: HttpResponse) {
        if (response.status.isSuccess()) return
        val retryAfter = response.headers[HttpHeaders.RetryAfter]?.toLongOrNull()
        throw MusicBrainzApiException(
            statusCode = response.status.value,
            rawBody = response.bodyAsText(),
            retryAfterSeconds = retryAfter,
        )
    }
}
