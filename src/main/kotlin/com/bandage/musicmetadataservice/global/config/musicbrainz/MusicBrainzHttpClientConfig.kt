package com.bandage.musicmetadataservice.global.config.musicbrainz

import com.bandage.musicmetadataservice.global.properties.MusicBrainzApiProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.jackson
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MusicBrainzHttpClientConfig(
    private val properties: MusicBrainzApiProperties,
) {

    /**
     * MusicBrainz Web Service 호출용 Ktor HttpClient.
     * - 엔진: CIO (코루틴 비동기)
     * - JSON: Jackson + Kotlin 모듈, unknown 필드 무시
     * - expectSuccess=false: 4xx/5xx 도 예외 없이 받아서 호출부에서 처리
     * - User-Agent: MusicBrainz 정책상 식별 가능한 형식 필수.
     *   "AppName/Version ( contact-email )" 형태로 모든 요청에 자동 부착.
     * - Accept: application/json — `fmt=json` 쿼리와 일관성 유지
     */
    @Bean
    fun musicBrainzHttpClient(): HttpClient =
        HttpClient(CIO) {
            expectSuccess = false
            install(ContentNegotiation) {
                jackson {
                    registerModule(KotlinModule.Builder().build())
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            install(DefaultRequest) {
                header(HttpHeaders.UserAgent, "${properties.userAgent} ( ${properties.contact} )")
                header(HttpHeaders.Accept, "application/json")
            }
        }
}
