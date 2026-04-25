package com.bandage.musicmetadataservice.global.error.exception

import com.bandage.musicmetadataservice.global.error.errorcode.ErrorCode

open class BusinessException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)
