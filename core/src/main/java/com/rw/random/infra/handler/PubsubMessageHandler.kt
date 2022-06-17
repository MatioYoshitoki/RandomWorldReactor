package com.rw.random.infra.handler

import com.rw.random.domain.entity.RWEvent
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

    fun sendToWorld(event: RWEvent) {
        val message = """
            {"dest": "/topic/world", "__PAYLOAD": $event}
        """.trimIndent()
        sendMessage(message)
    }

    fun sendToOwner(event: RWEvent) {
        if (event.source?.id != null) {
            val message = """
            {"dest": "/topic/owner/${event.source.id}", "__PAYLOAD": $event}
        """.trimIndent()
            sendMessage(message)
        }
        if (event.target?.id != null) {
            val message = """
            {"dest": "/topic/owner/${event.target.id}", "__PAYLOAD": $event}
        """.trimIndent()
            sendMessage(message)
        }
    }

    fun sendToUser(event: RWEvent) {
        if (event.source?.hasMaster != null && event.source.hasMaster && event.source.masterId != null) {
            val message = """
            {"dest": "/topic/user/${event.source.masterId}", "__PAYLOAD": $event}
        """.trimIndent()
            sendMessage(message)
        }
        if ((event.target?.hasMaster != null && event.target.hasMaster && event.target.masterId != null)) {
            val message = """
            {"dest": "/topic/user/${event.target.masterId}", "__PAYLOAD": $event}
        """.trimIndent()
            sendMessage(message)
        }
    }

    private fun sendMessage(msg: String) {
        try {
            SinksUtils.tryEmit(skins, msg, 20)
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
