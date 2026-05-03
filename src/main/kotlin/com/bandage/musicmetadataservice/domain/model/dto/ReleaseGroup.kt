package com.bandage.musicmetadataservice.domain.model.dto

/**
 * Release Group 도메인 모델 — 앨범/싱글/EP 등 발매 단위의 추상.
 *
 * 같은 ReleaseGroup 에 다국가/리마스터/디지털 등 여러 [Release] 가 매달릴 수 있다.
 *
 * - primaryType: "Album" / "Single" / "EP" / "Broadcast" / "Other"
 * - secondaryTypes: "Compilation" / "Soundtrack" / "Live" / "Remix" / "Demo" 등
 * - firstReleaseDate: 최초 발매일 (ISO 8601 부분 문자열)
 * - releaseCount: 본 ReleaseGroup 산하 release 수 (MB search 응답의 releases 배열 길이)
 * - score: MusicBrainz search 응답의 score
 */
data class ReleaseGroup(
    val id: String,
    val title: String,
    val primaryType: String? = null,
    val secondaryTypes: List<String> = emptyList(),
    val firstReleaseDate: String? = null,
    val artistCredit: List<ArtistRef> = emptyList(),
    val releaseCount: Int = 0,
    val score: Int? = null,
)