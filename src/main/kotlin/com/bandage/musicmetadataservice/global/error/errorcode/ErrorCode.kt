package com.bandage.musicmetadataservice.global.error.errorcode

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String,
) {
    // common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    NO_CHANGE(HttpStatus.BAD_REQUEST, "변경된 사항이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    // spotify

    // musicbrainz
    MUSICBRAINZ_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "MusicBrainz 요청 파라미터가 올바르지 않습니다."),
    MUSICBRAINZ_NOT_FOUND(HttpStatus.NOT_FOUND, "MusicBrainz 리소스를 찾을 수 없습니다."),
    MUSICBRAINZ_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "MusicBrainz 호출 한도를 초과했습니다. 잠시 후 다시 시도하세요."),
    MUSICBRAINZ_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "MusicBrainz 서비스가 일시적으로 사용 불가합니다."),
    MUSICBRAINZ_UPSTREAM_ERROR(HttpStatus.BAD_GATEWAY, "MusicBrainz 응답 처리 중 오류가 발생했습니다."),
}
