package com.rw.random.infra.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.rw.random.domain.dto.RedisStreamMessage
import com.rw.random.domain.repository.RedisPubsubRepository
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues

@Component
open class PubsubMessageHandler(
    private val redisPubsubRepository: RedisPubsubRepository,
    private val objectMapper: ObjectMapper
) : SmartLifecycle {

    private val skins = Sinks.many().unicast().onBackpressureBuffer(Queues.get<RedisStreamMessage>(256).get())

    override fun start() {
        skins.asFlux()
            .map { objectMapper.writeValueAsString(it) }
            .flatMap {
                redisPubsubRepository.pubMessage(it)
            }
            .subscribe()
    }

    override fun stop() {
    }

    override fun isRunning(): Boolean {
        return true
    }
}