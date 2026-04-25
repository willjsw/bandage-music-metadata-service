package com.bandage.musicmetadataservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MusicMetadataServiceApplication

fun main(args: Array<String>) {
    runApplication<MusicMetadataServiceApplication>(*args)
}
