package com.bandage.musicmetadataservice.domain.model.dto

/**
 * 발매(release) 도메인 모델 — 보통 앨범/싱글에 해당.
 *
 * - date: ISO 8601 부분 문자열 가능 ("2008", "2008-05", "2008-05-13")
 * - country: ISO 3166 alpha-2 코드
 * - artistCredit: 표시 순서 보존
 */
data class Release(
    val id: String,
    val title: String,
    val date: String? = null,
    val country: String? = null,
    val artistCredit: List<ArtistRef> = emptyList(),
)