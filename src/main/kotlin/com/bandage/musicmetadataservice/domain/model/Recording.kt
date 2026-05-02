package com.bandage.musicmetadataservice.domain.model

/**
 * 음원 녹음(track) 도메인 모델.
 *
 * 외부 메타데이터 소스(MusicBrainz, Spotify 등) 의 표현으로부터 어댑터에서 매핑된다.
 * 도메인은 외부 DTO 를 모르며, 외부 식별자만 [id] 로 보관한다.
 *
 * - id: 외부 소스의 고유 식별자 (MusicBrainz 의 MBID, Spotify 의 track id 등)
 * - lengthMs: 녹음 길이(밀리초). 소스가 제공하지 않으면 null
 * - artists: 표시용 아티스트 크레딧 순서 보존
 * - isrcs: ISRC 목록 (없으면 빈 리스트)
 */
data class Recording(
    val id: String,
    val title: String,
    val lengthMs: Long? = null,
    val artists: List<ArtistRef> = emptyList(),
    val isrcs: List<String> = emptyList(),
)
