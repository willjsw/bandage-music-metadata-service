package com.bandage.musicmetadataservice.global.error.exception

import com.bandage.musicmetadataservice.global.error.errorcode.ErrorCode

open class Exception(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
