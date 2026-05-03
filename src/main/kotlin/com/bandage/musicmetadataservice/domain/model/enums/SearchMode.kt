package com.bandage.musicmetadataservice.domain.model.enums

/**
 * 외부 검색 호출 시 query 변환 모드.
 *
 * - [EXACT]: 입력 query 를 그대로 외부 API 에 전달. 호출자가 Lucene 문법 직접 사용 가능.
 * - [LOOSE]: 어댑터에서 토큰별로 fuzzy(`~1`) 부착. 부분일치/오탈자 허용.
 *   (Lucene reserved char 가 포함된 query 는 EXACT 사용 권장)
 */
enum class SearchMode {
    EXACT,
    LOOSE,
}

/**
 * 통합 검색 정렬 키.
 *
 * - [SCORE]: MusicBrainz 가 반환한 검색 score (기본). 가중치 없이 그대로 사용.
 * - [RELEASE_COUNT]: release 수 desc — recording/release-group 의 인기도 proxy.
 *   artist 결과는 release count 0 으로 처리되어 뒤로 밀린다.
 */
enum class SearchSort {
    SCORE,
    RELEASE_COUNT,
}
