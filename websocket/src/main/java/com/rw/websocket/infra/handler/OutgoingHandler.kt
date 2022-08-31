package com.rw.websocket.infra.handler

import com.rw.websocket.infra.config.ApplicationProperties
import com.rw.websocket.infra.messaging.MapMessage
import com.rw.websocket.infra.utils.SimpleMessageUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
open class OutgoingHandler(
    private val properties: ApplicationProperties,
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val aggregateScheduler = Schedulers.newParallel("message-aggregate")
    private val serializeScheduler = Schedulers.newParallel("message-serialize")

    fun handleMessagesToClient(
        session: WebSocketSession,
        clientFlux: Flux<Message<MapMessage>>
    ): Flux<WebSocketMessage> {
        return outgoingMessages(messageTransformFlux(clientFlux), session)
    }

    private fun messageTransformFlux(
        clientChannel: Flux<Message<MapMessage>>
    ): Flux<Message<MapMessage>> {
        return clientChannel
    }

    private fun outgoingMessages(
        msgFlux: Flux<Message<MapMessage>>,
        session: WebSocketSession
    ): Flux<WebSocketMessage> {
        return msgFlux
            .doOnNext {
                log.debug("[sessionId={}] Send compress message to client: {}", session.id, it)
            }
            .flatMap {
                Mono.just(it.headers[SimpleMessageUtils.PAYLOAD_BYTES] as ByteArray)
            }
            .flatMap {
                if (session.isOpen) {
                    Mono.fromRunnable {
                        session.binaryMessage { df: DataBufferFactory -> df.wrap(it!!) }
                    }
                } else {
                    Mono.empty()
                }
            }
    }

}
