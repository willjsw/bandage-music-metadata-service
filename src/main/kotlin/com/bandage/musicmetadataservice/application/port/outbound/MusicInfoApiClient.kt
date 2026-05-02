package com.bandage.musicmetadataservice.application.port.outbound

import com.bandage.musicmetadataservice.domain.model.Artist
import com.bandage.musicmetadataservice.domain.model.Recording

/**
 * 외부 음악 메타데이터 API 호출 포트 (driven port).
 *
 * 구현체:
 * - MusicBrainzApiClient — 기본 활성 어댑터
 * - SpotifyApiClient — 보존되지만 빈 등록 비활성화
 *
 * 도메인 모델만 반환하며, 외부 DTO 는 어댑터 내부에서 매핑된다.
 */
interface MusicInfoApiClient {

    // ===== Recording =====

    /**
     * 검색 쿼리로 recording(track) 목록 조회.
     *
     * @param query 소스별 query syntax 그대로 (MusicBrainz: Lucene, Spotify: q 파라미터)
     */
    fun searchRecording(query: String, limit: Int = 25, offset: Int = 0): List<Recording>

    /**
     * 식별자(MBID 또는 외부 소스 id) 로 단일 recording 조회. 없으면 null.
     */
    fun lookupRecording(id: String): Recording?

    // ===== Artist =====

    fun searchArtist(query: String, limit: Int = 25, offset: Int = 0): List<Artist>

    fun lookupArtist(id: String): Artist?

    // ===== Legacy (보존용, 신규 사용 금지) =====

    @Deprecated("Use searchRecording / searchArtist instead", ReplaceWith("searchRecording(query)"))
    fun getToken(): String = throw UnsupportedOperationException("legacy method — use searchRecording/searchArtist")

    @Deprecated("Use searchRecording / searchArtist instead", ReplaceWith("searchRecording(query)"))
    fun getMusicInfo(): String = throw UnsupportedOperationException("legacy method — use searchRecording/searchArtist")
}
