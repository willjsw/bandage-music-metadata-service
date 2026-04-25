package com.bandage.musicmetadataservice.global.async.publisher

import com.bandage.musicmetadataservice.global.async.event.CommonEvent

interface EventPublisher {
    fun publish(event: CommonEvent)
}
