package com.bandage.musicmetadataservice.global.properties

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    value = [
        SpotifyApiProperties::class
    ],
)
class PropertiesConfig
