package com.matio.random.infra.handler

import com.matio.random.domain.entity.Human
import com.matio.random.domain.entity.ObjectDestroyEvent
import com.matio.random.domain.entity.RWEvent
import com.matio.random.infra.subscription.SubscriptionRegistry
import com.matio.random.infra.utils.SinksUtils
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues

@Component
open class WorldMessageDispatchHandler(
    private val subscriptionRegistry: SubscriptionRegistry
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
                }
            }
            .delayUntil { event ->
                if (event.target != null && event.target is Human) {
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


    fun createObj() {

    }

    fun destroyObj() {

    }

}