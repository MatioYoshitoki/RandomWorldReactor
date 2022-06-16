package com.rw.random.infra.handler

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.lang.Tuple
import com.rw.random.common.constants.BeingStatus
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
import reactor.util.function.Tuples

@Component
open class WorldMessageDispatchHandler(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val snowflake: Snowflake,
    private val pubsubMessageHandler: PubsubMessageHandler,
    private val applicationProperties: ApplicationProperties,
    private val zone: RWZone
) : SmartLifecycle, ApplicationEventPublisherAware {

    // 此处的队列大小与池中鱼的数量密切相关需要保证1:3的比例
    open val worldChannel: Sinks.Many<RWEvent> =
        Sinks.many().unicast().onBackpressureBuffer(Queues.get<RWEvent>(applicationProperties.eventChannelSize).get())
    private var running: Boolean = false
    private val log = LoggerFactory.getLogger(javaClass)


    fun sendMsg(event: RWEvent) {
        try {
            SinksUtils.tryEmit(worldChannel, event, 20)
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
            .onErrorContinue { err, it ->
                log.error("dispatch error!", err, it)
            }
            .filter { it.target == null || it.target !is Being || it.target.isAlive() }
            .flatMap { event ->
                if (event is TimeEvent) {
                    subscriptionRegistry.findAllObjByTopic(event.topic)
                        .map { Tuples.of(it, event) }
                } else {
                    if (event.source != null && event.target != null) {
                        Flux.just(Tuples.of(event.target.id, event), Tuples.of(event.source.id, event))
                    } else if (event.source != null) {
                        Flux.just(Tuples.of(event.source.id, event))
                    } else if (event.target != null) {
                        Flux.just(Tuples.of(event.target.id, event))
                    } else {
                        Flux.empty()
                    }
                }
            }
            .map { Tuples.of(subscriptionRegistry.findConsumerByObjId(it.t1), it.t2) }
            .filter { it.t1.isPresent }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe {
                it.t1.get().accept(it.t2)
            }
    }

    private fun pushMessageToClient(event: RWEvent) {
        // 可配置的消息转发类型
        if (applicationProperties.messageTypeNeedToSend.contains(event.eventType)) {
            pubsubMessageHandler.sendToOwner(event)
        }
    }

    /**
     * 销毁对象
     * */
    private fun destroyObj(event: ObjectDestroyEvent) {
        publishObjectStatusChangeEvent(event.source!!.id, BeingStatus.DEAD, event.source.hasMaster)
        zone.clearObj(event.source)
        if (event.target != null && (event.source is Fish)) {
            event.target.handler.accept(
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

    @Suppress("SameParameterValue")
    private fun publishObjectStatusChangeEvent(sourceId: Long, status: BeingStatus, hasMaster: Boolean) {
        this.eventPublisher.publishEvent(ObjectStatusModifyEvent.of(sourceId, status, hasMaster))
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
