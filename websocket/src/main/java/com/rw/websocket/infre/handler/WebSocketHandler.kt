package com.rw.websocket.infre.handler

import com.rw.random.common.utils.SecurityUtils
import com.rw.websocket.infre.messaging.MapMessage
import com.rw.websocket.infre.session.DefaultRandomWorldSessionManager
import com.rw.websocket.infre.session.SessionMetadataUtils
import com.rw.websocket.infre.utils.SimpleMessageUtils
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.core.scheduler.Schedulers
import reactor.netty.channel.AbortedException

@Component
class RandomWorldWebSocketHandler(
    private val sessionManager: DefaultRandomWorldSessionManager,
    private val incomingHandler: IncomingHandler,
    private val outgoingHandler: OutgoingHandler,
) : WebSocketHandler, ApplicationEventPublisherAware {
    private val log = LoggerFactory.getLogger(javaClass)

    private lateinit var eventPublisher: ApplicationEventPublisher

    override fun handle(session: WebSocketSession): Mono<Void> {
        val brokerSession = sessionManager.getOrCreate(session)
        val channel = brokerSession.channel

        return Mono.zip(
            subscribeIncomingMessages(session),
            session.send(subscribeOutgoingMessages(session, channel))
        )
            .onErrorResume {
                log.error("WebSocket handle message error", it)
                Mono.empty()
            }
            .then()
    }

    private fun subscribeIncomingMessages(session: WebSocketSession): Mono<Void> {
        val inFlux = session.receive()
        return inFlux
            .flatMap {
                incomingHandler.handleMessageFromClient(session, it)
            }
            .onErrorResume { err ->
                val uid = SessionMetadataUtils.getUid(sessionManager.getMetadata(session.id))
                when (err) {
                    is AbortedException -> {
                        log.warn("[sessionId={}, uid={}] {}", session.id, uid, err.message)
                    }
                    else -> {
                        log.error("[sessionId={}, uid={}] {}", session.id, uid, err.message, err)
                    }
                }
                Mono.empty()
            }
            .publishOn(Schedulers.boundedElastic())
            .doFinally {
                if (log.isDebugEnabled) {
                    log.debug("onFinally close session={}", session.id)
                }
                session.closeStatus()
                    // 如果没有 closeStatus 是 closeStatus() 默认不返回，所以需要我们补充一个默认值
                    .defaultIfEmpty(CloseStatus.NO_STATUS_CODE).subscribe { status ->
                        onClosed(session, status)
                    }
            }
            .then()
    }

    private fun subscribeOutgoingMessages(
        session: WebSocketSession,
        channel: Sinks.Many<Message<*>>
    ): Flux<WebSocketMessage> {
        val fx = channel
            .asFlux()
            .doOnNext {
                log.debug("MessageFlux: {}", it)
            }
            .filter {
                if (it.payload is MapMessage) {
                    true
                } else {
                    log.debug("message dropped ${it.payload.javaClass}: $it")
                    false
                }
            }
            .onErrorResume { err ->
                log.error("Subscribe outgoing messages failed", err)
                Mono.empty()
            }

        @Suppress("UNCHECKED_CAST")
        return outgoingHandler.handleMessagesToClient(session, fx as Flux<Message<MapMessage>>)
            .onErrorResume { err ->
                log.warn("Sending outgoing messages to client failed", err)
                Mono.empty()
            }
    }

    private fun onClosed(session: WebSocketSession, status: CloseStatus) {
        val sessionId: String = session.id
        log.warn("client close[sessionId=$sessionId, status=$status]")
//        this.eventPublisher.publishEvent(ConnectionClosedEvent.of(sessionId, status))
    }

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher
    }
}
