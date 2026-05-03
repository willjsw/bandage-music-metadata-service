package com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz

import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzArtistDto
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzArtistSearchResponse
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzRecordingDto
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzRecordingSearchResponse
import com.bandage.musicmetadataservice.adapter.outbound.external.musicbrainz.dto.MusicBrainzReleaseGroupSearchResponse
import com.bandage.musicmetadataservice.application.port.outbound.MusicInfoApiClient
import com.bandage.musicmetadataservice.domain.model.dto.Artist
import com.bandage.musicmetadataservice.domain.model.dto.Recording
import com.bandage.musicmetadataservice.domain.model.dto.ReleaseGroup
import com.bandage.musicmetadataservice.domain.model.dto.SearchEntityType
import com.bandage.musicmetadataservice.domain.model.enums.SearchMode
import com.bandage.musicmetadataservice.domain.model.enums.SearchSort
import com.bandage.musicmetadataservice.domain.model.dto.UnifiedSearchHit
import com.bandage.musicmetadataservice.global.error.errorcode.ErrorCode
import com.bandage.musicmetadataservice.global.error.exception.MusicBrainzApiException
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
 * - 404 응답은 lookup 시 null 반환, 그 외 비-2xx 는 [com.bandage.musicmetadataservice.global.error.exception.MusicBrainzApiException] throw
 *
 * 다중 인스턴스 / 멀티 프로세스 환경에서는 본 throttle 이 전역 보장을 못한다.
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

    override fun searchRecording(query: String, limit: Int, offset: Int, mode: SearchMode): List<Recording> =
        runBlocking {
            val response = throttledGet("/ws/2/recording/") {
                parameter("query", query.transformQuery(mode))
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

    override fun searchArtist(query: String, limit: Int, offset: Int, mode: SearchMode): List<Artist> =
        runBlocking {
            val response = throttledGet("/ws/2/artist/") {
                parameter("query", query.transformQuery(mode))
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

    override fun searchReleaseGroup(query: String, limit: Int, offset: Int, mode: SearchMode): List<ReleaseGroup> =
        runBlocking {
            val response = throttledGet("/ws/2/release-group/") {
                parameter("query", query.transformQuery(mode))
                parameter("limit", limit)
                parameter("offset", offset)
                parameter("fmt", "json")
            }
            ensureSuccess(response)
            val dto: MusicBrainzReleaseGroupSearchResponse = response.body()
            dto.releaseGroups.map { it.toDomain() }
        }

    override fun searchAll(
        query: String,
        limit: Int,
        mode: SearchMode,
        sort: SearchSort,
    ): List<UnifiedSearchHit> {
        // 직렬 호출. throttle 이 호출 간 1초 간격 강제 → 약 3초 소요
        val recordings = searchRecording(query, limit, mode = mode).map { it.toUnifiedHit() }
        val artists = searchArtist(query, limit, mode = mode).map { it.toUnifiedHit() }
        val releaseGroups = searchReleaseGroup(query, limit, mode = mode).map { it.toUnifiedHit() }

        val merged = recordings + artists + releaseGroups
        return when (sort) {
            SearchSort.SCORE -> merged.sortedWith(
                compareByDescending<UnifiedSearchHit> { it.score ?: 0 }
                    .thenByDescending { it.releaseCount },
            )
            SearchSort.RELEASE_COUNT -> merged.sortedWith(
                compareByDescending<UnifiedSearchHit> { it.releaseCount }
                    .thenByDescending { it.score ?: 0 },
            )
        }
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
        val errorCode = when (response.status.value) {
            400 -> ErrorCode.MUSICBRAINZ_INVALID_REQUEST
            404 -> ErrorCode.MUSICBRAINZ_NOT_FOUND
            429 -> ErrorCode.MUSICBRAINZ_RATE_LIMITED
            503 -> ErrorCode.MUSICBRAINZ_UNAVAILABLE
            else -> ErrorCode.MUSICBRAINZ_UPSTREAM_ERROR
        }
        throw MusicBrainzApiException(
            errorCode = errorCode,
            statusCode = response.status.value,
            rawBody = response.bodyAsText(),
            retryAfterSeconds = retryAfter,
        )
    }

    /**
     * [SearchMode.LOOSE] 시 토큰 단위 fuzzy(`~1`) 부착.
     *
     * - 공백 단위로 토큰화, 각 토큰에 Lucene reserved char escape 후 `~1` 부착
     * - 1자 토큰은 Lucene 이 fuzzy 적용을 거절할 수 있으므로 wildcard `*` 부착으로 대체
     * - field-prefixed 토큰(`artist:Beatles`) 은 prefix 보존 후 value 에만 fuzzy 부착
     */
    private fun String.transformQuery(mode: SearchMode): String =
        when (mode) {
            SearchMode.EXACT -> this
            SearchMode.LOOSE -> looseTokens(this)
        }

    private fun looseTokens(raw: String): String {
        val tokens = raw.trim().split(WHITESPACE).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return raw
        return tokens.joinToString(" ") { token -> applyFuzzy(token) }
    }

    private fun applyFuzzy(token: String): String {
        val (prefix, value) = splitFieldPrefix(token)
        val escaped = escapeLucene(value)
        val transformed =
            if (escaped.length <= 1) "$escaped*" else "$escaped~1"
        return "$prefix$transformed"
    }

    private fun splitFieldPrefix(token: String): Pair<String, String> {
        val colon = token.indexOf(':')
        if (colon <= 0 || colon == token.length - 1) return "" to token
        return token.substring(0, colon + 1) to token.substring(colon + 1)
    }

    private fun escapeLucene(value: String): String =
        value.replace(Regex("([+\\-!(){}\\[\\]^\"~*?:\\\\/])"), "\\\\$1")

    private fun Recording.toUnifiedHit(): UnifiedSearchHit =
        UnifiedSearchHit(
            type = SearchEntityType.RECORDING,
            id = id,
            title = title,
            subtitle = artists.firstOrNull()?.name,
            score = score,
            releaseCount = releaseCount,
            payload = this,
        )

    private fun Artist.toUnifiedHit(): UnifiedSearchHit =
        UnifiedSearchHit(
            type = SearchEntityType.ARTIST,
            id = id,
            title = name,
            subtitle = sortName ?: type,
            score = score,
            releaseCount = 0,
            payload = this,
        )

    private fun ReleaseGroup.toUnifiedHit(): UnifiedSearchHit =
        UnifiedSearchHit(
            type = SearchEntityType.RELEASE_GROUP,
            id = id,
            title = title,
            subtitle = artistCredit.firstOrNull()?.name,
            score = score,
            releaseCount = releaseCount,
            payload = this,
        )

    companion object {
        private val WHITESPACE = Regex("\\s+")
    }
}
