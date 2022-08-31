package com.rw.websocket.infra.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.rw.random.common.dto.RedisStreamMessage
import com.rw.websocket.domain.repository.UserFishRepository
import com.rw.websocket.infra.event.MessageSendEvent
import com.rw.websocket.infra.event.MessageSendEventPayload
import com.rw.websocket.infra.session.DefaultRandomWorldSessionManager
import com.rw.websocket.infra.subscription.SubscriptionRegistry
import com.rw.websocket.infra.utils.SimpleMessageUtils
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
open class MessageSendListener(
    private val objectMapper: ObjectMapper,
    private val userFishRepository: UserFishRepository,
    private val subscriptionRegistry: SubscriptionRegistry,
    private val sessionManager: DefaultRandomWorldSessionManager,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    @EventListener
    fun listen(messageSendEvent: MessageSendEvent) {
        val message = (messageSendEvent.source as MessageSendEventPayload).message
        log.debug("pre send message: $message")
        Flux.just(message)
            .map {
                objectMapper.readValue(it, RedisStreamMessage::class.java)
            }
            .filter { SimpleMessageUtils.legalDestination(it.dest) }
            .flatMap { msg ->
                currentDest(msg.dest!!)
                    .flatMapMany {
                        Flux.fromIterable(subscriptionRegistry.getSubscriptions(it))
                    }
                    .flatMap {
                        Mono.justOrEmpty(sessionManager.find(it.sessionId))
                    }
                    .map {
                        it!!.webSocketSession
                    }
                    .flatMap { directSendMessage(it, msg) }
            }
            .subscribe()

    }

    private fun currentDest(originDest: String): Mono<String> {
        return if (SimpleMessageUtils.isOwnerDestination(originDest)) {
            userFishRepository.findFishOwner(
                originDest.replace(SimpleMessageUtils.OWNER_DESTINATION_PREFIX, "").toLong()
            )
                .map {
                    SimpleMessageUtils.buildUserDestination(it)
                }
        } else {
            Mono.just(originDest)
        }
    }

    private fun directSendMessage(session: WebSocketSession, msg: RedisStreamMessage): Mono<Void> {
        val sendMsg = session.textMessage(objectMapper.writeValueAsString(msg.payload!!))
        log.debug("send msg: {}", sendMsg.payloadAsText)
        return if (session.isOpen) {
            session.send(Mono.just(sendMsg))
                .onErrorResume { err ->
                    log.error("SEND ERROR", err)
                    Mono.empty()
                }
        } else {
            Mono.empty()
        }
    }

}