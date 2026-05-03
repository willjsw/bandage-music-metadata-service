package com.bandage.musicmetadataservice.domain.model.dto

/**
 * 아티스트 도메인 모델 (단일 조회/검색 결과용).
 *
 * - id: 외부 소스 식별자 (MBID 등)
 * - sortName: 정렬 표기 (예: "Beatles, The")
 * - country: ISO 3166 alpha-2 코드 (소스가 제공하는 경우)
 * - type: "Person", "Group", "Orchestra" 등 (소스가 제공하는 경우)
 */
data class Artist(
    val id: String,
    val name: String,
    val sortName: String? = null,
    val country: String? = null,
    val type: String? = null,
    /** MusicBrainz search 응답의 score (0..100). lookup 결과에는 항상 null. */
    val score: Int? = null,
)

/**
 * recording 등 다른 엔티티의 크레딧으로 등장하는 경량 아티스트 참조.
 */
data class ArtistRef(
    val id: String,
    val name: String,
)
