package com.rw.websocket.infre.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.rw.random.common.constants.RedisKeyConstants
import com.rw.random.common.dto.RedisStreamMessage
import com.rw.websocket.infre.session.DefaultRandomWorldSessionManager
import com.rw.websocket.infre.subscription.SubscriptionRegistry
import com.rw.websocket.infre.utils.SimpleMessageUtils
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
open class RedisPubSubBrokerRelayReceiver(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val subscriptionRegistry: SubscriptionRegistry,
    private val sessionManager: DefaultRandomWorldSessionManager,
) : SmartLifecycle {

    private var isRunning = false
    private val log = LoggerFactory.getLogger(javaClass)

    @Suppress("UNCHECKED_CAST")
    fun receive(): Flux<RedisStreamMessage> {
        return redisTemplate.listenToChannel(
            RedisKeyConstants.REDIS_CHANNEL_KEY
        )
            .map {
                objectMapper.readValue(it.message, RedisStreamMessage::class.java)
            }
//            .doOnNext {
//                log.info("receive message from redis: {}", it)
//            }
            .filter { it.level!! <= 1 }
            .onErrorResume { err ->
                log.error("Build message failed, err_msg={}", err.message, err)
                Mono.empty()
            }
    }

    companion object {
        const val PAYLOAD_KEY = "__PAYLOAD"
    }

    override fun start() {
        this.isRunning = true
        receive()
            .map { objectMapper.writeValueAsString(it) }
            .flatMap { msg ->
                Flux.fromIterable(
                    subscriptionRegistry.getSubscriptions(SimpleMessageUtils.buildWorldDestination())
                )
                    .flatMap {
                        Mono.justOrEmpty(sessionManager.find(it.sessionId))
                    }
                    .map {
                        it!!.webSocketSession
                    }
                    .flatMap {
                        val sendMsg = it.textMessage(msg)
                        log.info("send msg: {}", sendMsg.payloadAsText)
                        it.send(Mono.just(sendMsg))
                    }
            }
            .subscribe()
    }

    override fun stop() {
        this.isRunning = false
    }

    override fun isRunning(): Boolean {
        return isRunning
    }
}
