package com.rw.websocket.infre.handler

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.messaging.Message
import org.springframework.messaging.ReactiveMessageHandler
import reactor.core.publisher.Mono
import java.time.Duration

open class DefaultMessageBrokerRelayPublisher(
    private val redisTemplate: ReactiveStringRedisTemplate
) : ReactiveMessageHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun handleMessage(message: Message<*>): Mono<Void> {
        log.debug("Send message to broker relay: {}", message)
        return send(message)
    }

    private fun send(message: Message<*>): Mono<Void> {
        return serializeMessageAsStringMap(message)
            .flatMap {
                redisTemplate.opsForStream<Any, Any>()
                    .add("topic_random_world", it)
                    .timeout(Duration.ofSeconds(1))
                    .retry(1)
                    .onErrorResume { err ->
                        log.error("send to redis error", err)
                        Mono.empty()
                    }
            }
            .then()
    }

    @Suppress("DuplicatedCode")
    private fun serializeMessageAsStringMap(message: Message<*>): Mono<MutableMap<String, String>> {

        val payloadMap = mutableMapOf<String, String>()
        message.headers.entries.forEach {
            payloadMap[it.key] = it.value.toString()
        }
        return when (val payloadData = message.payload) {
            is String -> {
                payloadMap[PAYLOAD_KEY] = payloadData
                Mono.just(payloadMap)
            }
            else -> {
                Mono.error(IllegalStateException("数据类型不支持"))
            }
        }
    }

    companion object {
        const val PAYLOAD_KEY = "__PAYLOAD"
    }
}