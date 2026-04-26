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
}
