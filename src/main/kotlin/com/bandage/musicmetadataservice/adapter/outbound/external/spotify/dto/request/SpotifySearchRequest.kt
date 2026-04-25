package com.bandage.musicmetadataservice.adapter.outbound.external.spotify.dto.request

/**
 * GET https://api.spotify.com/v1/search 요청 파라미터.
 *
 * 제약:
 * - limit: 0..50 (기본 20)
 * - offset: 0..1000 (offset + limit ≤ 1000)
 * - market: ISO 3166-1 alpha-2
 */
data class SpotifySearchRequest(
    val q: String,
    val type: List<SpotifySearchType>,
    val market: String? = null,
    val limit: Int = DEFAULT_LIMIT,
    val offset: Int = DEFAULT_OFFSET,
    val includeExternal: IncludeExternal? = null,
) {
    init {
        require(q.isNotBlank()) { "q must not be blank" }
        require(type.isNotEmpty()) { "type must not be empty" }
        require(limit in MIN_LIMIT..MAX_LIMIT) { "limit must be in $MIN_LIMIT..$MAX_LIMIT" }
        require(offset in MIN_OFFSET..MAX_OFFSET) { "offset must be in $MIN_OFFSET..$MAX_OFFSET" }
        require(offset + limit <= MAX_OFFSET) { "(offset + limit) must be ≤ $MAX_OFFSET" }
    }

    fun toQueryParams(): Map<String, String> =
        buildMap {
            put("q", q)
            put("type", type.joinToString(",") { it.value })
            market?.let { put("market", it) }
            put("limit", limit.toString())
            put("offset", offset.toString())
            includeExternal?.let { put("include_external", it.value) }
        }

    companion object {
        const val MIN_LIMIT = 0
        const val MAX_LIMIT = 50
        const val DEFAULT_LIMIT = 20
        const val MIN_OFFSET = 0
        const val MAX_OFFSET = 1000
        const val DEFAULT_OFFSET = 0
    }
}

enum class SpotifySearchType(val value: String) {
    ALBUM("album"),
    ARTIST("artist"),
    PLAYLIST("playlist"),
    TRACK("track"),
    SHOW("show"),
    EPISODE("episode"),
    AUDIOBOOK("audiobook"),
}

enum class IncludeExternal(val value: String) {
    AUDIO("audio"),
}
