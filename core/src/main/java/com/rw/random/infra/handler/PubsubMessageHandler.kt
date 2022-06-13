package com.rw.random.infra.handler

import com.rw.random.domain.repository.RedisPubsubRepository
import com.rw.random.infra.utils.SinksUtils
import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks
import reactor.util.concurrent.Queues

@Component
open class PubsubMessageHandler(
    private val redisPubsubRepository: RedisPubsubRepository,
) : SmartLifecycle {

    private var isRunning = false

    private val log = LoggerFactory.getLogger(javaClass)

    private val skins = Sinks.many().unicast().onBackpressureBuffer(Queues.get<String>(256).get())

    override fun start() {
        log.info("start send")
        this.isRunning = true
        skins.asFlux()
            .flatMap {
                redisPubsubRepository.pubMessage(it)
            }
            .subscribe()
    }

    fun sendMessage(msg: String) {
        try {
            SinksUtils.tryEmit(skins, msg)
        } catch (e: Exception) {
            log.error("send message to redis error!", e)
        }
    }

    override fun stop() {
        this.isRunning = false
    }

    override fun isRunning(): Boolean {
        return this.isRunning
    }
}
