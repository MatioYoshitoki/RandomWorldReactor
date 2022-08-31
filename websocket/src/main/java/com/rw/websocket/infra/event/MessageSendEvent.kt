package com.rw.websocket.infra.event

import org.springframework.context.ApplicationEvent

open class MessageSendEvent(payload: MessageSendEventPayload): ApplicationEvent(payload)

open class MessageSendEventPayload(
    val message: String
) {

    companion object {
        fun of(message: String): MessageSendEventPayload {
            return MessageSendEventPayload(message)
        }
    }
}
