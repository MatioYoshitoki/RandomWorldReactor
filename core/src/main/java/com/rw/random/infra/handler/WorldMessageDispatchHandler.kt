package com.rw.random.infra.handler

import cn.hutool.core.lang.Snowflake
import com.rw.random.common.dto.RedisStreamMessage
import com.rw.random.domain.entity.*
import com.rw.random.domain.entity.obj.Being
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.subscription.SubscriptionRegistry
import com.rw.random.infra.utils.SinksUtils
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues

@Component
open class WorldMessageDispatchHandler(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val snowflake: Snowflake,
    private val pubsubMessageHandler: PubsubMessageHandler,
) : SmartLifecycle {

    open val worldChannel: Sinks.Many<RWEvent> =
        Sinks.many().unicast().onBackpressureBuffer(Queues.get<RWEvent>(512).get())
    private var running: Boolean = false
    private val log = LoggerFactory.getLogger(javaClass)


    fun sendMsg(event: RWEvent) {
        try {
            SinksUtils.tryEmit(worldChannel, event)
        } catch (e: Exception) {
            log.error("send msg failed!", e)
        }
    }

    override fun start() {
        running = true
        worldChannel.asFlux()
            .doOnNext {
                if (it is ObjectDestroyEvent) {
                    subscriptionRegistry.findZoneByTopic(it.topic)?.clearObj(it.source!!)
                    if (it.target != null && (it.source is Fish)) {
                        val eater = subscriptionRegistry.findConsumerByObjId(it.target.id)
                        if (eater.isPresent) {
                            eater.get().accept(
                                EarnEvent(
                                    snowflake.nextId(),
                                    "Earn",
                                    it.source.weight,
                                    it.target.topic,
                                    it.target
                                )
                            )
                        }
                    }
                }
            }
            .doOnNext {
                if (it is BeAtkEvent) {
                    pubsubMessageHandler.sendMessage(
                        RedisStreamMessage(
                            it.source?.name,
                            it.source?.id,
                            it.target?.name,
                            it.target?.id,
                            it.eventType,
                            1,
                            it.msg
                        )
                    )
                } else {
                    pubsubMessageHandler.sendMessage(
                        RedisStreamMessage(
                            it.source?.name,
                            it.source?.id,
                            it.target?.name,
                            it.target?.id,
                            it.eventType,
                            2,
                            it.msg
                        )
                    )
                }
            }
            .filter { it !is InternalEvent }
            .delayUntil { event ->
                if (event.target != null && event.target is Being) {
                    if (!event.target.isAlive()) {
                        return@delayUntil Mono.empty<Void>()
                    }
                }
                subscriptionRegistry.findAllObjByTopic(event.topic)
                    .map { objId ->
                        subscriptionRegistry.findConsumerByObjId(objId)
                    }
                    .filter { it.isPresent }
                    .doOnNext {
//                        log.info("exchange msg: $event")
                        it.get().accept(event)
                    }
            }
            .subscribe()
    }

    override fun stop() {
        running = false
    }

    override fun isRunning(): Boolean {
        return running
    }

}