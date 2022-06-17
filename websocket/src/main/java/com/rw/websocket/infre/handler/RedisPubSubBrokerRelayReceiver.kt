package com.rw.websocket.infre.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.rw.random.common.constants.RedisKeyConstants
import com.rw.random.common.dto.RedisStreamMessage
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.infre.session.DefaultRandomWorldSessionManager
import com.rw.websocket.infre.subscription.SubscriptionRegistry
import com.rw.websocket.infre.utils.SimpleMessageUtils
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.GroupedFlux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple3
import reactor.util.function.Tuples
import java.time.Duration

@Component
open class RedisPubSubBrokerRelayReceiver(
    private val redisTemplate: ReactiveStringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val subscriptionRegistry: SubscriptionRegistry,
    private val sessionManager: DefaultRandomWorldSessionManager,
    private val userFishRepository: UserFishRepository,
) : SmartLifecycle {

    private var isRunning = false
    private val log = LoggerFactory.getLogger(javaClass)

    @Suppress("UNCHECKED_CAST")
    fun receive(): Flux<RedisStreamMessage> {
        return redisTemplate.listenToChannel(
            RedisKeyConstants.REDIS_CHANNEL_KEY
        )
            .doOnNext {
                log.trace("receive message from channel: ${it.message}")
            }
            .map {
                objectMapper.readValue(it.message, RedisStreamMessage::class.java)
            }
            .onErrorResume { err ->
                log.error("Build message failed, err_msg={}", err.message, err)
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
        receive()
            .filter { SimpleMessageUtils.legalDestination(it.dest) }
            .flatMap { msg ->
                val dest = if (SimpleMessageUtils.isOwnerDestination(msg.dest!!)) {
                    userFishRepository.findFishOwner(
                        msg.dest!!.replace(SimpleMessageUtils.OWNER_DESTINATION_PREFIX, "").toLong()
                    )
                        .map {
                            SimpleMessageUtils.buildUserDestination(it)
                        }
                } else {
                    Mono.just(msg.dest!!)
                }
                dest
                    .flatMapMany {
                        Flux.fromIterable(subscriptionRegistry.getSubscriptions(it))
                    }
                    .flatMap {
                        Mono.justOrEmpty(sessionManager.find(it.sessionId))
                    }
                    .map {
                        it!!.webSocketSession
                    }
                    .flatMap {
                        val sendMsg = it.textMessage(objectMapper.writeValueAsString(msg.payload!!))
//                        log.info("send msg: {}", sendMsg.payloadAsText)
                        it.send(Mono.just(sendMsg))
                    }
            }
            .onErrorResume {
                log.error("SEND ERROR", it)
                Mono.empty()
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
