package com.bandage.musicmetadataservice.global.config.spotify

import com.bandage.musicmetadataservice.global.properties.SpotifyApiProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(
    name = ["external.music.provider"],
    havingValue = "spotify",
    matchIfMissing = false,
)
@EnableConfigurationProperties(SpotifyApiProperties::class)
class SpotifyHttpClientConfig {

    /**
     * Spotify Web API 호출용 Ktor HttpClient.
     * - 엔진: CIO (코루틴 비동기)
     * - JSON: Jackson + Kotlin 모듈, unknown 필드 무시
     * - expectSuccess=false: 4xx/5xx 도 예외 없이 받아서 호출부에서 처리
     */
    @Bean
    fun spotifyHttpClient(): HttpClient =
        HttpClient(CIO) {
            expectSuccess = false
            install(ContentNegotiation) {
                jackson {
                    registerModule(KotlinModule.Builder().build())
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
        }
}