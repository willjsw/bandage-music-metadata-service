package com.bandage.musicmetadataservice.domain.model

/**
 * 통합 검색 단일 결과. recording / artist / release-group 어느 entity 에서 왔는지를 [type] 으로 표시.
 *
 * - title: 표시용 1차 라벨 (recording.title / artist.name / releaseGroup.title)
 * - subtitle: 표시용 2차 라벨 (recording 의 첫 아티스트 / artist 의 sortName / releaseGroup 의 첫 아티스트)
 * - score: MusicBrainz 의 검색 score (0..100). 가중치 없이 entity 간 동일 스케일로 사용.
 * - releaseCount: recording / release-group 의 release 수, artist 는 0
 * - payload: 원래 도메인 모델 (Recording / Artist / ReleaseGroup)
 */
data class UnifiedSearchHit(
    val type: SearchEntityType,
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val score: Int? = null,
    val releaseCount: Int = 0,
    val payload: Any,
)

enum class SearchEntityType {
    RECORDING,
    ARTIST,
    RELEASE_GROUP,
}
