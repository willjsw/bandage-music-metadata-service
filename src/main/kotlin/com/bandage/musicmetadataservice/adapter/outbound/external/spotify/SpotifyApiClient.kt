package com.bandage.musicmetadataservice.adapter.outbound.external.spotify

import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.request.SpotifySearchRequest
import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.SpotifyTokenResponse
import com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.response.search.SpotifySearchResponse
import com.bandage.musicmetadataservice.application.port.outbound.MusicInfoApiClient
import com.bandage.musicmetadataservice.global.config.SpotifyApiProperties
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference

/**
 * Spotify Web API 호출 어댑터.
 *
 * - Client Credentials Flow 로 access token 발급 후 캐시
 * - GET /v1/search 호출
 *
 * 토큰 캐시는 만료 60초 전부터 무효화되어 자동 재발급된다.
 */
@Component
class SpotifyApiClient(
    private val spotifyHttpClient: HttpClient,
    private val properties: SpotifyApiProperties,
) : MusicInfoApiClient {
    private val tokenCache = AtomicReference<CachedToken?>(null)

    override fun getToken(): String = runBlocking { ensureAccessToken() }

    override fun getMusicInfo(): String =
        throw UnsupportedOperationException("Use search(SpotifySearchRequest) instead")

    /**
     * Client Credentials Flow 로 access token 을 새로 발급한다 (캐시 우회).
     */
    fun issueAccessToken(): SpotifyTokenResponse =
        runBlocking {
            val token = fetchToken()
            tokenCache.set(CachedToken.of(token))
            token
        }

    /**
     * GET /v1/search 호출. 응답을 raw DTO 그대로 반환.
     */
    fun search(request: SpotifySearchRequest): SpotifySearchResponse =
        runBlocking {
            val token = ensureAccessToken()
            val response =
                spotifyHttpClient.get("${properties.apiBaseUrl}/v1/search") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    request.toQueryParams().forEach { (k, v) -> parameter(k, v) }
                }
            ensureSuccess(response)
            response.body()
        }

    private suspend fun ensureAccessToken(): String {
        tokenCache.get()?.takeUnless { it.isExpired() }?.let { return it.accessToken }
        val fresh = fetchToken()
        tokenCache.set(CachedToken.of(fresh))
        return fresh.accessToken
    }

    private suspend fun fetchToken(): SpotifyTokenResponse {
        val basic = Base64.getEncoder().encodeToString("${properties.clientId}:${properties.clientSecret}".toByteArray())
        val response =
            spotifyHttpClient.post("${properties.accountsBaseUrl}/api/token") {
                header(HttpHeaders.Authorization, "Basic $basic")
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("grant_type=client_credentials")
            }
        ensureSuccess(response)
        return response.body()
    }

    private suspend fun ensureSuccess(response: HttpResponse) {
        if (response.status.isSuccess()) return
        val retryAfter = response.headers[HttpHeaders.RetryAfter]?.toLongOrNull()
        throw SpotifyApiException(
            statusCode = response.status.value,
            rawBody = response.bodyAsText(),
            retryAfterSeconds = retryAfter,
        )
    }
}

private data class CachedToken(
    val accessToken: String,
    val expiresAt: Instant,
) {
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    companion object {
        private const val EXPIRY_MARGIN_SECONDS = 60L

        fun of(token: SpotifyTokenResponse): CachedToken =
            CachedToken(
                accessToken = token.accessToken,
                expiresAt = Instant.now().plusSeconds(token.expiresIn - EXPIRY_MARGIN_SECONDS),
            )
    }
}
