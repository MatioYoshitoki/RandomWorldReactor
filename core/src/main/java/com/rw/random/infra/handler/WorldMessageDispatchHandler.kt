package com.rw.random.infra.handler

import cn.hutool.core.lang.Snowflake
import com.rw.random.common.constants.BeingStatus
import com.rw.random.common.dto.RedisStreamMessage
import com.rw.random.domain.entity.*
import com.rw.random.domain.entity.obj.Being
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.infra.config.ApplicationProperties
import com.rw.random.infra.listener.ObjectStatusModifyEvent
import com.rw.random.infra.subscription.SubscriptionRegistry
import com.rw.random.infra.utils.SinksUtils
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.util.concurrent.Queues

@Component
open class WorldMessageDispatchHandler(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val snowflake: Snowflake,
    private val pubsubMessageHandler: PubsubMessageHandler,
    private val applicationProperties: ApplicationProperties,
) : SmartLifecycle, ApplicationEventPublisherAware {

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
            .publishOn(Schedulers.boundedElastic())
            .doOnNext { event ->
                if (event is ObjectDestroyEvent) {
                    Mono.just(1).subscribe { destroyObj(event) }
                }
            }
            .doOnNext { event -> Mono.just(1).subscribe { pushMessageToClient(event) } }
            .filter { it !is InternalEvent }
            .flatMap { event ->
                if (event.target != null && event.target is Being) {
                    if (!event.target.isAlive()) {
                        return@flatMap Mono.empty<Void>()
                    }
                }
                val targetIdList = mutableListOf<Long>()
                if (event.target != null) {
                    targetIdList.add(event.target.id)
                }
                if (event.source != null) {
                    targetIdList.add(event.source.id)
                }
                val flux = if (event is TimeEvent) {
                    subscriptionRegistry.findAllObjByTopic(event.topic)
                } else {
                    Flux.fromIterable(targetIdList)
                }
                flux
                    .map {
                        subscriptionRegistry.findConsumerByObjId(it)
                    }
                    .filter { it.isPresent }
                    .doOnNext {
                        it.get().accept(event)
                    }
            }
            .subscribe()
    }

    private fun pushMessageToClient(event: RWEvent) {
        // 可配置的消息转发类型
        if (applicationProperties.messageTypeNeedToSend.contains(event.eventType)) {
            val message = """
            {"source_name": "${event.source?.name}", "source_id": ${event.source?.id}, "target_name": "${event.target?.name}", "target_id": ${event.target?.id}, "event_type": "${event.eventType}", "level": 1, "message": "${event.msg}"}
            """.trimIndent()
            pubsubMessageHandler.sendMessage(message)
        }
    }

    /**
     * 销毁对象
     * */
    private fun destroyObj(event: ObjectDestroyEvent) {
        if (event.source!!.hasMaster) {
            publishObjectStatusChangeEvent(event.source.id, BeingStatus.DEAD)
        }
        subscriptionRegistry.findZoneByTopic(event.topic)?.clearObj(event.source)
        if (event.target != null && (event.source is Fish)) {
            val eater = subscriptionRegistry.findConsumerByObjId(event.target.id)
            if (eater.isPresent) {
                eater.get().accept(
                    EarnEvent(
                        snowflake.nextId(),
                        "Earn",
                        event.source.weight,
                        event.target.topic,
                        event.target
                    )
                )
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun publishObjectStatusChangeEvent(sourceId: Long, status: BeingStatus) {
        this.eventPublisher.publishEvent(ObjectStatusModifyEvent.of(sourceId, status))
    }

    override fun stop() {
        running = false
    }

    override fun isRunning(): Boolean {
        return running
    }

    private lateinit var eventPublisher: ApplicationEventPublisher

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher
    }

}
