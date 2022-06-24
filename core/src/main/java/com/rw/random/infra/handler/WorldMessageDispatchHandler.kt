package com.rw.random.infra.handler

import cn.hutool.core.lang.Snowflake
import cn.hutool.core.util.RandomUtil
import com.rw.random.common.constants.BeingStatus
import com.rw.random.domain.entity.*
import com.rw.random.domain.entity.obj.Being
import com.rw.random.domain.entity.obj.ChaosGod
import com.rw.random.domain.entity.obj.Fish
import com.rw.random.domain.service.UserFishService
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
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import kotlin.streams.toList

@Component
open class WorldMessageDispatchHandler(
    private val subscriptionRegistry: SubscriptionRegistry,
    private val snowflake: Snowflake,
    private val userFishService: UserFishService,
    private val pubsubMessageHandler: PubsubMessageHandler,
    private val applicationProperties: ApplicationProperties,
    private val zone: RWZone,
    private val protectNewFishMap: MutableMap<Long, Long>
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
            .doOnNext { doIfDestroyEvent(it) }
            .doOnNext { event -> Mono.just(1).subscribe { pushMessageToClient(event) } }
            .filter { isFishMessage(it) && isReceiverReachable(it) && filterIfAtkEvent(it)}
            .flatMap { dispatch(it) }
            .map { Tuples.of(subscriptionRegistry.findConsumerByObjId(it.t1), it.t2) }
            .filter { it.t1.isPresent }
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorContinue { err, it ->
                log.error("dispatch error!", err, it)
            }
            .subscribe { it.t1.get().accept(it.t2) }
    }

    private fun filterIfAtkEvent(event: RWEvent): Boolean {
        return if (event is ATKEvent) {
            val protectTime = protectNewFishMap[event.target!!.id]
            if (protectTime == null) {
                true
            } else if (protectTime <= System.currentTimeMillis()) {
                protectNewFishMap.remove(event.target.id)
                true
            } else false
        } else true
    }


    private fun doIfDestroyEvent(event: RWEvent) {
        if (event is ObjectDestroyEvent) {
            Mono.just(1)
                .flatMap {
                    if (event.source is Fish && event.source.hasMaster) {
                        userFishService.changeFishStatusToDead(event.source)
                    } else {
                        Mono.empty()
                    }
                }
                .subscribe { destroyObj(event) }
        }
    }

    private fun isFishMessage(event: RWEvent): Boolean {
        return event !is InternalEvent && event !is WorldMessageEvent
    }

    private fun isReceiverReachable(event: RWEvent): Boolean {
        return event.target == null || event.target !is Being || event.target.isAlive()
    }

    private fun dispatch(event: RWEvent): Flux<Tuple2<Long, out RWEvent>> {
        return if (event is TimeEvent) {
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

    private fun pushMessageToClient(event: RWEvent) {
        // 可配置的消息转发类型
        if (applicationProperties.messageTypeNeedToSend.contains(event.eventType)) {
            pubsubMessageHandler.sendToUser(event)
        }
        if (event is WorldMessageEvent) {
            pubsubMessageHandler.sendToWorld(event)
        }
    }

    /**
     * 销毁对象
     * */
    private fun destroyObj(event: ObjectDestroyEvent) {
        publishObjectStatusChangeEvent(event.source!!.id, BeingStatus.DEAD, event.source.hasMaster)
        zone.clearObj(event.source)
        if (event.target != null && event.target is Fish && event.source is Fish) {
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
        if (event.target != null && event.target is ChaosGod && event.source is Fish) {
            val target =
                RandomUtil.randomEleList(event.source.findAllHumanSameZone().filter { it != event.source }.toList(), 1)
                    .first()
            target.handler.accept(
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
