package com.bandage.musicmetadataservice.global.error.handler

import com.bandage.musicmetadataservice.global.common.response.ApiResponse
import com.bandage.musicmetadataservice.global.error.errorcode.ErrorCode
import com.bandage.musicmetadataservice.global.error.exception.BusinessException
import com.bandage.musicmetadataservice.global.error.exception.Exception
import com.bandage.musicmetadataservice.global.error.exception.MusicBrainzApiException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
open class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler
    protected fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = e.errorCode
        return ResponseEntity
            .status(errorCode.status)
            .body(ApiResponse.error(message = errorCode.message, code = errorCode.name))
    }

    @ExceptionHandler(MusicBrainzApiException::class)
    protected fun handleMusicBrainzApiException(e: MusicBrainzApiException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = e.errorCode
        log.warn(
            "MusicBrainz API error: status={}, retryAfter={}, body={}",
            e.statusCode,
            e.retryAfterSeconds,
            e.rawBody,
        )
        val builder = ResponseEntity.status(errorCode.status)
        e.retryAfterSeconds?.let { builder.header(HttpHeaders.RETRY_AFTER, it.toString()) }
        return builder.body(ApiResponse.error(message = errorCode.message, code = errorCode.name))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val fieldErrors =
            e.bindingResult.fieldErrors
                .associate { it.field to (it.defaultMessage ?: "잘못된 입력값입니다.") }
        val firstMessage = fieldErrors.values.firstOrNull() ?: ErrorCode.INVALID_INPUT_VALUE.message
        val response =
            ApiResponse.error(
                message = firstMessage,
                code = ErrorCode.INVALID_INPUT_VALUE.name,
                fieldErrors = fieldErrors.takeIf { it.isNotEmpty() },
            )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ApiResponse<Nothing>> {
        val response =
            ApiResponse.error(
                message = e.message,
                code = ErrorCode.INVALID_INPUT_VALUE.name,
            )
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    protected fun handleMethodNotSupported(e: HttpRequestMethodNotSupportedException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = ErrorCode.METHOD_NOT_ALLOWED
        return ResponseEntity
            .status(errorCode.status)
            .body(
                ApiResponse.error(
                    message = "${errorCode.message} (요청 메서드: ${e.method})",
                    code = errorCode.name,
                ),
            )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    protected fun handleNoResourceFound(e: NoResourceFoundException): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = ErrorCode.RESOURCE_NOT_FOUND
        return ResponseEntity
            .status(errorCode.status)
            .body(ApiResponse.error(message = errorCode.message, code = errorCode.name))
    }

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        val response = ApiResponse.error(message = errorCode.message, code = errorCode.name)
        return ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
