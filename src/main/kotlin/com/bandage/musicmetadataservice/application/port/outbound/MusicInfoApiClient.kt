package com.bandage.musicmetadataservice.application.port.outbound

import com.bandage.musicmetadataservice.domain.model.Artist
import com.bandage.musicmetadataservice.domain.model.Recording
import com.bandage.musicmetadataservice.domain.model.ReleaseGroup
import com.bandage.musicmetadataservice.domain.model.SearchMode
import com.bandage.musicmetadataservice.domain.model.SearchSort
import com.bandage.musicmetadataservice.domain.model.UnifiedSearchHit

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

    fun searchRecording(
        query: String,
        limit: Int = 25,
        offset: Int = 0,
        mode: SearchMode = SearchMode.EXACT,
    ): List<Recording>

    fun lookupRecording(id: String): Recording?

    // ===== Artist =====

    fun searchArtist(
        query: String,
        limit: Int = 25,
        offset: Int = 0,
        mode: SearchMode = SearchMode.EXACT,
    ): List<Artist>

    fun lookupArtist(id: String): Artist?

    // ===== Release Group =====

    fun searchReleaseGroup(
        query: String,
        limit: Int = 25,
        offset: Int = 0,
        mode: SearchMode = SearchMode.EXACT,
    ): List<ReleaseGroup>

    // ===== 통합 검색 =====

    /**
     * 같은 query 로 recording / artist / release-group 을 동시에 조회한 뒤
     * 단일 정렬된 hit 리스트로 반환한다.
     *
     * 가중치는 적용하지 않는다 — entity 간 score 를 동일 스케일로 사용한다.
     *
     * @param limit entity **당** 호출 limit (총 결과는 최대 3 * limit)
     */
    fun searchAll(
        query: String,
        limit: Int = 10,
        mode: SearchMode = SearchMode.EXACT,
        sort: SearchSort = SearchSort.SCORE,
    ): List<UnifiedSearchHit>

    // ===== Legacy (보존용, 신규 사용 금지) =====

    @Deprecated("Use searchRecording / searchArtist instead", ReplaceWith("searchRecording(query)"))
    fun getToken(): String = throw UnsupportedOperationException("legacy method — use searchRecording/searchArtist")

    @Deprecated("Use searchRecording / searchArtist instead", ReplaceWith("searchRecording(query)"))
    fun getMusicInfo(): String = throw UnsupportedOperationException("legacy method — use searchRecording/searchArtist")
}
