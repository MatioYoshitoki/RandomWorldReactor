package com.rw.websocket.infra.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.rw.random.common.constants.RedisKeyConstants
import com.rw.random.common.dto.RedisStreamMessage
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.infra.event.MessageSendEvent
import com.rw.websocket.infra.event.MessageSendEventPayload
import com.rw.websocket.infra.session.DefaultRandomWorldSessionManager
import com.rw.websocket.infra.subscription.SubscriptionRegistry
import com.rw.websocket.infra.utils.SimpleMessageUtils
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
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
    private val userFishRepository: UserFishRepository,
) : SmartLifecycle, ApplicationEventPublisherAware {

    private var isRunning = false
    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var publisher: ApplicationEventPublisher

    @Suppress("UNCHECKED_CAST")
    private fun receive(): Flux<String> {
        return redisTemplate.listenToChannel(
            RedisKeyConstants.REDIS_CHANNEL_KEY
        )
            .map { it.message }
            .doOnNext {
                log.trace("receive message from channel: $it")
            }
            .doOnNext{
                publisher.publishEvent(MessageSendEvent(MessageSendEventPayload.of(it)))
            }
            .onErrorResume { err ->
                log.error("Build message failed, err_msg={}, message={}", err.message, err)
                Mono.empty()
            }
    }

    companion object {
        const val PAYLOAD_KEY = "__PAYLOAD"
    }

//    private fun aggMessageFlux(groupedFlux: GroupedFlux<Tuple3<String, Long, String>, RedisStreamMessage>): Flux<RedisStreamMessage> {
//        val key = groupedFlux.key()
//        return if (key.t1 == "BeAtk") {
//            groupedFlux
//                .buffer(Duration.ofMillis(500))
//                .map {
//                    aggMessage(it, key.t2, key.t3)
//                }
//        } else {
//            groupedFlux
//        }
//    }

//    private fun aggMessage(
//        msg: List<RedisStreamMessage>,
//        targetId: Long,
//        targetName: String
//    ): RedisStreamMessage {
//        val message = RedisStreamMessage(targetId = targetId, targetName = targetName)
//
//        val msgText = """
//            $targetName 受到【${
//            msg.map { it.sourceName }.joinToString(",")
//        }】攻击, 生命值减少${msg.sumOf { it.payload?.toIntOrNull() ?: 0 }}。
//        """.trimIndent()
//        message.message = msgText
//        message.eventType = "BeAtkBatch"
//        message.level = 1
//        return message
//    }

    override fun start() {
        this.isRunning = true
        receive().subscribe()
    }

    override fun stop() {
        this.isRunning = false
    }

    override fun isRunning(): Boolean {
        return isRunning
    }

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        this.publisher = applicationEventPublisher
    }
}
