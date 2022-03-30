package com.rw.websocket.infre.handler

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
open class RedisPubSubBrokerRelayReceiver(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : SmartLifecycle {

    private val log = LoggerFactory.getLogger(javaClass)

    @Suppress("UNCHECKED_CAST")
    fun receive(): Flux<Message<*>> {
        return redisTemplate.listenToChannel(
            "topic_random_world"
        )
            .map {
                objectMapper.writeValueAsString(it)
            }
            .doOnNext {
                log.info("receive message from redis: {}", it)
            }
            .onErrorResume { err ->
                log.error("Build message failed, err_msg={}", err.message, err)
                Mono.empty()
            } as Flux<Message<*>>
    }

    companion object {
        const val PAYLOAD_KEY = "__PAYLOAD"
    }

    override fun start() {
        receive()
            .subscribe()
    }

    override fun stop() {
    }

    override fun isRunning(): Boolean {
        return true
    }
}