package com.bandage.musicmetadataservice.application.port.outbound

interface MusicInfoApiClient {
    fun getToken():String
    fun getMusicInfo():String
}